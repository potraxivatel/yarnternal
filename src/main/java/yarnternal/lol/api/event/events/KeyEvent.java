package yarnternal.lol.api.event.events;

import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import yarnternal.lol.api.event.Event;

/**
 * Событие клавиатуры: приходит из KeyboardMixin на каждый колбэк GLFW —
 * нажатие, повтор и отпускание. Подписчики (бинды, будущий GUI) сами решают,
 * какое действие им нужно.
 */
@Getter
public class KeyEvent implements Event {

    /** Код клавиши GLFW (GLFW_KEY_*). */
    private final int key;

    /** Аппаратный скан-код клавиши. */
    private final int scancode;

    /** GLFW_PRESS / GLFW_REPEAT / GLFW_RELEASE. */
    private final int action;

    /** Зажатые модификаторы (Shift/Ctrl/Alt) — GLFW_MOD_*. */
    private final int modifiers;

    public KeyEvent(int key, int scancode, int action, int modifiers) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.modifiers = modifiers;
    }

    public boolean isPress() {
        return action == GLFW.GLFW_PRESS;
    }

    public boolean isRepeat() {
        return action == GLFW.GLFW_REPEAT;
    }

    public boolean isRelease() {
        return action == GLFW.GLFW_RELEASE;
    }
}
