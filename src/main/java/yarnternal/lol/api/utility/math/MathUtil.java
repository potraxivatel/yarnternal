package yarnternal.lol.api.utility.math;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Math утилита — общие математические функции для клиента.
 * Все методы статические, создавать инстанс не нужно.
 */
public final class MathUtil {

    private MathUtil() {
        // утилитный класс — конструктор запрещён
    }

    /** Ограничивает значение рамками от min до max. */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Ограничивает значение рамками от min до max. */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Ограничивает значение рамками от min до max. */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Линейная интерполяция: плавно двигает значение от from к to. delta от 0 до 1. */
    public static float lerp(float from, float to, float delta) {
        return from + (to - from) * delta;
    }

    /** Линейная интерполяция: плавно двигает значение от from к to. delta от 0 до 1. */
    public static double lerp(double from, double to, double delta) {
        return from + (to - from) * delta;
    }

    /** Округляет до указанного количества знаков после запятой. */
    public static double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    /** Приводит угол к диапазону -180...180 (как yaw/pitch в майне). */
    public static float wrapDegrees(float angle) {
        angle %= 360.0f;
        if (angle >= 180.0f) {
            angle -= 360.0f;
        }
        if (angle < -180.0f) {
            angle += 360.0f;
        }
        return angle;
    }

    /** Приводит угол к диапазону -180...180 (как yaw/pitch в майне). */
    public static double wrapDegrees(double angle) {
        angle %= 360.0;
        if (angle >= 180.0) {
            angle -= 360.0;
        }
        if (angle < -180.0) {
            angle += 360.0;
        }
        return angle;
    }

    /** Кратчайшая разница между двумя углами (удобно для плавных поворотов). */
    public static float angleDifference(float a, float b) {
        return wrapDegrees(b - a);
    }

    /** Случайное число от min (включительно) до max (не включительно). */
    public static double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /** Случайное целое от min до max включительно. */
    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /** Пересчитывает значение из одного диапазона в другой (например 0..1 в 0..100). */
    public static double map(double value, double inMin, double inMax, double outMin, double outMax) {
        return outMin + (value - inMin) * (outMax - outMin) / (inMax - inMin);
    }
}
