package com.example.teamcity.api.requests.checked;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.Request;
import com.example.teamcity.api.requests.interfaces.CrudInterface;
import com.example.teamcity.api.requests.interfaces.SearchInterface;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Wrapper for API requests with response validation.
 * <p>
 * Performs CRUD and search operations while asserting correct status codes and extracting typed entities.
 *
 * @param <T> type of the entity extending {@link BaseModel}
 */
public final class CheckedBase<T extends BaseModel> extends Request implements CrudInterface, SearchInterface {

    private final UncheckedBase uncheckedBase;

    public CheckedBase(RequestSpecification spec, ApiEndpoint apiEndpoint) {
        super(spec, apiEndpoint);
        this.uncheckedBase = new UncheckedBase(spec, apiEndpoint);
    }

    @Step("Validate response for locator '{locator}'")
    private void validateResponse(Response response, String locator) {
        response.then().assertThat().statusCode(HttpStatus.SC_OK);
        if (response.getBody().asString().isEmpty()) {
            throw new IllegalStateException("Empty response for locator '%s'".formatted(locator));
        }
    }

    @Step("Extract single entity from response")
    private T extractEntity(Response response) {
        return response.as((Class<T>) apiEndpoint.getModelClass());
    }

    @Step("Extract entity list from response")
    private List<T> extractEntityList(Response response) {
        return response.jsonPath().getList(apiEndpoint.getJsonListKey(), (Class<T>) apiEndpoint.getModelClass());
    }


    /**
     * Sends a POST request to create the entity and validates the response.
     *
     * @param model entity to be created
     * @return raw API response
     */
    @Override
    @Step("Create entity: {model}")
    public Response create(BaseModel model) {
        Response response = uncheckedBase.create(model);
        validateResponse(response, model.toString());
        BaseModel createdModel = response.getBody().as(apiEndpoint.getModelClass());
        TestDataStorage.getInstance().addCreatedEntity(apiEndpoint, createdModel);
        return response;
    }

    /**
     * Sends a GET request to retrieve the entity by ID.
     *
     * @param id entity ID
     * @return deserialized entity
     */
    @Override
    @Step("Read entity by ID: {id}")
    public T read(String id) {
        Response response = uncheckedBase.read(id);
        validateResponse(response, id);
        return extractEntity(response);
    }

    /**
     * Sends a PUT request to update the entity and returns the updated version.
     *
     * @param id    entity ID
     * @param model updated entity data
     * @return updated entity
     */
    @Override
    @Step("Update entity with ID {id}: {model}")
    public T update(String id, BaseModel model) {
        Response response = uncheckedBase.update(id, model);
        validateResponse(response, id);
        return extractEntity(response);
    }

    /**
     * Sends a DELETE request to remove the entity by ID.
     *
     * @param id entity ID
     * @return raw response body as string
     */
    @Override
    @Step("Delete entity by ID: {id}")
    public Object delete(String id) {
        Response response = uncheckedBase.delete(id);
        validateResponse(response, id);
        return response.asString();
    }

    /**
     * Finds entities using a locator with pagination.
     *
     * @param locator locator query string
     * @return list of matching entities
     */
    @Override
    @Step("Find entities by locator with pagination: {locator}")
    public List<T> findEntitiesByLocatorQueryWithPagination(String locator) {
        Response response = uncheckedBase.findEntitiesByLocatorQueryWithPagination(locator);
        validateResponse(response, locator);
        return extractEntityList(response);
    }

    /**
     * Reads all entities with pagination.
     *
     * @return full list of entities
     */
    @Override
    @Step("Read all entities with pagination")
    public List<T> readEntitiesQueryWithPagination() {
        Response response = uncheckedBase.readEntitiesQueryWithPagination();
        validateResponse(response, "all");
        return extractEntityList(response);
    }

    /**
     * Reads entities using pagination with limit and offset.
     *
     * @param limit  max number of items
     * @param offset offset index
     * @return list of entities
     */
    @Override
    @Step("Read entities with limit={limit}, offset={offset}")
    public List<T> readEntitiesQueryWithPagination(int limit, int offset) {
        Response response = uncheckedBase.readEntitiesQueryWithPagination(limit, offset);
        validateResponse(response, "all");
        return extractEntityList(response);
    }

    /**
     * Reads an entity using a path parameter as ID.
     *
     * @param pathParam path param value
     * @return entity found
     */
    @Override
    @Step("Find entity by path param: {pathParam}")
    public T findEntityByPathParam(String pathParam) {
        Response response = uncheckedBase.read(pathParam);
        validateResponse(response, pathParam);
        return extractEntity(response);
    }

    /**
     * Finds entities by locator using limit and offset.
     *
     * @param locator locator string
     * @param limit   max number of items
     * @param offset  offset index
     * @return list of entities
     */
    @Override
    @Step("Find entities by locator '{locator}' with limit={limit}, offset={offset}")
    public List<T> findEntitiesByLocatorQueryWithPagination(String locator, int limit, int offset) {
        if (limit == 0) {
            return Collections.emptyList();
        }
        Response response = uncheckedBase.findEntitiesByLocatorQueryWithPagination(locator, limit, offset);
        validateResponse(response, locator);
        return extractEntityList(response);
    }

    /**
     * Finds the first entity matching a locator query.
     *
     * @param locator locator query
     * @return optional entity if found
     */
    @Step("Find first entity by locator: {locator}")
    public Optional<T> findFirstEntityByLocatorQuery(String locator) {
        Response response = uncheckedBase.findFirstEntityByLocatorQuery(locator);
        validateResponse(response, locator);
        List<T> entities = response.jsonPath().getList(apiEndpoint.getJsonListKey(), (Class<T>) apiEndpoint.getModelClass());
        return entities.isEmpty() ? Optional.empty() : Optional.of(entities.get(0));
    }

}
