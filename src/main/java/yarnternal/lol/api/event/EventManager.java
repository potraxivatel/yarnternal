package yarnternal.lol.api.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import yarnternal.lol.api.event.types.Priority;

/**
 * Менеджер событий: реестр подписок (класс события → список методов-слушателей),
 * регистрация и снятие слушателей, диспатч событий в порядке приоритета.
 * Живёт инстансом в главном классе: {@code Yarnternal.getInstance().getEventManager()}.
 *
 * <p>Как этим пользоваться:
 * <ol>
 *   <li>событие — класс, реализующий {@link Event};</li>
 *   <li>слушатель — метод с аннотацией {@link EventTarget} и одним
 *   параметром-событием (может быть даже приватным);</li>
 *   <li>{@code register(this)} — когда объект включается,
 *   {@code unregister(this)} — когда выключается;</li>
 *   <li>вызов — {@code call(new SomeEvent())} из нужной точки игры
 *   (тик, HUD и т.д.).</li>
 * </ol>
 *
 * <p>Написан по мотивам EventAPI от DarkMagician6.
 *
 * @author DarkMagician6 (оригинальное API)
 */
public final class EventManager {

    /**
     * Реестр: для каждого класса события — список методов-слушателей,
     * отсортированный по приоритету.
     */
    private final HashMap<Class<? extends Event>, List<MethodData>> registryMap = new HashMap<>();

    /**
     * Регистрирует все методы объекта, помеченные аннотацией {@link EventTarget}.
     *
     * @param object
     *               объект, чьи методы хотим зарегистрировать.
     */
    public void register(Object object) {
        for (final Method method : object.getClass().getDeclaredMethods()) {
            if (!isMethodBad(method)) {
                register(method, object);
            }
        }
    }

    /**
     * Регистрирует методы объекта, помеченные {@link EventTarget}, которые
     * принимают указанный класс события.
     *
     * @param object
     *                   объект, в котором ищем методы.
     * @param eventClass
     *                   класс события, которое слушает метод.
     */
    public void register(Object object, Class<? extends Event> eventClass) {
        for (final Method method : object.getClass().getDeclaredMethods()) {
            if (!isMethodBad(method, eventClass)) {
                register(method, object);
            }
        }
    }

    /**
     * Снимает регистрацию всех методов объекта, помеченных {@link EventTarget}.
     *
     * @param object
     *               объект, слушателей которого нужно снять.
     */
    public void unregister(Object object) {
        for (final List<MethodData> dataList : registryMap.values()) {
            for (final MethodData data : dataList) {
                if (data.getSource().equals(object)) {
                    dataList.remove(data);
                }
            }
        }

        cleanMap(true);
    }

    /**
     * Снимает регистрацию методов объекта, слушающих указанный класс события.
     *
     * @param object
     *                   объект-слушатель.
     * @param eventClass
     *                   класс события, чьи слушатели снимаются.
     */
    public void unregister(Object object, Class<? extends Event> eventClass) {
        if (registryMap.containsKey(eventClass)) {
            for (final MethodData data : registryMap.get(eventClass)) {
                if (data.getSource().equals(object)) {
                    registryMap.get(eventClass).remove(data);
                }
            }

            cleanMap(true);
        }
    }

    /**
     * Добавляет MethodData в реестр.
     * Если запись для класса события уже есть — добавляет данные в список
     * и пересортировывает его по приоритету ({@link Priority}).
     * Если нет — создаёт новую запись со списком из одних данных.
     *
     * @param method
     *               метод для регистрации.
     * @param object
     *               объект — владелец метода.
     */
    @SuppressWarnings("unchecked")
    private void register(Method method, Object object) {
        Class<? extends Event> indexClass = (Class<? extends Event>) method.getParameterTypes()[0];
        // Создаём MethodData из метода, который регистрируем.
        final MethodData data = new MethodData(object, method, method.getAnnotation(EventTarget.class).value());

        // Открываем доступ к методу — чтобы работали и приватные, и protected.
        data.getTarget().setAccessible(true);

        if (registryMap.containsKey(indexClass)) {
            if (!registryMap.get(indexClass).contains(data)) {
                registryMap.get(indexClass).add(data);
                sortListValue(indexClass);
            }
        } else {
            List<MethodData> list = new CopyOnWriteArrayList<>();
            list.add(data);
            registryMap.put(indexClass, list);
        }
    }

