package yarnternal.lol.common.ui;

import yarnternal.lol.core.Yarnternal;

/**
 * Палитра и геометрия ClickGUI в стиле Expensive 2.0.
 *
 * <p>Интерфейс монохромный: почти весь текст и контролы — оттенки серого,
 * а цвет активной темы живёт только в подсветке категории, логотипе,
 * свечении окна и цветовых превью. Все цвета — ARGB.
 */
public final class Theme {

    private Theme() {}

    // ── Фоны ─────────────────────────────────────────────────────────────────
    /** Затемнение экрана под размытым фоном. */
    public static final int OVERLAY        = 0x40000000;
    /** Основная площадь контента. */
    public static final int BG_DARK        = 0xFF121319;
    /** Карточка модуля. */
    public static final int BG_PANEL       = 0xFF191A21;
    public static final int BG_PANEL_HOVER = 0xFF1D1E27;
    /** Сайдбар: чуть светлее контента, как medium в Expensive. */
    public static final int BG_SIDEBAR     = 0xFF1C1D23;
    /** Поле поиска и сегментированные контролы. */
    public static final int BG_SEARCH      = 0xFF121319;
    public static final int BG_SEGMENT     = 0xFF0B0C0F;
    /** Дорожки слайдеров, клавиши, маленькие поля значений. */
    public static final int BG_TRACK       = 0xFF151619;
    public static final int BG_KEY         = 0xFF141518;
    /** Подложка аватара в профиле. */
    public static final int BG_PROFILE     = 0xFF141518;

    // ── Обводки ──────────────────────────────────────────────────────────────
    /** Диагональный градиент тонкой рамки карточки. */
    public static final int BORDER_BRIGHT  = 0xFF3F4867;
    public static final int BORDER_DARK    = 0xFF131727;
    /** Разделитель шапки карточки. */
    public static final int DIVIDER        = 0xFF202339;
    /** Разделитель сайдбара и контента. */
    public static final int SIDEBAR_LINE   = 0x33818699;

    // ── Текст ────────────────────────────────────────────────────────────────
    public static final int TEXT_WHITE     = 0xFFFFFFFF;
    /** Основной приглушённый текст Expensive. */
    public static final int TEXT_LIGHT     = 0xFF818699;
    /** Подписи настроек. */
    public static final int TEXT_SOFT      = 0xFFA1A6B3;
    public static final int TEXT_MUTED     = 0xFFA1A6B3;
    /** Невыбранные пункты сегментированного контрола. */
    public static final int TEXT_DARK      = 0xFF1A1E29;
    public static final int TEXT_DIM       = 0xFF5A5F70;

    // ── Контролы ─────────────────────────────────────────────────────────────
    public static final int TOGGLE_TRACK   = 0xFF141518;
    public static final int TOGGLE_OFF     = 0xFF2A3849;
    public static final int TOGGLE_ON      = 0xFF7F869A;

    public static final int SLIDER_TRACK   = 0xFF151619;
    public static final int SLIDER_FILL    = 0xFF808598;
    public static final int SLIDER_KNOB    = 0xFF808598;

    // ── Акцент (из активной темы StyleManager) ───────────────────────────────
    /** Фоллбэк, если тема ещё не инициализирована. */
    public static final int ACCENT         = 0xFF7A5CFF;
    public static final int ACCENT_ALT     = 0xFFFF4FD8;

    /** Первый цвет активной темы. */
    public static int accent() {
        Yarnternal y = Yarnternal.getInstance();
        return (y != null && y.getColorOne() != 0) ? y.getColorOne() : ACCENT;
    }

    /** Второй цвет активной темы. */
    public static int accentAlt() {
        Yarnternal y = Yarnternal.getInstance();
        return (y != null && y.getColorTwo() != 0) ? y.getColorTwo() : ACCENT_ALT;
    }

    /** Приглушённый акцент. */
    public static int accentDim() {
        int a = accent();
        int r = (a >> 16) & 0xFF;
        int g = (a >> 8) & 0xFF;
        int b = a & 0xFF;
        return (0xFF << 24) | ((r / 2) << 16) | ((g / 2) << 8) | (b / 2);
    }

    /** Полупрозрачный тинт для мягких заливок. */
    public static int accentTint() {
        return (accent() & 0x00FFFFFF) | 0x22000000;
    }

    /** Заливка слайдера остаётся серой, как в Expensive. */
    public static int sliderFill() {
        return SLIDER_FILL;
    }

    // ── Геометрия ────────────────────────────────────────────────────────────
    public static final int WINDOW_W       = 450;
    public static final int WINDOW_H       = 350;
    public static final int SIDEBAR_W      = 100;
    public static final int HEADER_H       = 0;

    public static final int RADIUS_WINDOW  = 8;
    public static final int RADIUS_CARD    = 3;
    public static final int RADIUS_CONTROL = 3;

    /** Карточки модулей: 160 px, две колонки, как в Expensive 2.0. */
    public static final int CARD_W         = 160;
    public static final int CARD_HEADER_H  = 30;
    public static final int CARD_DESC_H    = 11;
    /** Нижний отступ внутри карточки после последней настройки. */
    public static final int CARD_PAD       = 6;
    public static final int CARD_GAP       = 8;

    public static final int CONTENT_PAD    = 10;
    public static final int CONTENT_GAP    = 10;
    public static final int SETTING_ROW_H  = 22;

    // ── Сайдбар ──────────────────────────────────────────────────────────────
    public static final int SEARCH_X       = 7;
    public static final int SEARCH_Y       = 34;
    public static final int SEARCH_W       = 86;
    public static final int SEARCH_H       = 22;
    public static final int RADIUS_SEARCH  = 5;

    public static final int CATEGORY_X     = 5;
    public static final int CATEGORY_Y     = 66;
    public static final int CATEGORY_W     = 90;
    public static final int CATEGORY_H     = 21;
    public static final int SIDEBAR_ROW_H  = 25;
}
