package com.example.teamcity.api.spec.responce;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;


public class AccessErrorSpecs {

    public static ResponseSpecification authenticationRequired() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .expectBody(Matchers.containsString("Authentication required"))
                .build();
    }

    public static ResponseSpecification accessDenied() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .expectBody(Matchers.containsString("Access denied. Check the user has enough permissions to perform the operation."))
                .build();
    }

}

