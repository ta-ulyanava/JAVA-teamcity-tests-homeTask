package com.example.teamcity.api.controllers;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.responses.ResponseHandler;
import com.example.teamcity.api.responses.ResponseValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

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

    // Метод для создания проекта
    public Response createProject(Project project) {
        Response response = checkedRequest.<Project>getRequest(Endpoint.PROJECTS).create(project);  // Получаем ответ от запроса
        TestDataStorage.getInstance().addCreatedEntity(Endpoint.PROJECTS, project);  // Добавляем созданный проект в хранилище
        return response;  // Возвращаем ответ
    }

    // Метод для получения проекта по ID
    public Project getProjectById(String projectId) {
        return checkedRequest.<Project>getRequest(Endpoint.PROJECTS).read(projectId);
    }

    // Метод для получения проекта по имени
    public Project getProjectByName(String projectName) {
        return checkedRequest.<Project>getRequest(Endpoint.PROJECTS).read(projectName);
    }

    // Метод для удаления проекта
    public void deleteProject(String projectId) {
        checkedRequest.getRequest(Endpoint.PROJECTS).delete(projectId);
    }

    // Метод для создания вложенных проектов
    public List<Project> createNestedProjects(String parentProjectId, int count) {
        List<Project> nestedProjects = new ArrayList<>();
        String currentParentId = parentProjectId;

        for (int i = 0; i < count; i++) {
            var nestedProject = TestDataGenerator.generate(
                    List.of(),
                    Project.class,
                    RandomData.getString(),
                    RandomData.getString(),
                    new ParentProject(currentParentId, null)
            );

            Response response = createProject(nestedProject);
            ResponseHandler.logResponseDetails(response);
            ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);

            Project createdNestedProject = ResponseHandler.extractAndLogModel(response, Project.class);
            nestedProjects.add(createdNestedProject);
            currentParentId = createdNestedProject.getId(); // обновляем родительский ID для следующего проекта
        }

        return nestedProjects;
    }

    // Метод для создания дочерних проектов (сиблинг проектов)
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

    // Метод для создания некорректного проекта
    public Response createInvalidProject(Project project) {
        return uncheckedRequests.getRequest(Endpoint.PROJECTS).create(project);
    }
}
