package yarnternal.lol.common.impl.other;

import yarnternal.lol.api.utility.sound.SoundUtil;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.api.ModuleAnnotation;
import yarnternal.lol.common.api.ModuleList;
import yarnternal.lol.common.settings.ModeSetting;
import yarnternal.lol.common.settings.NumberSetting;

@ModuleAnnotation(name = "ClientSounds", desc = "Звуки переключения модулей клиента", type = ModuleList.Other)
public class ClientSounds extends Module {

    private final ModeSetting soundPreset = addSetting(new ModeSetting("Пресет", "Preset 1",
            "Preset 1", "Preset 2", "Preset 3", "Preset 4", "Preset 5", "Preset 6", "Preset 7"));

    private final NumberSetting volume = addSetting(new NumberSetting("Volume", 0.8, 0.05, 1.0, 0.05, ""));

    public ClientSounds() {
        setEnabled(true);

        soundPreset.onChange(val -> {
            SoundUtil.currentPreset = soundPreset.getIndex() + 1;
            SoundUtil.playToggle(true);
        });

        volume.onChange(val -> {
            SoundUtil.defaultVolume = val.floatValue();
        });
    }

    @Override
    public void onEnable() {
        SoundUtil.enabled = true;
        SoundUtil.currentPreset = soundPreset.getIndex() + 1;
        SoundUtil.defaultVolume = volume.getValue().floatValue();
    }

    @Override
    public void onDisable() {
        SoundUtil.enabled = false;
    }
}
