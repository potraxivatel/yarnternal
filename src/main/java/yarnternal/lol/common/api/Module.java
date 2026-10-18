package yarnternal.lol.common.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import yarnternal.lol.api.utility.client.IMinecraft;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Module implements IMinecraft {

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }

    public void onRender2D() {
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
    boolean toggled;

    public Module() {
        if (this.getClass().isAnnotationPresent(ModuleAnnotation.class)) {
            ModuleAnnotation anno = this.getClass().getAnnotation(ModuleAnnotation.class);
            this.name = anno.name();
            this.desc = anno.desc();
            this.moduleList = anno.type();
        }
    }

    public void toggle() {
        this.toggled = !this.toggled;
        if (this.toggled) {
            onEnable();
        } else {
            onDisable();
        }
    }

}
