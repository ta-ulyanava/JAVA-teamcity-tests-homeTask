package com.example.teamcity.api.requests;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import io.restassured.specification.RequestSpecification;

import java.util.EnumMap;

public class UncheckedRequest {

    private final EnumMap<ApiEndpoint, UncheckedBase> requests = new EnumMap<>(ApiEndpoint.class);

    public UncheckedRequest(RequestSpecification spec) {
        for (var endpoint : ApiEndpoint.values()) {
            requests.put(endpoint, new UncheckedBase(spec, endpoint));
        }
    }

    public UncheckedBase getRequest(ApiEndpoint apiEndpoint) {
        return requests.get(apiEndpoint);
    }
}
