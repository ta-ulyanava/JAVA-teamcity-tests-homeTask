package com.example.teamcity.api.requests;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.checked.CheckedBase;
import io.restassured.specification.RequestSpecification;

import java.util.EnumMap;

public class CheckedRequest {

    private final EnumMap<ApiEndpoint, CheckedBase> requests = new EnumMap<>(ApiEndpoint.class);

    public CheckedRequest(RequestSpecification spec) {
        for (var endpoint : ApiEndpoint.values()) {
            requests.put(endpoint, new CheckedBase(spec, endpoint));
        }
    }

    public <T extends BaseModel> CheckedBase<T> getRequest(ApiEndpoint apiEndpoint) {
        return (CheckedBase<T>) requests.get(apiEndpoint);
    }
    public <T extends BaseModel> CheckedBase<T> getRequest(ApiEndpoint apiEndpoint, Class<T> modelClass) {
        CheckedBase<?> base = requests.get(apiEndpoint);
        if (!modelClass.isAssignableFrom(apiEndpoint.getModelClass())) {
            throw new IllegalArgumentException("Requested class " + modelClass + " does not match endpoint's model class " + apiEndpoint.getModelClass());
        }
        return (CheckedBase<T>) base;
    }

}
