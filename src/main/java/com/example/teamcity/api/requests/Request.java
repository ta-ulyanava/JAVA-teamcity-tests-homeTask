package com.example.teamcity.api.requests;

import com.example.teamcity.api.enums.ApiEndpoint;
import io.restassured.specification.RequestSpecification;

public class Request {
    protected final RequestSpecification spec;
    protected final ApiEndpoint apiEndpoint;

    public Request(RequestSpecification spec, ApiEndpoint apiEndpoint) {
        this.spec = spec;
        this.apiEndpoint = apiEndpoint;
    }
}
