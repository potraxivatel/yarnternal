package yarnternal.lol.api.event.events;

import yarnternal.lol.api.event.Event;

/**
 * Тик клиента — Fabric зовёт его 20 раз в секунду (END_CLIENT_TICK).
 * Пример подписки: {@code @EventTarget private void onTickEvent(TickEvent event)}.
 */
public class TickEvent implements Event {
}
