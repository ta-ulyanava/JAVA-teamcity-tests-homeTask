package com.example.teamcity.api.responses;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.List;

/**
 * Utility class for extracting model objects from REST-assured responses.
 */
public class ResponseExtractor {

    /**
     * Extracts a single model object from the response body.
     *
     * @param response   the REST-assured response
     * @param modelClass the class of the model to extract
     * @param <T>        the type of the model
     * @return extracted model instance
     */
    @Step("Extract single model of type {modelClass}")
    public static <T> T extractModel(Response response, Class<T> modelClass) {
        return response.getBody().as(modelClass);
    }

    /**
     * Extracts a list of model objects from the response body.
     *
     * @param response   the REST-assured response
     * @param modelClass the class of the model to extract
     * @param <T>        the type of the model
     * @return list of extracted models
     */
    @Step("Extract list of models of type {modelClass}")
    public static <T> List<T> extractModelList(Response response, Class<T> modelClass) {
        return response.jsonPath().getList(".", modelClass);
    }
}
