package yarnternal.lol.common.api;

import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yarnternal.lol.common.impl.movement.*;
import yarnternal.lol.common.impl.other.*;
import yarnternal.lol.common.impl.player.*;
import yarnternal.lol.common.impl.render.*;

public final class ModuleProvider {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        register(new Sprint());
        register(new BridgeAssist());
        register(new TargetESP());
        register(new ClientSounds());
    }

    public void register(Module module) {
        modules.add(module);
    }

    public Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (module.getClass().equals(clazz)) {
                // clazz.cast вместо (T) — тот же результат, но без unchecked-каста
                return clazz.cast(module);
            }
        }
        return null;
    }

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

    /**
     * Звать из ClientTickEvents.START_CLIENT_TICK -- в начале тика, до отправки
     * пакетов движения.
     */
    public void onStartTick() {
        for (Module module : modules) {
            if (module.isToggled()) {
                module.onStartTick();
            }
        }
    }

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
