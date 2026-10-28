package yarnternal.lol.common.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.gui.DrawContext;
import yarnternal.lol.api.utility.client.IMinecraft;
import yarnternal.lol.api.utility.sound.SoundUtil;
import yarnternal.lol.common.settings.Setting;
import yarnternal.lol.core.Yarnternal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Module implements IMinecraft {

    // настройки модуля: меню рисует их в раскрытой карточке,
    // ConfigManager сохраняет их в .cfg
    List<Setting<?>> settings = new ArrayList<>();

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onStartTick() {
    }

    public void onTick() {
    }

    // context приходит из HUD-колбэка: у MinecraftClient нет геттера DrawContext,
    // так что без параметра модуль физически не смог бы ничего нарисовать
    public void onRender2D(DrawContext context) {
    }

    public void onRender3D() {
    }

    @NonFinal
    @Getter
    String name;

    @NonFinal
    @Getter
    String desc;

    @NonFinal
    @Getter
    ModuleList moduleList;

    @NonFinal
    @Getter
    boolean toggled;

    public Module() {
        if (this.getClass().isAnnotationPresent(ModuleAnnotation.class)) {
            ModuleAnnotation anno = this.getClass().getAnnotation(ModuleAnnotation.class);
            this.name = anno.name();
            this.desc = anno.desc();
            this.moduleList = anno.type();
        } else {
            // забыл @ModuleAnnotation — не роняем игру, просто берём имя класса
            this.name = getClass().getSimpleName();
            this.desc = "";
            this.moduleList = ModuleList.Other;
        }
    }

    /** Включить/выключить. Повторный вызов с тем же значением ничего не делает. */
    public void setEnabled(boolean enabled) {
        if (this.toggled == enabled) {
            return;
        }
        this.toggled = enabled;
        if (enabled) {
            // подписка на шину событий: @EventTarget-методы модуля оживают
            Yarnternal.getInstance().getEventManager().register(this);
            onEnable();
        } else {
            onDisable();
            // снимаем подписки: выключенный модуль молчит до следующего включения
            Yarnternal.getInstance().getEventManager().unregister(this);
        }

        // Звук переключения модуля (только если мир загружен, чтобы не спамить при старте)
        if (mc.world != null) {
            SoundUtil.playToggle(enabled);
        }
    }

    public void toggle() {
        setEnabled(!this.toggled);
    }

    /** Все настройки модуля в порядке добавления. */
    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    /** Добавляет настройку и возвращает её — удобно для полей модуля. */
    protected <S extends Setting<?>> S addSetting(S setting) {
        settings.add(setting);
        return setting;
    }
}
