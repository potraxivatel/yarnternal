package yarnternal.lol.common.ui.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import yarnternal.lol.api.systems.style.Style;
import yarnternal.lol.api.utility.render.GuiDrawUtil;
import yarnternal.lol.api.utility.render.GuiRender;
import yarnternal.lol.api.utility.render.color.ColorUtil;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.api.ModuleList;
import yarnternal.lol.common.keybind.KeybindManager;
import yarnternal.lol.common.settings.*;
import yarnternal.lol.common.ui.Theme;
import yarnternal.lol.core.Yarnternal;
import yarnternal.lol.core.data.ProfileData;

import java.util.*;

/**
 * ClickGUI в стиле Expensive 2.0: тёмное окно 450×350, сайдбар 100 px,
 * две колонки карточек по 160 px, тонкие градиентные обводки и серые
 * монохромные контролы. Акцент темы используется только для подсветки
 * категории, логотипа, свечения окна и цветовых превью.
 */
public final class ClickGuiScreen extends Screen {

    private static final int W = Theme.WINDOW_W;
    private static final int H = Theme.WINDOW_H;

    private int windowX;
    private int windowY;

    private boolean draggingWindow;
    private int dragOffsetX;
    private int dragOffsetY;

    private ModuleList selectedCategory = ModuleList.Other;
    private final Map<ModuleList, Float> categoryAnims = new EnumMap<>(ModuleList.class);

    private String searchQuery = "";
    private boolean searchFocused = false;

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private Module listeningBindModule = null;
    private KeybindSetting listeningBindSetting = null;

    private final Map<String, Float> toggleAnims = new HashMap<>();
    private final Map<String, Float> hoverAnims = new HashMap<>();
    private final Set<Module> collapsedModules = new HashSet<>();
    private boolean collapsedDefaultsApplied;
    private final long openedAt = System.nanoTime();

    private float descAnim = 0f;
    private Module currentHoveredModule = null;
    private Module lastHoveredModule = null;

    private NumberSetting draggingSlider = null;
    private double dragSliderX;
    private double dragSliderW;

    /** Прячем HUD, пока открыт ClickGUI, и возвращаем прежнее состояние при закрытии. */
    private boolean hudHiddenSaved;
    private boolean previousHudHidden;

    // ── Модели макета ────────────────────────────────────────────────────────

    private static final class SettingLayout {
        final Setting<?> setting;
        final int y;
        final int h;

        SettingLayout(Setting<?> setting, int y, int h) {
            this.setting = setting;
            this.y = y;
            this.h = h;
        }
    }

    private static final class CardLayout {
        final Module module;
        final int x;
        final int y;
        final int w;
        final int h;
        final List<SettingLayout> settings = new ArrayList<>();

        CardLayout(Module module, int x, int y, int w, int h) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    public ClickGuiScreen() {
        super(Text.literal("Yarnternal"));
    }

    public static void open() {
        MinecraftClient.getInstance().setScreen(new ClickGuiScreen());
    }

    @Override
    protected void init() {
        windowX = (this.width - W) / 2;
        windowY = (this.height - H) / 2;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            if (!hudHiddenSaved) {
                previousHudHidden = client.options.hudHidden;
                hudHiddenSaved = true;
            }
            client.options.hudHidden = true;
        }

        // По умолчанию все карточки свёрнуты: настройки открываются ПКМ.
        if (!collapsedDefaultsApplied) {
            collapsedDefaultsApplied = true;
            Yarnternal yarnternal = Yarnternal.getInstance();
            if (yarnternal != null && yarnternal.getModuleProvider() != null) {
                collapsedModules.addAll(yarnternal.getModuleProvider().getModules());
            }
        }

