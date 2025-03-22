package com.example.teamcity.api.requests.helpers;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.responses.ResponseExtractor;
import io.restassured.response.Response;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ProjectHelper {
    private ProjectHelper() {}

    public static Project createProject(CheckedRequest request, Project project) {
        Response response = (Response) request.getRequest(ApiEndpoint.PROJECTS).create(project);
        return ResponseExtractor.extractModel(response, Project.class);
    }


    public static List<Project> createNestedProjects(CheckedRequest request, List<Project> nestedProjects) {
        List<Project> created = new ArrayList<>();
        Project parent = createProject(request, nestedProjects.get(0));
        created.add(parent);

        for (int i = 1; i < nestedProjects.size(); i++) {
            Project current = nestedProjects.get(i);
            current.setParentProject(new ParentProject(parent.getId(), null));
            parent = createProject(request, current);
            created.add(parent);
        }

        return created;
    }
    public static List<Project> createSiblingProjects(CheckedRequest request, List<Project> siblings) {
        return siblings.stream()
                .map(project -> createProject(request, project))
                .collect(Collectors.toList());
    }


    public static Project findProjectByLocator(CheckedRequest request, String locatorType, String value) {
        String locator = locatorType + ":" + value;
        return (Project) request.getRequest(ApiEndpoint.PROJECTS)
                .findFirstEntityByLocatorQuery(locator)
                .orElse(null);
    }
    public static void assertLinearHierarchy(List<Project> createdProjects, SoftAssert softy) {
        for (int i = 0; i < createdProjects.size(); i++) {
            Project current = createdProjects.get(i);
            String expectedParentId = (i == 0) ? "_Root" : createdProjects.get(i - 1).getId();
            String actualParentId = current.getParentProject() != null ? current.getParentProject().getId() : null;

            if (i == 0) {
                softy.assertEquals(actualParentId, "_Root", "Root project should have '_Root' as a parent");
            } else {
                softy.assertEquals(actualParentId, expectedParentId,
                        "Parent project ID is incorrect for project " + current.getId());
            }
        }
    }
    public static void assertSiblingHierarchy(List<Project> createdProjects, String expectedParentId, SoftAssert softy) {
        createdProjects.forEach(project -> {
            String actualParentId = project.getParentProject() != null ? project.getParentProject().getId() : null;
            softy.assertEquals(
                    actualParentId,
                    expectedParentId,
                    "Parent project ID is incorrect for project " + project.getId()
            );
        });
    }

}
