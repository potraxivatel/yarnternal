package yarnternal.lol.api.utility.player;

import yarnternal.lol.api.utility.client.IMinecraft;

public final class PlayerUtil implements IMinecraft {

    private PlayerUtil() {
    }

    /**
     * Ник нашего игрока, либо пустая строка если игрок ещё не в мире.
     *
     * <p>Параметр здесь не нужен: метод сам берёт имя из {@code mc.player}.
     * Если передавать имя снаружи — это не утилита, а обычная проверка на null.
     */
    public static String getLocalName() {
        return mc.player == null ? "" : mc.player.getName().getString();
    }

    // TODO (trashtalk): ник противника-цели. Понадобится взять сущность под
    // прицелом (например через Raycast) и вернуть её имя — не только локальное.
}
