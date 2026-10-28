package yarnternal.lol.common.impl.movement;

import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.api.ModuleAnnotation;
import yarnternal.lol.common.api.ModuleList;

@ModuleAnnotation(name = "Sprint", desc = "Автоматический бег", type = ModuleList.Movement)
public class Sprint extends Module {

    @Override
    public void onTick() {
        if (mc.player == null || mc.player.input == null) return;

        if (mc.player.input.hasForwardMovement() && !mc.player.horizontalCollision) {
            mc.player.setSprinting(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }
}
