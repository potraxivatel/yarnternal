package yarnternal.lol.common.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Переключатель: On / Off. */
public final class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public void toggle() {
        setValue(!Boolean.TRUE.equals(value));
    }

    @Override
    public String displayValue() {
        return Boolean.TRUE.equals(value) ? "On" : "Off";
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(Boolean.TRUE.equals(value));
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            try {
                setValue(json.getAsBoolean());
            } catch (RuntimeException ignored) {
                // битый конфиг — оставляем значение по умолчанию
            }
        }
    }
}
