package yarnternal.lol.api.event;

import java.lang.annotation.*;

import yarnternal.lol.api.event.types.Priority;

/**
 * Помечает метод-слушатель события: ровно один параметр — класс события.
 * Метод может быть приватным — менеджер сам открывает к нему доступ.
 *
 * @see EventManager#register(Object)
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventTarget {

    /**
     * Приоритет метода: чем меньше значение, тем раньше он вызывается.
     *
     * @see Priority
     */
    byte value() default Priority.MEDIUM;
}
