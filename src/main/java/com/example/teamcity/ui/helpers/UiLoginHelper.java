package com.example.teamcity.ui.helpers;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.ui.pages.LoginPage;
import io.qameta.allure.Step;

public class UiLoginHelper {

    private final CheckedRequest checkedRequest;

    public UiLoginHelper(CheckedRequest checkedRequest) {
        this.checkedRequest = checkedRequest;
    }

    @Step("Login as user '{user.username}'")
    public void loginAs(User user) {
        checkedRequest.getRequest(ApiEndpoint.USERS).create(user);
        LoginPage.open().login(user);
    }
}
