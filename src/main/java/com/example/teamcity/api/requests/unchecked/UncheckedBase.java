package com.example.teamcity.api.requests.unchecked;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.interfaces.CrudInterface;
import com.example.teamcity.api.requests.Request;
import com.example.teamcity.api.requests.interfaces.SearchInterface;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

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
            // Если это локатор, делегируем findSingleByLocator
            return findSingleByLocator(idOrLocator);
        } else {
            // Если это ID, используем как часть пути
            return RestAssured
                    .given()
                    .spec(spec)
                    .get(apiEndpoint.getUrl() + "/" + idOrLocator);
        }
    }

    @Override
    public Response update(String locator, BaseModel model) {
        return RestAssured
                .given()
                .body(model)
                .spec(spec)
                .put(apiEndpoint.getUrl() + "/" + locator);
    }

    @Override
    public Response delete(String locator) {
        return RestAssured
                .given()
                .spec(spec)
                .delete(apiEndpoint.getUrl() + "/" + locator);
    }

    // --- Реализация SearchInterface ---//
    @Override
    public Response findSingleByLocator(String locator) {
        // Всегда используем локатор как параметр запроса
        return RestAssured
                .given()
                .spec(spec)
                .get(apiEndpoint.getUrl() + "?locator=" + locator);
    }

    @Override
    public Response readAll() {
        return RestAssured.given().spec(spec).get(apiEndpoint.getUrl());
    }

    @Override
    public Response findAllByLocator(String criteria) {
        return findSingleByLocator(criteria);
    }
}
