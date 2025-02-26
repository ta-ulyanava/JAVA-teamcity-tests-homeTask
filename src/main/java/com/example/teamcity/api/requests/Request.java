package com.example.teamcity.api.requests;

import com.example.teamcity.api.enums.Endpoint;
import io.restassured.specification.RequestSpecification;

/**
 * Request - это класс, описывающий меняющиеся параметры запроса,
 * такие как: спецификация, эндпоинд (relative URL, model)
 */
public class Request {

    protected final RequestSpecification spec;
    protected final Endpoint endpoint;

    public Request (RequestSpecification spec, Endpoint endpoint){
        this.spec=spec;
        this.endpoint=endpoint;
    }
}
