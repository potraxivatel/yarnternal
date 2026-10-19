package yarnternal.lol.api.utility.client;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import yarnternal.lol.api.utility.text.GradientUtil;

public class PrintUtil implements IMinecraft {

    public static void printer(String message) {
        if (mc.player != null) {
            // как в Expensive: имя клиента — градиент из красок текущей темы,
            // стрелка тёмно-серая, сам текст обычный
            MutableText prefix = GradientUtil.gradient("yarnternal", true)
                    .append(Text.literal(" » ").formatted(Formatting.DARK_GRAY));

            mc.player.sendMessage(prefix.append(Text.literal(message)), false);
        }
    }
}
