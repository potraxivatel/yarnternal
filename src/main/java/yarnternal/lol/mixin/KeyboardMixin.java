package yarnternal.lol.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yarnternal.lol.api.event.events.KeyEvent;
import yarnternal.lol.core.Yarnternal;

/**
 * Прокидывает нажатия клавиш в нашу шину событий.
 *
 * В 1.21.11 сигнатура колбэка: onKey(long window, int action, KeyInput input),
 * где KeyInput хранит код клавиши, скан-код и модификаторы.
 */
@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void yarnternal$onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getWindow() == null || window != client.getWindow().getHandle()) {
            return;
        }

        Yarnternal yarnternal = Yarnternal.getInstance();
        if (yarnternal == null || yarnternal.getEventManager() == null) {
            return;
        }

        yarnternal.getEventManager().call(new KeyEvent(
                input.key(),
                input.scancode(),
                action,
                input.modifiers()
        ));
    }
}
