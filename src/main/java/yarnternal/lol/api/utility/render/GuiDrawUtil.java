package yarnternal.lol.api.utility.render;

import yarnternal.lol.api.utility.render.color.ColorUtil;
import yarnternal.lol.common.ui.Theme;

import java.util.List;

/**
 * Примитивы ClickGUI в стиле Expensive 2.0 поверх {@link GuiRender}.
 *
 * <p>Геометрия — настоящие SDF-скругления, текст — Montserrat / Sora / Gilroy.
 * Все координаты в GUI-пикселях.
 */
public final class GuiDrawUtil {

    public static final int FONT_H = GuiRender.FONT_H;

    public static final double TITLE_SIZE  = GuiRender.size(12);
    public static final double BODY_SIZE   = GuiRender.size(9);
    public static final double SMALL_SIZE  = GuiRender.size(8);
    public static final double TINY_SIZE   = GuiRender.size(7.5);
    public static final double SEARCH_SIZE = GuiRender.size(9);
    public static final double BRAND_SIZE  = GuiRender.size(15);

    /** Отступ от подписи до сегментированного контрола. */
    public static final int SEGMENT_TOP = 11;
    /** Высота одной строки сегментированного контрола. */
    public static final int SEGMENT_LINE_H = 15;

    private GuiDrawUtil() {}

    // ── Поверхности ──────────────────────────────────────────────────────────

    public static void drawRoundRect(double x, double y, double w, double h,
                                     int color, double r) {
        GuiRender.rect(x, y, w, h, color, r);
    }

    public static void drawRoundRect(double x, double y, double w, double h,
                                     int fill, int border, double r, double borderWidth) {
        GuiRender.panel(x, y, w, h, r, fill, border, borderWidth);
    }

    /**
     * Карточка Expensive: тонкая диагональная градиентная обводка
     * (светлая слева-сверху, тёмная справа-снизу) и вертикальный градиент внутри.
     */
    public static void drawCard(double x, double y, double w, double h,
                                int top, int bottom, double radius) {
        drawCard(x, y, w, h, top, bottom, Theme.BORDER_BRIGHT, Theme.BORDER_DARK,
                radius);
    }

    public static void drawCard(double x, double y, double w, double h,
                                int top, int bottom, int borderA, int borderB,
                                double radius) {
        // Обводка 0.5 px: заливка-рамка, поверх неё — внутренний градиент.
        GuiRender.rect4(x, y, w, h, borderA, borderB, borderA, borderB, radius);
        GuiRender.rectV(x + 0.5, y + 0.5, w - 1, h - 1,
                top, bottom, Math.max(0, radius - 0.5));
    }

    /** Мягкое свечение вокруг окна: акцентные слои + глубокая чёрная тень. */
    public static void drawWindowGlow(double x, double y, double w, double h, double radius) {
        // Тень назад.
        for (int i = 12; i >= 1; i--) {
            int alpha = 3 + (12 - i);
            int c = withAlpha(0x000000, alpha);
            GuiRender.rect4(x - i + 3, y - i + 4, w + i * 2, h + i * 2,
                    c, c, c, c, radius + i);
        }

        int a = Theme.accent();
        int b = Theme.accentAlt();
        for (int i = 8; i >= 1; i--) {
            int alpha = 2 + (8 - i);
            GuiRender.rect4(x - i, y - i, w + i * 2, h + i * 2,
                    withAlpha(a, alpha), withAlpha(b, alpha),
                    withAlpha(b, alpha), withAlpha(a, alpha),
                    radius + i);
        }
    }

    /** Подсветка активной категории: мягкое свечение + акцентный градиент. */
    public static void drawCategoryHighlight(double x, double y, double w, double h,
                                             float anim) {
        if (anim <= 0.01f) {
            return;
        }
        int a = withAlpha(Theme.accent(), (int) (100 * anim));
        int b = withAlpha(Theme.accentAlt(), (int) (100 * anim));

        for (int i = 2; i >= 1; i--) {
            int alpha = (int) (7 * anim);
            GuiRender.rect4(x - i, y - i, w + i * 2, h + i * 2,
                    withAlpha(Theme.accent(), alpha), withAlpha(Theme.accentAlt(), alpha),
                    withAlpha(Theme.accentAlt(), alpha), withAlpha(Theme.accent(), alpha),
                    3 + i);
        }
        // Горизонтальный градиент: без диагональной «полосы» поперёк строки.
        GuiRender.rect4(x, y, w, h, a, b, b, a, 3);
    }

