package com.example.teamcity.api;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import java.util.List;

public class DummyTest extends BaseApiTest{
    @Test
    public void userShouldBeAbleToGetAllProjects(){
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filters(List.of(new RequestLoggingFilter(),new ResponseLoggingFilter()))
                .get("http://admin:admin@192.168.1.34:8111/app/rest/projects");
    }
}
