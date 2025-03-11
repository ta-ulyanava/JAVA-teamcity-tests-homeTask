package com.example.teamcity.ui;

import com.codeborne.selenide.Condition;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.WebRoute;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.ui.pages.ProjectPage;
import com.example.teamcity.api.ui.pages.ProjectsPage;
import com.example.teamcity.api.ui.pages.admin.CreateProjectPage;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static io.qameta.allure.Allure.step;

@Test(groups = "Regression")
public class CreateProjectTest extends BaseUiTest {

    @Test(description = "User should be able to create project", groups = {"Positive"})
    public void userCreatesProject() {
        // подготовка окружения
        loginAs(testData.getUser());

        // взаимодействие с UI
        CreateProjectPage.open("_Root")
                .createForm(WebRoute.GITHUB_REPO.getUrl())
                .setupProject(testData.getProject().getName(), testData.getBuildType().getName());

        // проверка состояния API
        // (корректность отправки данных с UI на API)
        var createdProject = superUserCheckRequests.<Project>getRequest(ApiEndpoint.PROJECTS).read("name:" + testData.getProject().getName());
        softy.assertNotNull(createdProject);

        // проверка состояния UI
        // (корректность считывания данных и отображение данных на UI)
        ProjectPage.open(createdProject.getId())
                .title.shouldHave(Condition.exactText(testData.getProject().getName()));

        var foundProjects = ProjectsPage.open()
                .getProjects().stream()
                .anyMatch(project -> project.getName().text().equals(testData.getProject().getName()));

        softy.assertTrue(foundProjects);
    }

//   @Test(description = "User should not be able to create a project without a name", groups = {"Negative"})
//    public void userCreatesProjectWithoutName() {
//        // подготовка окружения
//        step("Login as user");
//        loginAs(testData.getUser());
//        // взаимодействие с UI
//        step("Open `Create Project Page` (http://localhost:8111/admin/createObjectMenu.html)");
//        step("Send all project parameters (repository URL)");
//        step("Click `Proceed`");
//        step("Set Project Name value is empty");
//        step("Click `Proceed`");
//
//        // проверка состояния API
//        // (корректность отправки данных с UI на API)
//        step("Check that number of projects did not change");
//        // проверка состояния UI
//        // (корректность считывания данных и отображение данных на UI)
//        step("Check that error appears `Project name must not be empty`");
//
//    }
}
