package yarnternal.lol.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.util.Identifier;
import yarnternal.lol.common.api.ModuleProvider;
import yarnternal.lol.common.ui.hud.ColorTestHud;
import yarnternal.lol.core.provider.ClientInfoProvider;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Yarnternal implements ModInitializer {

    // просто по приколу добавил хотя возможно это топ
    @NonFinal
    boolean initialize;

    // создаётся в initProvider(); @NonFinal нужен, т.к. @FieldDefaults делает поля
    // final
    @NonFinal
    ClientInfoProvider clientInfoProvider;

    // создаётся в initManager(): реестр всех модулей клиента
    @NonFinal
    @Getter
    ModuleProvider moduleProvider;

    // делаем инстанс что бы другим классам было проще обращатся к главному
    @Getter
    static Yarnternal instance;

    public void initManager() {
        // вот тут будет инит всех менеджерей/демнов и тд типо вот
        // Foks foks = new foks();

        moduleProvider = new ModuleProvider();
        moduleProvider.init();

        // тик: Fabric зовёт нас 20 раз в секунду -> прокидываем включённым модулям
        ClientTickEvents.END_CLIENT_TICK.register(client -> moduleProvider.onTick());
    }

    // HUD — регистрируем тут (раньше жило в YarnternalClient, который удалили)
    public void initHud() {
        HudElementRegistry.addLast(
                Identifier.of("yarnternal", "color_test"),
                (context, tickCounter) -> ColorTestHud.render(context)
        );

        // модули рисуют свой HUD поверх всего (addLast = самый верхний слой)
        HudElementRegistry.addLast(
                Identifier.of("yarnternal", "modules"),
                (context, tickCounter) -> moduleProvider.onRender2D(context)
        );
    }

    // загрузчик
    @Override
    public void onInitialize() {

        instance = this;

        initialize = false;
        // инитаем тут чтобы все ок было и наш foks был уже в игре но щас утилиты фокс
        // неоту это к примеру
        initManager();
        initProvider();
        initHud();

        System.out.println(clientInfoProvider.getFullInfo());

        initialize = true;
    }

    public void initProvider() {
        clientInfoProvider = new ClientInfoProvider(
                "0.1.0",
                "yarnternal",
                "main");
    }

}
