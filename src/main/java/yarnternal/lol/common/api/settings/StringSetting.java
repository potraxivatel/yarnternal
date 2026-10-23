package yarnternal.lol.common.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Текстовая настройка: кастомные надписи (Watermark), префиксы, ники, спаммер и т.д.
 */
public final class StringSetting extends Setting<String> {

    private final int maxLength;

    public StringSetting(String name, String defaultValue) {
        this(name, defaultValue, 256);
    }

    public StringSetting(String name, String defaultValue, int maxLength) {
        super(name, defaultValue == null ? "" : defaultValue);
        this.maxLength = maxLength;
    }

    @Override
    public void setValue(String value) {
        if (value == null) {
            value = "";
        }
        if (value.length() > maxLength) {
            value = value.substring(0, maxLength);
        }
        super.setValue(value);
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public String displayValue() {
        return value;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            setValue(json.getAsString());
        }
    }
}
