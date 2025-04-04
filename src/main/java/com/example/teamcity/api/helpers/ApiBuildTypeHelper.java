package com.example.teamcity.api.helpers;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.requests.CheckedRequest;
import io.qameta.allure.Step;

public class ApiBuildTypeHelper {

    private final CheckedRequest checkedRequest;

    public ApiBuildTypeHelper(CheckedRequest checkedRequest) {
        this.checkedRequest = checkedRequest;
    }

    @Step("Verify that no build type with name '{buildTypeName}' exists in project '{projectId}'")
    public boolean isBuildTypeWithNameAbsent(String buildTypeName, String projectId) {
        String locator = String.format("project:(id:%s),name:%s", projectId, buildTypeName);
        return checkedRequest.<BuildType>getRequest(ApiEndpoint.BUILD_TYPES)
                .findFirstEntityByLocatorQuery(locator)
                .isEmpty();
    }

    @Step("Verify that no build type with ID '{buildTypeId}' exists")
    public boolean isBuildTypeWithIdAbsent(String buildTypeId) {
        return checkedRequest.<BuildType>getRequest(ApiEndpoint.BUILD_TYPES)
                .findFirstEntityByLocatorQuery("id:" + buildTypeId)
                .isEmpty();
    }

    @Step("Wait for build type '{buildTypeName}' in project '{projectId}' to appear in API")
    public BuildType waitForBuildTypeInApi(String buildTypeName, String projectId) {
        return waitForBuildTypeInApi(buildTypeName, projectId, 20);
    }

    public BuildType waitForBuildTypeInApi(String buildTypeName, String projectId, int timeoutSeconds) {
        String locator = String.format("project:(id:%s),name:%s", projectId, buildTypeName);
        for (int i = 0; i < timeoutSeconds; i++) {
            var maybeBuildType = checkedRequest.<BuildType>getRequest(ApiEndpoint.BUILD_TYPES)
                    .findFirstEntityByLocatorQuery(locator);
            if (maybeBuildType.isPresent()) return maybeBuildType.get();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        throw new RuntimeException("BuildType with name '" + buildTypeName + "' was not found in project '" + projectId + "' in API within " + timeoutSeconds + " seconds");
    }
}