    // ── Текст ────────────────────────────────────────────────────────────────

    public static void drawText(String text, double x, double y, int color) {
        GuiRender.text(text, x, y, color);
    }

    public static void drawText(String text, double x, double y, int color, double size) {
        GuiRender.text(text, x, y, color, size);
    }

    public static void drawText(String text, double x, double y, int color,
                                double size, GuiRender.Weight weight) {
        GuiRender.text(text, x, y, color, size, weight);
    }

    /**
     * Текст с плавным переливом accent → accentAlt → accent.
     * Градиент идёт по буквам и медленно смещается со временем.
     */
    public static void drawFlowText(String text, double x, double y, double size,
                                    GuiRender.Weight weight, float phase) {
        if (text == null || text.isEmpty()) {
            return;
        }

        int length = text.length();
        double span = Math.max(1, length - 1);
        for (int i = 0; i < length; i++) {
            String glyph = String.valueOf(text.charAt(i));
            double t = (i / span + phase) % 1.0;
            int color = t < 0.5
                    ? ColorUtil.lerp(Theme.accent(), Theme.accentAlt(), (float) (t * 2.0))
                    : ColorUtil.lerp(Theme.accentAlt(), Theme.accent(), (float) ((t - 0.5) * 2.0));

            GuiRender.text(glyph, x, y, color, size, weight);
            x += GuiRender.textWidth(glyph, size, weight);
        }
    }

    public static double textWidth(String text) {
        return GuiRender.textWidth(text);
    }

    public static double textWidth(String text, double size) {
        return GuiRender.textWidth(text, size);
    }

    public static double textWidth(String text, double size, GuiRender.Weight weight) {
        return GuiRender.textWidth(text, size, weight);
    }

    public static double textHeight(double size) {
        return GuiRender.textHeight(size);
    }

    public static double textHeight(double size, GuiRender.Weight weight) {
        return GuiRender.textHeight(size, weight);
    }

    public static double centeredX(String text, double x, double w) {
        return x + (w - textWidth(text)) / 2;
    }

    public static double centeredX(String text, double x, double w,
                                   double size, GuiRender.Weight weight) {
        return x + (w - textWidth(text, size, weight)) / 2;
    }

    /** Обрезает строку по ширине, добавляя «..» в конце. */
    public static String trim(String text, double maxWidth, double size,
                              GuiRender.Weight weight) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (textWidth(text, size, weight) <= maxWidth) {
            return text;
        }

        String value = text;
        while (value.length() > 1 && textWidth(value + "..", size, weight) > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "..";
    }

    // ── Поиск ────────────────────────────────────────────────────────────────

    public static void drawSearchBox(double x, double y, double w, double h,
                                     String query, boolean focused) {
        GuiRender.rect(x, y, w, h, Theme.BG_SEARCH, Theme.RADIUS_SEARCH);
        if (focused) {
            GuiRender.panel(x, y, w, h, Theme.RADIUS_SEARCH, 0,
                    withAlpha(Theme.accent(), 0x77), 0.5);
        }

        double ty = y + (h - textHeight(SEARCH_SIZE, GuiRender.Weight.BRAND)) / 2 - 0.5;
        if (query.isEmpty() && !focused) {
            drawText("Search...", x + 6, ty, withAlpha(Theme.TEXT_LIGHT, 0xCC),
                    SEARCH_SIZE, GuiRender.Weight.BRAND);
        } else {
            String display = query + (focused && (System.currentTimeMillis() % 1000 > 500) ? "_" : "");
            drawText(display, x + 6, ty, withAlpha(Theme.TEXT_LIGHT, 0xEE),
                    SEARCH_SIZE, GuiRender.Weight.BRAND);
        }
    }

    // ── Переключатели ────────────────────────────────────────────────────────

    /** Маленький серый switch Expensive. */
    public static void drawToggle(double x, double y, float anim) {
        double w = 16;
        double h = 7.5;

        GuiRender.rect(x, y, w, h, Theme.TOGGLE_TRACK, 3.75);

        int knobColor = blend(Theme.TOGGLE_OFF, Theme.TOGGLE_ON, anim);
        double kd = 6;
        double kx = x + 1.5 + 8.5 * anim;
        double ky = y + (h - kd) / 2;
        drawRoundRect(kx, ky, kd, kd, withAlpha(knobColor, 0x88), kd / 2);
        drawRoundRect(kx, ky, kd, kd, knobColor, kd / 2);
    }

    // ── Слайдер ──────────────────────────────────────────────────────────────

