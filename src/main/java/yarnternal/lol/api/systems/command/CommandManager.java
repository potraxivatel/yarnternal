package yarnternal.lol.common.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import yarnternal.lol.api.utility.client.PrintUtil;
import yarnternal.lol.common.command.impl.BindCommand;
import yarnternal.lol.common.command.impl.HelpCommand;
import yarnternal.lol.common.command.impl.MenuCommand;
import yarnternal.lol.common.command.impl.ModulesCommand;
import yarnternal.lol.common.command.impl.ThemeCommand;
import yarnternal.lol.common.command.impl.ToggleCommand;
import yarnternal.lol.common.command.impl.UnbindCommand;

/**
 * Реестр клиентских команд и перехват чата.
 *
 * Сообщение, начинающееся с точки, не отправляется на сервер: оно парсится
 * как команда. Всё остальное уходит в чат как обычно.
 */
public final class CommandManager {

    /** Префикс клиентских команд: ".help", ".bind". */
    public static final String PREFIX = ".";

    private final List<Command> commands = new ArrayList<>();
    private final Map<String, Command> byName = new HashMap<>();

    public void init() {
        register(new HelpCommand(this));
        register(new ModulesCommand());
        register(new ToggleCommand());
        register(new BindCommand());
        register(new UnbindCommand());
        register(new MenuCommand());
        register(new ThemeCommand());

        // ALLOW_CHAT спрашивает "можно ли отправить?"; false = отменить отправку.
        // Возвращаем false именно тогда, когда сообщение оказалось нашей командой.
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> !handle(message));
    }

    public void register(Command command) {
        commands.add(command);
        byName.put(command.getName().toLowerCase(Locale.ROOT), command);
        for (String alias : command.getAliases()) {
            byName.put(alias.toLowerCase(Locale.ROOT), command);
        }
    }

    /** Все команды в порядке регистрации (без алиасов). */
    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    /** @return true, если это была наша команда (в чат её отправлять не надо). */
    public boolean handle(String message) {
        String trimmed = message.trim();
        if (!trimmed.startsWith(PREFIX)) {
            return false;
        }

        String body = trimmed.substring(PREFIX.length()).trim();
        if (body.isEmpty()) {
            return true;
        }

        String[] parts = body.split("\\s+");
        Command command = byName.get(parts[0].toLowerCase(Locale.ROOT));
        if (command == null) {
            PrintUtil.printer("Неизвестная команда: " + PREFIX + parts[0] + " (список: " + PREFIX + "help)");
            return true;
        }

        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
        try {
            command.execute(args);
        } catch (Throwable throwable) {
            PrintUtil.printer("Ошибка в " + PREFIX + command.getName() + ": " + throwable.getMessage());
        }

        return true;
    }
}
