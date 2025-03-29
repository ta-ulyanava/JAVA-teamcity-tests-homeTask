package com.example.teamcity.api.requests.unchecked;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.Request;
import com.example.teamcity.api.requests.interfaces.CrudInterface;
import com.example.teamcity.api.requests.interfaces.SearchInterface;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;

public class UncheckedBase extends Request implements CrudInterface, SearchInterface {

    public UncheckedBase(RequestSpecification spec, ApiEndpoint apiEndpoint) {
        super(spec, apiEndpoint);
    }

    @Override
    @Step("Create entity: {model}")
    public Response create(BaseModel model) {
        return RestAssured
                .given()
                .spec(spec)
                .body(model)
                .post(apiEndpoint.getUrl());
    }

    @Step("Create entity from raw body")
    public Response create(String body) {
        return RestAssured
                .given()
                .spec(spec)
                .body(body)
                .post(apiEndpoint.getUrl());
    }

    @Override
    @Step("Read entity by ID or locator: {idOrLocator}")
    public Response read(String idOrLocator) {
        if (idOrLocator.contains(":")) {
            return findFirstEntityByLocatorQuery(idOrLocator);
        }
        return RestAssured
                .given()
                .spec(spec)
                .get(apiEndpoint.getUrl() + "/" + idOrLocator);
    }

    @Override
    @Step("Update entity with locator {locator}")
    public Response update(String locator, BaseModel model) {
        return RestAssured
                .given()
                .spec(spec)
                .body(model)
                .put(apiEndpoint.getUrl() + "/" + locator);
    }

    @Override
    @Step("Delete entity with locator {locator}")
    public Response delete(String locator) {
        return RestAssured
                .given()
                .spec(spec)
                .delete(apiEndpoint.getUrl() + "/" + locator);
    }

    @Override
    @Step("Find first entity by locator: {locator}")
    public Response findFirstEntityByLocatorQuery(String locator) {
        return RestAssured
                .given()
                .spec(spec)
                .queryParam("locator", locator)
                .get(apiEndpoint.getUrl());
    }

    @Override
    @Step("Find entities by locator with pagination: {locator}, default limit and offset")
    public Response findEntitiesByLocatorQueryWithPagination(String locator) {
        return findEntitiesByLocatorQueryWithPagination(locator, 100, 0);
    }

    @Override
    @Step("Find entities by locator: {locator}, limit: {limit}, offset: {offset}")
    public Response findEntitiesByLocatorQueryWithPagination(String locator, int limit, int offset) {
        return RestAssured
                .given()
                .spec(spec)
                .queryParam("locator", locator)
                .queryParam("count", limit)
                .queryParam("start", offset)
                .get(apiEndpoint.getUrl());
    }

    @Override
    @Step("Read all entities with default pagination")
    public Response readEntitiesQueryWithPagination() {
        return readEntitiesQueryWithPagination(100, 0);
    }

    @Override
    @Step("Read all entities with pagination - limit: {limit}, offset: {offset}")
    public Response readEntitiesQueryWithPagination(int limit, int offset) {
        return RestAssured
                .given()
                .spec(spec)
                .queryParam("count", limit)
                .queryParam("start", offset)
                .get(apiEndpoint.getUrl());
    }

    @Override
    @Step("Find entity by path param: {pathParam}")
    public Response findEntityByPathParam(String pathParam) {
        return RestAssured
                .given()
                .spec(spec)
                .get(apiEndpoint.getUrl() + "/" + pathParam);
    }

    @Step("Find all entities by locator: {locator}, limit: {limit}, offset: {offset}")
    public <T extends BaseModel> List<T> findAllEntitiesByLocator(String locator, int limit, int offset) {
        Response response = findEntitiesByLocatorQueryWithPagination(locator, limit, offset);
        List<T> entities = response.jsonPath().getList("project", (Class<T>) apiEndpoint.getModelClass());
        return entities != null ? entities : new ArrayList<>();
    }
}
