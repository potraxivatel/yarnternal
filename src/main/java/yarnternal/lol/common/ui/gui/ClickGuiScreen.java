package yarnternal.lol.common.ui.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Точка входа в меню клиента.
 *
 * <p>Старый интерфейс удалён — тут пустая заглушка, чтобы правый Shift
 * и команда ".menu" продолжали собираться и открывали экран. Свой GUI
 * пиши прямо в этом классе (render / mouseClicked / keyPressed).
 */
public final class ClickGuiScreen extends Screen {

    public ClickGuiScreen() {
        super(Text.literal("Yarnternal"));
    }

    /** Открыть меню: используется ClickGuiManager (RSHIFT) и .menu. */
    public static void open() {
        MinecraftClient.getInstance().setScreen(new ClickGuiScreen());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // фон рисует сам GUI — здесь специально ничего нет
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // тут будет твой интерфейс
    }
}
