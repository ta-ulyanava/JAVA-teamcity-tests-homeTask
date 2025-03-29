package com.example.teamcity.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.WebRoute;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.ui.pages.ProjectPage;
import com.example.teamcity.ui.pages.ProjectsPage;
import com.example.teamcity.ui.pages.admin.CreateProjectPage;
import io.qameta.allure.Step;
import org.testng.annotations.Test;


//@Test(groups = "Regression")
public class CreateProjectTest extends BaseUiTest {

    @Test(description = "User should be able to create project", groups = {"Positive"})
    public void userCreatesProject() {
        loginAs(testData.getUser());

        String expectedProjectName = testData.getProject().getName();
        String expectedBuildTypeName = testData.getBuildType().getName();

        CreateProjectPage.open("_Root")
                .createForm(WebRoute.GITHUB_REPO.getUrl())
                .setupProject(expectedProjectName, expectedBuildTypeName);

        var createdProject = superUserCheckRequests.<Project>getRequest(ApiEndpoint.PROJECTS)
                .findFirstEntityByLocatorQuery("name:" + expectedProjectName)
                .orElseThrow();

        ProjectPage.open(createdProject.getId())
                .title.shouldHave(Condition.exactText(expectedProjectName));

        var projectsPage = ProjectsPage.open();
        projectsPage.waitForProjectToAppear(expectedProjectName);

        boolean found = projectsPage.getProjects().stream()
                .anyMatch(project -> project.getName().text().equals(expectedProjectName));

        softy.assertTrue(found, "Project should appear on the Projects page");
        softy.assertAll();
    }


}
