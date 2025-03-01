package com.example.teamcity.api.responses;
import io.restassured.response.Response;
import org.hamcrest.Matchers;

public class ResponseValidator {

//    public static void checkStatusCode(Response response, int expectedStatusCode) {
//        response.then().assertThat().statusCode(expectedStatusCode);
//    }

    public static void validateNoErrors(Response response) {
        response.then().assertThat().body("errors", Matchers.nullValue());
    }

    public static void validateRequiredFields(Response response, String... requiredFields) {
        for (String field : requiredFields) {
            response.then().assertThat().body(Matchers.containsString("\"" + field + "\""));
        }
    }

//    public static void checkSuccessStatus(Response response, int expectedStatusCode) {
//        checkStatusCode(response, expectedStatusCode);
//    }

//    public static void checkErrorStatus(Response response, int expectedStatusCode) {
//        checkStatusCode(response, expectedStatusCode);
//    }

//    public static void checkErrorAndBody(Response response, int expectedStatusCode, String... expectedBodyContents) {
//        checkErrorStatus(response, expectedStatusCode);
//        for(String expected : expectedBodyContents) {
//            response.then().assertThat().body(Matchers.containsString(expected));
//        }
//    }


    public static void checkErrorWithMessage(Response response, int expectedStatusCode, String expectedMessage) {
        response.then().assertThat()
                .statusCode(expectedStatusCode)
                .body(Matchers.containsString(expectedMessage));
    }
}
