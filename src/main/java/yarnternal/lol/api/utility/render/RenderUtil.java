package yarnternal.lol.api.utility.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import yarnternal.lol.api.utility.render.color.ColorUtil;
import yarnternal.lol.core.Yarnternal;

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

    // ── Тема клиента: бегущая полоса ─────────────────────────────────────────
    // Краски темы хранятся в главном классе (Yarnternal) — тут только движение
    // полосы: две краски плавно заменяют друг друга, и полоса едет по экрану
    // и по времени. Сменил стиль в главном классе — рисунок перекрасился сам.

    /** Длина полного цикла «краска1 → краска2 → краска1» в пикселях. */
    private static float waveLength = 160.0f;

    /** Скорость полосы: сколько полных циклов в секунду проезжает одна точка. */
    private static float waveSpeed = 0.35f;

    /** Меняет длину полосы в пикселях (минимум 16, иначе полоса превращается в шум). */
    public static void setWaveLength(float length) {
        waveLength = Math.max(16.0f, length);
    }

    /** Меняет скорость полосы. */
    public static void setWaveSpeed(float speed) {
        waveSpeed = speed;
    }

    /** Цвет полосы в экранной точке x (по вертикали не смещается). */
    public static int waveColor(float x) {
        return waveColor(x, 0.0f);
    }

    /** Цвет полосы в экранной точке (x, y): по y волна идёт медленнее — получается лёгкая диагональ. */
    public static int waveColor(float x, float y) {
        Yarnternal client = Yarnternal.getInstance();
        float time = (System.currentTimeMillis() % 100_000L) / 1000.0f;
        return ColorUtil.wave(client.getColorOne(), client.getColorTwo(), x / waveLength + y / (waveLength * 2.0f) - time * waveSpeed);
    }

    /** Заливает прямоугольник полосой темы: шаг 4px — переходы плавные и без лишних квадов. */
    public static void fillWave(DrawContext context, float x, float y, float width, float height) {
        float step = 4.0f;
        for (float offset = 0.0f; offset < width; offset += step) {
            float segment = Math.min(step, width - offset);
            fill(context, x + offset, y, segment + 0.5f, height, waveColor(x + offset, y));
        }
    }

    /** Обводка толщиной 1px, нарисованная полосой темы. */
    public static void outlineWave(DrawContext context, float x, float y, float width, float height) {
        fillWave(context, x, y, width, 1.0f);
        fillWave(context, x, y + height - 1.0f, width, 1.0f);
        fill(context, x, y, 1.0f, height, waveColor(x, y));
        fill(context, x + width - 1.0f, y, 1.0f, height, waveColor(x + width, y));
    }

    /** Тёмная подложка панели: текущий цвет полосы, сильно затемнённый; alpha 0..255. */
    public static int waveBackground(float x, float alpha) {
        return ColorUtil.withAlpha(ColorUtil.multiply(waveColor(x), 0.22f), Math.round(alpha));
    }

    /** Панель в стиле: тёмная подложка + рамка полосой. alpha — прозрачность подложки (0..255). */
    public static void panelWave(DrawContext context, float x, float y, float width, float height, float alpha) {
        fill(context, x, y, width, height, waveBackground(x, alpha));
        outlineWave(context, x, y, width, height);
    }

    /** Текст цветом полосы в своей точке — заголовки медленно переливаются. */
    public static void textWave(DrawContext context, TextRenderer textRenderer, String text, float x, float y) {
        text(context, textRenderer, text, x, y, waveColor(x, y));
    }

    // ── Скруглённые прямоугольники ───────────────────────────────────────────
    // Без шейдеров: дуги собираются из пикселей по формуле окружности
    // (dx = √(r² − dy²)), а граничные пиксели получают прозрачность по доле
    // покрытия — маленький AA на CPU, поэтому края выходят гладкими.

    /** Ограничивает радиус: не больше половины меньшей стороны. */
    private static float clampRadius(float width, float height, float radius) {
        return Math.max(0.0f, Math.min(radius, Math.min(width, height) / 2.0f));
    }

    /** Доля пикселя (px, py), попавшая внутрь круга радиуса r с центром (cx, cy). */
    private static float pixelCoverage(float px, float py, float cx, float cy, float r) {
        float r2 = r * r;

        // пиксель целиком внутри круга?
        float farX = Math.max(Math.abs(px - cx), Math.abs(px + 1.0f - cx));
        float farY = Math.max(Math.abs(py - cy), Math.abs(py + 1.0f - cy));
        if (farX * farX + farY * farY <= r2) {
            return 1.0f;
        }

        // пиксель целиком снаружи?
        float nearX = Math.max(0.0f, Math.max(px - cx, cx - (px + 1.0f)));
        float nearY = Math.max(0.0f, Math.max(py - cy, cy - (py + 1.0f)));
        if (nearX * nearX + nearY * nearY >= r2) {
            return 0.0f;
        }

        // граничный пиксель: считаем по подточкам 4×4
        int samples = 4;
        int inside = 0;
        for (int sy = 0; sy < samples; sy++) {
            for (int sx = 0; sx < samples; sx++) {
                float sampleX = px + (sx + 0.5f) / samples;
                float sampleY = py + (sy + 0.5f) / samples;
                float dx = sampleX - cx;
                float dy = sampleY - cy;
                if (dx * dx + dy * dy <= r2) {
                    inside++;
                }
            }
        }
        return inside / (float) (samples * samples);
    }

    /** Цвет с прозрачностью, умноженной на покрытие (для сглаженных краёв). */
    private static int withCoverage(int color, float coverage) {
        return ColorUtil.withAlpha(color, Math.round(ColorUtil.getAlpha(color) * coverage));
    }

    /** Рисует один угол скругления по пикселям: прозрачность — по доле покрытия. */
    private static void fillCorner(DrawContext context, float cornerX, float cornerY, float centerX, float centerY, float radius, int color) {
        int size = (int) Math.ceil(radius);
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                float px = cornerX + j;
                float py = cornerY + i;
                float coverage = pixelCoverage(px, py, centerX, centerY, radius);
                if (coverage >= 0.99f) {
                    fill(context, px, py, 1.0f, 1.0f, color);
                } else if (coverage > 0.02f) {
                    fill(context, px, py, 1.0f, 1.0f, withCoverage(color, coverage));
                }
            }
        }
    }

    /** Угол скругления полосой темы: цвет волны в каждом пикселе + прозрачность по покрытию. */
    private static void waveCorner(DrawContext context, float cornerX, float cornerY, float centerX, float centerY, float radius) {
        int size = (int) Math.ceil(radius);
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                float px = cornerX + j;
                float py = cornerY + i;
                float coverage = pixelCoverage(px, py, centerX, centerY, radius);
                if (coverage > 0.02f) {
                    int color = waveColor(px, py);
                    fill(context, px, py, 1.0f, 1.0f, coverage >= 0.99f ? color : withCoverage(color, coverage));
                }
            }
        }
    }

    /** Угол обводки: покрытие кольца (радиус r минус r−1) — дуга толщиной 1px, но гладкая. */
    private static void outlineCorner(DrawContext context, float cornerX, float cornerY, float centerX, float centerY, float radius, int color) {
        int size = (int) Math.ceil(radius);
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                float px = cornerX + j;
                float py = cornerY + i;
                float coverage = pixelCoverage(px, py, centerX, centerY, radius)
                        - pixelCoverage(px, py, centerX, centerY, radius - 1.0f);
                if (coverage > 0.02f) {
                    fill(context, px, py, 1.0f, 1.0f, withCoverage(color, Math.min(1.0f, coverage)));
                }
            }
        }
    }

    /** Заливает прямоугольник со скруглёнными углами. */
    public static void fillRounded(DrawContext context, float x, float y, float width, float height, float radius, int color) {
        float r = clampRadius(width, height, radius);
        if (r < 1.0f) {
            fill(context, x, y, width, height, color);
            return;
        }

        // прямые участки: центральный столб и две боковые полосы
        fill(context, x + r, y, width - 2.0f * r, height, color);
        fill(context, x, y + r, r, height - 2.0f * r, color);
        fill(context, x + width - r, y + r, r, height - 2.0f * r, color);

        // углы — сглаженные, по пикселям
        fillCorner(context, x, y, x + r, y + r, r, color);
        fillCorner(context, x + width - r, y, x + width - r, y + r, r, color);
        fillCorner(context, x, y + height - r, x + r, y + height - r, r, color);
        fillCorner(context, x + width - r, y + height - r, x + width - r, y + height - r, r, color);
    }

    /** Обводка скруглённого прямоугольника толщиной 1px. */
    public static void outlineRounded(DrawContext context, float x, float y, float width, float height, float radius, int color) {
        float r = clampRadius(width, height, radius);
        if (r < 1.0f) {
            outline(context, x, y, width, height, color);
            return;
        }

        // прямые участки сторон (на 1px внутрь, чтобы сомкнуться с дугами)
        fill(context, x + r - 1.0f, y, width - 2.0f * r + 2.0f, 1.0f, color);
        fill(context, x + r - 1.0f, y + height - 1.0f, width - 2.0f * r + 2.0f, 1.0f, color);
        fill(context, x, y + r - 1.0f, 1.0f, height - 2.0f * r + 2.0f, color);
        fill(context, x + width - 1.0f, y + r - 1.0f, 1.0f, height - 2.0f * r + 2.0f, color);

        // дуги углов — гладкие, по пикселям
        outlineCorner(context, x, y, x + r, y + r, r, color);
        outlineCorner(context, x + width - r, y, x + width - r, y + r, r, color);
        outlineCorner(context, x, y + height - r, x + r, y + height - r, r, color);
        outlineCorner(context, x + width - r, y + height - r, x + width - r, y + height - r, r, color);
    }

    /** Панель со скруглёнными углами: фон + рамка 1px. */
    public static void panelRounded(DrawContext context, float x, float y, float width, float height, float radius, int background, int border) {
        fillRounded(context, x, y, width, height, radius, border);
        fillRounded(context, x + 1.0f, y + 1.0f, width - 2.0f, height - 2.0f, radius - 1.0f, background);
    }

    /** Заливает скруглённый прямоугольник полосой темы. */
    public static void fillWaveRounded(DrawContext context, float x, float y, float width, float height, float radius) {
        float r = clampRadius(width, height, radius);
        if (r < 1.0f) {
            fillWave(context, x, y, width, height);
            return;
        }

        float step = 4.0f;

        // центральная полоса — во всю высоту
        for (float offset = r; offset < width - r; offset += step) {
            float segment = Math.min(step, width - r - offset);
            fill(context, x + offset, y, segment + 0.5f, height, waveColor(x + offset, y));
        }

        // боковые полосы — только между углами
        for (float offset = 0.0f; offset < r; offset += step) {
            float segment = Math.min(step, r - offset);
            fill(context, x + offset, y + r, segment + 0.5f, height - 2.0f * r, waveColor(x + offset, y));
            fill(context, x + width - r + offset, y + r, segment + 0.5f, height - 2.0f * r, waveColor(x + width - r + offset, y));
        }

        // углы — гладкие, полоса темы по пикселям
        waveCorner(context, x, y, x + r, y + r, r);
        waveCorner(context, x + width - r, y, x + width - r, y + r, r);
        waveCorner(context, x, y + height - r, x + r, y + height - r, r);
        waveCorner(context, x + width - r, y + height - r, x + width - r, y + height - r, r);
    }

    /** Панель в стиле со скруглёнными углами: подложка + рамка полосой. alpha — прозрачность подложки (0..255). */
    public static void panelWaveRounded(DrawContext context, float x, float y, float width, float height, float radius, float alpha) {
        fillWaveRounded(context, x, y, width, height, radius);
        fillRounded(context, x + 1.0f, y + 1.0f, width - 2.0f, height - 2.0f, radius - 1.0f, waveBackground(x, alpha));
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