        if (getFilteredModules().isEmpty()) {
            for (ModuleList category : ModuleList.values()) {
                if (category == ModuleList.Themes) {
                    continue;
                }
                selectedCategory = category;
                if (!getFilteredModules().isEmpty()) {
                    break;
                }
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && hudHiddenSaved) {
            client.options.hudHidden = previousHudHidden;
            hudHiddenSaved = false;
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    // ── Главный рендер ───────────────────────────────────────────────────────

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiRender.begin(context);

        float open = Math.min(1f, (System.nanoTime() - openedAt) / 200_000_000f);
        GuiRender.setAlpha(1f - (float) Math.pow(1f - open, 3));

        // 1. Затемняем и размываем фон целиком — как Expensive.
        GuiRender.rect(0, 0, this.width, this.height, Theme.OVERLAY);
        GuiRender.flush();
        GuiRender.blur(0, 0, this.width, this.height, 0, 5);

        // 2. Свечение вокруг окна.
        GuiDrawUtil.drawWindowGlow(windowX, windowY, W, H, Theme.RADIUS_WINDOW);

        // 3. Окно: тонкая холодная рамка, контент справа, сайдбар слева.
        GuiRender.rect4(windowX, windowY, W, H,
                GuiDrawUtil.withAlpha(Theme.BORDER_BRIGHT, 0x77),
                GuiDrawUtil.withAlpha(Theme.BORDER_DARK, 0x77),
                GuiDrawUtil.withAlpha(Theme.BORDER_BRIGHT, 0x77),
                GuiDrawUtil.withAlpha(Theme.BORDER_DARK, 0x77),
                Theme.RADIUS_WINDOW);
        GuiRender.rectCorners(windowX + Theme.SIDEBAR_W, windowY + 0.5,
                W - Theme.SIDEBAR_W - 0.5, H - 1,
                Theme.BG_DARK, 0, Theme.RADIUS_WINDOW - 0.5,
                Theme.RADIUS_WINDOW - 0.5, 0);
        GuiRender.rectCorners(windowX + 0.5, windowY + 0.5,
                Theme.SIDEBAR_W - 0.5, H - 1,
                Theme.BG_SIDEBAR, Theme.RADIUS_WINDOW - 0.5, 0, 0,
                Theme.RADIUS_WINDOW - 0.5);
        GuiRender.rect(windowX + Theme.SIDEBAR_W, windowY + 1, 0.5, H - 2,
                Theme.SIDEBAR_LINE);

        // 4. Содержимое.
        renderSidebar(mouseX, mouseY);
        renderContent(mouseX, mouseY);

        // 5. Бренд в шапке сайдбара — цветом активной темы.
        renderBrand();

        // 6. Всплывающее описание по центру над окном.
        renderFloatingDescription();

        GuiRender.setAlpha(1f);
        GuiRender.end();

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderBrand() {
        String brand = "Yarnternal";
        double width = GuiDrawUtil.textWidth(brand, GuiDrawUtil.BRAND_SIZE,
                GuiRender.Weight.BRAND_BOLD);
        double x = windowX + (Theme.SIDEBAR_W - width) / 2;
        double th = GuiDrawUtil.textHeight(GuiDrawUtil.BRAND_SIZE,
                GuiRender.Weight.BRAND_BOLD);
        double y = windowY + (Theme.SEARCH_Y - th) / 2 + 1.5;
        float phase = (System.currentTimeMillis() % 7000L) / 7000.0f;

        GuiDrawUtil.drawFlowText(brand, x, y, GuiDrawUtil.BRAND_SIZE,
                GuiRender.Weight.BRAND_BOLD, phase);
    }

    // ── Сайдбар ──────────────────────────────────────────────────────────────

    private void renderSidebar(int mouseX, int mouseY) {
        int sx = windowX;
        int sy = windowY;

        int searchX = sx + Theme.SEARCH_X;
        int searchY = sy + Theme.SEARCH_Y;
        GuiDrawUtil.drawSearchBox(searchX, searchY, Theme.SEARCH_W, Theme.SEARCH_H,
                searchQuery, searchFocused);

        for (ModuleList category : ModuleList.values()) {
            int rowY = categoryY(category);
            int rowX = sx + Theme.CATEGORY_X;
            boolean active = category == selectedCategory;
            boolean hovered = mouseX >= rowX && mouseX < rowX + Theme.CATEGORY_W
                    && mouseY >= rowY && mouseY < rowY + Theme.CATEGORY_H;

            float target = active ? 1f : 0f;
            float anim = categoryAnims.getOrDefault(category, target);
            anim += (target - anim) * 0.25f;
            if (Math.abs(anim - target) < 0.004f) {
                anim = target;
            }
            categoryAnims.put(category, anim);

            if (anim > 0.01f) {
                GuiDrawUtil.drawCategoryHighlight(rowX, rowY, Theme.CATEGORY_W,
                        Theme.CATEGORY_H, anim);
            } else if (hovered) {
                GuiRender.rect(rowX, rowY, Theme.CATEGORY_W, Theme.CATEGORY_H,
                        0x12FFFFFF, 3);
            }

            int textColor = active ? Theme.TEXT_WHITE
                    : (hovered ? Theme.TEXT_SOFT : Theme.TEXT_LIGHT);
            double ty = rowY + (Theme.CATEGORY_H
                    - GuiDrawUtil.textHeight(GuiDrawUtil.TITLE_SIZE, GuiRender.Weight.CATEGORY)) / 2;
            GuiDrawUtil.drawText(category.name(), rowX + 11, ty, textColor,
                    GuiDrawUtil.TITLE_SIZE, GuiRender.Weight.CATEGORY);
        }

        renderProfile();
    }

    private void renderProfile() {
        int sx = windowX;
        int sepY = windowY + H - 35;
        GuiRender.rect(sx + 5, sepY, 90, 0.5, Theme.SIDEBAR_LINE);

        ProfileData profile = ProfileData.get();
        String username = profile.nickname();
        if (username == null || username.isBlank()) {
            username = "Player";
        }

        int avX = sx + 5;
        int avY = sepY + 5;

        // Градиент остаётся подложкой: сквозь скруглённые углы аватарки виден акцент темы.
        GuiRender.rectV(avX, avY, 25, 25,
                Theme.accent(), Theme.accentAlt(), 5);

        var avatarId = ProfileAvatar.texture();
        if (avatarId != null) {
            GuiRender.texture(avatarId, avX, avY, 25, 25, 0xFFFFFFFF);
        } else {
            String initial = username.substring(0, 1).toUpperCase(Locale.ROOT);
            double iw = GuiDrawUtil.textWidth(initial, GuiDrawUtil.BODY_SIZE, GuiRender.Weight.BRAND_BOLD);
            double ih = GuiDrawUtil.textHeight(GuiDrawUtil.BODY_SIZE, GuiRender.Weight.BRAND_BOLD);
            GuiDrawUtil.drawText(initial, avX + (25 - iw) / 2, avY + (25 - ih) / 2 - 1,
                    0xFFFFFFFF, GuiDrawUtil.BODY_SIZE, GuiRender.Weight.BRAND_BOLD);
        }

        // Длинный никнейм уменьшаем до комфортного размера, а не режем сразу.
        double nameSize = GuiDrawUtil.TITLE_SIZE;
        double nameMax = 61;
        while (nameSize > GuiDrawUtil.SMALL_SIZE
                && GuiDrawUtil.textWidth(username, nameSize,
                GuiRender.Weight.BRAND_BOLD) > nameMax) {
            nameSize -= 0.25;
        }
        String name = GuiDrawUtil.trim(username, nameMax, nameSize,
                GuiRender.Weight.BRAND_BOLD);
        GuiDrawUtil.drawText(name, sx + 34, avY, Theme.TEXT_WHITE,
                nameSize, GuiRender.Weight.BRAND_BOLD);

        String uid = profile.uid();
        if (uid == null || uid.isBlank()) {
            uid = "LOCAL";
        }
        String uidLine = GuiDrawUtil.trim("UID: " + uid, 62, GuiDrawUtil.SMALL_SIZE,
                GuiRender.Weight.BRAND);
        GuiDrawUtil.drawText(uidLine, sx + 34, avY + 15, Theme.TEXT_LIGHT,
                GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.BRAND);
    }

    private int categoryY(ModuleList target) {
        int y = windowY + Theme.CATEGORY_Y;
        for (ModuleList category : ModuleList.values()) {
            if (category == target) {
                return y;
            }
            y += Theme.SIDEBAR_ROW_H;
            if (category == ModuleList.Fun) {
                y += 6;
            }
        }
        return y;
    }

    // ── Контент ──────────────────────────────────────────────────────────────

    private void renderContent(int mouseX, int mouseY) {
        int cx = windowX + Theme.SIDEBAR_W;
        int cy = windowY;
        int cw = W - Theme.SIDEBAR_W;
        int ch = H;

        currentHoveredModule = null;
        if (selectedCategory != ModuleList.Themes || !searchQuery.trim().isEmpty()) {
            List<CardLayout> cards = buildCardLayouts(cx, cy, cw);
            for (CardLayout card : cards) {
                if (mouseX >= card.x && mouseX < card.x + card.w
                        && mouseY >= card.y && mouseY < card.y + card.h
                        && mouseY >= cy && mouseY < cy + ch) {
                    currentHoveredModule = card.module;
                    break;
                }
            }
        }

        GuiRender.scissor(cx, cy, cw, ch);

        if (selectedCategory == ModuleList.Themes && searchQuery.trim().isEmpty()) {
            renderThemes(mouseX, mouseY, cx, cy, cw, ch);
        } else {
            List<Module> modules = getFilteredModules();
            if (modules.isEmpty()) {
                renderEmpty(cx, cy, cw, ch);
                maxScroll = 0;
            } else {
                List<CardLayout> cards = buildCardLayouts(cx, cy, cw);
                for (CardLayout card : cards) {
                    if (card.y + card.h < cy || card.y > cy + ch) {
                        continue;
                    }
                    renderCard(card, mouseX, mouseY, cy, ch);
                }
            }
        }

        // Мягкие срезы сверху и снизу. Скругляем внешний правый угол,
        // иначе фейд закрашивает скругление окна и делает его квадратным.
        GuiRender.rectVCorners(cx, cy, cw, 14,
                GuiDrawUtil.withAlpha(Theme.BG_DARK, 0xAA),
                GuiDrawUtil.withAlpha(Theme.BG_DARK, 0),
                0, Theme.RADIUS_WINDOW - 0.5, 0, 0);
        GuiRender.rectVCorners(cx, cy + ch - 26, cw, 26,
                GuiDrawUtil.withAlpha(Theme.BG_DARK, 0), Theme.BG_DARK,
                0, 0, Theme.RADIUS_WINDOW - 0.5, 0);

        GuiRender.resetScissor();
        drawScrollbar(cx, cy, cw, ch);
    }

    private void renderFloatingDescription() {
        boolean active = currentHoveredModule != null && hasDescription(currentHoveredModule);
        float target = active ? 1f : 0f;
        descAnim += (target - descAnim) * 0.22f;
        if (Math.abs(descAnim - target) < 0.005f) {
            descAnim = target;
        }

        if (active) {
            lastHoveredModule = currentHoveredModule;
        }

        if (descAnim <= 0.01f || lastHoveredModule == null) {
            return;
        }

        String modName = lastHoveredModule.getName();
        String desc = lastHoveredModule.getDesc();
        if (desc == null || desc.isBlank()) {
            return;
        }

        int alpha = (int) (255 * descAnim);

        double nameW = GuiDrawUtil.textWidth(modName, GuiDrawUtil.BODY_SIZE, GuiRender.Weight.SEMIBOLD);
        double sepW = GuiDrawUtil.textWidth("—", GuiDrawUtil.BODY_SIZE, GuiRender.Weight.REGULAR);

        double maxDescW = Math.min(320, W - 40 - nameW - sepW);
        String trimmedDesc = GuiDrawUtil.trim(desc, maxDescW, GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);
        double descW = GuiDrawUtil.textWidth(trimmedDesc, GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);

        double pillH = 22;
        double pillW = 10 + 3 + 7 + nameW + 7 + sepW + 7 + descW + 10;
        double pillX = windowX + (W - pillW) / 2.0;

        // Плавно выплывает на 4 px вниз при появлении
        double baseY = windowY - pillH - 7;
        double pillY = baseY + (1f - descAnim) * 4;

        // Тень вокруг плашки
        int shadowColor = GuiDrawUtil.withAlpha(0x000000, (int) (45 * descAnim));
        GuiRender.rect(pillX - 2, pillY - 1, pillW + 4, pillH + 4, shadowColor, 6);

        // Карточка плашки
        int bg = GuiDrawUtil.withAlpha(Theme.BG_PANEL, (int) (245 * descAnim));
        int borderA = GuiDrawUtil.withAlpha(Theme.BORDER_BRIGHT, (int) (200 * descAnim));
        int borderB = GuiDrawUtil.withAlpha(Theme.BORDER_DARK, (int) (200 * descAnim));
        GuiDrawUtil.drawCard(pillX, pillY, pillW, pillH, bg, bg, borderA, borderB, 5);

        // Акцентная плашка-индикатор
        double indH = 8;
        double iy = pillY + (pillH - indH) / 2.0;
        GuiRender.rect(pillX + 9, iy, 3, indH,
                GuiDrawUtil.withAlpha(Theme.accent(), alpha), 1.5);

        // Имя модуля
        double nameH = GuiDrawUtil.textHeight(GuiDrawUtil.BODY_SIZE, GuiRender.Weight.SEMIBOLD);
        double ny = pillY + (pillH - nameH) / 2.0 - 0.5;
        GuiDrawUtil.drawText(modName, pillX + 18, ny,
                GuiDrawUtil.withAlpha(Theme.TEXT_WHITE, alpha),
                GuiDrawUtil.BODY_SIZE, GuiRender.Weight.SEMIBOLD);

        // Разделитель
        double sepX = pillX + 18 + nameW + 7;
        GuiDrawUtil.drawText("—", sepX, ny,
                GuiDrawUtil.withAlpha(Theme.TEXT_DIM, alpha),
                GuiDrawUtil.BODY_SIZE, GuiRender.Weight.REGULAR);

        // Описание
        double descX = sepX + sepW + 7;
        double descH = GuiDrawUtil.textHeight(GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);
        double dy = pillY + (pillH - descH) / 2.0 - 0.5;
        GuiDrawUtil.drawText(trimmedDesc, descX, dy,
                GuiDrawUtil.withAlpha(Theme.TEXT_SOFT, alpha),
                GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);
    }

    private void drawScrollbar(int cx, int cy, int cw, int ch) {
        if (maxScroll <= 0) {
            return;
        }

        double x = cx + cw - 4;
        double y = cy + 14;
        double height = ch - 28;
        GuiRender.rect(x, y, 2, height, GuiDrawUtil.withAlpha(0xFFFFFF, 0x18), 1);

        double thumbHeight = Math.max(16, height * (double) ch / (ch + maxScroll));
        double thumbY = y + (height - thumbHeight) * ((double) scrollOffset / maxScroll);
        GuiRender.rect(x, thumbY, 2, thumbHeight,
                GuiDrawUtil.withAlpha(Theme.accent(), 0xAA), 1);
    }

    private void renderEmpty(int cx, int cy, int cw, int ch) {
        String message = searchQuery.trim().isEmpty() ? "No modules" : "Nothing found";
        double tw = GuiDrawUtil.textWidth(message, GuiDrawUtil.BODY_SIZE, GuiRender.Weight.MEDIUM);
        double th = GuiDrawUtil.textHeight(GuiDrawUtil.BODY_SIZE, GuiRender.Weight.MEDIUM);
        GuiDrawUtil.drawText(message, cx + (cw - tw) / 2, cy + (ch - th) / 2,
                GuiDrawUtil.withAlpha(Theme.TEXT_LIGHT, 0xAA),
                GuiDrawUtil.BODY_SIZE, GuiRender.Weight.MEDIUM);
    }

    private List<CardLayout> buildCardLayouts(int cx, int cy, int cw) {
        List<Module> modules = getFilteredModules();
        List<CardLayout> cards = new ArrayList<>();

        int[] columnX = {
                cx + Theme.CONTENT_PAD,
                cx + Theme.CONTENT_PAD + Theme.CARD_W + Theme.CONTENT_GAP
        };
        int[] columnY = {
                cy + Theme.CONTENT_PAD - scrollOffset,
                cy + Theme.CONTENT_PAD - scrollOffset
        };

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int column = i % 2;
            int height = cardHeight(module);

            CardLayout card = new CardLayout(module, columnX[column], columnY[column],
                    Theme.CARD_W, height);

            if (!collapsedModules.contains(module)) {
                int settingY = card.y + Theme.CARD_HEADER_H + Theme.CARD_PAD;
                for (Setting<?> setting : module.getSettings()) {
                    if (!setting.isVisible()) {
                        continue;
                    }
                    int settingHeight = settingHeight(setting);
                    card.settings.add(new SettingLayout(setting, settingY, settingHeight));
                    settingY += settingHeight;
                }
            }

            cards.add(card);
            columnY[column] += height + Theme.CARD_GAP;
        }

        int contentBottom = Math.max(columnY[0], columnY[1]) + scrollOffset;
        int contentTop = cy + Theme.CONTENT_PAD;
        int contentHeight = contentBottom - contentTop;
        maxScroll = Math.max(0, contentHeight - (H - Theme.CONTENT_PAD * 2));
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }

        return cards;
    }

