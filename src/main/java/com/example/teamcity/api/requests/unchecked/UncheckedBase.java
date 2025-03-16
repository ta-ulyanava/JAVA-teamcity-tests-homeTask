package com.example.teamcity.api.requests.unchecked;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.CrudInterface;
import com.example.teamcity.api.requests.Request;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class UncheckedBase extends Request implements CrudInterface {
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
    public Response read(String locator) {
        if (locator.contains(":")) {
            // Если это локатор (содержит :), используем параметр запроса
            return RestAssured
                    .given()
                    .spec(spec)
                    .get(apiEndpoint.getUrl() + "?locator=" + locator);
        } else {
            // Если это ID, добавляем как часть пути
            return RestAssured
                    .given()
                    .spec(spec)
                    .get(apiEndpoint.getUrl() + "/" + locator);
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
}
