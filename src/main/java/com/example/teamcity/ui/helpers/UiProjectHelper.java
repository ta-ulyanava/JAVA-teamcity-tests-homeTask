package com.example.teamcity.ui.helpers;

import com.codeborne.selenide.Condition;
import com.example.teamcity.api.constants.TestConstants;
import com.example.teamcity.api.enums.WebRoute;
import com.example.teamcity.api.helpers.ApiProjectHelper;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.ui.pages.ProjectPage;
import com.example.teamcity.ui.pages.ProjectsPage;
import com.example.teamcity.ui.pages.admin.CreateProjectPage;
import io.qameta.allure.Step;
import org.testng.asserts.SoftAssert;

public class UiProjectHelper {

    @Step("Create project from GitHub with name '{projectName}' and build type '{buildTypeName}'")
    public static void createProjectFromGitHub(String projectName, String buildTypeName) {
        CreateProjectPage.open(TestConstants.ROOT_PROJECT_ID)
                .createForm(WebRoute.GITHUB_REPO.getUrl())
                .setupProject(projectName, buildTypeName);
    }

    @Step("Wait for project '{projectName}' to appear in API")
    public static Project waitForProjectViaApi(CheckedRequest requests, String projectName) {
        return ApiProjectHelper.waitForProjectInApi(requests, projectName, 20);
    }

    @Step("Verify project page title is '{expectedProjectName}' for project ID '{projectId}'")
    public static void verifyProjectPageTitle(String projectId, String expectedProjectName) {
        ProjectPage.open(projectId).title.shouldHave(Condition.exactText(expectedProjectName));
    }

    @Step("Verify project '{projectName}' is visible on the Projects page")
    public static void verifyProjectIsVisible(String projectName, SoftAssert softy) {
        ProjectsPage projectsPage = ProjectsPage.open();
        projectsPage.waitForProjectToAppear(projectName);
        softy.assertTrue(projectsPage.getVisibleProjectNames().contains(projectName),
                "Project should appear on the Projects page"
        );
    }
}
