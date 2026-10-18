package yarnternal.lol.api.utility.client;

import net.minecraft.client.MinecraftClient;

public interface IMinecraft {

    // Интерфейс что бы фоксить
    // обьявляем MinecraftClient mc что было легко юзать в коде от наследия
    // IMinecraft
    public static final MinecraftClient mc = MinecraftClient.getInstance();

    // так же обьявляем префикс и суффикс что бы PrintUtil наследовала IMinecraft
    public final String SUFFIX = "§lyarnternal";
    public final String PREFIX = "§8»";

}
