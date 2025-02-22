package com.example.teamcity.api;

import com.example.teamcity.BaseTest;
import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specifications;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class ProjectTests extends BaseTest {
    @Test(description = "User should be able to create Project with mandatory fields only", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMandatoryFieldsTest() {
        superUserCheckRequests.getRequest(Endpoint.USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));
        userCheckRequests.<Project>getRequest(Endpoint.PROJECTS).create(testData.getProject());
        var createdProject = userCheckRequests.<Project>getRequest(Endpoint.PROJECTS).read(testData.getProject().getId());

        softy.assertEquals(testData.getProject().getId(), createdProject.getId(), "Project id is not correct");
        softy.assertEquals(testData.getProject().getName(), createdProject.getName(), "Project name is not correct");
    }

    @Test(description = "User should be able to create Project with copyAllAssociatedSettings set to true", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsTest() throws JsonProcessingException {

        superUserCheckRequests.getRequest(Endpoint.USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));
        var projectWithCopyAll = generate(Arrays.asList(testData.getProject()), Project.class, testData.getProject().getId(), testData.getProject().getName(), null, true);
        userCheckRequests.<Project>getRequest(Endpoint.PROJECTS).create(projectWithCopyAll);
        var createdProject = userCheckRequests.<Project>getRequest(Endpoint.PROJECTS).read(projectWithCopyAll.getId());
        softy.assertEquals(projectWithCopyAll.getId(), createdProject.getId(), "Project ID does not match");
        softy.assertEquals(projectWithCopyAll.getName(), createdProject.getName(), "Project name does not match");

    }
    @Test(description = "User should be able to create a second Project with parentProject locator set to the first project's ID", groups = {"Positive", "CRUD"})
    public void userCreatesSecondProjectWithParentProjectTest() {
        superUserCheckRequests.getRequest(Endpoint.USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));
        userCheckRequests.<Project>getRequest(Endpoint.PROJECTS).create(testData.getProject());
        var secondProject = generate(Arrays.asList(testData.getProject()), Project.class, RandomData.getString(), RandomData.getString(),
                generate(Arrays.asList(testData.getProject()), ParentProject.class, testData.getProject().getId()));
        userCheckRequests.<Project>getRequest(Endpoint.PROJECTS).create(secondProject);
        var createdSecondProject = userCheckRequests.<Project>getRequest(Endpoint.PROJECTS).read(secondProject.getId());
        softy.assertEquals(createdSecondProject.getParentProject().getId(), testData.getProject().getId(), "Parent project ID is incorrect");
        softy.assertAll();
    }




}
