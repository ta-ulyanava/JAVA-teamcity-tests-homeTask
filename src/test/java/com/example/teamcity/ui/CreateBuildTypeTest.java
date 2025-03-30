package com.example.teamcity.ui;

import com.example.teamcity.api.constants.TestConstants;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.WebRoute;
import com.example.teamcity.api.helpers.ApiProjectHelper;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.ui.assertions.ValidateElement;
import com.example.teamcity.ui.errors.UiErrorMessages;
import com.example.teamcity.ui.helpers.UiBuildTypeHelper;
import com.example.teamcity.ui.pages.admin.CreateBuildTypePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import com.example.teamcity.api.helpers.ApiBuildTypeHelper;

@Test(groups = "Regression")
public class CreateBuildTypeTest extends BaseUiTest {

    @Feature("UI: Build Configuration Management")
    @Story("Create build configuration via GitHub URL")
    @Test(description = "User should be able to create build configuration", groups = {"Positive"})
    public void userCreatesBuildType() {
        loginAs(testData.getUser());

        Project createdProject = ApiProjectHelper.createProject(superUserCheckRequests, testData.getProject());
        String projectId = createdProject.getId();
        String expectedBuildTypeName = testData.getBuildType().getName();
        superUserCheckRequests.getRequest(ApiEndpoint.PROJECTS).read(projectId);
        UiBuildTypeHelper.createBuildTypeFromGitHub(projectId, expectedBuildTypeName);
        System.out.println("Waiting for build type: " + expectedBuildTypeName + " in project: " + projectId);

        BuildType createdBuildType = UiBuildTypeHelper.waitForBuildTypeInApi(superUserCheckRequests, expectedBuildTypeName, projectId);
        softy.assertEquals(createdBuildType.getName(), expectedBuildTypeName, "Build type name should match");
        softy.assertEquals(createdBuildType.getProjectId(), projectId, "Build type should belong to the correct project");

        UiBuildTypeHelper.verifyBuildTypeTitle(createdBuildType.getId(), expectedBuildTypeName);
        softy.assertAll();
    }

    @Feature("UI: Build Configuration Management")
    @Story("Create build configuration via GitHub URL")
    @Test(description = "User should not be able to create build configuration without name", groups = {"Negative"})
    public void userCannotCreateBuildTypeWithoutName() {
        loginAs(testData.getUser());
        Project createdProject = ApiProjectHelper.createProject(superUserCheckRequests, testData.getProject());
        String projectId = createdProject.getId();
        CreateBuildTypePage page = CreateBuildTypePage.open(projectId).createForm(WebRoute.GITHUB_REPO.getUrl()).setupBuildType("");
        softy.assertTrue(page.getErrorMessage().isDisplayed(), UiErrorMessages.BUILD_CONFIG_NAME_MUST_BE_NOT_NULL);
        softy.assertTrue(ApiBuildTypeHelper.isBuildTypeWithNameAbsent(superUserCheckRequests, "", projectId), "No build type with blank name should be created");
        softy.assertAll();
    }





}
