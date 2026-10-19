package yarnternal.lol.common.command.impl;

import yarnternal.lol.api.utility.client.PrintUtil;
import yarnternal.lol.common.command.Command;
import yarnternal.lol.common.command.CommandManager;

/** ".help" — список всех клиентских команд. */
public final class HelpCommand extends Command {

    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        super("help", "список команд клиента", "h");
        this.manager = manager;
    }

    @Override
    public void execute(String[] args) {
        PrintUtil.printer("Команды клиента (" + manager.getCommands().size() + "):");
        for (Command command : manager.getCommands()) {
            PrintUtil.printer(" " + command.getUsage() + " — " + command.getDescription());
        }
    }
}
