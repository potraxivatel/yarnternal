package yarnternal.lol.common.command.impl;

import yarnternal.lol.common.command.Command;
import yarnternal.lol.common.ui.gui.ClickGuiScreen;

/** ".menu" — открыть меню клиента (то же, что правый Shift). */
public final class MenuCommand extends Command {

    public MenuCommand() {
        super("menu", "открыть меню клиента", "gui", "clickgui");
    }

    @Override
    public void execute(String[] args) {
        ClickGuiScreen.open();
    }
}
