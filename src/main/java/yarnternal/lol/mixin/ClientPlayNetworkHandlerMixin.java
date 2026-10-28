package yarnternal.lol.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

/**
 * ClientPlayNetworkHandlerMixin — перехват входящих сетевых пакетов (Velocity, Explosion и т.д.).
 * Хуки законсервированы в бэкап до написания модуля Velocity.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    // TODO: подключить хуки отмены пакетов EntityVelocityUpdateS2CPacket и ExplosionS2CPacket, когда напишем Velocity
}
