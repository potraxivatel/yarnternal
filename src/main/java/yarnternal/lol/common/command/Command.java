package yarnternal.lol.common.command;

import java.util.List;

/**
 * Клиентская команда: выполняется локально, в чат не уходит.
 *
 * Новая команда = наследник + одна строчка register(...) в CommandManager.
 * args — всё после имени команды, уже разбитое по пробелам.
 */
public abstract class Command {

    private final String name;
    private final String description;
    private final List<String> aliases;

    protected Command(String name, String description, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = List.of(aliases);
    }

    public abstract void execute(String[] args);

    /** Как показывать команду в .help, например ".bind <модуль> <клавиша>". */
    public String getUsage() {
        return "." + name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
