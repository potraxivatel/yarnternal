package yarnternal.lol.common.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Настройка режима в стиле Expensive Client (ModeSetting).
 * <p>
 * Позволяет задавать режимы простыми строками без необходимости писать отдельный {@code enum},
 * а в коде использовать удобные {@code switch (mode.get())} и {@code mode.is("Matrix")}.
 */
public final class ModeSetting extends Setting<String> {

    private final List<String> modes;

    public ModeSetting(String name, String defaultVal, String... modes) {
        super(name, defaultVal);
        this.modes = Arrays.asList(modes);
        if (!this.modes.contains(defaultVal) && this.modes.size() > 0) {
            setValue(this.modes.get(0));
        }
    }

    public ModeSetting(String name, String... modes) {
        this(name, modes.length > 0 ? modes[0] : "", modes);
    }

    /** Проверка текущего режима без учета регистра (как в Expensive: mode.is("Matrix")). */
    public boolean is(String modeName) {
        return get().equalsIgnoreCase(modeName);
    }

    /** Алиас для get() — возвращает имя текущего режима. */
    public String get() {
        return getValue();
    }

    public List<String> getModes() {
        return Collections.unmodifiableList(modes);
    }

    public int getIndex() {
        for (int i = 0; i < modes.size(); i++) {
            if (modes.get(i).equalsIgnoreCase(get())) {
                return i;
            }
        }
        return 0;
    }

    /** Переключение на следующий режим по кругу (для клика в GUI). */
    public void cycle() {
        if (modes.isEmpty()) return;
        int next = (getIndex() + 1) % modes.size();
        setValue(modes.get(next));
    }

    @Override
    public String displayValue() {
        return getValue();
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            String str = json.getAsString();
            for (String m : modes) {
                if (m.equalsIgnoreCase(str)) {
                    setValue(m);
                    return;
                }
            }
        }
    }
}
