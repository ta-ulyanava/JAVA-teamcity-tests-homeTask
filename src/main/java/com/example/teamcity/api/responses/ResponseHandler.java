package com.example.teamcity.api.responses;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.teamcity.api.models.Identifiable;
import org.testng.asserts.SoftAssert;

public class ResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    // Логирование деталей ответа
    public static void logResponseDetails(Response response) {
        logger.info("Status Code: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.getBody().asString());
    }

    public static void logIfError(Response response) {
        int statusCode = response.getStatusCode();

        // Проверяем, входит ли статус в успешный диапазон 2xx (200–299)
        if (statusCode < HttpStatus.SC_OK || statusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
            logger.error("❌ API ERROR: Status Code: {}", statusCode);
            logger.error("Response Body: {}", response.getBody().asString());
        }
    }

    // Универсальная валидация поля в теле ответа на разные типы значений (строка, число, булевы)
    public static void validateResponseBody(Response response, String fieldName, Object expectedValue) {
        try {
            if (expectedValue instanceof String) {
                response.then().assertThat().body(fieldName, org.hamcrest.Matchers.equalTo(expectedValue));
            } else if (expectedValue instanceof Integer) {
                response.then().assertThat().body(fieldName, org.hamcrest.Matchers.equalTo(expectedValue));
            } else if (expectedValue instanceof Boolean) {
                response.then().assertThat().body(fieldName, org.hamcrest.Matchers.equalTo(expectedValue));
            } else if (expectedValue instanceof Double) {
                response.then().assertThat().body(fieldName, org.hamcrest.Matchers.equalTo(expectedValue));
            } else {
                throw new IllegalArgumentException("Unsupported data type for validation: " + expectedValue.getClass());
            }
        } catch (AssertionError | IllegalArgumentException e) {
            logger.error("Validation failed for field: {}, expected value: {}. Error: {}", fieldName, expectedValue, e.getMessage());
            throw e;  // Пробрасываем ошибку дальше
        }
    }

    // Извлечение модели данных из тела ответа
    public static <T> T extractModel(Response response, Class<T> modelClass) {
        logResponseDetails(response);  // Логируем ответ
        return response.getBody().as(modelClass);  // Преобразуем в объект
    }

    // Извлечение строки из ответа (если нужно)
    public static String extractString(Response response) {
        logResponseDetails(response);
        return response.getBody().asString();
    }

    // Извлечение объекта из ответа и логирование
    public static <T> T extractAndLogModel(Response response, Class<T> modelClass) {
        logResponseDetails(response);  // Логируем ответ
        return extractModel(response, modelClass);  // Извлекаем модель
    }

    // Валидация сущности с помощью TestValidator
    public static <T extends Identifiable> void validateEntityFields(T expected, T actual, SoftAssert softy) {
        TestValidator.validateEntityFields(expected, actual, softy);  // Используем универсальную валидацию
    }
}
