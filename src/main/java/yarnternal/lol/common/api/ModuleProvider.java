package yarnternal.lol.common.api;

import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Реестр модулей клиента: хранит все модули, отдаёт их по имени/классу
 * и прокидывает хуки тем, что включены.
 */
public final class ModuleProvider {

    private final List<Module> modules = new ArrayList<>();

    /**
     * Регистрация всех модулей клиента. Появился новый модуль — добавь одну
     * строчку register(...), и он сразу попадёт в меню/команды и получит хуки.
     */
    public void init() {
        // register(new KillAura());
        // register(new Sprint());
    }

    public void register(Module module) {
        modules.add(module);
    }

    // ── Поиск ────────────────────────────────────────────────────────────────

    /** Поиск по имени из аннотации — для команд и конфига ("killaura" и т.п.). */
    public Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    /** Поиск по классу — когда модуль известен на этапе компиляции. */
    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (module.getClass().equals(clazz)) {
                // clazz.cast вместо (T) — тот же результат, но без unchecked-каста
                return clazz.cast(module);
            }
        }
        return null;
    }

    // ── Списки ───────────────────────────────────────────────────────────────

    /** Все модули. Список только для чтения, чтобы его случайно не сломали снаружи. */
    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    /** Только включённые модули. */
    public List<Module> getEnabledModules() {
        List<Module> enabled = new ArrayList<>();
        for (Module module : modules) {
            if (module.isToggled()) {
                enabled.add(module);
            }
        }
        return enabled;
    }

    // ── Прокидка хуков -- сюда попадаем из Fabric-событий ───────────────────

    /** Звать из ClientTickEvents.END_CLIENT_TICK -- 20 раз в секунду. */
    public void onTick() {
        for (Module module : modules) {
            if (module.isToggled()) {
                module.onTick();
            }
        }
    }

    /** Звать из HUD-колбэка -- каждый кадр. */
    public void onRender2D(DrawContext context) {
        for (Module module : modules) {
            if (module.isToggled()) {
                module.onRender2D(context);
            }
        }
    }
}
