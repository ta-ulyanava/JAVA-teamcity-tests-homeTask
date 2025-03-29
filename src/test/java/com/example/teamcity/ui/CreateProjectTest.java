package com.example.teamcity.ui;

import com.codeborne.selenide.Condition;
import com.example.teamcity.api.constants.TestConstants;
import com.example.teamcity.api.enums.WebRoute;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.helpers.ProjectHelper;
import com.example.teamcity.ui.pages.ProjectPage;
import com.example.teamcity.ui.pages.ProjectsPage;
import com.example.teamcity.ui.pages.admin.CreateProjectPage;
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

        CreateProjectPage.open(TestConstants.ROOT_PROJECT_ID)
                .createForm(WebRoute.GITHUB_REPO.getUrl())
                .setupProject(expectedProjectName, expectedBuildTypeName);

        Project createdProject = ProjectHelper.waitForProjectInApi(superUserCheckRequests, expectedProjectName, 20);

        ProjectPage.open(createdProject.getId())
                .title.shouldHave(Condition.exactText(expectedProjectName));

        ProjectsPage projectsPage = ProjectsPage.open();
        projectsPage.waitForProjectToAppear(expectedProjectName);

        softy.assertTrue(
                projectsPage.getVisibleProjectNames().contains(expectedProjectName),
                "Project should appear on the Projects page"
        );

        softy.assertAll();
    }

    }



