package com.example.teamcity.ui.helpers;

import com.codeborne.selenide.Condition;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.helpers.ApiBuildTypeHelper;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.enums.WebRoute;
import com.example.teamcity.ui.pages.BuildTypePage;
import com.example.teamcity.ui.pages.admin.CreateBuildTypePage;
import io.qameta.allure.Step;
import org.testng.asserts.SoftAssert;

public class UiBuildTypeHelper {

    private final ApiBuildTypeHelper apiHelper;

    public UiBuildTypeHelper(CheckedRequest checkedRequest) {
        this.apiHelper = new ApiBuildTypeHelper(checkedRequest);
    }

    @Step("Create build configuration '{buildTypeName}' from GitHub for project '{projectId}'")
    public void createBuildTypeFromGitHub(String projectId, String buildTypeName) {
        CreateBuildTypePage.open(projectId)
                .createForm(WebRoute.GITHUB_REPO.getUrl())
                .setupBuildType(buildTypeName);
    }

    @Step("Wait for build configuration '{buildTypeName}' in project '{projectId}' to appear in API")
    public BuildType waitForBuildTypeInApi(String buildTypeName, String projectId) {
        return apiHelper.waitForBuildTypeInApi(buildTypeName, projectId);
    }

    @Step("Verify BuildType title is '{expectedName}' for buildType ID '{buildTypeId}'")
    public void verifyBuildTypeTitle(String buildTypeId, String expectedName) {
        BuildTypePage.open(buildTypeId).getTitle().shouldHave(Condition.exactText(expectedName));
    }

    @Step("Verify BuildType with name '{expectedName}' was not created in project '{projectId}'")
    public void verifyBuildTypeIsNotCreated(String expectedName, String projectId, SoftAssert softy) {
        boolean notCreated = apiHelper.isBuildTypeWithNameAbsent(expectedName, projectId);
        softy.assertTrue(notCreated, "Build Type with name '" + expectedName + "' should not be created");
    }
}
