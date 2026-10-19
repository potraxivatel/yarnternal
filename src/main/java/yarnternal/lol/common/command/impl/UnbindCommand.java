package yarnternal.lol.common.command.impl;

import yarnternal.lol.api.utility.client.PrintUtil;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.command.Command;
import yarnternal.lol.common.keybind.KeybindManager;
import yarnternal.lol.core.Yarnternal;

/** ".unbind <модуль>" — снять бинд с модуля. */
public final class UnbindCommand extends Command {

    public UnbindCommand() {
        super("unbind", "снять бинд с модуля", "ub");
    }

    @Override
    public String getUsage() {
        return ".unbind <модуль>";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            PrintUtil.printer("Использование: " + getUsage());
            return;
        }

        Module module = Yarnternal.getInstance().getModuleProvider().getModuleByName(args[0]);
        if (module == null) {
            PrintUtil.printer("Модуль не найден: " + args[0]);
            return;
        }

        KeybindManager keybinds = Yarnternal.getInstance().getKeybindManager();
        Integer key = keybinds.getKey(module);
        if (key == null) {
            PrintUtil.printer(module.getName() + " не привязан ни к какой клавише");
            return;
        }

        keybinds.unbind(module);
        PrintUtil.printer(module.getName() + " отвязан от " + KeybindManager.keyName(key));
    }
}
