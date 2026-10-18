package yarnternal.lol.common.ui.style;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import yarnternal.lol.api.utility.render.RenderUtil;
import yarnternal.lol.api.utility.render.color.ColorUtil;

/**
 * Стиль клиента — одна цветовая схема для всего: HUD, панелей, меню (постепенно и GUI).
 * В основе два цвета темы, которые заменяют друг друга бегущей полосой: цвет плавно
 * перетекает из основного во второй и обратно, а сама полоса едет по экрану и по времени.
 *
 * Всё, что рисуется «в стиле», идёт через эти методы: Style.panel(...), Style.fill(...),
 * Style.text(...). Поменял setPrimary/setSecondary — и клиент мгновенно перекрасился целиком.
 */
public final class Style {

    private Style() {
        // статичный класс — конструктор запрещён
    }

    // ── Цвета темы ───────────────────────────────────────────────────────────

    /** Основной цвет темы. */
    private static int primary = ColorUtil.fromHex("#7A5CFF");

    /** Второй цвет темы — тот, в который перетекает основной. */
    private static int secondary = ColorUtil.fromHex("#FF4FD8");

    /** Длина полного цикла «цвет1 → цвет2 → цвет1» в пикселях. */
    private static float waveLength = 160.0f;

    /** Скорость полосы: сколько полных циклов в секунду проезжает одна точка. */
    private static float waveSpeed = 0.35f;

    /** Основной цвет темы. */
    public static int getPrimary() {
        return primary;
    }

    /** Второй цвет темы. */
    public static int getSecondary() {
        return secondary;
    }

    /** Меняет основной цвет темы. */
    public static void setPrimary(int color) {
        primary = color;
    }

    /** Меняет второй цвет темы. */
    public static void setSecondary(int color) {
        secondary = color;
    }

    /** Меняет длину полосы в пикселях (минимум 16, иначе полоса превращается в шум). */
    public static void setWaveLength(float length) {
        waveLength = Math.max(16.0f, length);
    }

    /** Меняет скорость полосы. */
    public static void setWaveSpeed(float speed) {
        waveSpeed = speed;
    }

    // ── Полоса ───────────────────────────────────────────────────────────────

    /** Цвет полосы по фазе: 0 — основной, 0.5 — второй, 1 — снова основной (значения вне 0..1 заворачиваются). */
    public static int wave(float phase) {
        float p = phase - (float) Math.floor(phase);
        float triangle = p < 0.5f ? p * 2.0f : 2.0f - p * 2.0f;
        return ColorUtil.lerp(primary, secondary, smooth(triangle));
    }

    /** Цвет полосы в экранной точке x (полоса бежит по экрану и со временем). */
    public static int sample(float x) {
        return sample(x, 0.0f);
    }

    /** Цвет полосы в экранной точке (x, y): по y волна идёт медленнее — получается лёгкая диагональ. */
    public static int sample(float x, float y) {
        float time = (System.currentTimeMillis() % 100_000L) / 1000.0f;
        return wave(x / waveLength + y / (waveLength * 2.0f) - time * waveSpeed);
    }

    private static float smooth(float t) {
        // smoothstep: сглаживает «углы» треугольника, переходы выглядят мягче
        return t * t * (3.0f - 2.0f * t);
    }

    // ── Готовое рисование в стиле ────────────────────────────────────────────

    /** Заливает прямоугольник полосой темы: шаг 4px — переходы плавные и без лишних квадов. */
    public static void fill(DrawContext context, float x, float y, float width, float height) {
        float step = 4.0f;
        for (float offset = 0.0f; offset < width; offset += step) {
            float segment = Math.min(step, width - offset);
            RenderUtil.fill(context, x + offset, y, segment + 0.5f, height, sample(x + offset, y));
        }
    }

    /** Рамка толщиной 1px, нарисованная полосой темы. */
    public static void border(DrawContext context, float x, float y, float width, float height) {
        fill(context, x, y, width, 1.0f);
        fill(context, x, y + height - 1.0f, width, 1.0f);
        RenderUtil.fill(context, x, y, 1.0f, height, sample(x, y));
        RenderUtil.fill(context, x + width - 1.0f, y, 1.0f, height, sample(x + width, y));
    }

    /** Тёмная подложка панели: текущий цвет полосы, сильно затемнённый. */
    public static int background(float x, float alpha) {
        return ColorUtil.withAlpha(ColorUtil.multiply(sample(x), 0.22f), Math.round(alpha));
    }

    /** Панель в стиле: тёмная подложка + рамка полосой. alpha — прозрачность подложки (0..255). */
    public static void panel(DrawContext context, float x, float y, float width, float height, float alpha) {
        RenderUtil.fill(context, x, y, width, height, background(x, alpha));
        border(context, x, y, width, height);
    }

    /** Текст цветом полосы в своей точке — заголовки медленно переливаются. */
    public static void text(DrawContext context, TextRenderer textRenderer, String text, float x, float y) {
        RenderUtil.text(context, textRenderer, text, x, y, sample(x, y));
    }
}
