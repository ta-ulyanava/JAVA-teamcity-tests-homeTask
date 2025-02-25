package com.example.teamcity.api.responses;

import com.example.teamcity.api.models.Identifiable;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.asserts.SoftAssert;
import java.util.function.Function;

public class TestValidator {

    public static <T extends Identifiable> void validateEntityFields(T expected, T actual, SoftAssert softAssert) {
        softAssert.assertEquals(actual.getId(), expected.getId(), "ID не совпадает");
        softAssert.assertEquals(actual.getName(), expected.getName(), "Имя не совпадает");
    }

    public static <T> void validateFieldValueFromResponse(Response response, Class<T> modelClass, Function<T, String> fieldAccessor, String expectedValue, SoftAssert softAssert) {
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        T model = ResponseExtractor.extractModel(response, modelClass);
        softAssert.assertEquals(fieldAccessor.apply(model), expectedValue, "Field value mismatch");
    }
}
