package com.example.teamcity.api.requests.unchecked;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.interfaces.CrudInterface;
import com.example.teamcity.api.requests.Request;
import com.example.teamcity.api.requests.interfaces.SearchInterface;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
            return findFirstEntityByLocatorQuery(idOrLocator); // Если передан локатор, используем поиск
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
    public Response findFirstEntityByLocatorQuery(String locator) {
        return RestAssured
                .given()
                .spec(spec)
                .queryParam("locator", locator)
                .get(apiEndpoint.getUrl());
    }

    @Override
    public Response findEntitiesByLocatorQueryWithPagination(String locator) {
        return findEntitiesByLocatorQueryWithPagination(locator, 100, 0);
    }

    @Override
    public Response findEntitiesByLocatorQueryWithPagination(String locator, int limit, int offset) {
        // Выполняем запрос с пагинацией
        Response response = RestAssured
                .given()
                .spec(spec)
                .queryParam("locator", locator)
                .queryParam("count", limit)
                .queryParam("start", offset)
                .get(apiEndpoint.getUrl());

        // Проверяем, если count == 0, выбрасываем ошибку
        if (response.jsonPath().getInt("count") == 0) {
            throw new NoSuchElementException("No entities found for locator: " + locator); // выбрасываем исключение
        }

        // Возвращаем обычный ответ, если есть данные
        return response;
    }



    @Override
    public Response readEntitiesQueryWithPagination() {
        return readEntitiesQueryWithPagination(100, 0);
    }

    @Override
    public Response readEntitiesQueryWithPagination(int limit, int offset) {
        return RestAssured
                .given()
                .spec(spec)
                .queryParam("count", limit)
                .queryParam("start", offset)
                .get(apiEndpoint.getUrl());
    }
    @Override
    public Response findEntityByPathParam(String pathParam) {
        return RestAssured
                .given()
                .spec(spec)
                .get(apiEndpoint.getUrl() + "/" + pathParam);
    }


    public <T extends BaseModel> List<T> findAllEntitiesByLocator(String locator, int limit, int offset) {
        Response response = findEntitiesByLocatorQueryWithPagination(locator, limit, offset);

        // Проверяем если count == 0
        if (response.jsonPath().getInt("count") == 0) {
            return new ArrayList<>(); // Возвращаем пустой список
        }

        // Если count больше 0, извлекаем список сущностей
        return response.jsonPath().getList("project", (Class<T>) apiEndpoint.getModelClass());
    }


}
