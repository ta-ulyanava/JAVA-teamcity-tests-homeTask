package com.example.teamcity.api.controllers;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import io.restassured.specification.RequestSpecification;

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
    public void createInvalidProject(Project project) {
        uncheckedRequests.getRequest(Endpoint.PROJECTS).create(project)
                .then().assertThat().statusCode(400);
    }
}
