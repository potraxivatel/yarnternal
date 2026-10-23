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

    /** Поиск темы по имени (без учёта регистра). */
    public Style getStyleByName(String name) {
        if (name == null) {
            return null;
        }
        for (Style style : styles) {
            if (style.name().equalsIgnoreCase(name.trim())) {
                return style;
            }
        }
        return null;
    }

    /** Текущий стиль клиента. */
    public Style current() {
        return Yarnternal.getInstance().getStyle();
    }

    /** Включает стиль: главный класс пересоберёт краски, клиент перекрасится и выбор сохранится в конфиг. */
    public void apply(Style style) {
        if (style == null) {
            return;
        }
        Yarnternal y = Yarnternal.getInstance();
        if (y != null) {
            y.setStyle(style);
            if (y.getConfigManager() != null) {
                y.getConfigManager().saveClient();
            }
        }
    }

    /** Включает стиль по названию. Возвращает true, если стиль найден и применён. */
    public boolean apply(String name) {
        Style style = getStyleByName(name);
        if (style != null) {
            apply(style);
            return true;
        }
        return false;
    }
}