    private int cardHeight(Module module) {
        boolean expanded = !collapsedModules.contains(module)
                && hasVisibleSettings(module);
        if (!expanded) {
            return Theme.CARD_HEADER_H + Theme.CARD_PAD;
        }

        int height = Theme.CARD_HEADER_H + Theme.CARD_PAD * 2;
        for (Setting<?> setting : module.getSettings()) {
            if (setting.isVisible()) {
                height += settingHeight(setting);
            }
        }
        return height;
    }

    private boolean hasDescription(Module module) {
        return module.getDesc() != null && !module.getDesc().isBlank();
    }

    private boolean hasVisibleSettings(Module module) {
        for (Setting<?> setting : module.getSettings()) {
            if (setting.isVisible()) {
                return true;
            }
        }
        return false;
    }

    private int settingHeight(Setting<?> setting) {
        if (setting instanceof ModeSetting mode) {
            return segmentedHeight(segmentLabels(mode.getModes()));
        }
        if (setting instanceof MultiBooleanSetting multi) {
            return segmentedHeight(segmentLabels(new ArrayList<>(multi.getOptions())));
        }
        if (setting instanceof EnumSetting<?> enumSetting) {
            return segmentedHeight(segmentLabels(enumLabels(enumSetting)));
        }
        return Theme.SETTING_ROW_H;
    }

