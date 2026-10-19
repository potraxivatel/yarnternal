package yarnternal.lol.common.modules.other;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.MinecraftClient;
import yarnternal.lol.api.event.EventTarget;
import yarnternal.lol.api.event.events.Render2DEvent;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.api.ModuleAnnotation;
import yarnternal.lol.common.api.ModuleList;
import yarnternal.lol.common.settings.BooleanSetting;
import yarnternal.lol.common.settings.EnumSetting;
import yarnternal.lol.common.settings.NumberSetting;
import yarnternal.lol.render.renderer.Renderer2D;
import yarnternal.lol.render.utils.HudRenderer;
import yarnternal.lol.render.utils.color.Color;

/**
 * Демо нового рендер-стека: блюр, шейдерные скругления (SDF, без «пикселей»
 * на краях) и свой шрифт Comfortaa.
 *
 * <p>Заодно показывает, как настройки модуля выглядят в меню: ширина панели
 * (слайдер), позиция (комбобокс) и блюр (переключатель).
 */
@ModuleAnnotation(name = "RenderTest", desc = "Демо: шейдерный рендер (скругления, блюр, шрифт)", type = ModuleList.Other)
public final class RenderTestModule extends Module {

    // палитра клиента
    private static final Color ACCENT = new Color(122, 92, 255, 255);      // #7A5CFF
    private static final Color ACCENT_ALT = new Color(255, 79, 216, 255);  // #FF4FD8

    private static final Color PANEL_TOP = new Color(24, 22, 34, 200);
    private static final Color PANEL_BOTTOM = new Color(14, 13, 20, 215);
    private static final Color TEXT_DIM = new Color(168, 163, 190, 255);

    public enum Position {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    private final NumberSetting width = addSetting(new NumberSetting("Width", 300, 240, 500, 10, " px"));
    private final EnumSetting<Position> position = addSetting(new EnumSetting<>("Position", Position.TOP_RIGHT));
    private final BooleanSetting blur = addSetting(new BooleanSetting("Blur", true));

    @EventTarget
    private void onRender2D(Render2DEvent event) {
        var window = MinecraftClient.getInstance().getWindow();
        double fbW = window.getFramebufferWidth();
        double fbH = window.getFramebufferHeight();

        // масштаб под разрешение (1.0 на 1080p, 2.0 на 4K) — чтобы демо
        // выглядело одинаково на любом мониторе
        double s = Math.max(1.0, fbH / 1080.0);

        double w = width.getValue() * s, h = 104 * s;
        double margin = 20 * s;
        double x = switch (position.getValue()) {
            case TOP_LEFT, BOTTOM_LEFT -> margin;
            case TOP_RIGHT, BOTTOM_RIGHT -> fbW - w - margin;
        };
        double y = switch (position.getValue()) {
            case TOP_LEFT, TOP_RIGHT -> margin;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> fbH - h - margin;
        };

        // 1. Блюр: снимаем кадр и размываем его пирамидой (6 уровней текстур)
        if (blur.getValue()) {
            Renderer2D.BLUR.update(1);
            blurQuad(x, y, w, h, 12 * s, 1.5);
        } else {
            Renderer2D.COLOR.roundedQuad(x, y, w, h, new Color(0, 0, 0, 170), 12 * s, 1.5);
        }

        // 2. Панель: вертикальный градиент со скруглением — края гладкие за счёт SDF
        Renderer2D.COLOR.roundedQuad(x, y, w, h,
                PANEL_TOP, PANEL_TOP, PANEL_BOTTOM, PANEL_BOTTOM,
                12 * s, 12 * s, 12 * s, 12 * s, 1.5);

        // 3. Акцентная полоса слева (градиент фиолетовый -> розовый)
        Renderer2D.COLOR.roundedQuad(x + 10 * s, y + 12 * s, 3 * s, h - 24 * s,
                ACCENT_ALT, ACCENT, ACCENT_ALT, ACCENT,
                1.5 * s, 1.5 * s, 1.5 * s, 1.5 * s, 1.5);

        // 4. Текст своим шрифтом (Comfortaa), тень — своя, не ванильная
        HudRenderer.INSTANCE.text("Yarnternal", x + 24 * s, y + 14 * s, ACCENT, true, 1.0 * s);
        HudRenderer.INSTANCE.text("SDF corners + blur + Comfortaa", x + 24 * s, y + 40 * s, Color.WHITE, false, 0.7 * s);
        HudRenderer.INSTANCE.text("Width: " + width.displayValue() + "   Position: " + position.displayValue(),
                x + 24 * s, y + 60 * s, TEXT_DIM, false, 0.7 * s);
        HudRenderer.INSTANCE.text("no more pixel corners", x + 24 * s, y + 80 * s, TEXT_DIM, false, 0.7 * s);
    }

    /**
     * Блюр-квад со скруглением. Координаты — в пикселях фреймбуфера
     * (из-за unscaled-проекции), поэтому UV считаем от размеров фреймбуфера,
     * а не от scaled-размеров окна.
     */
    private static void blurQuad(double x, double y, double w, double h, double radius, double smoothness) {
        var window = MinecraftClient.getInstance().getWindow();
        double fbW = window.getFramebufferWidth();
        double fbH = window.getFramebufferHeight();

        float u1 = (float) (x / fbW);
        float v1 = (float) (1.0 - y / fbH);
        float u2 = (float) ((x + w) / fbW);
        float v2 = (float) (1.0 - (y + h) / fbH);

        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.roundedTexQuad(x, y, w, h, 0, u1, v1, u2, v2,
                Color.WHITE, radius, radius, radius, radius, smoothness);
        Renderer2D.TEXTURE.render(Renderer2D.BLUR.getBlurredTexture(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
    }
}
