package yarnternal.lol.common.ui.gui;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import yarnternal.lol.api.event.EventTarget;
import yarnternal.lol.api.event.events.KeyEvent;
import yarnternal.lol.core.Yarnternal;

/**
 * Открытие меню по клавише (правый Shift) — как Insert в NeverLose.
 * Работает только в мире и только когда не открыт чат/другой экран.
 */
public final class ClickGuiManager {

    private ClickGuiManager() {
    }

    public static void init() {
        Yarnternal.getInstance().getEventManager().register(new Listener());
    }

    /** Подписчик на клавиши: вынесен отдельно, чтобы не светить статикой в шине. */
    private static final class Listener {

        @EventTarget
        private void onKey(KeyEvent event) {
            if (!event.isPress() || event.getKey() != GLFW.GLFW_KEY_RIGHT_SHIFT) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.currentScreen != null) {
                return;
            }

            ClickGuiScreen.open();
        }
    }
}
