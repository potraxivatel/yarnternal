package yarnternal.lol.common.keybind;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import yarnternal.lol.api.event.EventTarget;
import yarnternal.lol.api.event.events.KeyEvent;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.core.Yarnternal;

/**
 * Привязки клавиш: какая клавиша переключает какой модуль.
 *
 * Клавиши приходят из KeyboardMixin -> KeyEvent, дальше просто ищем бинд.
 * Работаем только когда не открыт чат/меню — иначе бинды съедали бы ввод.
 */
public final class KeybindManager {

    /** Удобные сокращения для имён клавиш, которых нет в translation key. */
    private static final Map<String, String> KEY_ALIASES = Map.ofEntries(
            Map.entry("shift", "left.shift"),
            Map.entry("lshift", "left.shift"),
            Map.entry("rshift", "right.shift"),
            Map.entry("ctrl", "left.control"),
            Map.entry("lctrl", "left.control"),
            Map.entry("rctrl", "right.control"),
            Map.entry("alt", "left.alt"),
            Map.entry("lalt", "left.alt"),
            Map.entry("ralt", "right.alt")
    );

    /** Код клавиши -> модуль, который она переключает. */
    private final Map<Integer, Module> bindings = new HashMap<>();

    public void init() {
        Yarnternal.getInstance().getEventManager().register(this);
    }

    @EventTarget
    private void onKey(KeyEvent event) {
        if (!event.isPress()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen != null) {
            return;
        }

        Module module = bindings.get(event.getKey());
        if (module != null) {
            module.toggle();
        }
    }

    /** Назначает клавишу модулю. Старый бинд этого модуля снимается. */
    public void bind(Module module, int key) {
        unbind(module);
        bindings.put(key, module);
    }

    /** Снимает все бинды модуля. */
    public void unbind(Module module) {
        bindings.values().removeIf(bound -> bound == module);
    }

    /** Клавиша модуля или null, если модуль не привязан. */
    public Integer getKey(Module module) {
        for (Map.Entry<Integer, Module> entry : bindings.entrySet()) {
            if (entry.getValue() == module) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** Модуль на этой клавише (для проверки конфликтов при .bind) или null. */
    public Module getModule(int key) {
        return bindings.get(key);
    }

    /** Код клавиши -> человекочитаемое имя: 82 -> "R", 32 -> "SPACE". */
    public static String keyName(int key) {
        String name = InputUtil.Type.KEYSYM.createFromCode(key)
                .getTranslationKey()
                .replace("key.keyboard.", "");
        return name.replace('.', ' ').toUpperCase(Locale.ROOT);
    }

    /** "r"/"f1"/"space" -> код GLFW или -1, если такой клавиши нет. */
    public static int keyCode(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        normalized = KEY_ALIASES.getOrDefault(normalized, normalized);

        try {
            int code = InputUtil.fromTranslationKey("key.keyboard." + normalized).getCode();
            return code < 0 ? -1 : code;
        } catch (Exception ignored) {
            return -1;
        }
    }
}
