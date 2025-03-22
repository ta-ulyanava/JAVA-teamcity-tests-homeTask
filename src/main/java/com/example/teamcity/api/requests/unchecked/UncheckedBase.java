package com.example.teamcity.api.requests.unchecked;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.interfaces.CrudInterface;
import com.example.teamcity.api.requests.Request;
import com.example.teamcity.api.requests.interfaces.SearchInterface;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;

public class UncheckedBase extends Request implements CrudInterface, SearchInterface {

    public UncheckedBase(RequestSpecification spec, ApiEndpoint apiEndpoint) {
        super(spec, apiEndpoint);
    }

    @Override
    public Response create(BaseModel model) {
        return RestAssured
                .given()
                .spec(spec)
                .body(model)
                .post(apiEndpoint.getUrl());
    }

    public Response create(String body) {
        return RestAssured
                .given()
                .spec(spec)
                .body(body)
                .post(apiEndpoint.getUrl());
    }

    @Override
    public Response read(String idOrLocator) {
        if (idOrLocator.contains(":")) {
            return findSingleByLocator(idOrLocator); // Если передан локатор, используем поиск
        }
        return RestAssured
                .given()
                .spec(spec)
                .get(apiEndpoint.getUrl() + "/" + idOrLocator); // Если передан ID, используем как путь
    }


    @Override
    public Response update(String locator, BaseModel model) {
        return RestAssured
                .given()
                .spec(spec)
                .body(model)
                .put(apiEndpoint.getUrl() + "/" + locator);
    }

    @Override
    public Response delete(String locator) {
        return RestAssured
                .given()
                .spec(spec)
                .delete(apiEndpoint.getUrl() + "/" + locator);
    }
    // --- Методы поиска --- //

    @Override
    public Response findSingleByLocator(String locator) {
        return RestAssured
                .given()
                .spec(spec)
                .queryParam("locator", locator)
                .get(apiEndpoint.getUrl());
    }

    @Override
    public Response findAllByLocator(String locator) {
        return findAllByLocator(locator, 100, 0);
    }

    @Override
    public Response findAllByLocator(String locator, int limit, int offset) {
        return RestAssured
                .given()
                .spec(spec)
                .queryParam("locator", locator)
                .queryParam("count", limit)
                .queryParam("start", offset)
                .get(apiEndpoint.getUrl());
    }

    @Override
    public Response readAll() {
        return readAll(100, 0);
    }

    @Override
    public Response readAll(int limit, int offset) {
        return RestAssured
                .given()
                .spec(spec)
                .queryParam("count", limit)
                .queryParam("start", offset)
                .get(apiEndpoint.getUrl());
    }

    public <T extends BaseModel> List<T> findAllEntitiesByLocator(String locator, int limit, int offset) {
        Response response = findAllByLocator(locator, limit, offset);
        return response.jsonPath().getList("", (Class<T>) apiEndpoint.getModelClass());
    }
}
