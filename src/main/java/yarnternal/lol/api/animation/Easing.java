package yarnternal.lol.api.animation;

/**
 * Функциональный интерфейс для кривых плавности (Easing functions).
 * Принимает прогресс {@code x} в диапазоне [0.0 .. 1.0]
 * и возвращает интерполированное значение.
 *
 * <p>Поддерживает как встроенные кривые, так и кастомные лямбды:
 * {@code Easing custom = x -> (float) Math.sin(x * Math.PI / 2.0);}</p>
 */
@FunctionalInterface
public interface Easing {

    float ease(float x);

    // ── Встроенные популярные кривые ──────────────────────────────────────────

    /** Линейная интерполяция без ускорения и замедления. */
    Easing LINEAR = x -> x;

    /** Плавное ускорение в начале (квадратичное). */
    Easing EASE_IN_QUAD = x -> x * x;

    /** Плавное замедление к концу (квадратичное). */
    Easing EASE_OUT_QUAD = x -> 1.0f - (1.0f - x) * (1.0f - x);

    /** Плавный разгон и плавное торможение. */
    Easing EASE_IN_OUT_QUAD = x -> x < 0.5f
            ? 2.0f * x * x
            : 1.0f - (float) Math.pow(-2.0f * x + 2.0f, 2) / 2.0f;

    /** Ускорение кубическое. */
    Easing EASE_IN_CUBIC = x -> x * x * x;

    /** Мягкое замедление кубическое (очень приятно для скролла и списков). */
    Easing EASE_OUT_CUBIC = x -> 1.0f - (float) Math.pow(1.0f - x, 3);

    Easing EASE_IN_OUT_CUBIC = x -> x < 0.5f
            ? 4.0f * x * x * x
            : 1.0f - (float) Math.pow(-2.0f * x + 2.0f, 3) / 2.0f;

    Easing EASE_OUT_QUART = x -> 1.0f - (float) Math.pow(1.0f - x, 4);

    Easing EASE_OUT_EXPO = x -> x >= 1.0f ? 1.0f : 1.0f - (float) Math.pow(2.0, -10.0 * x);

    // ── Пружинные кривые (с отскоком / overshoot) ────────────────────────────

    /**
     * Сочный отскок с вылетом за 100% и мягким возвратом на место.
     * Идеально подходит для появления окон, иконок таргета и модалок.
     */
    Easing EASE_OUT_BACK = x -> {
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        return 1.0f + c3 * (float) Math.pow(x - 1.0f, 3) + c1 * (float) Math.pow(x - 1.0f, 2);
    };

    /** Уход назад перед броском вперед. */
    Easing EASE_IN_BACK = x -> {
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        return c3 * x * x * x - c1 * x * x;
    };

    /** Эффект отскока мячика / желе при приземлении. */
    Easing EASE_OUT_BOUNCE = x -> {
        float n1 = 7.5625f;
        float d1 = 2.75f;

        if (x < 1.0f / d1) {
            return n1 * x * x;
        } else if (x < 2.0f / d1) {
            float xSub = x - 1.5f / d1;
            return n1 * xSub * xSub + 0.75f;
        } else if (x < 2.5f / d1) {
            float xSub = x - 2.25f / d1;
            return n1 * xSub * xSub + 0.9375f;
        } else {
            float xSub = x - 2.625f / d1;
            return n1 * xSub * xSub + 0.984375f;
        }
    };

    /** Пружинистая резинка (Elastic). */
    Easing EASE_OUT_ELASTIC = x -> {
        if (x <= 0.0f) return 0.0f;
        if (x >= 1.0f) return 1.0f;
        return (float) (Math.pow(2.0, -10.0 * x) * Math.sin((x * 10.0f - 0.75f) * ((2.0 * Math.PI) / 3.0)) + 1.0);
    };
}
