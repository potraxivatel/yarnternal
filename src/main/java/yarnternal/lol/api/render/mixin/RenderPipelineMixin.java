package yarnternal.lol.render.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import yarnternal.lol.render.mixininterface.IRenderPipeline;

@Mixin(RenderPipeline.class)
public abstract class RenderPipelineMixin implements IRenderPipeline {

    @Unique
    private boolean yarn$lineSmooth;

    @Override
    public void yarn$setLineSmooth(boolean lineSmooth) {
        this.yarn$lineSmooth = lineSmooth;
    }

    @Override
    public boolean yarn$getLineSmooth() {
        return yarn$lineSmooth;
    }
}
