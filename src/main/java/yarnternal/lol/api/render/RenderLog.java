package yarnternal.lol.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Лог рендер-стека. Свой логгер, чтобы не тянуть главный класс клиента
 * в каждую утилиту.
 */
public final class RenderLog {

    public static final Logger LOG = LoggerFactory.getLogger("yarnternal-render");

    private RenderLog() {
    }
}
