package yarnternal.lol.core;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.util.Identifier;
import yarnternal.lol.api.event.EventManager;
import yarnternal.lol.api.event.events.TickEvent;
import yarnternal.lol.api.systems.style.Style;
import yarnternal.lol.api.systems.style.StyleManager;
import yarnternal.lol.api.utility.render.color.ColorUtil;
import yarnternal.lol.common.api.ModuleProvider;
import yarnternal.lol.common.command.CommandManager;
import yarnternal.lol.common.config.ConfigManager;
import yarnternal.lol.common.keybind.KeybindManager;
import yarnternal.lol.common.ui.gui.ClickGuiManager;
import yarnternal.lol.common.ui.hud.ColorTestHud;
import yarnternal.lol.core.provider.ClientInfoProvider;
import yarnternal.lol.render.RenderBootstrap;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Yarnternal implements ModInitializer {

    // просто по приколу добавил хотя возможно это топ
    @NonFinal
    boolean initialize;

    // создаётся в initProvider(); @NonFinal нужен, т.к. @FieldDefaults делает поля
    // final
    @NonFinal
    ClientInfoProvider clientInfoProvider;

    // создаётся в initManager(): реестр всех модулей клиента
    @NonFinal
    @Getter
    ModuleProvider moduleProvider;

    // создаётся в initManager(): шина событий клиента
    @NonFinal
    @Getter
    EventManager eventManager;

    // создаётся в initManager(): привязки клавиш к модулям
    @NonFinal
    @Getter
    KeybindManager keybindManager;

    // создаётся в initManager(): .cfg-конфиги и масштаб меню
    @NonFinal
    @Getter
    ConfigManager configManager;

    // создаётся в initManager(): клиентские команды с префиксом "."
    @NonFinal
    @Getter
    CommandManager commandManager;

    // создаётся в initStyles(): каталог всех тем клиента
    @NonFinal
    @Getter
    StyleManager styleManager;

    // текущая тема клиента: ставится в initStyles().
    // Цвета хранятся тут, в главном классе, — вся отрисовка берёт их отсюда.
    @NonFinal
    @Getter
    Style style;

    @NonFinal
    @Getter
    int colorOne;

    @NonFinal
    @Getter
    int colorTwo;

    // делаем инстанс что бы другим классам было проще обращатся к главному
    @Getter
    static Yarnternal instance;

    /** Меняет тему клиента — краски стиля пересобираются один раз. */
    public void setStyle(Style newStyle) {
        style = newStyle;
        colorOne = ColorUtil.fromHex(newStyle.colorOne());
        colorTwo = ColorUtil.fromHex(newStyle.colorTwo());
    }

    public void initManager() {
        // вот тут будет инит всех менеджерей/демнов и тд типо вот
        // Foks foks = new foks();

        eventManager = new EventManager();

        // клавиши: миксин -> KeyEvent -> бинды модулей
        keybindManager = new KeybindManager();
        keybindManager.init();

        initStyles();

        moduleProvider = new ModuleProvider();
        moduleProvider.init();

        // конфиги: config/yarnternal/configs/*.cfg + client.json (menu scale)
        configManager = new ConfigManager();

        // команды: ".help", ".bind", ".modules" и т.д. (перехват чата)
        commandManager = new CommandManager();
        commandManager.init();

        // меню клиента: открытие по правому Shift (.menu — то же самое)
        ClickGuiManager.init();

        // тик: Fabric зовёт нас 20 раз в секунду -> событие подписчикам
        // и хуки включённым модулям
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            eventManager.call(new TickEvent());
            moduleProvider.onTick();
        });

        // рендер-стек (шейдерные скругления, блюр, шрифты) — инициализируем,
        // когда клиент и GPU уже готовы
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> RenderBootstrap.init());
    }

    /**
     * Собирает все темы клиента: имя + две краски. Список живёт тут, в главном
     * классе (как в Expensive), — добавить тему = одна строчка.
     */
    public void initStyles() {
        List<Style> styles = List.of(
                new Style("Клиентский", "#7A5CFF", "#FF4FD8", false),
                new Style("Морской", "#053F6F", "#3E8EDE", false),
                new Style("Малиновый", "#6D0A28", "#E23B6B", false),
                new Style("Черничный", "#3B0463", "#9B5DE5", false),
                new Style("Огненный", "#B31200", "#FFB300", false),
                new Style("Металлический", "#1F1F1F", "#9E9E9E", false),
                new Style("Новогодний", "#B00538", "#F5E6E8", false),
                new Style("Полночь", "#0D1B2A", "#1B4965", false),
                new Style("Изумрудный", "#064E3B", "#10B981", false),
                new Style("Королевский", "#1E1B4B", "#6366F1", false),
                new Style("Бордовый", "#4A0E1E", "#A8324A", false),
                new Style("Киберпанк", "#0A0A0A", "#00F0FF", false),
                new Style("Неон", "#1A0033", "#FF00E5", false),
                new Style("Токсичный", "#0B3D0B", "#39FF14", false),
                new Style("Космос", "#0B0C2A", "#7B68EE", false),
                new Style("Вулкан", "#1A0000", "#FF4500", false),
                new Style("Океан", "#001F3F", "#00B4D8", false),
                new Style("Графит", "#232526", "#6E6E6E", false),
                new Style("Кофе", "#2B1B12", "#A9744F", false),
                new Style("Вино", "#2E0A1E", "#8E2D5A", false),
                new Style("Хаки", "#1F2A1F", "#7A8B5A", false),
                new Style("Сапфир", "#0A1F44", "#3D7EFF", false),
                new Style("Тёмная слива", "#2A0A2E", "#8B3A9C", false),
                new Style("Гроза", "#0F0F1A", "#5B5BD6", false),
                new Style("Матрица", "#000000", "#00FF41", false),
                new Style("Кровь", "#1A0000", "#C1121F", false),
                new Style("Северное сияние", "#0B132B", "#3A86FF", false),
                new Style("Ретро-волна", "#2B0F3A", "#FF6EC7", false),
                new Style("Тёмный шоколад", "#1E120B", "#6F4E37", false),

                new Style("Необычный", "#F3A0E8", "#ABFAF3", true),
                new Style("Закатный", "#FF6B35", "#F7C59F", true),
                new Style("Сакура", "#FFC2D1", "#FFE5EC", true),
                new Style("Лаванда", "#E0C3FC", "#8EC5FC", true),
                new Style("Мятный", "#A8E6CF", "#DCEDC1", true),
                new Style("Персик", "#FFDAB9", "#FFE4C4", true),
                new Style("Небо", "#A1C4FD", "#C2E9FB", true),
                new Style("Лимон", "#FFF59D", "#FFFDE7", true),
                new Style("Розовый кварц", "#F8BBD0", "#E1BEE7", true),
                new Style("Аквамарин", "#A0E7E5", "#B4F8C8", true),
                new Style("Крем", "#FFF8E7", "#FFE8CC", true),
                new Style("Весенний", "#C1FBA4", "#B9FBC0", true),
                new Style("Романтичный", "#FFB6C1", "#FF69B4", true),
                new Style("Пастель", "#FBC2EB", "#A6C1EE", true),
                new Style("Жемчуг", "#F5F5F5", "#E8E8E8", true),
                new Style("Сирень", "#D8B4FE", "#F0ABFC", true),
                new Style("Цитрус", "#FDE68A", "#FCA5A5", true),
                new Style("Мороз", "#E0F7FA", "#B2EBF2", true),
                new Style("Тёплый песок", "#F5DEB3", "#FFE4B5", true),
                new Style("Кофе с молоком", "#D7CCC8", "#EFEBE9", true),

                new Style("Прикольный", "#3DDC97", "#3AA0F5", false),
                new Style("Лесной", "#1B4332", "#52B788", false),
                new Style("Фруктовый", "#FF6B6B", "#FFD93D", true),
                new Style("Поп", "#FF006E", "#8338EC", false),
                new Style("Сладкий", "#FF8FAB", "#FBC4AB", true),
                new Style("Барби", "#FF69B4", "#FFB6C1", true),
                new Style("Мультик", "#4CC9F0", "#F72585", false),
                new Style("Радужный", "#F94144", "#577590", false),
                new Style("Арбуз", "#FF595E", "#8AC926", false),
                new Style("Мороженое", "#FFAFCC", "#BDE0FE", true),
                new Style("Клоун", "#FFD23F", "#EE4266", true),
                new Style("Джунгли", "#2D6A4F", "#95D5B2", false),
                new Style("Лагуна", "#0077B6", "#90E0EF", false),
                new Style("Карамель", "#FF9F1C", "#FFBF69", true),
                new Style("Фламинго", "#FF5D8F", "#FFB3C6", true),
                new Style("Галактика", "#3A0CA3", "#F72585", false),
                new Style("Супергерой", "#003049", "#D62828", false),
                new Style("Взрыв", "#F77F00", "#FCBF49", true),

                new Style("Хэллоуин", "#1A1A1A", "#FF7A00", false),
                new Style("Пасха", "#F7E7CE", "#B5E48C", true),
                new Style("День св. Валентина", "#8B0000", "#FF69B4", false),
                new Style("Осенний", "#6B3E26", "#D9A066", false),
                new Style("Зимний", "#1E3A5F", "#A8DADC", false),
                new Style("Летний", "#FFB703", "#219EBC", true),
                new Style("Космический новый год", "#0B0C2A", "#FFD700", false),
                new Style("День рождения", "#FF6B9D", "#FFC75F", true),
                new Style("Свадебный", "#FADDE1", "#E8E8E8", true),
                new Style("Спорт", "#1D3557", "#E63946", false),
                new Style("Геймерский", "#0F0F0F", "#B026FF", false),
                new Style("Ретро-ТВ", "#3A2E1F", "#C8A45C", false),
                new Style("Пиксель", "#2C2C54", "#FFDD59", false),
                new Style("Стимпанк", "#3B2F2F", "#B8860B", false),
                new Style("Кибер-нуар", "#101010", "#FF003C", false),
                new Style("Пастельный сон", "#CDB4DB", "#FFC8DD", true));

        styleManager = new StyleManager(styles);
        setStyle(styles.get(0));
    }

    // HUD — регистрируем тут (раньше жило в YarnternalClient, который удалили)
    public void initHud() {
        // витрина рендера: подписывается на Render2DEvent и рисует шейдером
        // (скругления, градиенты, свой шрифт) — см. ColorTestHud
        eventManager.register(new ColorTestHud());

        // модули рисуют свой HUD поверх всего (addLast = самый верхний слой).
        // Render2DEvent больше не тут: его постит рендер-хук в конце InGameHud.render,
        // когда открыт кадр HudRenderer (так подписчики видят и DrawContext, и шейдерный рендер)
        HudElementRegistry.addLast(
                Identifier.of("yarnternal", "modules"),
                (context, tickCounter) -> moduleProvider.onRender2D(context));
    }

    // загрузчик
    @Override
    public void onInitialize() {

        instance = this;

        initialize = false;
        // инитаем тут чтобы все ок было и наш foks был уже в игре но щас утилиты фокс
        // неоту это к примеру
        initManager();
        initProvider();
        initHud();

        System.out.println(clientInfoProvider.getFullInfo());

        initialize = true;
    }

    public void initProvider() {
        clientInfoProvider = new ClientInfoProvider(
                "0.1.0",
                "yarnternal",
                "main");
    }

}
