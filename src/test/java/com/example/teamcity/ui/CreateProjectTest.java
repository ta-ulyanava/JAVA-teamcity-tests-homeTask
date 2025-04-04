package com.example.teamcity.ui;

import com.example.teamcity.api.models.Project;
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
        uiProjectHelper.createProjectFromGitHub(expectedProjectName, expectedBuildTypeName);
        Project createdProject = uiProjectHelper.waitForProjectViaApi(superUserCheckRequests, expectedProjectName);
        uiProjectHelper.verifyProjectPageTitle(createdProject.getId(), expectedProjectName);
        uiProjectHelper.verifyProjectIsVisible(expectedProjectName, softy);
        softy.assertAll();
    }

}