    /** Тонкая дорожка слайдера с серой ручкой — как в Expensive. */
    public static void drawSliderTrack(double x, double y, double w, double fraction) {
        double fill = Math.max(0, Math.min(1, fraction)) * w;

        GuiRender.rect(x, y, w, 4, Theme.SLIDER_TRACK, 2);
        if (fill > 0) {
            GuiRender.rect(x, y, Math.min(fill, w), 4, Theme.SLIDER_FILL, 2);
        }

        double kd = 8;
        double kx = x + fill - kd / 2;
        double ky = y + 2 - kd / 2;
        drawRoundRect(kx, ky, kd, kd, withAlpha(Theme.SLIDER_KNOB, 0x66), kd / 2);
        drawRoundRect(kx, ky, kd, kd, Theme.SLIDER_KNOB, kd / 2);
    }

    // ── Сегментированный контрол ─────────────────────────────────────────────

    public static int segmentedLineCount(List<String> options, double maxWidth,
                                         double size, GuiRender.Weight weight) {
        double limit = Math.max(10, maxWidth - 7);
        int lines = 1;
        double lineW = 0;
        for (String option : options) {
            double w = textWidth(option, size, weight) + 3;
            if (lineW > 0 && lineW + w > limit) {
                lines++;
                lineW = 0;
            }
            lineW += w;
        }
        return lines;
    }

    /** Четыре и больше вариантов раскладываем в сетку 2×N, а не в рваную строку. */
    public static int segmentedColumns(List<String> options) {
        return options.size() >= 4 ? 2 : 0;
    }

    public static int segmentedRows(List<String> options, double maxWidth,
                                    double size, GuiRender.Weight weight) {
        int columns = segmentedColumns(options);
        if (columns > 0) {
            return (options.size() + columns - 1) / columns;
        }
        return segmentedLineCount(options, maxWidth, size, weight);
    }

    public static double segmentedWidth(List<String> options, double maxWidth,
                                        double size, GuiRender.Weight weight) {
        if (segmentedColumns(options) > 0) {
            return maxWidth;
        }
        double limit = Math.max(10, maxWidth - 7);
        int lines = segmentedLineCount(options, maxWidth, size, weight);
        double[] sums = new double[lines];
        int line = 0;
        for (String option : options) {
            double w = textWidth(option, size, weight) + 3;
            if (sums[line] > 0 && sums[line] + w > limit) {
                line++;
            }
            sums[line] += w;
        }
        double max = 0;
        for (double sum : sums) {
            max = Math.max(max, sum);
        }
        return Math.min(maxWidth, Math.max(10, max + 7));
    }

    /**
     * Рисует сегментированный контрол и возвращает его высоту.
     * Выбранный пункт светлый, остальные почти растворяются в фоне.
     */
    public static double drawSegmented(double x, double y, double maxWidth,
                                       List<String> options, int selected,
                                       double size, GuiRender.Weight weight) {
        boolean[] states = new boolean[options.size()];
        if (selected >= 0 && selected < states.length) {
            states[selected] = true;
        }
        return drawSegmentedStates(x, y, maxWidth, options, states, size, weight);
    }

