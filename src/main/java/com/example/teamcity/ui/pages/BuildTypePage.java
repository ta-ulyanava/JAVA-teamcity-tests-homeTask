package com.example.teamcity.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.api.enums.WebRoute;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

/**
 * Page object for the Build Configuration (BuildType) page in TeamCity UI.
 */
public class BuildTypePage extends BasePage {

    private final SelenideElement title = $(".ring-heading-heading.ring-global-font.BuildTypePageHeader__heading--De");

    /**
     * Opens the build configuration page by buildType ID.
     *
     * @param buildTypeId ID of the build configuration
     * @return initialized BuildTypePage
     */
    @Step("Open BuildType page for buildType ID '{buildTypeId}'")
    public static BuildTypePage open(String buildTypeId) {
        return Selenide.open(WebRoute.BUILD_TYPE_PAGE.getUrl().formatted(buildTypeId), BuildTypePage.class);
    }

    /**
     * Returns the title element of the Build Configuration page.
     *
     * @return title Selenide element
     */
    public SelenideElement getTitle() {
        return title;
    }
}