    private int segmentedHeight(List<String> labels) {
        int rows = GuiDrawUtil.segmentedRows(labels, Theme.CARD_W - 20,
                GuiDrawUtil.TINY_SIZE, GuiRender.Weight.SEMIBOLD);
        return GuiDrawUtil.SEGMENT_TOP + GuiDrawUtil.SEGMENT_LINE_H * rows;
    }

    /** Обрезает подписи сегментов, чтобы длинные режимы не вылезали за карточку. */
    private List<String> segmentLabels(List<String> raw) {
        List<String> labels = new ArrayList<>(raw.size());
        for (String value : raw) {
            labels.add(GuiDrawUtil.trim(value, Theme.CARD_W - 30,
                    GuiDrawUtil.TINY_SIZE, GuiRender.Weight.SEMIBOLD));
        }
        return labels;
    }

    private List<String> enumLabels(EnumSetting<?> setting) {
        List<String> labels = new ArrayList<>();
        for (Object constant : setting.getConstants()) {
            labels.add(EnumSetting.pretty(((Enum<?>) constant).name()));
        }
        return labels;
    }

    // ── Карточка модуля ──────────────────────────────────────────────────────

    private void renderCard(CardLayout card, int mouseX, int mouseY, int cy, int ch) {
        Module module = card.module;
        boolean enabled = module.isToggled();
        boolean hovered = mouseX >= card.x && mouseX < card.x + card.w
                && mouseY >= card.y && mouseY < card.y + card.h
                && mouseY >= cy && mouseY < cy + ch;

        float hover = animated(hoverAnims, module.getName(), hovered ? 1f : 0f, 0.18f);
        float toggle = animated(toggleAnims, module.getName(), enabled ? 1f : 0f, 0.20f);

        int fill = GuiDrawUtil.blend(Theme.BG_PANEL, Theme.BG_PANEL_HOVER, hover);
        GuiDrawUtil.drawCard(card.x, card.y, card.w, card.h, fill, fill,
                Theme.RADIUS_CARD);

        if (!card.settings.isEmpty()) {
            GuiRender.rect(card.x + 10, card.y + Theme.CARD_HEADER_H,
                    card.w - 20, 0.5, Theme.DIVIDER);
        }

        // Имя модуля — единственное, что меняет цвет при включении.
        int nameColor = GuiDrawUtil.blend(Theme.TEXT_LIGHT, Theme.TEXT_WHITE, toggle);
        String name = module.getName();

        Integer boundKey = Yarnternal.getInstance().getKeybindManager() != null
                ? Yarnternal.getInstance().getKeybindManager().getKey(module)
                : null;
        boolean listening = listeningBindModule == module;
        String keyLabel = listening ? "..." : (boundKey != null
                ? KeybindManager.keyName(boundKey) : null);

        boolean collapsible = hasVisibleSettings(module);
        boolean collapsed = collapsedModules.contains(module);
        double chevronSpace = collapsible ? 18 : 0;

        double titleH = GuiDrawUtil.textHeight(GuiDrawUtil.TITLE_SIZE, GuiRender.Weight.SEMIBOLD);
        double titleY = card.y + (Theme.CARD_HEADER_H - titleH) / 2.0 - 0.5;
        double headerCenterY = card.y + Theme.CARD_HEADER_H / 2.0;

        double keyWidth = 0;
        if (keyLabel != null) {
            keyWidth = GuiDrawUtil.textWidth(keyLabel, GuiDrawUtil.TINY_SIZE,
                    GuiRender.Weight.MEDIUM);
            double keyH = GuiDrawUtil.textHeight(GuiDrawUtil.TINY_SIZE, GuiRender.Weight.MEDIUM);
            double keyY = card.y + (Theme.CARD_HEADER_H - keyH) / 2.0 - 0.5;
            GuiDrawUtil.drawText(keyLabel,
                    card.x + card.w - 10 - chevronSpace - keyWidth,
                    keyY,
                    listening ? Theme.accent() : GuiDrawUtil.withAlpha(Theme.TEXT_LIGHT, 0x99),
                    GuiDrawUtil.TINY_SIZE, GuiRender.Weight.MEDIUM);
        }

        double titleMax = card.w - 20 - chevronSpace
                - (keyWidth > 0 ? keyWidth + 8 : 0);
        name = GuiDrawUtil.trim(name, titleMax, GuiDrawUtil.TITLE_SIZE,
                GuiRender.Weight.SEMIBOLD);
        GuiDrawUtil.drawText(name, card.x + 10, titleY, nameColor,
                GuiDrawUtil.TITLE_SIZE, GuiRender.Weight.SEMIBOLD);

        if (collapsible) {
            drawCollapseChevron(card, collapsed, headerCenterY);
        }

        for (SettingLayout layout : card.settings) {
            renderSetting(card, layout, mouseX, mouseY);
        }
    }

