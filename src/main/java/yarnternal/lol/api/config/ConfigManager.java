package yarnternal.lol.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import yarnternal.lol.api.systems.style.Style;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.api.ModuleProvider;
import yarnternal.lol.common.settings.Setting;
import yarnternal.lol.core.Yarnternal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Менеджер конфигов Neverlose-типа: сохраняет состояние модулей и их настроек
 * в {@code .minecraft/config/yarnternal/configs/*.cfg}, умеет загружать,
 * дублировать, удалять и шарить конфиг через буфер обмена.
 *
 * <p>Формат — обычный JSON, так что конфиг можно спокойно скинуть другому
 * человеку или положить в гит.
 */
public final class ConfigManager {

    public record ConfigEntry(String name, long size, long modified) {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Длина имени файла конфига: хватает с запасом и не упирается в лимит ФС. */
    private static final int MAX_NAME_LENGTH = 32;

    private final Path dir;
    private final Path clientFile;
    private double menuScale = 1.0;

    public ConfigManager() {
        Path root = FabricLoader.getInstance().getConfigDir().resolve("yarnternal");
        this.dir = root.resolve("configs");
        this.clientFile = root.resolve("client.json");
        ensureDir();
        loadClient();
    }

    // ── конфиги ─────────────────────────────────────────────────────────────

    public List<ConfigEntry> list() {
        ensureDir();
        List<ConfigEntry> entries = new ArrayList<>();

        try (var stream = Files.list(dir)) {
            stream.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".cfg"))
                    .forEach(path -> {
                        String file = path.getFileName().toString();
                        String name = file.substring(0, file.length() - 4);
                        try {
                            entries.add(new ConfigEntry(name, Files.size(path),
                                    Files.getLastModifiedTime(path).toMillis()));
                        } catch (IOException ignored) {
                            entries.add(new ConfigEntry(name, 0, 0));
                        }
                    });
        } catch (IOException ignored) {
            return entries;
        }

        entries.sort(Comparator.comparing(ConfigEntry::name, String.CASE_INSENSITIVE_ORDER));
        return entries;
    }

    /** Сохраняет текущее состояние клиента. null — если файл не записался. */
    public String save(String rawName) {
        ModuleProvider provider = provider();
        if (provider == null) {
            return null;
        }

        String name = sanitize(rawName);
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);

        JsonObject modules = new JsonObject();
        for (Module module : provider.getModules()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("enabled", module.isToggled());

            JsonObject settings = new JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                settings.add(setting.getName(), setting.toJson());
            }
            entry.add("settings", settings);
            modules.add(module.getName(), entry);
        }

        root.add("modules", modules);
        return write(dir.resolve(name + ".cfg"), GSON.toJson(root)) ? name : null;
    }

    /** Загружает конфиг по имени. Возвращает false, если файла нет/он битый. */
    public boolean load(String rawName) {
        Path file = dir.resolve(sanitize(rawName) + ".cfg");
        if (!Files.isRegularFile(file)) {
            return false;
        }

        try {
            JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            apply(root);
            return true;
        } catch (RuntimeException | IOException ignored) {
            return false;
        }
    }

    public boolean delete(String rawName) {
        try {
            return Files.deleteIfExists(dir.resolve(sanitize(rawName) + ".cfg"));
        } catch (IOException ignored) {
            return false;
        }
    }

    public String duplicate(String rawSource, String rawTarget) {
        Path source = dir.resolve(sanitize(rawSource) + ".cfg");
        if (!Files.isRegularFile(source)) {
            return null;
        }

        String sourceName = sanitize(rawSource);
        String base = sanitize(rawTarget);
        if (base.equals(sourceName)) {
            base = sourceName + "_copy";
        }

        // не перезаписываем чужой конфиг: при совпадении имени добавляем _2, _3...
        String target = base;
        int suffix = 2;
        while (Files.exists(dir.resolve(target + ".cfg"))) {
            target = base + "_" + suffix++;
        }

        try {
            Files.copy(source, dir.resolve(target + ".cfg"));
            return target;
        } catch (IOException ignored) {
            return null;
        }
    }

    /** Кладёт JSON конфига в буфер обмена. */
    public boolean share(String rawName) {
        Path file = dir.resolve(sanitize(rawName) + ".cfg");
        if (!Files.isRegularFile(file)) {
            return false;
        }

        try {
            MinecraftClient.getInstance().keyboard.setClipboard(Files.readString(file, StandardCharsets.UTF_8));
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /** Импортирует конфиг из буфера обмена под свободным именем imported_N. */
    public String importClipboard() {
        String clipboard;
        try {
            clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
        } catch (RuntimeException ignored) {
            return null;
        }

        if (clipboard == null || clipboard.isBlank()) {
            return null;
        }

        try {
            JsonElement parsed = JsonParser.parseString(clipboard);
            if (!parsed.isJsonObject()) {
                return null;
            }

            JsonObject root = parsed.getAsJsonObject();
            if (!root.has("modules")) {
                // в буфере не конфиг — не пишем мусор в папку
                return null;
            }

            String base = "imported";
            String name = base;
            for (int i = 1; Files.exists(dir.resolve(name + ".cfg")); i++) {
                name = base + "_" + i;
            }
            return write(dir.resolve(name + ".cfg"), GSON.toJson(root)) ? name : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public void openFolder() {
        ensureDir();
        Util.getOperatingSystem().open(dir);
    }

    // ── масштаб меню ────────────────────────────────────────────────────────

    public double getMenuScale() {
        return menuScale;
    }

    public void setMenuScale(double scale) {
        this.menuScale = Math.max(0.5, Math.min(1.5, scale));
    }

    /** Пишет client.json (масштаб меню и активная тема). false — если файл не записался. */
    public boolean saveClient() {
        JsonObject root = new JsonObject();
        root.addProperty("menuScale", menuScale);
        Yarnternal client = Yarnternal.getInstance();
        if (client != null && client.getStyle() != null) {
            root.addProperty("theme", client.getStyle().name());
        }
        return write(clientFile, GSON.toJson(root));
    }

    private void loadClient() {
        if (!Files.isRegularFile(clientFile)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(clientFile, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("menuScale")) {
                setMenuScale(root.get("menuScale").getAsDouble());
            }
            if (root.has("theme")) {
                String themeName = root.get("theme").getAsString();
                Yarnternal client = Yarnternal.getInstance();
                if (client != null && client.getStyleManager() != null) {
                    Style style = client.getStyleManager().getStyleByName(themeName);
                    if (style != null) {
                        client.setStyle(style);
                    }
                }
            }
        } catch (RuntimeException | IOException ignored) {
            // битый client.json — просто дефолтный масштаб
        }
    }

    // ── внутреннее ───────────────────────────────────────────────────────────

    private void apply(JsonObject root) {
        ModuleProvider provider = provider();
        if (provider == null) {
            return;
        }

        JsonObject modules = root.getAsJsonObject("modules");
        if (modules == null) {
            return;
        }

        for (String moduleName : modules.keySet()) {
            Module module = provider.getModuleByName(moduleName);
            if (module == null) {
                continue;
            }

            JsonObject entry = modules.getAsJsonObject(moduleName);
            if (entry.has("enabled")) {
                module.setEnabled(entry.get("enabled").getAsBoolean());
            }

            JsonObject settings = entry.getAsJsonObject("settings");
            if (settings == null) {
                continue;
            }

            for (String key : settings.keySet()) {
                for (Setting<?> setting : module.getSettings()) {
                    if (setting.getName().equals(key)) {
                        setting.fromJson(settings.get(key));
                        break;
                    }
                }
            }
        }
    }

    private ModuleProvider provider() {
        Yarnternal client = Yarnternal.getInstance();
        return client == null ? null : client.getModuleProvider();
    }

    private void ensureDir() {
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {
            // если не смогли создать — save() просто не запишет файл
        }
    }

    private static boolean write(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException ignored) {
            // вызывающий сам решает, что показать: методы возвращают null/false
            return false;
        }
    }

    /**
     * Имя конфига -> безопасное имя файла. Буквы (включая кириллицу) и цифры
     * сохраняются, пробелы становятся подчёркиваниями, остальное — "_".
     */
    private static String sanitize(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        name = name.replaceAll("[^\\p{L}\\p{N}_\\- ]", "_")
                .replace(' ', '_')
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        if (name.isEmpty()) {
            return "default";
        }
        return name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
    }

    /** Размер файла для списка: "1.2 KB". */
    public static String sizeText(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
    }
}
