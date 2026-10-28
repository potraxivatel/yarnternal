package yarnternal.lol.render.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yarnternal.lol.api.event.events.Render2DEvent;
import yarnternal.lol.core.Yarnternal;
import yarnternal.lol.render.utils.HudRenderer;
import yarnternal.lol.render.utils.Utils;

/**
 * Хук на конец отрисовки ванильного HUD: включаем unscaled-координаты,
 * открываем кадр HudRenderer и раздаём Render2DEvent — подписчики рисуют
 * шейдерные скругления, блюр и свой шрифт.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void yarn$render2D(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.hudHidden) {
            return;
        }

        Utils.updateFrameTime();

        context.createNewRootLayer();

        Utils.unscaledProjection();
        HudRenderer.INSTANCE.begin(context);

        try {
            Yarnternal.getInstance().getEventManager().call(new Render2DEvent(
                    context,
                    context.getScaledWindowWidth(),
                    context.getScaledWindowHeight(),
                    Utils.frameTime,
                    tickCounter.getTickProgress(true)
            ));
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            HudRenderer.INSTANCE.end();
            context.createNewRootLayer();
            Utils.scaledProjection();
        }
    }
}
