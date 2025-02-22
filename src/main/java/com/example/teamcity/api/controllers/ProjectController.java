package com.example.teamcity.api.controllers;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;

public class ProjectController {
    private final CheckedRequest checkedRequest;
    private final UncheckedRequest uncheckedRequests;

    public ProjectController(RequestSpecification spec) {
        this.checkedRequest = new CheckedRequest(spec);
        this.uncheckedRequests = new UncheckedRequest(spec);
    }

    /** ✅ Создание проекта (позитивный сценарий) */
    public void createProject(Project project) {
        checkedRequest.<Project>getRequest(Endpoint.PROJECTS).create(project);
    }

    /** ✅ Чтение проекта (позитивный сценарий) */
    public Project getProject(String projectId) {
        return checkedRequest.<Project>getRequest(Endpoint.PROJECTS).read(projectId);
    }

    /** ✅ Удаление проекта (позитивный сценарий) */
    public void deleteProject(String projectId) {
        checkedRequest.getRequest(Endpoint.PROJECTS).delete(projectId);
    }

    /** ❌ Попытка создать проект с некорректными данными (негативный сценарий) */
    /** ❌ Попытка создать проект с некорректными данными (негативный сценарий) */
    public Response createInvalidProject(Project project) {
        return uncheckedRequests.getRequest(Endpoint.PROJECTS).create(project);
    }

    /** ✅ Создание вложенных проектов */
    public List<Project> createNestedProjects(String rootProjectId, int count) {
        List<Project> nestedProjects = new ArrayList<>();
        String parentProjectId = rootProjectId;

        for (int i = 0; i < count; i++) {
            var nestedProject = TestDataGenerator.generate(
                    List.of(), // Никакие другие проекты не участвуют в генерации
                    Project.class,
                    RandomData.getString(), // Новый ID проекта
                    RandomData.getString(), // Новое имя проекта
                    new ParentProject(parentProjectId, null) // Родитель — предыдущий проект
            );

            createProject(nestedProject);
            nestedProjects.add(nestedProject);
            parentProjectId = nestedProject.getId(); // Делаем новый проект родителем следующего
        }

        return nestedProjects;
    }

}
