package com.example.teamcity.api.requests.interfaces;

/**
 * Универсальный интерфейс для поиска и чтения сущностей через API TeamCity.
 */
public interface SearchInterface<T> {

    /**
     * Получить одну сущность по locator-параметру.
     * Пример: ?locator=name:Project123
     */
    Object findFirstEntityByLocatorQuery(String locator);

    /**
     * Получить все сущности, соответствующие locator-параметру.
     */
    Object findEntitiesByLocatorQueryWithPagination(String locator);

    /**
     * Получить все сущности, соответствующие locator-параметру, с пагинацией.
     */
    Object findEntitiesByLocatorQueryWithPagination(String locator, int limit, int offset);

    /**
     * Прочитать все сущности без фильтрации (в пределах лимита по умолчанию).
     */
    Object readEntitiesQueryWithPagination();

    /**
     * Прочитать все сущности с пагинацией (count + start).
     */
    Object readEntitiesQueryWithPagination(int limit, int offset);
    /**
     * Выполняет поиск сущности по path-параметру, например:
     * GET /app/rest/projects/name:<имя>
     *
     * Используется, когда API поддерживает доступ по конкретному параметру пути.
     *
     * @param pathParam параметр пути (например, "name:Build")
     * @return найденная сущность
     */
    T findEntityByPathParam(String pathParam);

}
