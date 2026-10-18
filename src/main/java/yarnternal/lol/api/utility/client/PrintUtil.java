package yarnternal.lol.api.utility.client;

import net.minecraft.text.Text;

public class PrintUtil implements IMinecraft {

    public static void printer(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(SUFFIX + " " + PREFIX + " " + message), false);
        }
    }
}
