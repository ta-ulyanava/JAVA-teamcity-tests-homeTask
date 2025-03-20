//package com.example.teamcity.api;
//
//import com.example.teamcity.BaseTest;
//import com.example.teamcity.api.controllers.ProjectController;
//import com.example.teamcity.api.enums.ApiEndpoint;
//import com.example.teamcity.api.generators.RandomData;
//import com.example.teamcity.api.models.Project;
//import com.example.teamcity.api.responses.ResponseExtractor;
//import com.example.teamcity.api.validation.EntityValidator;
//import com.example.teamcity.api.spec.request.RequestSpecifications;
//import com.example.teamcity.api.spec.ResponseSpecifications;
//import io.restassured.response.Response;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
//import java.util.List;
//
//@Test(groups = {"Regression"})
//public class ProjectSearchTest extends BaseTest {
//
//    private ProjectController projectController;
//
//    @BeforeMethod(alwaysRun = true)
//    public void setup() {
//        super.beforeTest();
//        superUserCheckRequests.getRequest(ApiEndpoint.USERS).create(testData.getUser());
//        projectController = new ProjectController(RequestSpecifications.authSpec(testData.getUser()));
//    }
//
//    @Test(description = "User should be able to find a project by its exact name", groups = {"Positive", "Search"})
//    public void userCanFindProjectByExactNameTest() {
//        var project = testData.getProject();
//        var createdProject = projectController.createAndReturnProject(project);
//
//        Response response = projectController.findProjectByNameResponse(createdProject.getName());
//        response.then().spec(ResponseSpecifications.checkSuccess());
//
//        var foundProject = ResponseExtractor.extractModel(response, Project.class);
//        EntityValidator.validateEntityFields(createdProject, foundProject, softy);
//        softy.assertAll();
//    }
//
//    @Test(description = "User should be able to find projects by partial name match", groups = {"Positive", "Search"})
//    public void userCanFindProjectsByPartialNameTest() {
//        var commonName = "TestProject_" + RandomData.getString();
//
//        var baseProject = testData.getProject();
//        baseProject.setName(commonName + "_Base");
//
//        var similarProject1 = testData.getProject();
//        similarProject1.setName(commonName + "_Similar1");
//
//        var similarProject2 = testData.getProject();
//        similarProject2.setName(commonName + "_Similar2");
//
//        var createdBase = projectController.createAndReturnProject(baseProject);
//        var created1 = projectController.createAndReturnProject(similarProject1);
//        var created2 = projectController.createAndReturnProject(similarProject2);
//
//        Response response = projectController.searchProjectByNameResponse(commonName);
//        response.then().spec(ResponseSpecifications.checkSuccess());
//
//        var foundProjects = ResponseExtractor.extractModelList(response, Project.class);
//        softy.assertFalse(foundProjects.isEmpty(), "No projects found by partial name search");
//        softy.assertTrue(foundProjects.size() >= 3, "Not all test projects were found in search results");
//
//        var foundBase = projectController.findProjectInList(foundProjects, createdBase);
//        var foundSimilar1 = projectController.findProjectInList(foundProjects, created1);
//        var foundSimilar2 = projectController.findProjectInList(foundProjects, created2);
//
//        softy.assertNotNull(foundBase, "Base project not found in search results");
//        softy.assertNotNull(foundSimilar1, "Similar project 1 not found in search results");
//        softy.assertNotNull(foundSimilar2, "Similar project 2 not found in search results");
//
//        EntityValidator.validateEntityFields(createdBase, foundBase, softy);
//        EntityValidator.validateEntityFields(created1, foundSimilar1, softy);
//        EntityValidator.validateEntityFields(created2, foundSimilar2, softy);
//
//        softy.assertAll();
//    }
//
//    @Test(description = "User should not be able to find a non-existent project by name", groups = {"Negative", "Search"})
//    public void userCannotFindNonExistentProjectTest() {
//        var nonExistentProject = testData.getProject();
//        nonExistentProject.setName("NonExistent_" + RandomData.getString());
//        Response response = projectController.findProjectByNameResponse(nonExistentProject.getName());
//        response.then().spec(ResponseSpecifications.checkProjectNotFound(nonExistentProject.getName()));
//        softy.assertAll();
//    }
//
//    @Test(description = "User should not be able to find a project without authentication", groups = {"Negative", "Auth"})
//    public void userCannotFindProjectWithoutAuthTest() {
//        var project = testData.getProject();
//        var createdProject = projectController.createAndReturnProject(project);
//
//        var unauthController = new ProjectController(RequestSpecifications.unauthSpec());
//        Response response = unauthController.findProjectByNameResponse(createdProject.getName());
//        response.then().spec(ResponseSpecifications.checkUnauthorizedAccess());
//        softy.assertAll();
//    }
//
//    @Test(description = "User should be able to get all projects", groups = {"Positive", "Search"})
//    public void userCanGetAllProjectsTest() {
//        var testPrefix = "TestProject_" + RandomData.getString();
//
//        var project1 = testData.getProject();
//        project1.setName(testPrefix + "_1");
//
//        var project2 = testData.getProject();
//        project2.setName(testPrefix + "_2");
//
//        var created1 = projectController.createAndReturnProject(project1);
//        var created2 = projectController.createAndReturnProject(project2);
//
//        Response response = projectController.getAllProjectsResponse();
//        response.then().spec(ResponseSpecifications.checkSuccess());
//
//        var allProjects = ResponseExtractor.extractModelList(response, Project.class);
//        softy.assertFalse(allProjects.isEmpty(), "Project list should not be empty");
//
//        var found1 = projectController.findProjectInList(allProjects, created1);
//        var found2 = projectController.findProjectInList(allProjects, created2);
//
//        softy.assertNotNull(found1, "First test project not found in the list");
//        softy.assertNotNull(found2, "Second test project not found in the list");
//
//        EntityValidator.validateEntityFields(created1, found1, softy);
//        EntityValidator.validateEntityFields(created2, found2, softy);
//
//        softy.assertAll();
//    }
//}
