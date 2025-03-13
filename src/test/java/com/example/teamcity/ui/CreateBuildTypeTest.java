package com.example.teamcity.ui;

import com.codeborne.selenide.Condition;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.WebRoute;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.controllers.ProjectController;
import com.example.teamcity.api.spec.Specifications;
import com.example.teamcity.api.ui.pages.BuildTypePage;
import com.example.teamcity.api.ui.pages.admin.CreateBuildTypePage;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.ui.validation.ValidateElement;
import com.example.teamcity.api.ui.errors.UiErrors;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import static io.qameta.allure.Allure.step;

@Test(groups = "Regression")
public class CreateBuildTypeTest extends BaseUiTest {
    private ProjectController projectController;
    private UncheckedRequest uncheckedRequest;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        super.beforeTest();
        loginAs(testData.getUser());
        projectController = new ProjectController(Specifications.authSpec(testData.getUser()));
        projectController.createProject(testData.getProject());
        uncheckedRequest = new UncheckedRequest(Specifications.superUserAuthSpec());
    }

    @Test(description = "User should be able to create build type", groups = {"Positive"})
    public void userCreatesBuildType() {
        // подготовка окружения выполнена в setup

        // взаимодействие с UI
        step("Create Build Type via UI", () -> 
            CreateBuildTypePage.open(testData.getProject().getId())
                .createForm(WebRoute.GITHUB_REPO.getUrl())
                .setupBuildType(testData.getBuildType().getName())
        );

        // проверка состояния API
        // (корректность отправки данных с UI на API)
        var buildTypeId = step("Verify Build Type creation via API", () -> {
            Response response = uncheckedRequest.getRequest(ApiEndpoint.BUILD_TYPES)
                .read("name:" + testData.getBuildType().getName());
            softy.assertNotNull(response, "Build Type should be created");
            return response.jsonPath().getString("buildType[0].id");
        });

        // проверка состояния UI
        // (корректность считывания данных и отображение данных на UI)
        step("Verify Build Type page UI", () -> {
            var page = BuildTypePage.open(buildTypeId);
            ValidateElement.byText(page.getTitle(), testData.getBuildType().getName(), softy);
        });

        softy.assertAll();
    }

    @Test(description = "User should not be able to create build type without name", groups = {"Negative"})
    public void userCannotCreateBuildTypeWithoutName() {
        // взаимодействие с UI
        step("Try to create Build Type without name", () -> {
            var page = CreateBuildTypePage.open(testData.getProject().getId())
                .createForm(WebRoute.GITHUB_REPO.getUrl())
                .setupBuildType("");  // Пустое имя
            ValidateElement.byText(page.getErrorMessage(), UiErrors.BUILD_CONFIG_NAME_MUST_BE_NOT_NULL, softy);
        });

        // проверка состояния API
        // (корректность валидации на API)
        step("Verify Build Type was not created", () -> {
            var buildTypes = superUserCheckRequests.<BuildType>getRequest(ApiEndpoint.BUILD_TYPES)
                .read("project:" + testData.getProject().getId());
            softy.assertNull(buildTypes, "Build Type should not be created");
        });

        softy.assertAll();
    }
}



