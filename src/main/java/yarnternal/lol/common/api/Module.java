package yarnternal.lol.common.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.gui.DrawContext;
import yarnternal.lol.api.utility.client.IMinecraft;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Module implements IMinecraft {

    public void onEnable() {
    }

    public void onDisable() {
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
            onEnable();
        } else {
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!this.toggled);
    }

}