    /**
     * Удаляет запись из реестра по ключу.
     *
     * @param indexClass
     *                   класс события — ключ записи, которую удаляем.
     */
    public void removeEntry(Class<? extends Event> indexClass) {
        Iterator<Map.Entry<Class<? extends Event>, List<MethodData>>> mapIterator = registryMap.entrySet().iterator();

        while (mapIterator.hasNext()) {
            if (mapIterator.next().getKey().equals(indexClass)) {
                mapIterator.remove();
                break;
            }
        }
    }

    /**
     * Чистит реестр от пустых записей.
     * Удаляем через итератор — так запись уходит целиком, без следов.
     *
     * @param onlyEmptyEntries
     *                         true — удалять только пустые записи; false — все.
     */
    public void cleanMap(boolean onlyEmptyEntries) {
        Iterator<Map.Entry<Class<? extends Event>, List<MethodData>>> mapIterator = registryMap.entrySet().iterator();

        while (mapIterator.hasNext()) {
            if (!onlyEmptyEntries || mapIterator.next().getValue().isEmpty()) {
                mapIterator.remove();
            }
        }
    }

    /**
     * Сортирует список слушателей события по приоритету.
     *
     * @param indexClass
     *                   класс события, чей список сортируем.
     */
    private void sortListValue(Class<? extends Event> indexClass) {
        List<MethodData> sortedList = new CopyOnWriteArrayList<>();

        for (final byte priority : Priority.VALUE_ARRAY) {
            for (final MethodData data : registryMap.get(indexClass)) {
                if (data.getPriority() == priority) {
                    sortedList.add(data);
                }
            }
        }

        // Заменяем старый список отсортированным.
        registryMap.put(indexClass, sortedList);
    }

    /**
     * Проверяет, не подходит ли метод для приёма событий.
     * Проверки: ровно один параметр и наличие аннотации {@link EventTarget}.
     *
     * @param method
     *               метод для проверки.
     *
     * @return true — метод не должен получать события.
     */
    private boolean isMethodBad(Method method) {
        return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(EventTarget.class);
    }

    /**
     * Проверяет, не подходит ли метод для приёма указанного события.
     * Проверка: параметр метода — это именно класс нужного события.
     *
     * @param method
     *                   метод для проверки.
     * @param eventClass
     *                   класс события, для которого ищем слушателя.
     *
     * @return true — метод не должен получать это событие.
     */
    private boolean isMethodBad(Method method, Class<? extends Event> eventClass) {
        return isMethodBad(method) || !method.getParameterTypes()[0].equals(eventClass);
    }

    /**
     * Вызывает событие: берёт из реестра слушателей по классу события
     * и раздаёт им событие в порядке приоритета.
     *
     * @param event
     *              событие для диспатча.
     *
     * @return событие после диспатча (слушатели могли его изменить).
     */
    public Event call(final Event event) {
        List<MethodData> dataList = registryMap.get(event.getClass());

        if (dataList != null) {
            for (final MethodData data : dataList) {
                invoke(data, event);
            }
        }

        return event;
    }

    /**
     * Вызывает метод-слушатель, передавая ему событие как аргумент.
     *
     * @param data
     *                 данные метода, который вызываем.
     * @param argument
     *                 событие — аргумент метода.
     */
    private void invoke(MethodData data, Event argument) {
        try {
            data.getTarget().invoke(data.getSource(), argument);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Данные подписки: объект-владелец, метод-слушатель и приоритет.
     *
     * @author DarkMagician6 (оригинальное API)
     */
    private static final class MethodData {

        private final Object source;

        private final Method target;

        private final byte priority;

        /**
         * Заполняет данные.
         *
         * @param source
         *                 объект-владелец метода: VM знает, у кого его вызывать.
         * @param target
         *                 метод-слушатель, которому шлём событие.
         * @param priority
         *                 приоритет метода — по нему реестр сортирует список.
         */
        public MethodData(Object source, Method target, byte priority) {
            this.source = source;
            this.target = target;
            this.priority = priority;
        }

        /**
         * @return объект-владелец метода.
         */
        public Object getSource() {
            return source;
        }

        /**
         * @return метод, слушающий события.
         */
        public Method getTarget() {
            return target;
        }

        /**
         * @return приоритет метода.
         */
        public byte getPriority() {
            return priority;
        }

    }

}
