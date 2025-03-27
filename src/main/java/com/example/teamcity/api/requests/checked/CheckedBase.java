package com.example.teamcity.api.requests.checked;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.interfaces.CrudInterface;
import com.example.teamcity.api.requests.Request;
import com.example.teamcity.api.requests.interfaces.SearchInterface;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class CheckedBase<T extends BaseModel> extends Request implements CrudInterface, SearchInterface {
    private final UncheckedBase uncheckedBase;

    public CheckedBase(RequestSpecification spec, ApiEndpoint apiEndpoint) {
        super(spec, apiEndpoint);
        this.uncheckedBase = new UncheckedBase(spec, apiEndpoint);
    }

    private void validateResponse(Response response, String locator) {
        response.then().assertThat().statusCode(HttpStatus.SC_OK);
        if (response.getBody().asString().isEmpty()) {
            throw new IllegalStateException("Empty response for locator '%s'".formatted(locator));
        }
    }

    private T extractEntity(Response response) {
        return response.as((Class<T>) apiEndpoint.getModelClass());
    }

    private List<T> extractEntityList(Response response) {
        return response.jsonPath().getList("project", (Class<T>) apiEndpoint.getModelClass());
    }

    @Override
    public Response create(BaseModel model) {
        Response response = uncheckedBase.create(model);
        validateResponse(response, model.toString());
        BaseModel createdModel = response.getBody().as(apiEndpoint.getModelClass());
        TestDataStorage.getInstance().addCreatedEntity(apiEndpoint, createdModel);
        return response;
    }

    @Override
    public T read(String id) {
        Response response = uncheckedBase.read(id);
        validateResponse(response, id);
        return extractEntity(response);
    }

    @Override
    public T update(String id, BaseModel model) {
        Response response = uncheckedBase.update(id, model);
        validateResponse(response, id);
        return extractEntity(response);
    }

    @Override
    public Object delete(String id) {
        Response response = uncheckedBase.delete(id);
        validateResponse(response, id);
        return response.asString();
    }

    //    public Optional<T> findSingleByLocator(String locator) {
//        Response response = uncheckedBase.findSingleByLocator(locator);
//        validateResponse(response, locator);
//        return Optional.of(response.as((Class<T>) apiEndpoint.getModelClass()));
//    }
//    @Override
//    public Optional<T> findFirstEntityByLocatorQuery(String locator) {
//        Response response = uncheckedBase.findFirstEntityByLocatorQuery(locator);
//        validateResponse(response, locator);
//        List<T> projects = response.jsonPath().getList("project", (Class<T>) apiEndpoint.getModelClass());
//        if (projects.isEmpty()) return Optional.empty();
//        return Optional.of(projects.get(0));
//    }

    @Override
    public List<T> findEntitiesByLocatorQueryWithPagination(String locator) {
        Response response = uncheckedBase.findEntitiesByLocatorQueryWithPagination(locator);
        validateResponse(response, locator);
        return extractEntityList(response);
    }


//    @Override
//    public List<T> findEntitiesByLocatorQueryWithPagination(String locator, int limit, int offset) {
//        Response response = uncheckedBase.findEntitiesByLocatorQueryWithPagination(locator, limit, offset);
//        validateResponse(response, locator);
//        return extractEntityList(response);
//    }

    @Override
    public List<T> readEntitiesQueryWithPagination() {
        Response response = uncheckedBase.readEntitiesQueryWithPagination();
        validateResponse(response, "all");
        return extractEntityList(response);
    }

    @Override
    public List<T> readEntitiesQueryWithPagination(int limit, int offset) {
        Response response = uncheckedBase.readEntitiesQueryWithPagination(limit, offset);
        validateResponse(response, "all");
        return extractEntityList(response);
    }
    @Override
    public T findEntityByPathParam(String pathParam) {
        Response response = uncheckedBase.read(pathParam);
        validateResponse(response, pathParam);
        return extractEntity(response);
    }

    @Override
    public List<T> findEntitiesByLocatorQueryWithPagination(String locator, int limit, int offset) {
        if (limit == 0) {
            return Collections.emptyList();
        }
        
        Response response = uncheckedBase.findEntitiesByLocatorQueryWithPagination(locator, limit, offset);
        validateResponse(response, locator);
        return extractEntityList(response);
    }

    @Override
    public Optional<T> findFirstEntityByLocatorQuery(String locator) {
        Response response = uncheckedBase.findFirstEntityByLocatorQuery(locator);
        validateResponse(response, locator);

        // Проверяем, что count не равен 0
        int count = response.jsonPath().getInt("count");
        if (count == 0) {
            return Optional.empty(); // Возвращаем пустой Optional, если данных нет
        }

        List<T> projects = response.jsonPath().getList("project", (Class<T>) apiEndpoint.getModelClass());
        return projects.isEmpty() ? Optional.empty() : Optional.of(projects.get(0));
    }


}
