package com.example.teamcity.api.helpers;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.responses.ResponseExtractor;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class for working with TeamCity Projects via API.
 * <p>
 * Includes methods for creating, finding, and asserting project hierarchies.
 */
public class ApiProjectHelper {

    public ApiProjectHelper() {}

    /**
     * Creates a single project via API.
     *
     * @param request request handler
     * @param project project model to create
     * @return created project
     */
    @Step("Create single project: {project.name}")
    public Project createProject(CheckedRequest request, Project project) {
        Response response = (Response) request.getRequest(ApiEndpoint.PROJECTS).create(project);
        return ResponseExtractor.extractModel(response, Project.class);
    }

    /**
     * Creates a list of nested projects, where each next project is a child of the previous.
     *
     * @param request        request handler
     * @param nestedProjects list of projects to nest
     * @return list of created projects with correct parent-child relationships
     */
    @Step("Create nested project hierarchy")
    public List<Project> createNestedProjects(CheckedRequest request, List<Project> nestedProjects) {
        List<Project> created = new ArrayList<>();

        Project parent = createProject(request, nestedProjects.get(0));
        created.add(parent);

        for (int i = 1; i < nestedProjects.size(); i++) {
            Project current = nestedProjects.get(i);

            // Устанавливаем parent явно через объект
            current.setParentProject(new ParentProject(parent.getId(), null));

            parent = createProject(request, current);
            created.add(parent);
        }

        return created;
    }



    /**
     * Creates a list of sibling projects with the same parent.
     *
     * @param request  request handler
     * @param siblings list of sibling projects
     * @return list of created sibling projects
     */
    @Step("Create sibling projects")
    public List<Project> createSiblingProjects(CheckedRequest request, List<Project> siblings) {
        return siblings.stream()
                .map(project -> createProject(request, project))
                .collect(Collectors.toList());
    }

    /**
     * Finds a single project using a given locator.
     *
     * @param request      request handler
     * @param locatorType  locator key (e.g., "name", "id")
     * @param value        locator value
     * @return found project or null
     */
    @Step("Find project by locator: {locatorType}:{value}")
    public Project findProjectByLocator(CheckedRequest request, String locatorType, String value) {
        String locator = locatorType + ":" + value;
        return (Project) request.getRequest(ApiEndpoint.PROJECTS)
                .findFirstEntityByLocatorQuery(locator)
                .orElse(null);
    }

    /**
     * Verifies that a list of projects forms a valid linear hierarchy.
     *
     * @param createdProjects list of projects to verify
     * @param softy           SoftAssert instance for assertions
     */
    @Step("Assert linear hierarchy of created projects")
    public void assertLinearHierarchy(List<Project> createdProjects, SoftAssert softy) {
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

    /**
     * Verifies that a list of sibling projects share the same parent ID.
     *
     * @param createdProjects  list of sibling projects
     * @param expectedParentId parent ID to assert
     * @param softy            SoftAssert instance for assertions
     */
    @Step("Assert sibling hierarchy of created projects")
    public void assertSiblingHierarchy(List<Project> createdProjects, String expectedParentId, SoftAssert softy) {
        createdProjects.forEach(project -> {
            String actualParentId = project.getParentProject() != null ? project.getParentProject().getId() : null;
            softy.assertEquals(
                    actualParentId,
                    expectedParentId,
                    "Parent project ID is incorrect for project " + project.getId()
            );
        });
    }

    /**
     * Creates multiple independent projects.
     *
     * @param request  request handler
     * @param projects list of projects to create
     * @return list of created projects
     */
    @Step("Create multiple independent projects")
    public List<Project> createProjects(CheckedRequest request, List<Project> projects) {
        List<Project> createdProjects = new ArrayList<>();
        for (Project project : projects) {
            createdProjects.add(createProject(request, project));
        }
        return createdProjects;
    }

    /**
     * Finds projects by locator with pagination support.
     *
     * @param request request handler
     * @param locator TeamCity locator query (e.g., name:prefix*)
     * @param count   max number of results
     * @param start   starting index for pagination
     * @return list of found projects
     */
    @Step("Find projects by locator with pagination: {locator}, count={count}, start={start}")
    public List<Project> findProjectsByLocatorWithPagination(CheckedRequest request, String locator, int count, int start) {
        Object result = request.getRequest(ApiEndpoint.PROJECTS)
                .findEntitiesByLocatorQueryWithPagination(locator, count, start);

        if (result instanceof Response) {
            return ResponseExtractor.extractModelList((Response) result, Project.class);
        } else if (result instanceof List<?>) {
            List<Project> projects = new ArrayList<>();
            for (Object obj : (List<?>) result) {
                if (obj instanceof Project) {
                    projects.add((Project) obj);
                }
            }
            return projects;
        }

        return new ArrayList<>();
    }
    /**
     * Waits for a project with the given name to appear in the API within the specified timeout.
     *
     * @param projectName     name of the project to wait for
     * @param timeoutSeconds  maximum wait time in seconds
     * @return found {@link Project}
     * @throws RuntimeException if the project is not found within the timeout
     */
    @Step("Wait for project to appear in API: {projectName}")
    public Project waitForProjectInApi(CheckedRequest request, String projectName, int timeoutSeconds) {
        for (int i = 0; i < timeoutSeconds; i++) {
            var maybeProject = request.<Project>getRequest(ApiEndpoint.PROJECTS)
                    .findFirstEntityByLocatorQuery("name:" + projectName);
            if (maybeProject.isPresent()) return maybeProject.get();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Project with name '" + projectName + "' was not found in API within " + timeoutSeconds + " seconds");
    }

    @Step("Get role scope for project")
    public String getRoleScope(Role role, String projectId) {
        return role == Role.AGENT_MANAGER ? "g" : projectId;
    }

    @Step("Get parent project ID based on role")
    public String getParentProjectId(Role role, String projectId) {
        return role == Role.AGENT_MANAGER ? "_Root" : projectId;
    }
}
