package yarnternal.lol.api.systems.style;

/**
 * Стиль клиента — одна тема: имя и две краски, между которыми бежит полоса.
 * Тема — это данные: экземпляры собираются списком в главном классе
 * (Yarnternal#initStyles), а выбранную тему и её краски клиент держит там же.
 *
 * @param name     имя темы — стабильный ключ: по нему тема ищется в конфиге, имена не должны повторяться
 * @param colorOne первая краска, hex-строка "#RRGGBB"
 * @param colorTwo вторая краска — та, в которую перетекает первая
 * @param isLight  светлая ли тема — чтобы решать, тёмным или светлым рисовать поверх
 */
public record Style(String name, String colorOne, String colorTwo, boolean isLight) {
}
