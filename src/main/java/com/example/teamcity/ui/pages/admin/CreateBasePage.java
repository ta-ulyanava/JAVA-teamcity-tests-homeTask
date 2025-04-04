package com.example.teamcity.ui.pages.admin;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.ui.pages.BasePage;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

/**
 * Base page for creating TeamCity objects (projects, build configurations, etc.).
 * <p>
 * Provides shared form controls and actions used across create pages.
 */
public abstract class CreateBasePage extends BasePage {

    protected static final String CREATE_URL = "/admin/createObjectMenu.html?projectId=%s&showMode=%s";

    protected SelenideElement urlInput = $("#url");
    protected SelenideElement submitButton = $(Selectors.byAttribute("value", "Proceed"));
    protected SelenideElement buildTypeNameInput = $("#buildTypeName");
    protected SelenideElement connectionSuccessfulMessage = $(".connectionSuccessful");

    /**
     * Fills in the URL input and verifies that the connection is successful after clicking proceed.
     *
     * @param url the URL to enter
     */
    @Step("Submit creation form with URL: {url}")
    protected void baseCreateForm(String url) {
        urlInput.val(url);
        submitButton.click();
        connectionSuccessfulMessage.should(Condition.appear, BASE_WAITING);
    }
}
