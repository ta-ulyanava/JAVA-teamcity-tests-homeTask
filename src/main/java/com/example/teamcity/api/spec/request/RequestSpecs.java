package com.example.teamcity.api.spec.request;

import com.example.teamcity.api.config.Config;
import com.example.teamcity.api.models.User;
import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.List;

/**
 * Provides reusable REST-assured request specifications for TeamCity API tests.
 */
public class RequestSpecs {

    private static RequestSpecs spec;

    private static RequestSpecBuilder reqBuilder() {
        return new RequestSpecBuilder()
                .setBaseUri("http://" + Config.getProperty("host"))
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }

    /**
     * Creates a request specification without authentication.
     *
     * @return unauthenticated request spec
     */
    @Step("Build unauthenticated request specification")
    public static RequestSpecification unauthSpec() {
        return new RequestSpecBuilder()
                .setBaseUri("http://" + Config.getProperty("host"))
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setRelaxedHTTPSValidation()
                .addFilters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()))
                .build();
    }

    /**
     * Creates a request specification using basic authentication with given user credentials.
     *
     * @param user TeamCity user
     * @return authenticated request spec
     */
    @Step("Build request specification with user authentication")
    public static RequestSpecification authSpec(User user) {
        var requestBuilder = reqBuilder();
        requestBuilder.setBaseUri("http://%s:%s@%s".formatted(user.getUsername(), user.getPassword(), Config.getProperty("host")));
        return requestBuilder.build();
    }

    /**
     * Creates a request specification using super user authentication token.
     *
     * @return super user authenticated request spec
     */
    @Step("Build request specification with super user token")
    public static RequestSpecification superUserAuthSpec() {
        var requestBuilder = reqBuilder();
        requestBuilder.setBaseUri("http://:%s@%s".formatted(Config.getProperty("superUserToken"), Config.getProperty("host")));
        return requestBuilder.build();
    }
}
