package yarnternal.lol.common.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Выпадающий список: хранит одну константу enum. */
public final class EnumSetting<T extends Enum<T>> extends Setting<T> {

    private final T[] constants;

    public EnumSetting(String name, T defaultValue) {
        super(name, defaultValue);
        this.constants = defaultValue.getDeclaringClass().getEnumConstants();
    }

    /** Следующее значение по кругу — для клика по комбобоксу. */
    public void cycle() {
        if (constants.length == 0) {
            return;
        }
        int next = (value.ordinal() + 1) % constants.length;
        setValue(constants[next]);
    }

    public T[] getConstants() {
        return constants;
    }

    @Override
    public String displayValue() {
        return pretty(value.name());
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value.name());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) {
            return;
        }
        try {
            setValue(Enum.valueOf(value.getDeclaringClass(), json.getAsString()));
        } catch (IllegalArgumentException ignored) {
            // значение из старого конфига — оставляем текущее
        }
    }

    /** SCREAMING_SNAKE_CASE -> "Screaming Snake Case". */
    public static String pretty(String raw) {
        String[] parts = raw.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }
}
