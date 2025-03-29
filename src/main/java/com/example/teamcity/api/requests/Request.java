package com.example.teamcity.api.requests;

import com.example.teamcity.api.enums.ApiEndpoint;
import io.restassured.specification.RequestSpecification;

/**
 * Base class for all request types.
 * <p>
 * Stores the request specification and associated API endpoint.
 */
public class Request {

    protected final RequestSpecification spec;
    protected final ApiEndpoint apiEndpoint;

    /**
     * Initializes a request for the given API endpoint with the provided specification.
     *
     * @param spec        RestAssured request specification
     * @param apiEndpoint TeamCity API endpoint
     */
    public Request(RequestSpecification spec, ApiEndpoint apiEndpoint) {
        this.spec = spec;
        this.apiEndpoint = apiEndpoint;
    }
}
