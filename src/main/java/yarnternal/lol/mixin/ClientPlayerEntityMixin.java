package yarnternal.lol.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * ClientPlayerEntityMixin — перехват пакетов движения, сброса спринта и замедления от предметов.
 * Хуки для Aura и NoSlow законсервированы до написания соответствующих модулей.
 */
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    // TODO: подключить хуки Aura (silent rotation) и NoSlow, когда модули будут написаны
}
