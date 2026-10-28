package yarnternal.lol.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Доступ к приватному {@code lastSelectedSlot} менеджера взаимодействий.
 * <p>
 * После CrystalBind мы вручную возвращаем слот и отправляем
 * {@code UpdateSelectedSlotC2SPacket}. Если не обновить это поле, следующий
 * {@code interactBlock} решит, что пакет уже отправлен, и сервер останется
 * со старым слотом.
 */
@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerAccessor {

    @Accessor("lastSelectedSlot")
    void yarnternal$setLastSelectedSlot(int slot);
}
