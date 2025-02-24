package com.example.teamcity.api.responses;

import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.apache.http.HttpStatus;

public class ResponseValidator {

    // Проверка статуса ответа
    public static void checkStatusCode(Response response, int expectedStatusCode) {
        response.then().assertThat().statusCode(expectedStatusCode);
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

    // Новый метод для проверки ошибки и тела
    public static void checkErrorAndBody(Response response, int expectedStatusCode, String expectedBodyContent) {
        checkErrorStatus(response, expectedStatusCode); // Проверка статуса ошибки
        response.then().assertThat()
                .body(Matchers.containsString(expectedBodyContent)); // Проверка содержания в теле ошибки
    }
}
