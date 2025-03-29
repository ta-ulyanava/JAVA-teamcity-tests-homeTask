package com.example.teamcity.api.requests;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import io.qameta.allure.Step;
import io.restassured.specification.RequestSpecification;

import java.util.EnumMap;

/**
 * Factory class for accessing UncheckedBase instances for TeamCity API endpoints.
 * <p>
 * Provides direct access to API calls without response validation.
 */
public class UncheckedRequest {

    private final EnumMap<ApiEndpoint, UncheckedBase> requests = new EnumMap<>(ApiEndpoint.class);

    /**
     * Initializes unchecked request handlers for all supported API endpoints.
     *
     * @param spec request specification with headers, auth, etc.
     */
    public UncheckedRequest(RequestSpecification spec) {
        for (var endpoint : ApiEndpoint.values()) {
            requests.put(endpoint, new UncheckedBase(spec, endpoint));
        }
    }

    /**
     * Returns an unchecked request handler for the specified endpoint.
     *
     * @param apiEndpoint TeamCity API endpoint
     * @return UncheckedBase instance for the given endpoint
     */
    @Step("Get UncheckedBase for {apiEndpoint}")
    public UncheckedBase getRequest(ApiEndpoint apiEndpoint) {
        return requests.get(apiEndpoint);
    }
}
