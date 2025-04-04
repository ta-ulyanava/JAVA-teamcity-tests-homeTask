package com.example.teamcity.api.generators.domain;

import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import io.qameta.allure.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for generating test data related to TeamCity Projects.
 */
public class ProjectTestData {

    /**
     * Generates a unique project ID using a random UUID.
     *
     * @return a unique project ID
     */
    @Step("Generate unique project ID")
    public static String projectId() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates a list of projects with unique values.
     *
     * @param count number of projects to create
     * @return list of uniquely generated projects
     */
    @Step("Create {count} projects with unique values")
    public static List<Project> createProjects(int count) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Project project = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), "name", RandomData.getUniqueName());
            projects.add(project);
        }
        return projects;
    }

    /**
     * Generates a list of projects with a common part in a specified field.
     *
     * @param count      number of projects to create
     * @param field      field to insert common part
     * @param commonPart shared value prefix
     * @return list of projects with a shared field prefix
     */
    @Step("Create {count} projects with common part '{commonPart}' in field '{field}'")
    public static List<Project> createProjectsWithCommonPart(int count, String field, String commonPart) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String fieldValue = commonPart + "_" + RandomData.getUniqueName();
            Project project = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), field, fieldValue);
            if (project.getParentProject() == null) {
                ParentProject parent = new ParentProject();
                parent.setId("_Root");
                project.setParentProject(parent);
            }
            projects.add(project);
        }
        return projects;
    }

    /**
     * Generates a list of projects with the same value in a specified field.
     *
     * @param count      number of projects to create
     * @param field      field to be set
     * @param fieldValue value to set for all projects
     * @return list of projects with identical field values
     */
    @Step("Create {count} projects with same value '{fieldValue}' in field '{field}'")
    public static List<Project> createProjectsWithSameFieldValue(int count, String field, String fieldValue) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Project project = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), field, fieldValue);
            projects.add(project);
        }
        return projects;
    }

    /**
     * Creates a nested hierarchy of projects.
     *
     * @param count number of nested projects
     * @return list of nested projects
     */
    @Step("Create nested project hierarchy of depth {count}")
    public static List<Project> nestedProjects(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        List<Project> nestedProjects = new ArrayList<>();
        String currentParentId = "_Root";
        for (int i = 0; i < count; i++) {
            Project project = TestDataGenerator.generate(
                    Project.class,
                    RandomData.getUniqueName(),
                    projectId(),
                    new ParentProject(currentParentId, null)
            );
            nestedProjects.add(project);
            currentParentId = project.getId();
        }
        return nestedProjects;
    }

    /**
     * Creates a list of sibling projects sharing the same parent ID.
     *
     * @param parentId parent project ID
     * @param count    number of siblings to create
     * @return list of sibling projects
     */
    @Step("Create {count} sibling projects with parent ID '{parentId}'")
    public static List<Project> siblingProjects(String parentId, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        List<Project> siblingProjects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Project project = TestDataGenerator.generate(
                    Project.class,
                    RandomData.getUniqueName(),
                    projectId(),
                    new ParentProject(parentId, null)
            );
            siblingProjects.add(project);
        }
        return siblingProjects;
    }

    /**
     * Creates a list of projects with an exact value in a specified field.
     *
     * @param count      number of projects to create
     * @param field      field to be set
     * @param exactValue exact value to set for all projects
     * @return list of projects with the exact value
     */
    @Step("Create {count} projects with exact value '{exactValue}' in field '{field}'")
    public static List<Project> createProjectsWithExactFieldValue(int count, String field, String exactValue) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String projectId = RandomData.getUniqueId();
            Project project = new Project();
            project.setId(projectId);
            try {
                java.lang.reflect.Field classField = Project.class.getDeclaredField(field);
                classField.setAccessible(true);
                classField.set(project, exactValue);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Cannot set field " + field, e);
            }
            ParentProject parent = new ParentProject();
            parent.setId("_Root");
            project.setParentProject(parent);
            projects.add(project);
        }
        return projects;
    }

    /**
     * Creates a list of projects with a common name prefix and numeric suffix.
     *
     * @param count      number of projects to create
     * @param prefixName name prefix
     * @return list of projects with names like prefix_1, prefix_2, ...
     */
    @Step("Create {count} projects with name prefix '{prefixName}' and numeric suffix")
    public static List<Project> createProjectsWithPrefixAndNumericSuffix(int count, String prefixName) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String projectId = RandomData.getUniqueId();
            Project project = new Project();
            project.setId(projectId);
            project.setName(prefixName + "_" + (i + 1));
            ParentProject parent = new ParentProject();
            parent.setId("_Root");
            project.setParentProject(parent);
            projects.add(project);
        }
        return projects;
    }
}
