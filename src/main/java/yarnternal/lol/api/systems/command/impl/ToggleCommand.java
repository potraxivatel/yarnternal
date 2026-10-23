package yarnternal.lol.common.command.impl;

import yarnternal.lol.api.utility.client.PrintUtil;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.command.Command;
import yarnternal.lol.core.Yarnternal;

/** ".toggle <модуль>" — включить или выключить модуль. */
public final class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle", "включить/выключить модуль", "t");
    }

    @Override
    public String getUsage() {
        return ".toggle <модуль>";
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

        module.toggle();
        PrintUtil.printer(module.getName() + " -> " + (module.isToggled() ? "включён" : "выключен"));
    }
}