    /** Маленький указатель в шапке: сворачивает и разворачивает настройки модуля. */
    private void drawCollapseChevron(CardLayout card, boolean collapsed, double cy) {
        double cx = card.x + card.w - 13;
        int color = GuiDrawUtil.withAlpha(Theme.TEXT_LIGHT, 0xDD);

        if (collapsed) {
            GuiRender.triangle(cx - 2, cy - 4, cx - 2, cy + 4, cx + 3, cy, color);
        } else {
            GuiRender.triangle(cx - 4, cy - 2, cx + 4, cy - 2, cx, cy + 3, color);
        }
    }

    // ── Настройки ────────────────────────────────────────────────────────────

    private void renderSetting(CardLayout card, SettingLayout layout,
                               int mouseX, int mouseY) {
        Setting<?> setting = layout.setting;
        int x = card.x;
        int w = card.w;
        int y = layout.y;

        if (setting instanceof BooleanSetting bool) {
            int labelColor = Theme.TEXT_SOFT;
            String label = GuiDrawUtil.trim(setting.getName(), w - 46,
                    GuiDrawUtil.BODY_SIZE, GuiRender.Weight.REGULAR);
            double ty = y + (Theme.SETTING_ROW_H
                    - GuiDrawUtil.textHeight(GuiDrawUtil.BODY_SIZE,
                    GuiRender.Weight.REGULAR)) / 2 - 0.5;
            GuiDrawUtil.drawText(label, x + 10, ty, labelColor,
                    GuiDrawUtil.BODY_SIZE, GuiRender.Weight.REGULAR);

            String key = card.module.getName() + "." + setting.getName();
            float anim = animated(toggleAnims, key, bool.getValue() ? 1f : 0f, 0.20f);
            GuiDrawUtil.drawToggle(x + w - 26, y + (Theme.SETTING_ROW_H - 7.5) / 2, anim);

        } else if (setting instanceof NumberSetting number) {
            String value = number.displayValue();
            double valueWidth = GuiDrawUtil.textWidth(value, GuiDrawUtil.SMALL_SIZE,
                    GuiRender.Weight.REGULAR);
            String label = GuiDrawUtil.trim(setting.getName(),
                    w - 20 - valueWidth - 8, GuiDrawUtil.SMALL_SIZE,
                    GuiRender.Weight.REGULAR);
            GuiDrawUtil.drawText(label, x + 10, y + 3, Theme.TEXT_MUTED,
                    GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);
            GuiDrawUtil.drawText(value, x + w - 10 - valueWidth, y + 3, Theme.TEXT_MUTED,
                    GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);

            GuiDrawUtil.drawSliderTrack(x + 10, y + 15, w - 20, number.fraction());

        } else if (setting instanceof ModeSetting mode) {
            int selected = mode.getIndex();
            renderSegmented(setting.getName(), segmentLabels(mode.getModes()),
                    selected, x, y, w);

        } else if (setting instanceof MultiBooleanSetting multi) {
            List<String> raw = new ArrayList<>(multi.getOptions());
            List<String> labels = segmentLabels(raw);
            boolean[] states = new boolean[raw.size()];
            for (int i = 0; i < raw.size(); i++) {
                states[i] = multi.isSelected(raw.get(i));
            }
            renderSegmentedStates(setting.getName(), labels, states, x, y, w);

        } else if (setting instanceof EnumSetting<?> enumSetting) {
            int selected = enumSetting.getValue().ordinal();
            renderSegmented(setting.getName(), segmentLabels(enumLabels(enumSetting)),
                    selected, x, y, w);

        } else if (setting instanceof ColorSetting color) {
            String label = GuiDrawUtil.trim(setting.getName(), w - 64,
                    GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);
            double ty = y + (Theme.SETTING_ROW_H
                    - GuiDrawUtil.textHeight(GuiDrawUtil.SMALL_SIZE,
                    GuiRender.Weight.REGULAR)) / 2 - 0.5;
            GuiDrawUtil.drawText(label, x + 10, ty, Theme.TEXT_MUTED,
                    GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);
            GuiDrawUtil.drawColorSwatch(x + w - 34, y + 6, 24, 10,
                    color.get(), color.isRainbow());

        } else if (setting instanceof KeybindSetting keySetting) {
            boolean listening = listeningBindSetting == keySetting;
            String key = keySetting.getKey() == 0 ? "NONE" : keySetting.getKeyName();
            double boxWidth = GuiDrawUtil.keyBoxWidth(key, listening);
            String label = GuiDrawUtil.trim(setting.getName(), w - 20 - boxWidth - 8,
                    GuiDrawUtil.BODY_SIZE, GuiRender.Weight.REGULAR);
            double ty = y + (Theme.SETTING_ROW_H
                    - GuiDrawUtil.textHeight(GuiDrawUtil.BODY_SIZE,
                    GuiRender.Weight.REGULAR)) / 2 - 0.5;
            GuiDrawUtil.drawText(label, x + 10, ty, Theme.TEXT_SOFT,
                    GuiDrawUtil.BODY_SIZE, GuiRender.Weight.REGULAR);
            GuiDrawUtil.drawKeyBox(x + w - 10 - boxWidth, y + 5, key, listening);

        } else if (setting instanceof StringSetting string) {
            String raw = string.getValue() == null ? "" : string.getValue();
            String value = GuiDrawUtil.trim(raw, 54, GuiDrawUtil.TINY_SIZE,
                    GuiRender.Weight.MEDIUM);
            double valueWidth = Math.max(20, GuiDrawUtil.textWidth(value,
                    GuiDrawUtil.TINY_SIZE, GuiRender.Weight.MEDIUM) + 8);
            String label = GuiDrawUtil.trim(setting.getName(), w - 20 - valueWidth - 8,
                    GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);
            double ty = y + (Theme.SETTING_ROW_H
                    - GuiDrawUtil.textHeight(GuiDrawUtil.SMALL_SIZE,
                    GuiRender.Weight.REGULAR)) / 2 - 0.5;
            GuiDrawUtil.drawText(label, x + 10, ty, Theme.TEXT_MUTED,
                    GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);

            double boxX = x + w - 10 - valueWidth;
            GuiRender.rect(boxX, y + 5, valueWidth, 12, Theme.BG_TRACK, 2);
            GuiDrawUtil.drawText(value, boxX + 4, y + 5
                            + (12 - GuiDrawUtil.textHeight(GuiDrawUtil.TINY_SIZE,
                            GuiRender.Weight.MEDIUM)) / 2,
                    Theme.TEXT_MUTED, GuiDrawUtil.TINY_SIZE, GuiRender.Weight.MEDIUM);
        }
    }

