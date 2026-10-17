package yarnternal.lol.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.fabricmc.api.ModInitializer;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Yarnternal implements ModInitializer {

    // просто по приколу добавил хотя возможно это топ
    @NonFinal
    boolean initialize;

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

        initialize = true;
    }
}
