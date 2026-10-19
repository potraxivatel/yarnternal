package yarnternal.lol.api.event.types;

/**
 * Приоритеты подписки: чем меньше значение, тем раньше вызывается метод.
 * Порядок вызова: HIGHEST → HIGH → MEDIUM → LOW → LOWEST.
 */
public final class Priority {
    public static final byte HIGHEST = 0,
            HIGH = 1,
            MEDIUM = 2,
            LOW = 3,
            LOWEST = 4;

    /**
     * Порядок сортировки списка слушателей.
     */
    public static final byte[] VALUE_ARRAY;

    static {
        VALUE_ARRAY = new byte[] {
                HIGHEST,
                HIGH,
                MEDIUM,
                LOW,
                LOWEST
        };
    }
}
