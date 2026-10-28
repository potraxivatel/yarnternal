package yarnternal.lol.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * LivingEntityRendererMixin — визуальный поворот головы и тела персонажа в третьем лице (F5).
 * Будет подключен обратно, когда напишем ротации в Ауре.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
}
