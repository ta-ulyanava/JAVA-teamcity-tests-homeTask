package com.example.teamcity.api.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.api.enums.UrlConstant;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class BuildTypePage extends BasePage {
    private final SelenideElement title = $(".ring-heading-heading.ring-global-font.BuildTypePageHeader__heading--De");

    public static BuildTypePage open(String buildTypeId) {
        return Selenide.open(UrlConstant.BUILD_TYPE_PAGE.getUrl().formatted(buildTypeId), BuildTypePage.class);
    }

    public SelenideElement getTitle() {
        return title;
    }
} 