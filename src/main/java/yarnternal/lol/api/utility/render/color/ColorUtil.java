package yarnternal.lol.api.utility.render.color;

import yarnternal.lol.api.utility.math.MathUtil;

/**
 * Color утилита — работа с цветом в формате ARGB (0xAARRGGBB), как в Minecraft.
 * Все методы статические, создавать инстанс не нужно.
 */
public final class ColorUtil {

    private ColorUtil() {
        // утилитный класс — конструктор запрещён
    }

    /** Собирает цвет из компонентов (каждый 0-255). Формат результата: ARGB. */
    public static int rgba(int red, int green, int blue, int alpha) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    /** Собирает непрозрачный (alpha = 255) цвет из компонентов. */
    public static int rgb(int red, int green, int blue) {
        return rgba(red, green, blue, 255);
    }

    /** Парсит цвет из hex-строки: "#RRGGBB", "#AARRGGBB" (решётка необязательна). */
    public static int fromHex(String hex) {
        String value = hex.trim().replace("#", "");
        if (value.length() == 6) {
            return 0xFF000000 | Integer.parseInt(value, 16);
        }
        return (int) Long.parseLong(value, 16);
    }

    /** Переводит цвет обратно в hex-строку вида "#AARRGGBB" (удобно для дебага). */
    public static String toHex(int color) {
        return String.format("#%08X", color);
    }

    /** Красный компонент (0-255). */
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    /** Зелёный компонент (0-255). */
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    /** Синий компонент (0-255). */
    public static int getBlue(int color) {
        return color & 0xFF;
    }

    /** Прозрачность (0-255). */
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    /** Ставит новую прозрачность (0-255). */
    public static int withAlpha(int color, int alpha) {
        return rgba(getRed(color), getGreen(color), getBlue(color), MathUtil.clamp(alpha, 0, 255));
    }

    /** Умножает текущую прозрачность на множитель (0..1 — прозрачнее, больше 1 — плотнее). */
    public static int multiplyAlpha(int color, float factor) {
        return withAlpha(color, Math.round(getAlpha(color) * factor));
    }

    /** Плавный переход между двумя цветами: t=0 → from, t=1 → to. */
    public static int lerp(int from, int to, float t) {
        float delta = MathUtil.clamp(t, 0.0f, 1.0f);
        return rgba(
                Math.round(MathUtil.lerp(getRed(from), getRed(to), delta)),
                Math.round(MathUtil.lerp(getGreen(from), getGreen(to), delta)),
                Math.round(MathUtil.lerp(getBlue(from), getBlue(to), delta)),
                Math.round(MathUtil.lerp(getAlpha(from), getAlpha(to), delta))
        );
    }

    /** Бегущая «полоса» между двумя цветами: phase 0 → from, 0.5 → to, 1 → снова from
     *  (значения вне 0..1 заворачиваются). Переход сглажен — резких «углов» не видно. */
    public static int wave(int from, int to, float phase) {
        float p = phase - (float) Math.floor(phase);
        float triangle = p < 0.5f ? p * 2.0f : 2.0f - p * 2.0f;
        return lerp(from, to, smoothstep(triangle));
    }

    /** Сглаживает линейный ход 0..1 → 0..1: убирает резкие «углы» на стыках. */
    private static float smoothstep(float t) {
        float x = MathUtil.clamp(t, 0.0f, 1.0f);
        return x * x * (3.0f - 2.0f * x);
    }

    /** Цвет из HSV: hue 0..360, saturation/value 0..1, alpha 0-255. */
    public static int hsv(float hue, float saturation, float value, int alpha) {
        float h = (hue % 360.0f + 360.0f) % 360.0f;
        float s = MathUtil.clamp(saturation, 0.0f, 1.0f);
        float v = MathUtil.clamp(value, 0.0f, 1.0f);

        int sector = (int) (h / 60.0f) % 6;
        float f = h / 60.0f - (int) (h / 60.0f);
        float p = v * (1.0f - s);
        float q = v * (1.0f - f * s);
        float t = v * (1.0f - (1.0f - f) * s);

        float r;
        float g;
        float b;
        switch (sector) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return rgba(Math.round(r * 255.0f), Math.round(g * 255.0f), Math.round(b * 255.0f), alpha);
    }

    /** Радужный цвет: position 0..1 задаёт hue (удобно пускать по времени или координате). */
    public static int rainbow(float position, float saturation, float value) {
        return hsv((position % 1.0f) * 360.0f, saturation, value, 255);
    }

    /** Затемняет (factor < 1) или осветляет (factor > 1) цвет, прозрачность не трогает. */
    public static int multiply(int color, float factor) {
        return rgba(
                MathUtil.clamp(Math.round(getRed(color) * factor), 0, 255),
                MathUtil.clamp(Math.round(getGreen(color) * factor), 0, 255),
                MathUtil.clamp(Math.round(getBlue(color) * factor), 0, 255),
                getAlpha(color)
        );
    }
}
