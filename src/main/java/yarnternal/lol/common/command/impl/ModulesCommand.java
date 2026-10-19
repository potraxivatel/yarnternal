package yarnternal.lol.common.command.impl;

import yarnternal.lol.api.utility.client.PrintUtil;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.api.ModuleProvider;
import yarnternal.lol.common.command.Command;
import yarnternal.lol.common.keybind.KeybindManager;
import yarnternal.lol.core.Yarnternal;

/** ".modules" — все модули, их состояние и бинды. */
public final class ModulesCommand extends Command {

    public ModulesCommand() {
        super("modules", "все модули, их состояние и бинды", "mods");
    }

    @Override
    public void execute(String[] args) {
        ModuleProvider provider = Yarnternal.getInstance().getModuleProvider();
        KeybindManager keybinds = Yarnternal.getInstance().getKeybindManager();

        PrintUtil.printer("Модули (" + provider.getModules().size() + "):");
        for (Module module : provider.getModules()) {
            Integer key = keybinds.getKey(module);
            String bind = key == null ? "" : " [" + KeybindManager.keyName(key) + "]";
            String state = module.isToggled() ? "[on]  " : "[off] ";
            PrintUtil.printer(" " + state + module.getName() + bind);
        }
    }
}
