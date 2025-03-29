package com.example.teamcity.api.requests;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.checked.CheckedBase;
import io.qameta.allure.Step;
import io.restassured.specification.RequestSpecification;

import java.util.EnumMap;

/**
 * Factory class for accessing type-safe CheckedBase instances for each API endpoint.
 */
public class CheckedRequest {

    private final EnumMap<ApiEndpoint, CheckedBase> requests = new EnumMap<>(ApiEndpoint.class);

    /**
     * Initializes checked requests for all available API endpoints using the provided specification.
     *
     * @param spec request specification with authentication and headers
     */
    public CheckedRequest(RequestSpecification spec) {
        for (var endpoint : ApiEndpoint.values()) {
            requests.put(endpoint, new CheckedBase(spec, endpoint));
        }
    }

    /**
     * Returns a type-safe CheckedBase for the specified API endpoint.
     *
     * @param apiEndpoint target endpoint
     * @param <T>         expected model type
     * @return CheckedBase instance for the endpoint
     */
    @Step("Get CheckedBase for {apiEndpoint}")
    public <T extends BaseModel> CheckedBase<T> getRequest(ApiEndpoint apiEndpoint) {
        return (CheckedBase<T>) requests.get(apiEndpoint);
    }

    /**
     * Returns a CheckedBase instance with an additional runtime check that the model class matches the endpoint.
     *
     * @param apiEndpoint endpoint to use
     * @param modelClass  class of expected model
     * @param <T>         model type
     * @return CheckedBase instance for the endpoint
     */
    @Step("Get CheckedBase for {apiEndpoint} with validation against class {modelClass}")
    public <T extends BaseModel> CheckedBase<T> getRequest(ApiEndpoint apiEndpoint, Class<T> modelClass) {
        CheckedBase<?> base = requests.get(apiEndpoint);
        if (!modelClass.isAssignableFrom(apiEndpoint.getModelClass())) {
            throw new IllegalArgumentException("Requested class " + modelClass + " does not match endpoint's model class " + apiEndpoint.getModelClass());
        }
        return (CheckedBase<T>) base;
    }
}
