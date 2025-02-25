package com.example.teamcity.api.controllers;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.responses.ResponseExtractor;
import com.example.teamcity.api.responses.ResponseHandler;
import com.example.teamcity.api.responses.ResponseValidator;
import com.example.teamcity.api.responses.TestValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;

public class ProjectController {
    private final CheckedRequest checkedRequest;
    private final UncheckedRequest uncheckedRequests;

    public ProjectController(RequestSpecification spec) {
        this.checkedRequest = new CheckedRequest(spec);
        this.uncheckedRequests = new UncheckedRequest(spec);
    }

    public Response createProject(Project project) {
        Response response = checkedRequest.<Project>getRequest(Endpoint.PROJECTS).create(project);
        TestDataStorage.getInstance().addCreatedEntity(Endpoint.PROJECTS, project);
        return response;
    }
    public Project createAndReturnProject(Project project) {
        Response response = createProject(project);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        return ResponseExtractor.extractModel(response, Project.class);
    }
    public void validateCreatedProject(Project expectedProject, Project actualProject, SoftAssert softAssert) {
        TestValidator.validateEntityFields(expectedProject, actualProject, softAssert);
        softAssert.assertAll();
    }

    public Project getProjectById(String projectId) {
        return checkedRequest.<Project>getRequest(Endpoint.PROJECTS).read(projectId);
    }

    public Project getProjectByName(String projectName) {
        return checkedRequest.<Project>getRequest(Endpoint.PROJECTS).read(projectName);
    }

    public void deleteProject(String projectId) {
        checkedRequest.getRequest(Endpoint.PROJECTS).delete(projectId);
    }

    public List<Project> createNestedProjects(String parentProjectId, int count) {
        List<Project> nestedProjects = new ArrayList<>();
        String currentParentId = parentProjectId;

        for (int i = 0; i < count; i++) {
            var nestedProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject(currentParentId, null));
            Response response = createProject(nestedProject);
            ResponseHandler.logIfError(response);
            Project createdNestedProject = ResponseHandler.extractAndLogModel(response, Project.class);
            nestedProjects.add(createdNestedProject);
            currentParentId = createdNestedProject.getId();
        }
        return nestedProjects;
    }

    public List<Project> createSiblingProjects(String parentProjectId, int count) {
        List<Project> siblingProjects = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Project siblingProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getUniqueName(), RandomData.getUniqueId(), new ParentProject(parentProjectId, null));
            Response response = createProject(siblingProject);
            ResponseHandler.logIfError(response);
            ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
            siblingProjects.add(ResponseHandler.extractAndLogModel(response, Project.class));
        }
        return siblingProjects;
    }

    public Response createInvalidProjectFromProject(Project project) {
        return uncheckedRequests.getRequest(Endpoint.PROJECTS).create(project);
    }
    public Response createInvalidProjectFromString(String projectJson) {
        return uncheckedRequests.getRequest(Endpoint.PROJECTS).create(projectJson);
    }



}
