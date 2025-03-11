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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.restassured.response.Response;

import static com.codeborne.selenide.Selenide.$;
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
        // Взаимодействие с UI
        step("Create Build Type via UI", () -> 
            CreateBuildTypePage.open(testData.getProject().getId())
                .createForm(WebRoute.GITHUB_REPO.getUrl())
                .setupBuildType(testData.getBuildType().getName())
        );

        // Проверка состояния API и получение ID
        Response buildTypeResponse = step("Verify Build Type creation via API", () -> {
            Response apiResponse = uncheckedRequest.getRequest(ApiEndpoint.BUILD_TYPES)
                .read("name:" + testData.getBuildType().getName());
            System.out.println("Created build type response: " + apiResponse.asString());
            softy.assertNotNull(apiResponse, "Build Type should be created");
            return apiResponse;
        });

        // Проверка состояния UI с правильным ID
        step("Verify Build Type page UI", () -> {
            String buildTypeId = buildTypeResponse.jsonPath().getString("buildType[0].id");
            System.out.println("Opening build type page with ID: " + buildTypeId);
            BuildTypePage.open(buildTypeId)
                .getTitle()
                .shouldHave(Condition.exactText(testData.getBuildType().getName()));
        });

        softy.assertAll();
    }

    @Test(description = "User should not be able to create build type without name", groups = {"Negative"})
    public void userCannotCreateBuildTypeWithoutName() {
        // Взаимодействие с UI
        step("Try to create Build Type without name", () -> {
            CreateBuildTypePage.open(testData.getProject().getId())
                .createForm(WebRoute.GITHUB_REPO.getUrl())
                .setupBuildType("");  // Пустое имя
        });

        // Проверка состояния UI - должно появиться сообщение об ошибке
        step("Verify error message is displayed", () -> {
            $(".error").shouldHave(Condition.text("Build configuration name must not be empty"));
        });

        // Проверка состояния API - Build Type не должен быть создан
        step("Verify Build Type was not created", () -> {
            var buildTypes = superUserCheckRequests.<BuildType>getRequest(ApiEndpoint.BUILD_TYPES)
                .read("project:" + testData.getProject().getId());
            softy.assertNull(buildTypes, "Build Type should not be created");
        });

        softy.assertAll();
    }
}



