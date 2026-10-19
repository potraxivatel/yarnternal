package yarnternal.lol.common.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Locale;

/** Числовая настройка со слайдером: min..max с шагом step. */
public final class NumberSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;
    private final String suffix;

    public NumberSetting(String name, double defaultValue, double min, double max, double step, String suffix) {
        super(name, snap(defaultValue, min, max, step));
        this.min = min;
        this.max = max;
        this.step = step <= 0 ? 0.01 : step;
        this.suffix = suffix == null ? "" : suffix;
    }

    @Override
    public void setValue(Double value) {
        if (value == null) {
            return;
        }
        super.setValue(snap(value, min, max, step));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    /** Положение ручки слайдера: 0..1. */
    public double fraction() {
        return max <= min ? 0 : (value - min) / (max - min);
    }

    public void setFraction(double fraction) {
        double clamped = Math.max(0, Math.min(1, fraction));
        setValue(min + (max - min) * clamped);
    }

    @Override
    public String displayValue() {
        String number = step >= 1
                ? String.valueOf(Math.round(value))
                : String.format(Locale.ROOT, "%.2f", value);
        return number + suffix;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            try {
                setValue(json.getAsDouble());
            } catch (RuntimeException ignored) {
                // битый конфиг — оставляем значение по умолчанию
            }
        }
    }

    private static double snap(double value, double min, double max, double step) {
        double clamped = Math.max(min, Math.min(max, value));
        if (step <= 0) {
            return clamped;
        }
        double snapped = Math.round(clamped / step) * step;
        return Math.max(min, Math.min(max, snapped));
    }
}
