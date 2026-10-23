package yarnternal.lol.common.impl.movement;

import yarnternal.lol.common.api.ModuleAnnotation;
import yarnternal.lol.common.api.ModuleList;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.settings.BooleanSetting;
import yarnternal.lol.common.settings.EnumSetting;

@ModuleAnnotation(name = "Sprint", desc = "Автоматический бег", type = ModuleList.Movement)
public class Sprint extends Module {

    public enum Mode {
        LEGIT,
        RAGE
    }

    private final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("Mode", Mode.LEGIT));
    private final BooleanSetting keepSprint = addSetting(new BooleanSetting("Keep Sprint", true));

    @Override
    public void onTick() {
        if (mc.player != null) {
            mc.player.setSprinting(true);
            //я потом доделаю я просто спешу в школу
        }
    }

}
