package com.example.teamcity.ui.helpers;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.ui.pages.LoginPage;
import io.qameta.allure.Step;

public class UiLoginHelper {

    @Step("Login as user '{user.username}'")
    public static void loginAs(User user, CheckedRequest requests) {
        requests.getRequest(ApiEndpoint.USERS).create(user);
        LoginPage.open().login(user);
    }
}