    private void renderSegmented(String label, List<String> options, int selected,
                                 int x, int y, int w) {
        GuiDrawUtil.drawText(GuiDrawUtil.trim(label, w - 20, GuiDrawUtil.SMALL_SIZE,
                        GuiRender.Weight.REGULAR),
                x + 10, y + 1, Theme.TEXT_MUTED,
                GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);
        GuiDrawUtil.drawSegmented(x + 10, y + GuiDrawUtil.SEGMENT_TOP, w - 20,
                options, selected, GuiDrawUtil.TINY_SIZE, GuiRender.Weight.SEMIBOLD);
    }

    private void renderSegmentedStates(String label, List<String> options,
                                       boolean[] states, int x, int y, int w) {
        GuiDrawUtil.drawText(GuiDrawUtil.trim(label, w - 20, GuiDrawUtil.SMALL_SIZE,
                        GuiRender.Weight.REGULAR),
                x + 10, y + 1, Theme.TEXT_MUTED,
                GuiDrawUtil.SMALL_SIZE, GuiRender.Weight.REGULAR);
        GuiDrawUtil.drawSegmentedStates(x + 10, y + GuiDrawUtil.SEGMENT_TOP, w - 20,
                options, states, GuiDrawUtil.TINY_SIZE, GuiRender.Weight.SEMIBOLD);
    }

    // ── Вкладка тем ──────────────────────────────────────────────────────────

