package com.example.teamcity.api.requests.unchecked;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.CrudInterface;
import com.example.teamcity.api.requests.Request;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

public class UncheckedBase extends Request implements CrudInterface {
    public UncheckedBase(RequestSpecification spec, Endpoint endpoint) {
        super(spec, endpoint);
    }

    @Override
    public Response create(BaseModel model) {
        return RestAssured
                .given()
                .spec(spec)
                .body(model)
                .post(endpoint.getUrl());
    }

    public Response create(String body) {
        return RestAssured
                .given()
                .spec(spec)
                .body(body)
                .post(endpoint.getUrl());
    }

    @Override
    public Response read(String locator) {
        if (locator.contains(":")) {
            // Если это локатор (содержит :), используем параметр запроса
            return RestAssured
                    .given()
                    .spec(spec)
                    .get(endpoint.getUrl() + "?locator=" + locator);
        } else {
            // Если это ID, добавляем как часть пути
            return RestAssured
                    .given()
                    .spec(spec)
                    .get(endpoint.getUrl() + "/" + locator);
        }
    }

    @Override
    public Response update(String locator, BaseModel model) {
        return RestAssured
                .given()
                .body(model)
                .spec(spec)
                .put(endpoint.getUrl() + "/" + locator);
    }

    @Override
    public Response delete(String locator) {
        return RestAssured
                .given()
                .spec(spec)
                .delete(endpoint.getUrl() + "/" + locator);
    }
}
