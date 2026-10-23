package yarnternal.lol.render.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yarnternal.lol.render.renderer.MeshUniforms;

/**
 * В конце кадра чистит кольцевой буфер униформ меша — иначе он
 * переполнится за длинную сессию.
 */
@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {

    @Inject(method = "flipFrame", at = @At("TAIL"))
    private static void yarn$flipFrame(CallbackInfo info) {
        MeshUniforms.flipFrame();
    }
}
