package com.example.teamcity.api.responses;

import io.restassured.response.Response;

public class ResponseExtractor {
    public static <T> T extractModel(Response response, Class<T> modelClass) {
        return response.getBody().as(modelClass);
    }
}
