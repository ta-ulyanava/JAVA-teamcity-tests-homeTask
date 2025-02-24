package com.example.teamcity.api.controllers;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ProjectController {
    private final CheckedRequest checkedRequest;
    private final UncheckedRequest uncheckedRequests;

    public ProjectController(RequestSpecification spec) {
        this.checkedRequest = new CheckedRequest(spec);
        this.uncheckedRequests = new UncheckedRequest(spec);
    }

    public Response createProject(Project project) {
        Response response = checkedRequest.<Project>getRequest(Endpoint.PROJECTS).create(project);  // Получаем ответ от запроса
        TestDataStorage.getInstance().addCreatedEntity(Endpoint.PROJECTS, project);  // Добавляем созданный проект в хранилище
        return response;  // Возвращаем ответ
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

    public Response createInvalidProject(Project project) {
        return uncheckedRequests.getRequest(Endpoint.PROJECTS).create(project);
    }

    public List<Project> createNestedProjects(String rootProjectId, int count) {
        List<Project> nestedProjects = new ArrayList<>();
        String parentProjectId = rootProjectId;

        for (int i = 0; i < count; i++) {
            var nestedProject = TestDataGenerator.generate(
                    List.of(),
                    Project.class,
                    RandomData.getString(),
                    RandomData.getString(),
                    new ParentProject(parentProjectId, null)
            );

            createProject(nestedProject);
            nestedProjects.add(nestedProject);
            parentProjectId = nestedProject.getId();
        }

        return nestedProjects;
    }

    public List<Project> createSiblingProjects(String parentProjectId, int count) {
        List<Project> siblingProjects = IntStream.range(0, count)
                .mapToObj(i -> TestDataGenerator.generate(
                        List.of(),
                        Project.class,
                        RandomData.getString(),
                        RandomData.getString(),
                        new ParentProject(parentProjectId, null)
                ))
                .toList();

        siblingProjects.forEach(this::createProject);

        return siblingProjects;
    }
}
