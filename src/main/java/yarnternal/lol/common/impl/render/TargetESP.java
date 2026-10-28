package yarnternal.lol.common.impl.render;

import lombok.Getter;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;
import yarnternal.lol.api.animation.Animation;
import yarnternal.lol.api.animation.Easing;
import yarnternal.lol.api.utility.client.PrintUtil;
import yarnternal.lol.api.utility.render.color.ColorUtil;
import yarnternal.lol.api.utility.render.ProjectionUtil;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.api.ModuleAnnotation;
import yarnternal.lol.common.api.ModuleList;
import yarnternal.lol.common.settings.BooleanSetting;
import yarnternal.lol.common.settings.ModeSetting;
import yarnternal.lol.common.settings.NumberSetting;
import yarnternal.lol.common.ui.Theme;

@ModuleAnnotation(name = "TargetESP", desc = "Рисует маркер на цели в Aura или же когда Энтити на прицеле", type = ModuleList.Render)
public class TargetESP extends Module {

    private static final Identifier TARGET_TEXTURE = Identifier.of("yarnternal", "icon/textures/target.png");

    @Getter
    public final BooleanSetting onlyAura = addSetting(new BooleanSetting("Рисовать только при вкл. Aura", false));

    private final ModeSetting colorMode = addSetting(new ModeSetting("Цвет", "Тема (Волна)",
            "Тема (Волна)", "Тема (Акцент)", "Белый"));

    private final NumberSetting size = addSetting(new NumberSetting("Size", 50.0, 10.0, 160.0, 1.0, " px"));

    private final Animation scaleAnim = new Animation(0.0, 250, Easing.EASE_OUT_BACK);
    private Vector2f lastPos = null;

    public TargetESP() {
        onlyAura.onChange(enabled -> {
            if (enabled) {
                PrintUtil.printer("Нет киллауры - нет конфетки");
            }
        });
    }

    @Override
    public void onDisable() {
        scaleAnim.reset(0.0);
        lastPos = null;
    }

    @Override
    public void onRender2D(DrawContext context) {
        if (mc.world == null || mc.player == null)
            return;

        boolean hasTarget = mc.targetedEntity != null && !onlyAura.getValue();
        float tickDelta = mc.getRenderTickCounter().getTickProgress(false);

        if (hasTarget) {
            Vector2f pos = ProjectionUtil.projectEntityCenter(mc.targetedEntity, tickDelta);
            if (pos != null) {
                lastPos = pos;
            }
            // Сочное появление с пружинкой
            scaleAnim.run(1.0, 260, Easing.EASE_OUT_BACK);
        } else {
            // Мягкий и быстрый уход при потере цели
            scaleAnim.run(0.0, 180, Easing.EASE_IN_CUBIC);
        }

        float scale = scaleAnim.getValueFloat();
        if (scale <= 0.001f || lastPos == null) {
            return;
        }

        float angle = (System.currentTimeMillis() % 3600) / 10.0f;
        float s = size.getValue().floatValue();
        int half = Math.round(s / 2.0f);
        int width = half * 2;

        int baseColor = switch (colorMode.get()) {
            case "Тема (Акцент)" -> Theme.accent();
            case "Белый" -> 0xFFFFFFFF;
            default -> {
                float time = (System.currentTimeMillis() % 2400L) / 2400.0f;
                yield ColorUtil.wave(Theme.accent(), Theme.accentAlt(), time);
            }
        };

        // Мягко гасим альфу при схлопывании
        int color = ColorUtil.multiplyAlpha(baseColor, Math.clamp(scale, 0.0f, 1.0f));

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(lastPos.x, lastPos.y);
        matrices.scale(scale, scale);
        matrices.rotate((float) Math.toRadians(angle));

        context.drawTexture(RenderPipelines.GUI_TEXTURED, TARGET_TEXTURE, -half, -half, 0.0f, 0.0f, width, width, width, width, color);

        matrices.popMatrix();
    }
}
