package com.example.teamcity.api.spec;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public class ErrorResponseSpecifications {

    public static ResponseSpecification entityNotFound() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody("message", Matchers.containsString("Nothing is found by locator"))
                .build();
    }

    public static ResponseSpecification entityNotFound(String entityType, String id) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody(Matchers.containsString("%s cannot be found by external id '%s'".formatted(entityType, id)))
                .build();
    }
//
//    public static ResponseSpecification authenticationRequired() {
//        return new ResponseSpecBuilder()
//                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
//                .expectBody("message", Matchers.containsString("Authentication required"))
//                .build();
//    }



    public static ResponseSpecification badRequest(String message) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody("message", Matchers.containsString(message))
                .build();
    }
}