    private void renderThemes(int mouseX, int mouseY, int cx, int cy, int cw, int ch) {
        Yarnternal client = Yarnternal.getInstance();
        if (client == null || client.getStyleManager() == null) {
            maxScroll = 0;
            return;
        }

        List<Style> styles = client.getStyleManager().getStyles();
        Style current = client.getStyleManager().current();

        int cardW = Theme.CARD_W;
        int cardH = 42;
        int gap = Theme.CARD_GAP;
        int startY = cy + Theme.CONTENT_PAD - scrollOffset;

        for (int i = 0; i < styles.size(); i++) {
            int column = i % 2;
            int row = i / 2;
            int x = cx + Theme.CONTENT_PAD + column * (cardW + Theme.CONTENT_GAP);
            int y = startY + row * (cardH + gap);
            if (y + cardH < cy || y > cy + ch) {
                continue;
            }

            Style style = styles.get(i);
            boolean active = current != null && style.name().equals(current.name());
            boolean hovered = mouseX >= x && mouseX < x + cardW
                    && mouseY >= y && mouseY < y + cardH
                    && mouseY >= cy && mouseY < cy + ch;

            int borderA = active ? Theme.accent() : Theme.BORDER_BRIGHT;
            int borderB = active ? Theme.accentAlt() : Theme.BORDER_DARK;
            int fill = hovered ? Theme.BG_PANEL_HOVER : Theme.BG_PANEL;
            GuiDrawUtil.drawCard(x, y, cardW, cardH, fill, fill, borderA, borderB,
                    Theme.RADIUS_CARD);

            int c1 = ColorUtil.fromHex(style.colorOne());
            int c2 = ColorUtil.fromHex(style.colorTwo());
            GuiRender.rect4(x + 8, y + (cardH - 16) / 2.0, 26, 16,
                    c1, c2, c2, c1, 3);

            String name = GuiDrawUtil.trim(style.name(), cardW - 58,
                    GuiDrawUtil.BODY_SIZE, GuiRender.Weight.SEMIBOLD);
            double ty = y + (cardH - GuiDrawUtil.textHeight(GuiDrawUtil.BODY_SIZE,
                    GuiRender.Weight.SEMIBOLD)) / 2;
            GuiDrawUtil.drawText(name, x + 42, ty,
                    active ? Theme.TEXT_WHITE : (hovered ? Theme.TEXT_SOFT : Theme.TEXT_LIGHT),
                    GuiDrawUtil.BODY_SIZE, GuiRender.Weight.SEMIBOLD);

            if (active) {
                double ix = x + cardW - 14;
                double iy = y + cardH / 2.0;
                GuiRender.line(ix - 4, iy, ix - 1, iy + 3, Theme.accent());
                GuiRender.line(ix - 1, iy + 3, ix + 4, iy - 3, Theme.accent());
            }
        }

        int rows = (styles.size() + 1) / 2;
        int contentHeight = rows * (cardH + gap);
        maxScroll = Math.max(0, contentHeight - (ch - Theme.CONTENT_PAD * 2));
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
    }

    private boolean mouseClickedThemes(int mx, int my, int cx, int cy) {
        Yarnternal client = Yarnternal.getInstance();
        if (client == null || client.getStyleManager() == null) {
            return false;
        }

        List<Style> styles = client.getStyleManager().getStyles();
        int cardW = Theme.CARD_W;
        int cardH = 42;
        int gap = Theme.CARD_GAP;
        int startY = cy + Theme.CONTENT_PAD - scrollOffset;

        for (int i = 0; i < styles.size(); i++) {
            int column = i % 2;
            int row = i / 2;
            int x = cx + Theme.CONTENT_PAD + column * (cardW + Theme.CONTENT_GAP);
            int y = startY + row * (cardH + gap);

            if (mx >= x && mx < x + cardW && my >= y && my < y + cardH) {
                client.getStyleManager().apply(styles.get(i));
                return true;
            }
        }
        return false;
    }

    // ── Ввод: мышь ───────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(Click click, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(click, consumed);
        }

        int mx = (int) click.x();
        int my = (int) click.y();
        int button = click.button();

