package yarnternal.lol.common.command.impl;

import yarnternal.lol.api.utility.client.PrintUtil;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.command.Command;
import yarnternal.lol.common.keybind.KeybindManager;
import yarnternal.lol.core.Yarnternal;

/** ".bind <модуль> <клавиша>" — привязать клавишу к модулю. */
public final class BindCommand extends Command {

    public BindCommand() {
        super("bind", "привязать клавишу к модулю", "b");
    }

    @Override
    public String getUsage() {
        return ".bind <модуль> <клавиша>";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            PrintUtil.printer("Использование: " + getUsage());
            return;
        }

        Module module = Yarnternal.getInstance().getModuleProvider().getModuleByName(args[0]);
        if (module == null) {
            PrintUtil.printer("Модуль не найден: " + args[0]);
            return;
        }

        int key = KeybindManager.keyCode(args[1]);
        if (key == -1) {
            PrintUtil.printer("Неизвестная клавиша: " + args[1]);
            return;
        }

        KeybindManager keybinds = Yarnternal.getInstance().getKeybindManager();
        Module previous = keybinds.getModule(key);
        keybinds.bind(module, key);

        PrintUtil.printer(module.getName() + " -> " + KeybindManager.keyName(key));
        if (previous != null && previous != module) {
            PrintUtil.printer("Клавиша была занята модулем " + previous.getName() + " — бинд перенесён");
        }
    }
}
