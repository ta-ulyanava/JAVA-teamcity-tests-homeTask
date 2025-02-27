package com.example.teamcity.api;

import com.example.teamcity.BaseTest;
import com.example.teamcity.api.controllers.ProjectController;
import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.spec.Specifications;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class ProjectTests extends BaseTest {

    private ProjectController projectController;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        super.beforeTest();
        superUserCheckRequests.getRequest(Endpoint.USERS).create(testData.getUser());
        projectController = new ProjectController(Specifications.authSpec(testData.getUser()));
    }

    @Test(description = "User should be able to create a project with the minimum required fields", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMandatoryFieldsTest() {
        projectController.createProject(testData.getProject());
        var createdProject = projectController.getProject(testData.getProject().getId());
        softy.assertEquals(testData.getProject().getId(), createdProject.getId(), "Project id is not correct");
        softy.assertEquals(testData.getProject().getName(), createdProject.getName(), "Project name is not correct");
        softy.assertAll();
    }

    @Test(description = "User should be able to create Project with copyAllAssociatedSettings set to true", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsTest() {
        var projectWithCopyAll = generate(Arrays.asList(testData.getProject()), Project.class, testData.getProject().getId(), testData.getProject().getName(), null, true);
        projectController.createProject(projectWithCopyAll);
        var createdProject = projectController.getProject(projectWithCopyAll.getId());
        softy.assertEquals(projectWithCopyAll.getId(), createdProject.getId(), "Project ID does not match");
        softy.assertEquals(projectWithCopyAll.getName(), createdProject.getName(), "Project name does not match");
        softy.assertAll();
    }

    @Test(description = "User should be able to create a nested project", groups = {"Positive", "CRUD"})
    public void userCreatesNestedProjectTest() {
        projectController.createProject(testData.getProject());
        var nestedProjects = projectController.createNestedProjects(testData.getProject().getId(), 1);
        var secondProject = nestedProjects.get(0);
        var createdSecondProject = projectController.getProject(secondProject.getId());
        softy.assertEquals(createdSecondProject.getParentProject().getId(), testData.getProject().getId(), "Parent project ID is incorrect");
        softy.assertAll();
    }

    @Test(description = "User should be able to create a max amount of nested projects", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesMaxAmountNestedProjectsTest() {
        projectController.createProject(testData.getProject());
        int maxNestedProjects = 10;
        var nestedProjects = projectController.createNestedProjects(testData.getProject().getId(), maxNestedProjects);
        softy.assertEquals(nestedProjects.size(), maxNestedProjects, "The number of nested projects is incorrect");
        for (int i = 1; i < nestedProjects.size(); i++) {
            var parentProject = projectController.getProject(nestedProjects.get(i).getParentProject().getId());
            softy.assertEquals(parentProject.getId(), nestedProjects.get(i - 1).getId(), "Parent project ID is incorrect for project " + nestedProjects.get(i).getId());
        }
        softy.assertAll();
    }


    @Test(description = "User should not be able to create a Project with a non-existent parentProject locator", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithNonExistentParentProjectTest() {
        var response = projectController.createInvalidProject(generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("non_existent_locator", null)));

        response.then().assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(Matchers.containsString("Project cannot be found by external id 'non_existent_locator'"));
    }



    @Test(description = "User should not be able to create Project with empty name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithEmptyNameTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "");
        Response response = projectController.createInvalidProject(invalidProject);
        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project name cannot be empty"));
    }

    @Test(description = "User should not be able to create Project with empty id", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithEmptyIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "", RandomData.getString());
        Response response = projectController.createInvalidProject(invalidProject);
        response.then().assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(Matchers.containsString("Project ID cannot be empty"));
    }
    @Test(description = "User should not be able to create a Project with an existing ID", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingIdTest() {
        projectController.createProject(testData.getProject());

        var duplicateProject = TestDataGenerator.generate(List.of(testData.getProject()), Project.class, testData.getProject().getId(), RandomData.getString());

        Response response = projectController.createInvalidProject(duplicateProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID \"%s\" is already used by another project".formatted(testData.getProject().getId())));
    }
    @Test(description = "User should not be able to create a Project with an existing name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingNameTest() {
        projectController.createProject(testData.getProject());

        var duplicateProject = TestDataGenerator.generate(List.of(testData.getProject()), Project.class, RandomData.getString(), testData.getProject().getName());

        Response response = projectController.createInvalidProject(duplicateProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project with this name already exists: %s".formatted(testData.getProject().getName())));
    }


}
