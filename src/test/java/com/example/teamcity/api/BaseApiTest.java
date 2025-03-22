package com.example.teamcity.api;

import com.example.teamcity.BaseTest;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.requests.checked.CheckedBase;
import com.example.teamcity.api.requests.helpers.UserHelper;
import com.example.teamcity.api.responses.ResponseExtractor;
import com.example.teamcity.api.spec.request.RequestSpecs;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;

public abstract class BaseApiTest extends BaseTest {

    protected CheckedRequest userCheckedRequest;
    protected UncheckedRequest userUncheckedRequest;
    protected UncheckedRequest superUserUncheckedRequest = new UncheckedRequest(RequestSpecs.superUserAuthSpec());

    @BeforeMethod(alwaysRun = true)
    public void configureUserRequests() {
        super.beforeTest();
        Response response = superUserUncheckedRequest.getRequest(ApiEndpoint.USERS).create(testData.getUser());  // Исправлено на superUserUncheckedRequest
        User createdUser = ResponseExtractor.extractModel(response, User.class);
        TestDataStorage.getInstance().addCreatedEntity(ApiEndpoint.USERS, String.valueOf(createdUser.getId()));

        userCheckedRequest = new CheckedRequest(RequestSpecs.authSpec(testData.getUser()));
        userUncheckedRequest = new UncheckedRequest(RequestSpecs.authSpec(testData.getUser()));
    }

    protected User createUserAndExtractModel(User user) {
        Response response = (Response) userCheckedRequest.getRequest(ApiEndpoint.USERS).create(user);
        return ResponseExtractor.extractModel(response, User.class);
    }

    protected User createUserWithRole(Role role, String projectId) {
        return UserHelper.createUserWithRole(userCheckedRequest, testData.getUser(), role, projectId);  // заменено на userCheckedRequest

    }

    protected CheckedBase<Project> getCheckedProjectRequest() {
        return userCheckedRequest.getRequest(ApiEndpoint.PROJECTS, Project.class);
    }
}
