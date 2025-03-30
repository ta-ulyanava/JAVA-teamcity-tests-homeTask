package com.example.teamcity.ui.helpers;

import com.codeborne.selenide.Condition;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.WebRoute;
import com.example.teamcity.api.helpers.ApiBuildTypeHelper;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.ui.pages.BuildTypePage;
import com.example.teamcity.ui.pages.admin.CreateBuildTypePage;
import io.qameta.allure.Step;
import org.testng.asserts.SoftAssert;

public class UiBuildTypeHelper {

    @Step("Create build configuration '{buildTypeName}' from GitHub for project '{projectId}'")
    public static void createBuildTypeFromGitHub(String projectId, String buildTypeName) {
        CreateBuildTypePage.open(projectId).createForm(WebRoute.GITHUB_REPO.getUrl()).setupBuildType(buildTypeName);
    }
    @Step("Wait for build configuration '{buildTypeName}' in project '{projectId}' to appear in API")
    public static BuildType waitForBuildTypeInApi(CheckedRequest requests, String buildTypeName, String projectId) {
        return ApiBuildTypeHelper.waitForBuildTypeInApi(requests, buildTypeName, projectId, 20);
    }


    @Step("Verify BuildType title is '{expectedName}' for buildType ID '{buildTypeId}'")
    public static void verifyBuildTypeTitle(String buildTypeId, String expectedName) {
        BuildTypePage.open(buildTypeId).getTitle().shouldHave(Condition.exactText(expectedName));
    }

    @Step("Verify BuildType with empty name was not created")
    public static void verifyBuildTypeIsNotCreated(String expectedName, CheckedRequest requests, SoftAssert softy) {
        var maybeBuildType = requests.<BuildType>getRequest(ApiEndpoint.BUILD_TYPES)
                .findFirstEntityByLocatorQuery("name:" + expectedName);
        softy.assertFalse(maybeBuildType.isPresent(), "Build Type with empty name should not be created");
    }

}
