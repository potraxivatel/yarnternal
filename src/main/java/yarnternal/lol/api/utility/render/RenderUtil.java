package yarnternal.lol.api.utility.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import yarnternal.lol.api.utility.render.color.ColorUtil;

/**
 * Render утилита — обёртки для рисования в HUD через DrawContext.
 * Координаты как в майне: x/y — левый верхний угол, дальше ширина и высота.
 * Все методы статические, создавать инстанс не нужно.
 */
public final class RenderUtil {

    private RenderUtil() {
        // утилитный класс — конструктор запрещён
    }

    /** Заливает прямоугольник цветом. */
    public static void fill(DrawContext context, float x, float y, float width, float height, int color) {
        context.fill(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), color);
    }

    /** Вертикальный градиент: сверху top → снизу bottom. */
    public static void fillGradient(DrawContext context, float x, float y, float width, float height, int top, int bottom) {
        context.fillGradient(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), top, bottom);
    }

    /** Горизонтальный градиент: слева left → справа right (собирается из вертикальных полос). */
    public static void fillHorizontalGradient(DrawContext context, float x, float y, float width, float height,
                                              int left, int right, int steps) {
        int safeSteps = Math.max(2, steps);
        float stepWidth = width / safeSteps;
        for (int i = 0; i < safeSteps; i++) {
            int color = ColorUtil.lerp(left, right, (i + 1) / (float) safeSteps);
            fill(context, x + i * stepWidth, y, stepWidth + 0.5f, height, color);
        }
    }

    /** Радужная полоса слева направо (offset сдвигает цвета — удобно анимировать по времени). */
    public static void fillRainbow(DrawContext context, float x, float y, float width, float height, float offset) {
        int steps = Math.max(2, Math.round(width));
        float stepWidth = width / steps;
        for (int i = 0; i < steps; i++) {
            int color = ColorUtil.rainbow(offset + (float) i / steps, 1.0f, 1.0f);
            fill(context, x + i * stepWidth, y, stepWidth + 0.5f, height, color);
        }
    }

    /** Обводка прямоугольника толщиной в 1 пиксель. */
    public static void outline(DrawContext context, float x, float y, float width, float height, int color) {
        fill(context, x, y, width, 1.0f, color);
        fill(context, x, y + height - 1.0f, width, 1.0f, color);
        fill(context, x + 1.0f, y + 1.0f, 1.0f, height - 2.0f, color);
        fill(context, x + width - 1.0f, y + 1.0f, 1.0f, height - 2.0f, color);
    }

    /** Панель: фон + рамка. */
    public static void panel(DrawContext context, float x, float y, float width, float height, int background, int border) {
        fill(context, x, y, width, height, background);
        outline(context, x, y, width, height, border);
    }

    // ── Шрифты ───────────────────────────────────────────────────────────────
    // Клиентский шрифт лежит в assets/yarnternal/font/client.json и НЕ трогает
    // ванильные шрифты игры: он подключается только когда текст рисуют отсюда.

    /** Идентификатор клиентского шрифта (assets/yarnternal/font/client.json). */
    public static final Identifier CLIENT_FONT_ID = Identifier.of("yarnternal", "client");

    private static final Style CLIENT_FONT_STYLE =
            Style.EMPTY.withFont(new StyleSpriteSource.Font(CLIENT_FONT_ID));

    /** Рисует текст клиентским шрифтом (с тенью). */
    public static void text(DrawContext context, TextRenderer textRenderer, String text, float x, float y, int color) {
        text(context, textRenderer, text, x, y, color, true);
    }

    /** Рисует текст клиентским шрифтом. */
    public static void text(DrawContext context, TextRenderer textRenderer, String text, float x, float y, int color, boolean shadow) {
        drawText(context, textRenderer, Text.literal(text).setStyle(CLIENT_FONT_STYLE), x, y, color, shadow);
    }

    /** Рисует текст ванильным шрифтом игры (клиентский шрифт не используется). */
    public static void textVanilla(DrawContext context, TextRenderer textRenderer, String text, float x, float y, int color, boolean shadow) {
        drawText(context, textRenderer, Text.literal(text), x, y, color, shadow);
    }

    private static void drawText(DrawContext context, TextRenderer textRenderer, Text text, float x, float y, int color, boolean shadow) {
        context.drawText(textRenderer, text, Math.round(x), Math.round(y), color, shadow);
    }
}
