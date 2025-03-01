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
import com.example.teamcity.api.spec.ValidationResponseSpecifications;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

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
        response.then().spec(ValidationResponseSpecifications.checkBadRequest()); // Проверяем, что нет ошибок валидации
        TestDataStorage.getInstance().addCreatedEntity(Endpoint.PROJECTS, project);
        return response;
    }

    public Project createAndReturnProject(Project project) {
        Response response = createProject(project);
        return ResponseExtractor.extractModel(response, Project.class);
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
            response.then().spec(ValidationResponseSpecifications.checkBadRequest()); // Проверяем ошибки
            Project createdNestedProject = ResponseExtractor.extractModel(response, Project.class);
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
            response.then().spec(ValidationResponseSpecifications.checkBadRequest()); // Проверяем ошибки
            siblingProjects.add(ResponseExtractor.extractModel(response, Project.class));
        }
        return siblingProjects;
    }

    public Response createInvalidProjectFromProject(Project project) {
        Response response = uncheckedRequests.getRequest(Endpoint.PROJECTS).create(project);
        response.then().spec(ValidationResponseSpecifications.checkBadRequest());
        return response;
    }

    public Response createInvalidProjectFromString(String projectJson) {
        Response response = uncheckedRequests.getRequest(Endpoint.PROJECTS).create(projectJson);
        response.then().spec(ValidationResponseSpecifications.checkBadRequest());
        return response;
    }
}
