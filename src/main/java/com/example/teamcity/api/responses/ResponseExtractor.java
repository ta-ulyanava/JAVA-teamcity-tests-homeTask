package com.example.teamcity.api.responses;

import io.restassured.response.Response;
import java.util.List;

public class ResponseExtractor {
    public static <T> T extractModel(Response response, Class<T> modelClass) {
        return response.getBody().as(modelClass);
    }

    public static <T> List<T> extractModelList(Response response, Class<T> modelClass) {
        return response.jsonPath().getList(".", modelClass);
    }
}
