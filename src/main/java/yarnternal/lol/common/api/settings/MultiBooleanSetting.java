package yarnternal.lol.common.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Мульти-выбор: независимые переключатели под одной группой (например, Targets: Players, Monsters, Animals).
 */
public final class MultiBooleanSetting extends Setting<Map<String, Boolean>> {

    public MultiBooleanSetting(String name, List<String> options, List<String> defaultSelected) {
        super(name, initMap(options, defaultSelected));
    }

    public MultiBooleanSetting(String name, String... options) {
        this(name, Arrays.asList(options), Collections.emptyList());
    }

    private static Map<String, Boolean> initMap(List<String> options, List<String> selected) {
        Map<String, Boolean> map = new LinkedHashMap<>();
        for (String opt : options) {
            map.put(opt, selected != null && selected.contains(opt));
        }
        return map;
    }

    public boolean isSelected(String option) {
        return Boolean.TRUE.equals(value.get(option));
    }

    public void setSelected(String option, boolean selected) {
        if (!value.containsKey(option)) {
            return;
        }
        Map<String, Boolean> updated = new LinkedHashMap<>(value);
        updated.put(option, selected);
        setValue(updated);
    }

    public void toggle(String option) {
        setSelected(option, !isSelected(option));
    }

    public List<String> getSelected() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : value.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    public Set<String> getOptions() {
        return Collections.unmodifiableSet(value.keySet());
    }

    @Override
    public String displayValue() {
        List<String> selected = getSelected();
        if (selected.isEmpty()) {
            return "None";
        }
        return String.join(", ", selected);
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        for (String sel : getSelected()) {
            array.add(sel);
        }
        return array;
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json == null) {
            return;
        }
        Map<String, Boolean> updated = new LinkedHashMap<>();
        for (String key : value.keySet()) {
            updated.put(key, false);
        }

        if (json.isJsonArray()) {
            for (JsonElement elem : json.getAsJsonArray()) {
                String key = elem.getAsString();
                if (updated.containsKey(key)) {
                    updated.put(key, true);
                }
            }
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            for (String key : obj.keySet()) {
                if (updated.containsKey(key)) {
                    updated.put(key, obj.get(key).getAsBoolean());
                }
            }
        }
        setValue(updated);
    }
}
