package yarnternal.lol.common.script;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Папка Lua-скриптов и её содержимое.
 *
 * <p>Пока это только менеджер файлов: меню показывает список .lua,
 * открывает папку и перечитывает её. Исполнение скриптов появится,
 * когда в проект добавится Lua-рантайм.
 */
public final class ScriptManager {

    public record ScriptEntry(String name, long size) {
    }

    private ScriptManager() {
    }

    public static Path folder() {
        return FabricLoader.getInstance().getConfigDir().resolve("yarnternal").resolve("scripts");
    }

    public static void ensureFolder() {
        try {
            Files.createDirectories(folder());
        } catch (IOException ignored) {
            // папка недоступна — список просто будет пустым
        }
    }

    public static List<ScriptEntry> list() {
        ensureFolder();
        List<ScriptEntry> entries = new ArrayList<>();

        try (var stream = Files.list(folder())) {
            stream.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".lua"))
                    .forEach(path -> {
                        String name = path.getFileName().toString();
                        try {
                            entries.add(new ScriptEntry(name, Files.size(path)));
                        } catch (IOException ignored) {
                            entries.add(new ScriptEntry(name, 0));
                        }
                    });
        } catch (IOException ignored) {
            return entries;
        }

        entries.sort(Comparator.comparing(ScriptEntry::name, String.CASE_INSENSITIVE_ORDER));
        return entries;
    }

    public static void openFolder() {
        ensureFolder();
        Util.getOperatingSystem().open(folder());
    }
}
