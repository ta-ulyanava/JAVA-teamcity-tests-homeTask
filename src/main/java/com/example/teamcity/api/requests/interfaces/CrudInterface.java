package com.example.teamcity.api.requests.interfaces;

import com.example.teamcity.api.models.BaseModel;

/**
 * Interface for basic CRUD operations on TeamCity entities.
 */
public interface CrudInterface {

    /**
     * Sends a POST request to create a new entity.
     *
     * @param model entity to create
     * @return response object or created entity
     */
    Object create(BaseModel model);

    /**
     * Sends a GET request to read an entity by its ID.
     *
     * @param id identifier of the entity
     * @return response object or found entity
     */
    Object read(String id);

    /**
     * Sends a PUT request to update an existing entity.
     *
     * @param id    identifier of the entity
     * @param model updated entity data
     * @return response object or updated entity
     */
    Object update(String id, BaseModel model);

    /**
     * Sends a DELETE request to remove an entity by ID.
     *
     * @param id identifier of the entity
     * @return response object or confirmation string
     */
    Object delete(String id);
}
