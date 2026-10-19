package yarnternal.lol.render.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.GlCommandEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yarnternal.lol.render.mixininterface.IRenderPipeline;

import static org.lwjgl.opengl.GL11C.*;

/**
 * Включает GL_LINE_SMOOTH для пайплайнов, у которых выставлен флаг
 * сглаживания линий (ExtendedRenderPipelineBuilder#withLineSmooth).
 */
@Mixin(GlCommandEncoder.class)
public abstract class GlCommandEncoderMixin {

    @Inject(method = "setPipelineAndApplyState", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_polygonMode(II)V"))
    private void yarn$lineSmooth(RenderPipeline pipeline, CallbackInfo info) {
        if (((IRenderPipeline) pipeline).yarn$getLineSmooth()) {
            glEnable(GL_LINE_SMOOTH);
            glLineWidth(1);
        } else {
            glDisable(GL_LINE_SMOOTH);
        }
    }
}
