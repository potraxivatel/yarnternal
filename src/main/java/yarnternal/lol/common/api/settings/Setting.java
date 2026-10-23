package yarnternal.lol.common.settings;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Базовая настройка модуля. Настройки живут на модуле, меню рисует их
 * в раскрытой карточке, а {@link yarnternal.lol.common.config.ConfigManager}
 * сохраняет/загружает их вместе с конфигом.
 *
 * <p>Модуль может подписаться на изменения через {@link #onChange(Consumer)}:
 * например, пересобрать кэш, когда поехал слайдер.
 */
public abstract class Setting<T> {

    private final String name;
    protected T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();
    private Supplier<Boolean> visible = () -> true;

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    /**
     * Ставит значение. null и повтор того же значения игнорируются,
     * так что подписчики не получают ложных срабатываний.
     */
    public void setValue(T value) {
        if (value == null || Objects.equals(this.value, value)) {
            return;
        }
        this.value = value;
        onChanged();
        for (Consumer<T> listener : listeners) {
            listener.accept(value);
        }
    }

    /** Подписка на изменения — то, что раньше не мог сделать модуль. */
    public void onChange(Consumer<T> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /** Проверяет, видима ли настройка в GUI (для динамических условий видимости). */
    public boolean isVisible() {
        return visible == null || visible.get();
    }

    /** Задаёт условие отображения настройки в GUI (например, visible(() -> mode.getValue() == Mode.Custom)). */
    @SuppressWarnings("unchecked")
    public <S extends Setting<?>> S visible(Supplier<Boolean> condition) {
        this.visible = condition;
        return (S) this;
    }

    /** Хук для наследников самой настройки (не для модулей: они используют onChange). */
    protected void onChanged() {
    }

    /** Как значение выглядит в интерфейсе: "On", "300 px", "Blur". */
    public abstract String displayValue();

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement json);
}
