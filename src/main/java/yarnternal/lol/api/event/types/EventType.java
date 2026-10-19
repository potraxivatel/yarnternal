package yarnternal.lol.api.event.types;

/**
 * Тип события (как в оригинальном EventAPI):
 * PRE / ON / POST — фазы события, SEND / RECEIVE — сетевые пакеты.
 */
public enum EventType {
    PRE,
    ON,
    POST,
    SEND,
    RECEIVE
}
