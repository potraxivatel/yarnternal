package yarnternal.lol.api.utility.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import yarnternal.lol.api.utility.render.color.ColorUtil;
import yarnternal.lol.core.Yarnternal;

/**
 * Градиентный текст в духе Expensive: каждая буква получает свой оттенок
 * между двумя красками текущей темы клиента.
 *
 * Это не «полный RGB»-радуга: волна идёт только между colorOne и colorTwo,
 * поэтому после setStyle(...) новый текст сразу красится в новую тему.
 */
public final class GradientUtil {

    /** Полный проход волны, мс (как 360° при speed 10 в Expensive). */
    private static final float WAVE_PERIOD_MS = 3600.0f;

    /** Сдвиг фазы на символ (в Expensive — index * 16 при speed 10). */
    private static final float CHAR_PHASE = 16.0f / 360.0f;

    private static final int FALLBACK_FIRST = 0xFF7A5CFF;
    private static final int FALLBACK_SECOND = 0xFFFF4FD8;

    private GradientUtil() {
        // утилитный класс — конструктор запрещён
    }

    /** Градиент текущими красками клиента, без жирного. */
    public static MutableText gradient(String text) {
        return gradient(text, false);
    }

    /** Градиент текущими красками клиента. Волна двигается по времени. */
    public static MutableText gradient(String text, boolean bold) {
        Yarnternal client = Yarnternal.getInstance();
        int first = client != null ? client.getColorOne() : FALLBACK_FIRST;
        int second = client != null ? client.getColorTwo() : FALLBACK_SECOND;
        return gradient(text, first, second, bold);
    }

    /** Градиент конкретными цветами (ARGB; alpha в тексте не используется). */
    public static MutableText gradient(String text, int first, int second, boolean bold) {
        float time = (System.currentTimeMillis() % (long) WAVE_PERIOD_MS) / WAVE_PERIOD_MS;

        MutableText result = Text.empty();
        for (int i = 0; i < text.length(); i++) {
            // волна from -> to -> from; у каждого символа свой сдвиг фазы
            int color = ColorUtil.saturate(
                    ColorUtil.wave(first, second, time + i * CHAR_PHASE),
                    1.5f
            );

            result.append(Text.literal(String.valueOf(text.charAt(i)))
                    .setStyle(Style.EMPTY
                            .withColor(color & 0xFFFFFF)
                            .withBold(bold)));
        }
        return result;
    }
}
