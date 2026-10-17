package yarnternal.lol.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.fabricmc.api.ModInitializer;
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

    // делаем инстанс что бы другим классам было проще обращатся к главному
    @Getter
    static Yarnternal instance;

    public void initManager() {
        // вот тут будет инит всех менеджерей/демнов и тд типо вот
        // Foks foks = new foks();
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
