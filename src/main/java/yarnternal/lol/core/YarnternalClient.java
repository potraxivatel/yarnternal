package yarnternal.lol.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.util.Identifier;
import yarnternal.lol.hud.ColorTestHud;

/**
 * Клиентский загрузчик — выполняется только в игре (не на сервере).
 * Здесь регистрируем HUD-элементы и всё, что связано с клиентской частью.
 */
public class YarnternalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // цветной тест — рисуется поверх всего HUD каждый кадр
        HudElementRegistry.addLast(
                Identifier.of("yarnternal", "color_test"),
                (context, tickCounter) -> ColorTestHud.render(context)
        );
    }
}
