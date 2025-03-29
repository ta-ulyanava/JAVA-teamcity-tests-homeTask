package com.example.teamcity.api.spec.responce;

import io.qameta.allure.Step;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;

public class IncorrectDataSpecs {

    @Step("Expect 400 Bad Request due to invalid starting character in field '{field}'")
    public static ResponseSpecification badRequestWithIncorrectFieldFormat(String entityType, String field, String value, String firstCharacter) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(containsString(
                        "%s %s \"%s\" is invalid: starts with non-letter character '%s'."
                                .formatted(entityType, field, value, firstCharacter)))
                .build();
    }

    @Step("Expect 400 Bad Request due to field '{field}' being too long")
    public static ResponseSpecification badRequestFieldTooLong(String entityType, String field, String value, int maxLength) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(containsString(
                        "%s \"%s\" is invalid: it is %d characters long while the maximum length is %d."
                                .formatted(field, value, value.length(), maxLength)))
                .build();
    }

    @Step("Expect 400 Bad Request due to unsupported character '{character}' in field '{field}'")
    public static ResponseSpecification badRequestUnsupportedCharacter(String entityType, String field, String value, String character) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(containsString(
                        "%s %s \"%s\" is invalid: contains unsupported character '%s'."
                                .formatted(entityType, field, value, character)))
                .build();
    }

    @Step("Expect 400 Bad Request due to non-latin character in field '{field}'")
    public static ResponseSpecification badRequestNonLatinLetter(String entityType, String field, String value) {
        String firstChar = value.substring(0, 1);
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(containsString(
                        "%s %s \"%s\" is invalid: contains non-latin letter '%s'."
                                .formatted(entityType, field, value, firstChar)))
                .build();
    }

    @Step("Expect 400 Bad Request due to empty field '{field}'")
    public static ResponseSpecification badRequestEmptyField(String entityType, String field) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(anyOf(
                        containsString("%s %s must not be empty.".formatted(entityType, field)),
                        containsString("%s %s cannot be empty.".formatted(entityType, field)),
                        containsString("Given %s %s is empty.".formatted(entityType, field))
                ))
                .build();
    }

    @Step("Expect 400 Bad Request due to duplicated value '{value}' in field '{field}'")
    public static ResponseSpecification badRequestDuplicatedField(String entityType, String field, String value) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(anyOf(
                        containsString("%s %s \"%s\" is already used".formatted(entityType, field, value)),
                        containsString("%s with this %s already exists: %s".formatted(entityType, field, value))
                ))
                .build();
    }

    @Step("Expect 404 Not Found for entity '{entityType}' by locator '{locatorType}:{locatorValue}'")
    public static ResponseSpecification entityNotFoundByLocator(String entityType, String locatorType, String locatorValue) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody(anyOf(
                        containsString("No %s found by locator".formatted(entityType.toLowerCase())),
                        containsString("%s cannot be found by external %s '%s'.".formatted(entityType, locatorType, locatorValue)),
                        containsString("Nothing is found by locator"),
                        containsString("Could not find the entity requested")
                ))
                .build();
    }

    @Step("Expect empty result list for entity '{entityType}' by locator '{locatorType}:{locatorValue}'")
    public static ResponseSpecification emptyEntityListReturned(String entityType, String locatorType, String locatorValue) {
        String encodedLocator = URLEncoder.encode(locatorType + ":" + locatorValue, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody("count", equalTo(0))
                .expectBody("href", containsString(encodedLocator))
                .build();
    }

    @Step("Expect 400 Bad Request due to negative pagination parameters")
    public static ResponseSpecification badRequestNegativePaginationParameters() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(containsString("Invalid pagination parameters"))
                .build();
    }
}
