package yarnternal.lol.common.modules.other;

import yarnternal.lol.api.event.EventTarget;
import yarnternal.lol.api.event.events.TickEvent;
import yarnternal.lol.api.utility.client.PrintUtil;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.api.ModuleAnnotation;
import yarnternal.lol.common.api.ModuleList;
import yarnternal.lol.common.settings.BooleanSetting;
import yarnternal.lol.common.settings.NumberSetting;

/**
 * Демо-модуль: показывает, как модуль подписывается на события
 * и как его настройки выглядят в меню.
 */
@ModuleAnnotation(name = "EventTest", desc = "Демо: подписки на события", type = ModuleList.Other)
public final class EventTestModule extends Module {

    private final NumberSetting interval = addSetting(new NumberSetting("Interval", 5, 1, 20, 1, " s"));
    private final BooleanSetting notifications = addSetting(new BooleanSetting("Notifications", true));

    private int ticks;

    @Override
    public void onEnable() {
        ticks = 0;
        if (notifications.getValue()) {
            PrintUtil.printer("EventTest включён — шина событий на связи");
        }
    }

    @Override
    public void onDisable() {
        if (notifications.getValue()) {
            PrintUtil.printer("EventTest выключен");
        }
    }

    /**
     * Подписка на тик: метод вызывается из EventManager#call(new TickEvent()).
     * Без параметров в аннотации — приоритет по умолчанию (MEDIUM);
     * можно указать явно: @EventTarget(value = Priority.HIGHEST).
     * Вызываются даже private-методы — менеджер сам открывает к ним доступ.
     */
    @EventTarget
    private void onTickEvent(TickEvent event) {
        int every = Math.max(1, (int) Math.round(interval.getValue() * 20));
        if (++ticks % every == 0 && notifications.getValue()) {
            PrintUtil.printer("EventTest: тик #" + ticks);
        }
    }
}
