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

        // Dev-хук для скриншотов: ./gradlew runClient -PdebugGui
        if (Boolean.getBoolean("yarnternal.debugGui")) {
            Thread thread = new Thread(() -> {
                MinecraftClient client = null;
                for (int i = 0; i < 600; i++) {
                    client = MinecraftClient.getInstance();
                    if (client != null && client.player != null) {
                        break;
                    }
                    try {
                        Thread.sleep(250L);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                if (client == null || client.player == null) {
                    return;
                }
                try {
                    Thread.sleep(3_000L);
                } catch (InterruptedException e) {
                    return;
                }
                MinecraftClient target = client;
                target.execute(() -> {
                    if (!(target.currentScreen instanceof ClickGuiScreen)) {
                        System.out.println("[yarnternal] debug GUI: opening");
                        target.setScreen(new ClickGuiScreen());
                    }
                });
            }, "yarnternal-debug-gui");
            thread.setDaemon(true);
            thread.start();
        }
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
