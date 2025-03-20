package com.example.teamcity.api;

import com.example.teamcity.BaseTest;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.Roles;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.responses.ResponseExtractor;
import com.example.teamcity.api.spec.request.RequestSpecs;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;

import java.util.List;

public abstract class BaseApiTest extends BaseTest {

    protected CheckedRequest userCheckedRequest;
    protected UncheckedRequest userUncheckedRequest;

    @BeforeMethod(alwaysRun = true)
    public void configureUserRequests() {
        super.beforeTest();
        Response response = superUserCheckRequests.getRequest(ApiEndpoint.USERS).create(testData.getUser());
        User createdUser = ResponseExtractor.extractModel(response, User.class);
        TestDataStorage.getInstance().addCreatedEntity(ApiEndpoint.USERS, String.valueOf(createdUser.getId()));


        userCheckedRequest = new CheckedRequest(RequestSpecs.authSpec(testData.getUser()));
        userUncheckedRequest = new UncheckedRequest(RequestSpecs.authSpec(testData.getUser()));
    }
    protected Project createProjectAndExtractModel(Project project) {
        Response response = (Response) userCheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(project);
        return ResponseExtractor.extractModel(response, Project.class);
    }

    protected User createUserAndExtractModel(User user) {
        Response response = (Response) userCheckedRequest.getRequest(ApiEndpoint.USERS).create(user);
        return ResponseExtractor.extractModel(response, User.class);
    }

    protected User createUserWithRole(Role role, String projectId) {
        var user = testData.getUser();
        user.setUsername(RandomData.getUniqueName());
        String roleScope = projectId.equals("g") ? "p:_Root" : "p:" + projectId;
        user.setRoles(new Roles(List.of(new com.example.teamcity.api.models.Role(role.getRoleName(), roleScope))));
        superUserCheckRequests.getRequest(ApiEndpoint.USERS).create(user);
        return user;
    }
    protected Project findSingleProjectByLocator(String locatorType, String locatorValue) {
        return (Project) userCheckedRequest.getRequest(ApiEndpoint.PROJECTS)
                .findSingleByLocator(locatorType + ":" + locatorValue)
                .orElse(null);
    }
//    protected Project findSingleProjectByLocatorUnchecked(String locatorType, String locatorValue) {
//        return (Project) userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS)
//                .findSingleByLocator(locatorType + ":" + locatorValue)
//                .orElse(null);
//    }


}