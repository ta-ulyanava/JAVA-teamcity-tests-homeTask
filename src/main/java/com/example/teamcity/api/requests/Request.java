package com.example.teamcity.api.requests;

import com.example.teamcity.api.enums.Endpoint;
import io.restassured.specification.RequestSpecification;

// Класс, в который мы вынесли все меняющиеся параметры запросов (Базовая логика работы с Rest Assured):
// 1 эндпоинты
// 2 саму меняющуюся сущность
// 3 спецификацию тк в ней указано кто отправил запрос (админ, гость и тп) тк в ней есть метод авторизации
public class Request {
    /**
     * Request - это класс, описывающий меняющиеся параметры запроса,
     * такие как: спецификация, эндпоинд (relative URL, model)
     */
    protected final RequestSpecification spec;
    protected final Endpoint endpoint;

    public Request (RequestSpecification spec, Endpoint endpoint){
        this.spec=spec;
        this.endpoint=endpoint;
    }
}
