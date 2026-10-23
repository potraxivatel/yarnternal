package yarnternal.lol.render.mixin;

import net.minecraft.client.gl.ShaderLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yarnternal.lol.render.renderer.YarnRenderPipelines;

/**
 * После каждой (пере)загрузки шейдеров компилирует наши кастомные
 * пайплайны — сырой GLSL из assets/yarnternal/shaders.
 */
@Mixin(ShaderLoader.class)
public abstract class ShaderLoaderMixin {

    @Inject(method = "apply(Lnet/minecraft/client/gl/ShaderLoader$Definitions;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("TAIL"))
    private void yarn$precompilePipelines(CallbackInfo info) {
        YarnRenderPipelines.precompile();
    }
}
