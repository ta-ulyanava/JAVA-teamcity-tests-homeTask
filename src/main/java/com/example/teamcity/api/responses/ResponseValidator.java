package com.example.teamcity.api.responses;

import io.restassured.response.Response;
import org.hamcrest.Matchers;

public class ResponseValidator {

    // Проверка статуса ответа
    public static void checkStatusCode(Response response, int expectedStatusCode) {
        response.then().assertThat().statusCode(expectedStatusCode);
    }

    // Универсальная валидация поля в теле ответа на разные типы значений (строка, число, булево)
    public static void validateResponseBody(Response response, String fieldName, Object expectedValue) {
        if (expectedValue instanceof String) {
            response.then().assertThat().body(fieldName, Matchers.equalTo(expectedValue));
        } else if (expectedValue instanceof Integer) {
            response.then().assertThat().body(fieldName, Matchers.equalTo(expectedValue));
        } else if (expectedValue instanceof Boolean) {
            response.then().assertThat().body(fieldName, Matchers.equalTo(expectedValue));
        } else if (expectedValue instanceof Double) {
            response.then().assertThat().body(fieldName, Matchers.equalTo(expectedValue));
        } else {
            throw new IllegalArgumentException("Unsupported data type for validation: " + expectedValue.getClass());
        }
    }

    // Валидация на отсутствие ошибок в теле ответа
    public static void validateNoErrors(Response response) {
        response.then().assertThat().body("errors", Matchers.nullValue());
    }

    // Валидация наличия обязательных полей
    public static void validateRequiredFields(Response response, String... requiredFields) {
        for (String field : requiredFields) {
            response.then().assertThat().body(Matchers.containsString("\"" + field + "\""));
        }
    }

    // Проверка успешного выполнения запроса по статусу
    public static void checkSuccessStatus(Response response, int expectedStatusCode ) {
        checkStatusCode(response, expectedStatusCode);
    }

    // Проверка на ошибку (например, 404)
    public static void checkErrorStatus(Response response, int expectedStatusCode) {
        checkStatusCode(response, expectedStatusCode);
    }
}
