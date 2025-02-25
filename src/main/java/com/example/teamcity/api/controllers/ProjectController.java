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
            ResponseHandler.logIfError(response); // Теперь логируем только ошибки

            Project createdNestedProject = ResponseHandler.extractAndLogModel(response, Project.class);
            nestedProjects.add(createdNestedProject);
            currentParentId = createdNestedProject.getId(); // Обновляем родительский ID для следующего проекта
        }

        return nestedProjects;
    }

    // Метод для пакетного создания sibling-проектов
    // Метод для последовательного создания sibling-проектов
    public List<Project> createSiblingProjects(String parentProjectId, int count) {
        List<Project> siblingProjects = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Генерируем новый проект
            Project siblingProject = TestDataGenerator.generate(
                    List.of(),
                    Project.class,
                    RandomData.getUniqueName(),
                    RandomData.getUniqueId(),
                    new ParentProject(parentProjectId, null)
            );

            // Создаём проект через API
            Response response = createProject(siblingProject);

            // Логируем ошибку, если есть
            ResponseHandler.logIfError(response);

            // Проверяем, что проект успешно создан
            ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);

            // Извлекаем и добавляем в список
            siblingProjects.add(ResponseHandler.extractAndLogModel(response, Project.class));
        }

        return siblingProjects;
    }


    // Метод для создания некорректного проекта
    public Response createInvalidProject(Project project) {
        return uncheckedRequests.getRequest(Endpoint.PROJECTS).create(project);
    }



}
