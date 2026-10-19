package yarnternal.lol.api.event.types.situations;

/**
 * Набор ситуаций для будущих событий-подсказок: например, «мало ХП — пора
 * заесть гепл» или «предмет в руке вот-вот сломается».
 * SITUATION_ARRAY — порядок, в котором ситуации перебираются.
 */
public final class Situation {
    public static final byte CRITICAL_FOR_USER = 1,
            CRITICAL_FOR_ENEMY = 1,
            CRITICAL_FOR_ARMOR = 2,
            CRITICAL_FOR_ITEM_IN_MAIN_HAND = 3;

    public static final byte[] SITUATION_ARRAY;

    static {
        SITUATION_ARRAY = new byte[] {
                CRITICAL_FOR_USER,
                CRITICAL_FOR_ENEMY,
                CRITICAL_FOR_ARMOR,
                CRITICAL_FOR_ITEM_IN_MAIN_HAND
        };
    }

}
