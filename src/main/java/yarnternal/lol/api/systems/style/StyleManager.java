package yarnternal.lol.api.systems.style;

import java.util.List;
import yarnternal.lol.core.Yarnternal;

/**
 * Менеджер стилей — каталог тем клиента и точка их смены.
 * Сами темы собираются списком в главном классе (Yarnternal#initStyles),
 * а выбранную тему и её краски тоже держит главный класс.
 */
public final class StyleManager {

    private final List<Style> styles;

    public StyleManager(List<Style> styles) {
        this.styles = List.copyOf(styles);
    }

    /** Все темы клиента. Список только для чтения, чтобы его случайно не сломали снаружи. */
    public List<Style> getStyles() {
        return styles;
    }

    /** Текущий стиль клиента. */
    public Style current() {
        return Yarnternal.getInstance().getStyle();
    }

    /** Включает стиль: главный класс пересоберёт краски, и клиент перекрасится сам. */
    public void apply(Style style) {
        Yarnternal.getInstance().setStyle(style);
    }
}
