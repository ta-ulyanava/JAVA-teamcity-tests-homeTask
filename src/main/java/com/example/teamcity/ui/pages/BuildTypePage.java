package com.example.teamcity.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.api.enums.WebRoute;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class BuildTypePage extends BasePage {
    private final SelenideElement title = $(".ring-heading-heading.ring-global-font.BuildTypePageHeader__heading--De");

    @Step("Open BuildType page")
    public static BuildTypePage open(String buildTypeId) {
        return Selenide.open(WebRoute.BUILD_TYPE_PAGE.getUrl().formatted(buildTypeId), BuildTypePage.class);
    }

    public SelenideElement getTitle() {
        return title;
    }
} 