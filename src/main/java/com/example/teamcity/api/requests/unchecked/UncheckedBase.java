package com.example.teamcity.api.requests.unchecked;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.CrudInterface;
import com.example.teamcity.api.requests.Request;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Класс, описывающий любой CRUD endpoint для непроверяемых запросов.
 * Непроверяемый запрос- без асертов, отправили и "что получили, то получили"
 * По нему мы будем генерировать endpoints для конкретного объекта и для конкретного типа юзера
 */

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

    @Override
    public Response read(String id) {
        return RestAssured
                .given()

                .spec(spec)

                .get(endpoint.getUrl() + "/id:" + id);

    }

    @Override
    public Response update(String id, BaseModel model) {
        return RestAssured
                .given()
                .body(model)
                .spec(spec)
                .put(endpoint.getUrl() + "/id:" + id);
    }

    @Override
    public Response delete(String id) {
        return RestAssured
                .given()
                .spec(spec)
                .delete(endpoint.getUrl() + "/id:" + id);
    }
}
