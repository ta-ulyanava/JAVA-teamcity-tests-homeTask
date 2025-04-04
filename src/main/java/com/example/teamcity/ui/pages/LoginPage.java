package com.example.teamcity.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.api.models.User;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

/**
 * Page object representing the TeamCity login page.
 */
public class LoginPage extends BasePage {

    private static final String LOGIN_URL = "/login.html";

    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement loginButton = $(".loginButton");

    /**
     * Opens the TeamCity login page.
     *
     * @return initialized LoginPage
     */
    @Step("Open login page")
    public static LoginPage open() {
        return Selenide.open(LOGIN_URL, LoginPage.class);
    }

    /**
     * Logs in using provided user credentials and navigates to the projects page.
     *
     * @param user TeamCity user with valid credentials
     * @return ProjectsPage after successful login
     */
    @Step("Login as user: {user.username}")
    public ProjectsPage login(User user) {
        usernameInput.val(user.getUsername());
        passwordInput.val(user.getPassword());
        loginButton.click();
        return Selenide.page(ProjectsPage.class);
    }
}
