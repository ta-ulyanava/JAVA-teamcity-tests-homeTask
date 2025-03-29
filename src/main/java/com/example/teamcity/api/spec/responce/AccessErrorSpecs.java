package com.example.teamcity.api.spec.responce;

import io.qameta.allure.Step;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

/**
 * Provides reusable response specifications for access-related errors in TeamCity API.
 */
public class AccessErrorSpecs {

    /**
     * Expects 401 Unauthorized with message "Authentication required".
     *
     * @return response specification for unauthenticated access
     */
    @Step("Expect authentication required error (401)")
    public static ResponseSpecification authenticationRequired() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .expectBody(Matchers.containsString("Authentication required"))
                .build();
    }

    /**
     * Expects 403 Forbidden with standard access denied message.
     *
     * @return response specification for access denied error
     */
    @Step("Expect access denied error (403)")
    public static ResponseSpecification accessDenied() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .expectBody(Matchers.containsString(
                        "Access denied. Check the user has enough permissions to perform the operation."))
                .build();
    }
}