    /** Сегментированный контрол с произвольным набором выбранных пунктов. */
    public static double drawSegmentedStates(double x, double y, double maxWidth,
                                             List<String> options, boolean[] active,
                                             double size, GuiRender.Weight weight) {
        int columns = segmentedColumns(options);
        int rows = segmentedRows(options, maxWidth, size, weight);
        double boxW = segmentedWidth(options, maxWidth, size, weight);
        double boxH = SEGMENT_LINE_H * rows;

        GuiRender.rect(x, y, boxW, boxH, Theme.BG_SEGMENT, 3);

        if (columns > 0) {
            double cellW = boxW / columns;
            for (int i = 0; i < options.size(); i++) {
                int row = i / columns;
                int column = i % columns;
                double cellX = x + column * cellW;
                double cellY = y + row * SEGMENT_LINE_H;
                boolean on = active != null && i < active.length && active[i];

                if (on) {
                    GuiRender.rect(cellX + 2, cellY + 2, cellW - 4,
                            SEGMENT_LINE_H - 4, 0x1FFFFFFF, 3);
                }

                String option = trim(options.get(i), cellW - 8, size, weight);
                drawText(option, cellX + (cellW - textWidth(option, size, weight)) / 2,
                        cellY + (SEGMENT_LINE_H - textHeight(size, weight)) / 2,
                        on ? Theme.TEXT_MUTED : Theme.TEXT_DIM, size, weight);
            }
            return boxH;
        }

        double limit = Math.max(10, maxWidth - 7);
        double ox = 0;
        int line = 0;
        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            double ow = textWidth(option, size, weight) + 3;
            if (ox > 0 && ox + ow > limit) {
                ox = 0;
                line++;
            }

            boolean on = active != null && i < active.length && active[i];
            double ty = y + line * SEGMENT_LINE_H
                    + (SEGMENT_LINE_H - textHeight(size, weight)) / 2;
            drawText(option, x + 5 + ox, ty,
                    on ? Theme.TEXT_MUTED : Theme.TEXT_DIM, size, weight);

            ox += ow;
        }
        return boxH;
    }

    /** Прямоугольники пунктов сегментированного контрола: x, y, w, h. */
    public static double[] segmentBounds(double x, double y, double maxWidth,
                                         List<String> options, int index,
                                         double size, GuiRender.Weight weight) {
        if (index < 0 || index >= options.size()) {
            return null;
        }
        int columns = segmentedColumns(options);
        double boxW = segmentedWidth(options, maxWidth, size, weight);
        if (columns > 0) {
            double cellW = boxW / columns;
            int row = index / columns;
            int column = index % columns;
            return new double[]{x + column * cellW, y + row * SEGMENT_LINE_H,
                    cellW, SEGMENT_LINE_H};
        }

        double limit = Math.max(10, maxWidth - 7);
        double ox = 0;
        int line = 0;
        for (int i = 0; i <= index; i++) {
            String option = options.get(i);
            double ow = textWidth(option, size, weight) + 3;
            if (ox > 0 && ox + ow > limit) {
                ox = 0;
                line++;
            }
            if (i == index) {
                return new double[]{x + 5 + ox, y + line * SEGMENT_LINE_H, ow,
                        SEGMENT_LINE_H};
            }
            ox += ow;
        }
        return null;
    }

    // ── Мелкие поля ──────────────────────────────────────────────────────────

    public static void drawKeyBox(double x, double y, String text, boolean listening) {
        String label = listening ? "..." : text;
        double tw = textWidth(label, TINY_SIZE, GuiRender.Weight.MEDIUM);
        double w = Math.max(10, tw + 6);
        double h = 12;

        GuiRender.rect(x, y, w, h, Theme.BG_KEY, 2);
        drawText(label, x + (w - tw) / 2,
                y + (h - textHeight(TINY_SIZE, GuiRender.Weight.MEDIUM)) / 2,
                listening ? Theme.accent() : Theme.TEXT_MUTED,
                TINY_SIZE, GuiRender.Weight.MEDIUM);
    }

    public static double keyBoxWidth(String text, boolean listening) {
        String label = listening ? "..." : text;
        return Math.max(10, textWidth(label, TINY_SIZE, GuiRender.Weight.MEDIUM) + 6);
    }

    public static void drawColorSwatch(double x, double y, double w, double h,
                                       int color, boolean rainbow) {
        if (rainbow) {
            GuiRender.rect(x, y, w, h, 0x5939465C, 2);
            int segments = Math.max(1, (int) (w / 3));
            double segW = w / segments;
            for (int i = 0; i < segments; i++) {
                int c = ColorUtil.rainbow((float) i / segments, 0.8f, 1.0f);
                GuiRender.rect(x + 1 + i * segW, y + 1,
                        Math.min(segW + 0.4, x + w - 1 - (x + 1 + i * segW)),
                        h - 2, c, 1);
            }
        } else {
            GuiRender.rect(x, y, w, h, withAlpha(0xFFFFFF, 0x22), 2);
            GuiRender.rect(x + 0.5, y + 0.5, w - 1, h - 1, color, 1.5);
        }
    }

    // ── Хелперы ──────────────────────────────────────────────────────────────

    /** Линейная интерполяция двух ARGB-цветов: t=0 → a, t=1 → b. */
    public static int blend(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >> 24) & 0xFF, ra = (a >> 16) & 0xFF, ga = (a >> 8) & 0xFF, ba2 = a & 0xFF;
        int ab = (b >> 24) & 0xFF, rb = (b >> 16) & 0xFF, gb = (b >> 8) & 0xFF, bb2 = b & 0xFF;
        return ((int) (aa + (ab - aa) * t) << 24)
             | ((int) (ra + (rb - ra) * t) << 16)
             | ((int) (ga + (gb - ga) * t) << 8)
             | (int) (ba2 + (bb2 - ba2) * t);
    }

    /** Возвращает цвет с заменённой альфой. */
    public static int withAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }
}
