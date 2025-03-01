package com.example.teamcity.api.spec;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public class ValidationResponseSpecifications {

    public static ResponseSpecification checkProjectWithNameAlreadyExists(String projectName) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.containsString("Project with this name already exists: " + projectName))
                .build();
    }
    public static ResponseSpecification checkBadRequest() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build();
    }
    public static ResponseSpecification checkProjectWithIdAlreadyExists(String projectId) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.containsString("Project ID \"" + projectId + "\" is already used by another project"))
                .build();
    }

    public static ResponseSpecification checkProjectNotFound(String projectId) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody(Matchers.containsString("Project cannot be found by external id '" + projectId + "'"))
                .build();
    }

    public static ResponseSpecification checkUnauthorizedAccess() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .expectBody(Matchers.containsString("Authentication required"))
                .build();
    }

    public static ResponseSpecification checkForbiddenAccess() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .expectBody(Matchers.containsString("Access denied"))
                .build();
    }

    public static ResponseSpecification checkInvalidCopySettings() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.containsString("Cannot deserialize value of type `java.lang.Boolean`"))
                .build();
    }

    public static ResponseSpecification checkInvalidProjectId() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.containsString("ID should start with a latin letter and contain only latin letters, digits and underscores"))
                .build();
    }

    public static ResponseSpecification checkProjectIdTooLong(int maxLength) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.containsString("Project ID is invalid: it is " + (maxLength + 1) + " characters long while the maximum length is " + maxLength))
                .build();
    }

}