        // Перетаскивание окна за верхнюю кромку (логотип тоже подходит).
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && mx >= windowX && mx <= windowX + W
                && my >= windowY - 12 && my <= windowY + 8) {
            draggingWindow = true;
            dragOffsetX = mx - windowX;
            dragOffsetY = my - windowY;
            return true;
        }

        // Поле поиска.
        int searchX = windowX + Theme.SEARCH_X;
        int searchY = windowY + Theme.SEARCH_Y;
        if (mx >= searchX && mx < searchX + Theme.SEARCH_W
                && my >= searchY && my < searchY + Theme.SEARCH_H) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        // Категории в сайдбаре.
        if (mx >= windowX && mx < windowX + Theme.SIDEBAR_W
                && my >= windowY && my < windowY + H) {
            for (ModuleList category : ModuleList.values()) {
                int rowY = categoryY(category);
                if (my >= rowY && my < rowY + Theme.CATEGORY_H) {
                    if (selectedCategory != category) {
                        selectedCategory = category;
                        scrollOffset = 0;
                        listeningBindModule = null;
                        listeningBindSetting = null;
                    }
                    return true;
                }
            }
            return true;
        }

        // Контент.
        int cx = windowX + Theme.SIDEBAR_W;
        int cy = windowY;
        int cw = W - Theme.SIDEBAR_W;

        if (mx >= cx && mx < cx + cw && my >= cy && my < cy + H) {
            if (selectedCategory == ModuleList.Themes && searchQuery.trim().isEmpty()) {
                return mouseClickedThemes(mx, my, cx, cy);
            }

            List<CardLayout> cards = buildCardLayouts(cx, cy, cw);
            for (CardLayout card : cards) {
                if (mx < card.x || mx >= card.x + card.w
                        || my < card.y || my >= card.y + card.h) {
                    continue;
                }

                for (SettingLayout layout : card.settings) {
                    if (my >= layout.y && my < layout.y + layout.h) {
                        return handleSettingClick(card, layout, mx, my, button);
                    }
                }

                if (card.settings.isEmpty() || my < card.y + Theme.CARD_HEADER_H) {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                            && hasVisibleSettings(card.module)
                            && mx >= card.x + card.w - 26) {
                        toggleModuleCollapsed(card.module);
                        return true;
                    }
                    return handleCardHeaderClick(card.module, button);
                }
                return true;
            }
        }

        return super.mouseClicked(click, consumed);
    }

    private boolean handleCardHeaderClick(Module module, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            listeningBindModule = listeningBindModule == module ? null : module;
            listeningBindSetting = null;
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (hasVisibleSettings(module)) {
                toggleModuleCollapsed(module);
            }
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            module.toggle();
            return true;
        }
        return false;
    }

    private void toggleModuleCollapsed(Module module) {
        if (!collapsedModules.remove(module)) {
            collapsedModules.add(module);
            listeningBindModule = null;
            listeningBindSetting = null;
        }
    }

    private boolean handleSettingClick(CardLayout card, SettingLayout layout,
                                       int mx, int my, int button) {
        Setting<?> setting = layout.setting;
        int x = card.x;
        int w = card.w;

        if (setting instanceof BooleanSetting bool) {
            bool.toggle();
            return true;

        } else if (setting instanceof NumberSetting number) {
            double trackX = x + 10;
            double trackW = w - 20;
            double fraction = (mx - trackX) / trackW;
            number.setFraction(fraction);
            draggingSlider = number;
            dragSliderX = trackX;
            dragSliderW = trackW;
            return true;

        } else if (setting instanceof ModeSetting mode) {
            List<String> labels = segmentLabels(mode.getModes());
            for (int i = 0; i < labels.size(); i++) {
                if (insideSegment(labels, i, x + 10, layout.y + GuiDrawUtil.SEGMENT_TOP, w - 20, mx, my)) {
                    mode.setValue(mode.getModes().get(i));
                    return true;
                }
            }
            return true;

        } else if (setting instanceof MultiBooleanSetting multi) {
            List<String> raw = new ArrayList<>(multi.getOptions());
            List<String> labels = segmentLabels(raw);
            for (int i = 0; i < labels.size(); i++) {
                if (insideSegment(labels, i, x + 10, layout.y + GuiDrawUtil.SEGMENT_TOP, w - 20, mx, my)) {
                    multi.toggle(raw.get(i));
                    return true;
                }
            }
            return true;

        } else if (setting instanceof EnumSetting<?> enumSetting) {
            List<String> labels = segmentLabels(enumLabels(enumSetting));
            for (int i = 0; i < labels.size(); i++) {
                if (insideSegment(labels, i, x + 10, layout.y + GuiDrawUtil.SEGMENT_TOP, w - 20, mx, my)) {
                    setEnumValue(enumSetting, i);
                    return true;
                }
            }
            return true;

        } else if (setting instanceof ColorSetting color) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                color.setRainbow(!color.isRainbow());
            } else {
                int value = color.get();
                float[] hsv = java.awt.Color.RGBtoHSB((value >> 16) & 0xFF,
                        (value >> 8) & 0xFF, value & 0xFF, null);
                hsv[0] = (hsv[0] + 0.12f) % 1.0f;
                int rgb = java.awt.Color.HSBtoRGB(hsv[0], Math.max(0.6f, hsv[1]),
                        Math.max(0.7f, hsv[2]));
                color.setValue((value & 0xFF000000) | (rgb & 0x00FFFFFF));
            }
            return true;

        } else if (setting instanceof KeybindSetting keySetting) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                keySetting.setKey(0);
                listeningBindSetting = null;
            } else {
                listeningBindSetting = listeningBindSetting == keySetting
                        ? null : keySetting;
                listeningBindModule = null;
            }
            return true;
        }

        return true;
    }

    private boolean insideSegment(List<String> labels, int index, double x, double y,
                                  double maxWidth, int mx, int my) {
        double[] bounds = GuiDrawUtil.segmentBounds(x, y, maxWidth, labels, index,
                GuiDrawUtil.TINY_SIZE, GuiRender.Weight.SEMIBOLD);
        if (bounds == null) {
            return false;
        }
        return mx >= bounds[0] && mx < bounds[0] + bounds[2]
                && my >= bounds[1] && my < bounds[1] + bounds[3];
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setEnumValue(EnumSetting<?> setting, int index) {
        EnumSetting raw = setting;
        Object[] constants = raw.getConstants();
        if (index >= 0 && index < constants.length) {
            raw.setValue(constants[index]);
        }
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        int mx = (int) click.x();
        int my = (int) click.y();

        if (draggingWindow) {
            windowX = Math.max(0, Math.min(this.width - W, mx - dragOffsetX));
            windowY = Math.max(0, Math.min(this.height - H, my - dragOffsetY));
            return true;
        }

        if (draggingSlider != null && dragSliderW > 0) {
            draggingSlider.setFraction((mx - dragSliderX) / dragSliderW);
            return true;
        }

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        draggingWindow = false;
        draggingSlider = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {
        int cx = windowX + Theme.SIDEBAR_W;
        int cy = windowY;
        int cw = W - Theme.SIDEBAR_W;

        if ((int) mouseX >= cx && (int) mouseX < cx + cw
                && (int) mouseY >= cy && (int) mouseY < cy + H) {
            scrollOffset -= (int) (verticalAmount * 20);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    // ── Ввод: клавиатура ─────────────────────────────────────────────────────

    @Override
    public boolean charTyped(CharInput charInput) {
        if (searchFocused && charInput.isValidChar() && searchQuery.length() < 18) {
            searchQuery += charInput.asString();
            scrollOffset = 0;
            return true;
        }
        return super.charTyped(charInput);
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        int key = keyInput.key();

        if (listeningBindModule != null) {
            KeybindManager manager = Yarnternal.getInstance().getKeybindManager();
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE
                    || key == GLFW.GLFW_KEY_BACKSPACE) {
                if (manager != null) {
                    manager.unbind(listeningBindModule);
                }
            } else if (manager != null) {
                manager.bind(listeningBindModule, key);
            }
            listeningBindModule = null;
            return true;
        }

        if (listeningBindSetting != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE
                    || key == GLFW.GLFW_KEY_BACKSPACE) {
                listeningBindSetting.setKey(0);
            } else {
                listeningBindSetting.setKey(key);
            }
            listeningBindSetting = null;
            return true;
        }

        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    scrollOffset = 0;
                }
                return true;
            }
        }

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }

        return super.keyPressed(keyInput);
    }

    // ── Анимации ─────────────────────────────────────────────────────────────

    private float animated(Map<String, Float> map, String key, float target, float speed) {
        float current = map.getOrDefault(key, target);
        current += (target - current) * speed;
        if (Math.abs(current - target) < 0.004f) {
            current = target;
        }
        map.put(key, current);
        return current;
    }

    // ── Фильтрация ───────────────────────────────────────────────────────────

    private List<Module> getFilteredModules() {
        List<Module> all = Yarnternal.getInstance().getModuleProvider().getModules();
        List<Module> result = new ArrayList<>();
        String query = searchQuery.trim().toLowerCase(Locale.ROOT);

        for (Module module : all) {
            if (!query.isEmpty()) {
                if (module.getName().toLowerCase(Locale.ROOT).contains(query)
                        || (module.getDesc() != null
                        && module.getDesc().toLowerCase(Locale.ROOT).contains(query))) {
                    result.add(module);
                }
                continue;
            }

            if (module.getModuleList() == selectedCategory) {
                result.add(module);
            }
        }
        return result;
    }
}
