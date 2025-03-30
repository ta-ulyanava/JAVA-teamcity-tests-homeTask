package com.example.teamcity.ui;

import com.example.teamcity.api.models.Project;
import com.example.teamcity.ui.helpers.UiProjectHelper;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Test(groups = "Regression")
public class CreateProjectTest extends BaseUiTest {

    @Feature("UI: Project Management")
    @Story("Create project via GitHub URL")
    @Test(description = "User should be able to create project", groups = {"Positive"})
    public void userCreatesProject() {
        loginAs(testData.getUser());
        String expectedProjectName = testData.getProject().getName();
        String expectedBuildTypeName = testData.getBuildType().getName();
        UiProjectHelper.createProjectFromGitHub(expectedProjectName, expectedBuildTypeName);
        Project createdProject = UiProjectHelper.waitForProjectViaApi(superUserCheckRequests, expectedProjectName);
        UiProjectHelper.verifyProjectPageTitle(createdProject.getId(), expectedProjectName);
        UiProjectHelper.verifyProjectIsVisible(expectedProjectName, softy);
        softy.assertAll();
    }
}
