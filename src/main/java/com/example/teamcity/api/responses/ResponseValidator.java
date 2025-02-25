package com.example.teamcity.api.responses;

import com.example.teamcity.api.models.BaseModel;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.asserts.SoftAssert;

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

    // Проверка успешного выполнения запроса
    public static void checkSuccessStatus(Response response, int expectedStatusCode) {
        checkStatusCode(response, expectedStatusCode);
    }

    // Проверка ошибки по статусу
    public static void checkErrorStatus(Response response, int expectedStatusCode) {
        checkStatusCode(response, expectedStatusCode);
    }

    // Проверка ошибки + содержимого тела
    public static void checkErrorAndBody(Response response, int expectedStatusCode, String... expectedBodyContents) {
        checkErrorStatus(response, expectedStatusCode);
        for(String expected : expectedBodyContents) {
            response.then().assertThat().body(Matchers.containsString(expected));
        }
    }


    // Универсальная проверка на ошибку с конкретным сообщением в теле
    public static void checkErrorWithMessage(Response response, int expectedStatusCode, String expectedMessage) {
        response.then().assertThat()
                .statusCode(expectedStatusCode)
                .body(Matchers.containsString(expectedMessage));
    }
}
