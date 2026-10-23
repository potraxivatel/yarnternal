package yarnternal.lol.render.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.profiler.Profilers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yarnternal.lol.api.event.events.Render3DEvent;
import yarnternal.lol.core.Yarnternal;
import yarnternal.lol.render.renderer.Renderer3D;
import yarnternal.lol.render.renderer.YarnRenderPipelines;
import yarnternal.lol.render.utils.Utils;
import yarnternal.lol.render.utils.render.RenderUtils;

/**
 * Хук на рендер мира: подгоняет модельную матрицу под камеру (с учётом
 * тряски) и раздаёт Render3DEvent — подписчики рисуют боксы/линии в мире.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private Camera camera;

    @Shadow
    protected abstract void bobView(MatrixStack matrices, float tickDelta);

    @Shadow
    protected abstract void tiltViewWhenHurt(MatrixStack matrices, float tickDelta);

    @Unique
    private Renderer3D yarn$renderer;

    @Unique
    private Renderer3D yarn$depthRenderer;

    @Unique
    private final MatrixStack yarn$matrices = new MatrixStack();

    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"}))
    private void yarn$render3D(RenderTickCounter tickCounter, CallbackInfo ci,
                               @Local(ordinal = 0) Matrix4f projection,
                               @Local(ordinal = 1) Matrix4f position,
                               @Local(ordinal = 0) float tickDelta,
                               @Local MatrixStack matrixStack) {
        if (!Utils.canUpdate()) return;

        Profilers.get().push("yarnternal_render");

        if (yarn$renderer == null) {
            yarn$renderer = new Renderer3D(YarnRenderPipelines.WORLD_COLORED_LINES, YarnRenderPipelines.WORLD_COLORED);
        }
        if (yarn$depthRenderer == null) {
            yarn$depthRenderer = new Renderer3D(YarnRenderPipelines.WORLD_COLORED_LINES_DEPTH, YarnRenderPipelines.WORLD_COLORED_DEPTH);
        }

        Render3DEvent event = new Render3DEvent(
                matrixStack, yarn$renderer, yarn$depthRenderer,
                Utils.frameTime, tickDelta,
                camera.getCameraPos().x, camera.getCameraPos().y, camera.getCameraPos().z
        );

        RenderSystem.getModelViewStack().pushMatrix().mul(position);

        yarn$matrices.push();
        tiltViewWhenHurt(yarn$matrices, camera.getLastTickProgress());
        if (client.options.getBobView().getValue()) {
            bobView(yarn$matrices, camera.getLastTickProgress());
        }

        Matrix4f inverseBob = new Matrix4f(yarn$matrices.peek().getPositionMatrix()).invert();
        RenderSystem.getModelViewStack().mul(inverseBob);
        yarn$matrices.pop();

        RenderUtils.updateScreenCenter(projection, position);

        yarn$renderer.begin();
        yarn$depthRenderer.begin();
        Yarnternal.getInstance().getEventManager().call(event);
        yarn$renderer.render(matrixStack);
        yarn$depthRenderer.render(matrixStack);

        RenderSystem.getModelViewStack().popMatrix();

        Profilers.get().pop();
    }
}
