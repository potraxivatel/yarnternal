package yarnternal.lol.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import yarnternal.lol.util.ColorUtil;
import yarnternal.lol.util.RenderUtil;

/**
 * Цветной тест — панель, которая каждый кадр рисуется поверх игры.
 * Проверяет, что RenderUtil и ColorUtil реально работают: градиенты,
 * палитра, прозрачность и анимированная радуга.
 */
public final class ColorTestHud {

    private ColorTestHud() {
        // утилитный класс — конструктор запрещён
    }

    /** Вызывается каждый кадр (регистрация в YarnternalClient). */
    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        float x = 8.0f;
        float y = 8.0f;
        float width = 186.0f;
        float height = 96.0f;

        // фон + рамка
        RenderUtil.panel(context, x, y, width, height, ColorUtil.rgba(16, 16, 16, 190), ColorUtil.rgb(255, 255, 255));

        // заголовок
        RenderUtil.text(context, textRenderer, "Yarnternal - цветной тест", x + 6.0f, y + 5.0f, ColorUtil.fromHex("#FF55FF"));

        // вертикальный и горизонтальный градиенты
        RenderUtil.fillGradient(context, x + 6.0f, y + 18.0f, 84.0f, 10.0f, ColorUtil.rgb(255, 85, 85), ColorUtil.rgb(85, 85, 255));
        RenderUtil.fillHorizontalGradient(context, x + 96.0f, y + 18.0f, 84.0f, 10.0f, ColorUtil.rgb(85, 255, 85), ColorUtil.rgb(255, 85, 255), 32);

        // анимированная радуга (сдвигается со временем)
        float offset = (System.currentTimeMillis() % 3000L) / 3000.0f;
        RenderUtil.fillRainbow(context, x + 6.0f, y + 32.0f, 174.0f, 8.0f, offset);

        // палитра базовых цветов
        int[] palette = {
                ColorUtil.rgb(255, 85, 85),   // красный
                ColorUtil.rgb(255, 170, 0),   // оранжевый
                ColorUtil.rgb(255, 255, 85),  // жёлтый
                ColorUtil.rgb(85, 255, 85),   // зелёный
                ColorUtil.rgb(85, 255, 255),  // голубой
                ColorUtil.rgb(85, 85, 255),   // синий
                ColorUtil.rgb(255, 85, 255),  // фиолетовый
                ColorUtil.rgb(255, 255, 255), // белый
                ColorUtil.rgb(0, 0, 0),       // чёрный
        };
        for (int i = 0; i < palette.length; i++) {
            float squareX = x + 6.0f + i * 19.0f;
            RenderUtil.fill(context, squareX, y + 46.0f, 16.0f, 12.0f, palette[i]);
            RenderUtil.outline(context, squareX, y + 46.0f, 16.0f, 12.0f, ColorUtil.rgba(255, 255, 255, 120));
        }

        // шкала прозрачности (один цвет, разная alpha)
        for (int i = 0; i < 10; i++) {
            int alpha = Math.round(255.0f * (i + 1) / 10.0f);
            RenderUtil.fill(context, x + 6.0f + i * 17.4f, y + 64.0f, 15.0f, 10.0f, ColorUtil.rgba(85, 170, 255, alpha));
        }

        // информация из ColorUtil (hex и компоненты)
        int sample = ColorUtil.fromHex("#8034C6FF");
        RenderUtil.text(context, textRenderer, "HEX " + ColorUtil.toHex(sample)
                + "  A" + ColorUtil.getAlpha(sample)
                + " R" + ColorUtil.getRed(sample)
                + " G" + ColorUtil.getGreen(sample)
                + " B" + ColorUtil.getBlue(sample), x + 6.0f, y + 80.0f, ColorUtil.rgb(200, 200, 200));
    }
}
