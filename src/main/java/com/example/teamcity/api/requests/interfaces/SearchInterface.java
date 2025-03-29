package com.example.teamcity.api.requests.interfaces;

/**
 * Generic interface for searching and reading entities from the TeamCity API.
 *
 * @param <T> the type of entity returned by path parameter lookup
 */
public interface SearchInterface<T> {

    /**
     * Finds the first entity that matches the given locator query.
     * Example: ?locator=name:Project123
     *
     * @param locator locator query string
     * @return the first matched entity or empty result
     */
    Object findFirstEntityByLocatorQuery(String locator);

    /**
     * Finds all entities matching the given locator query.
     *
     * @param locator locator query string
     * @return list of matched entities
     */
    Object findEntitiesByLocatorQueryWithPagination(String locator);

    /**
     * Finds entities matching the locator with limit and offset (pagination).
     *
     * @param locator locator query string
     * @param limit   maximum number of results to return
     * @param offset  offset index for pagination
     * @return paginated list of matched entities
     */
    Object findEntitiesByLocatorQueryWithPagination(String locator, int limit, int offset);

    /**
     * Reads all entities without filtering (within default API limits).
     *
     * @return list of all entities
     */
    Object readEntitiesQueryWithPagination();

    /**
     * Reads entities with pagination using limit and offset.
     *
     * @param limit  maximum number of results
     * @param offset offset index for pagination
     * @return paginated list of all entities
     */
    Object readEntitiesQueryWithPagination(int limit, int offset);

    /**
     * Finds a specific entity using a path parameter.
     * Example: GET /app/rest/projects/name:<name>
     *
     * @param pathParam path parameter (e.g., "name:Build")
     * @return found entity
     */
    T findEntityByPathParam(String pathParam);
}
