package com.example.teamcity.api;

import com.example.teamcity.BaseTest;
import com.example.teamcity.api.controllers.ProjectController;
import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.spec.Specifications;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class ProjectTests extends BaseTest {

    private ProjectController projectController;

    @BeforeMethod(alwaysRun = true) // ✅ Исправлено на @BeforeMethod
    public void setup() {
        super.beforeTest();
        superUserCheckRequests.getRequest(Endpoint.USERS).create(testData.getUser());
        projectController = new ProjectController(Specifications.authSpec(testData.getUser()));
    }

    @Test(description = "User should be able to create Project with mandatory fields only", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMandatoryFieldsTest() {
        projectController.createProject(testData.getProject());
        var createdProject = projectController.getProject(testData.getProject().getId());

        softy.assertEquals(testData.getProject().getId(), createdProject.getId(), "Project id is not correct");
        softy.assertEquals(testData.getProject().getName(), createdProject.getName(), "Project name is not correct");
        softy.assertAll();
    }

    @Test(description = "User should be able to create Project with copyAllAssociatedSettings set to true", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsTest() {
        var projectWithCopyAll = generate(
                Arrays.asList(testData.getProject()),
                Project.class,
                testData.getProject().getId(),
                testData.getProject().getName(),
                null,
                true
        );

        projectController.createProject(projectWithCopyAll);
        var createdProject = projectController.getProject(projectWithCopyAll.getId());

        softy.assertEquals(projectWithCopyAll.getId(), createdProject.getId(), "Project ID does not match");
        softy.assertEquals(projectWithCopyAll.getName(), createdProject.getName(), "Project name does not match");
        softy.assertAll();
    }

    @Test(description = "User should be able to create a second Project with parentProject locator set to the first project's ID", groups = {"Positive", "CRUD"})
    public void userCreatesSecondProjectWithParentProjectTest() {
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

        int maxNestedProjects = 10; // или другое максимальное число, зависящее от требований
        var nestedProjects = projectController.createNestedProjects(testData.getProject().getId(), maxNestedProjects);


        softy.assertEquals(nestedProjects.size(), maxNestedProjects, "The number of nested projects is incorrect");
        for (int i = 1; i < nestedProjects.size(); i++) {
            var parentProject = projectController.getProject(nestedProjects.get(i).getParentProject().getId());
            softy.assertEquals(parentProject.getId(), nestedProjects.get(i - 1).getId(), "Parent project ID is incorrect for project " + nestedProjects.get(i).getId());
        }

        softy.assertAll();
    }


}
