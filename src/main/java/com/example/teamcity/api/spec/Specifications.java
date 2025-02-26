package com.example.teamcity.api.spec;

import com.example.teamcity.api.config.Config;
import com.example.teamcity.api.models.User;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.List;


public class Specifications {
    private static Specifications spec;

    private static RequestSpecBuilder reqBuilder() {

        RequestSpecBuilder reqBuilder = new RequestSpecBuilder();
        reqBuilder.setBaseUri("https://" + Config.getProperty("host"));
        reqBuilder.setContentType(ContentType.JSON);
        reqBuilder.setAccept(ContentType.JSON);
        reqBuilder.addFilters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
        return reqBuilder;
    }


    public static RequestSpecification unauthSpec() {
        var requestBuilder = reqBuilder();
        return reqBuilder()

                .build();
    }


    public static RequestSpecification authSpec(User user) {
        var requestBuilder = reqBuilder();

        requestBuilder.setBaseUri("http://%s:%s@%s".formatted(user.getUsername(), user.getPassword(), Config.getProperty("host")));
        return requestBuilder.build();
    }

    public static RequestSpecification superUserAuthSpec() {
        var requestBuilder = reqBuilder();
        requestBuilder.setBaseUri("http://:%s@%s".formatted(Config.getProperty("superUserToken"), Config.getProperty("host")));
        return requestBuilder.build();
    }

}
