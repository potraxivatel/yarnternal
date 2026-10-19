package yarnternal.lol.render.mixin;

import net.minecraft.client.render.ProjectionMatrix2;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Доступ к матрице проекции ванильного ProjectionMatrix2 —
 * нужен Utils#unscaledProjection для unscaled-координат HUD.
 */
@Mixin(ProjectionMatrix2.class)
public interface ProjectionMatrix2Accessor {

    @Invoker("getMatrix")
    Matrix4f yarn$callGetMatrix(float width, float height);
}
