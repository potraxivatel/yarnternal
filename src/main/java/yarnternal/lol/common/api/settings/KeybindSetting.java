package yarnternal.lol.common.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.lwjgl.glfw.GLFW;
import yarnternal.lol.common.keybind.KeybindManager;

/**
 * Настройка привязки клавиши: код клавиши GLFW + режим нажатия (Hold / Toggle).
 */
public final class KeybindSetting extends Setting<Integer> {

    public enum ActionType {
        TOGGLE,
        HOLD
    }

    private ActionType actionType = ActionType.TOGGLE;

    public KeybindSetting(String name, int defaultKey) {
        super(name, defaultKey);
    }

    public KeybindSetting(String name, int defaultKey, ActionType type) {
        super(name, defaultKey);
        this.actionType = type;
    }

    public int getKey() {
        return value != null ? value : GLFW.GLFW_KEY_UNKNOWN;
    }

    public void setKey(int key) {
        setValue(key);
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType type) {
        this.actionType = type;
    }

    public boolean isHold() {
        return actionType == ActionType.HOLD;
    }

    /** Проверяет, зажата ли клавиша прямо сейчас в окне GLFW. */
    public boolean isPressed(long windowHandle) {
        int key = getKey();
        if (key <= GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        return GLFW.glfwGetKey(windowHandle, key) == GLFW.GLFW_PRESS;
    }

    public String getKeyName() {
        int key = getKey();
        if (key <= GLFW.GLFW_KEY_UNKNOWN || key == 0) {
            return "None";
        }
        try {
            return KeybindManager.keyName(key);
        } catch (Exception e) {
            return "KEY " + key;
        }
    }

    @Override
    public String displayValue() {
        String name = getKeyName();
        return isHold() ? name + " [Hold]" : name;
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("key", getKey());
        obj.addProperty("type", actionType.name());
        return obj;
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json == null) {
            return;
        }
        if (json.isJsonPrimitive()) {
            setValue(json.getAsInt());
            return;
        }
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("key")) {
                setValue(obj.get("key").getAsInt());
            }
            if (obj.has("type")) {
                try {
                    this.actionType = ActionType.valueOf(obj.get("type").getAsString());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }
}
