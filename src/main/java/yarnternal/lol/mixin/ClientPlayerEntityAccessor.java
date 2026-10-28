package yarnternal.lol.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Доступ к приватному {@code lastSprinting} клиента.
 * <p>
 * Когда Aura сама отправляет STOP/START_SPRINTING, ванильный
 * {@code sendSprintingPacket()} не должен отправить такой же пакет ещё раз.
 * Для этого синхронизируем его внутреннее состояние.
 */
@Mixin(ClientPlayerEntity.class)
public interface ClientPlayerEntityAccessor {

    @Accessor("lastSprinting")
    void yarnternal$setLastSprinting(boolean lastSprinting);
}
