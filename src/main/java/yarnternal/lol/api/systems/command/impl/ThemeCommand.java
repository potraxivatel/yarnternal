package yarnternal.lol.common.command.impl;

import yarnternal.lol.api.systems.style.Style;
import yarnternal.lol.api.systems.style.StyleManager;
import yarnternal.lol.api.utility.client.PrintUtil;
import yarnternal.lol.common.command.Command;
import yarnternal.lol.core.Yarnternal;

public final class ThemeCommand extends Command {

    public ThemeCommand() {
        super("theme", "сменить или посмотреть тему клиента", "style");
    }

    @Override
    public String getUsage() {
        return ".theme <название | list>";
    }

    @Override
    public void execute(String[] args) {
        Yarnternal client = Yarnternal.getInstance();
        if (client == null || client.getStyleManager() == null) {
            PrintUtil.printer("Менеджер тем недоступен.");
            return;
        }

        StyleManager sm = client.getStyleManager();

        if (args.length == 0) {
            Style cur = sm.current();
            PrintUtil.printer("Текущая тема: " + (cur != null ? cur.name() : "нет"));
            PrintUtil.printer("Использование: " + getUsage());
            return;
        }

        if (args[0].equalsIgnoreCase("list")) {
            StringBuilder sb = new StringBuilder("Список тем: ");
            var list = sm.getStyles();
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i).name());
                if (i < list.size() - 1) {
                    sb.append(", ");
                }
            }
            PrintUtil.printer(sb.toString());
            return;
        }

        String targetName = String.join(" ", args);
        if (sm.apply(targetName)) {
            PrintUtil.printer("Тема переключена на: " + sm.current().name());
        } else {
            PrintUtil.printer("Тема не найдена: " + targetName + ". Напишите .theme list для просмотра всех тем.");
        }
    }
}
