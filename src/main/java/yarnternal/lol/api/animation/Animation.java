package yarnternal.lol.api.animation;

import lombok.Getter;
import lombok.Setter;

/**
 * Универсальный контроллер анимации на основе времени (миллисекунд).
 * Не зависит от частоты кадров (FPS-independent) и плавно работает на любых герцовках.
 *
 * <p>При смене цели на лету анимация не дёргается, а мягко разворачивается
 * из текущего промежуточного значения.</p>
 */
public class Animation {

    private double value;
    @Getter
    private double targetValue;
    private double startValue;

    @Getter @Setter
    private long duration;
    @Getter @Setter
    private Easing easing;

    private long startTime;

    public Animation(double initialValue, long duration, Easing easing) {
        this.value = initialValue;
        this.startValue = initialValue;
        this.targetValue = initialValue;
        this.duration = duration;
        this.easing = easing;
        this.startTime = System.currentTimeMillis();
    }

    public Animation(long duration, Easing easing) {
        this(0.0, duration, easing);
    }

    public Animation() {
        this(0.0, 200, Easing.EASE_OUT_QUAD);
    }

    /**
     * Запускает анимацию к целевому значению {@code target}.
     * Если цель не поменялась — ничего не делает.
     * При смене цели на лету сохраняет текущее значение, чтобы не было рывков.
     */
    public void run(double target) {
        if (Math.abs(this.targetValue - target) < 1e-6) {
            return;
        }
        this.startValue = getValue();
        this.targetValue = target;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Запуск с переопределением длительности и кривой (например, долгое появление и быстрый уход).
     */
    public void run(double target, long duration, Easing easing) {
        this.duration = duration;
        this.easing = easing;
        run(target);
    }

    /**
     * Текущее значение анимации с учётом Easing.
     */
    public double getValue() {
        if (isFinished()) {
            return value = targetValue;
        }
        float progress = getProgress();
        float eased = easing.ease(progress);
        return value = startValue + (targetValue - startValue) * eased;
    }

    /**
     * Удобный float-геттер для работы с рендером и матрицами.
     */
    public float getValueFloat() {
        return (float) getValue();
    }

    /**
     * Линейный прогресс от 0.0f до 1.0f.
     */
    public float getProgress() {
        if (duration <= 0) return 1.0f;
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.min(1.0f, Math.max(0.0f, elapsed / (float) duration));
    }

    /**
     * Завершилась ли текущая фаза анимации.
     */
    public boolean isFinished() {
        return (System.currentTimeMillis() - startTime) >= duration;
    }

    /**
     * Мгновенный сброс в конкретное значение без плавного перехода.
     */
    public void reset(double val) {
        this.value = val;
        this.startValue = val;
        this.targetValue = val;
        this.startTime = 0;
    }

    /**
     * Проверяет, стремится ли анимация к значению (с погрешностью).
     */
    public boolean isTarget(double target) {
        return Math.abs(this.targetValue - target) < 1e-6;
    }
}
