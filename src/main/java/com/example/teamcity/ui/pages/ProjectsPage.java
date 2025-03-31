package com.example.teamcity.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.ui.elements.ProjectElement;
import io.qameta.allure.Step;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ProjectsPage extends BasePage {

    private static final String PROJECTS_URL = "/favorite/projects";
    private static final String PROJECTS_LOCATOR = "span[class*='MiddleEllipsis']";

    private final SelenideElement header = $(".MainPanel__router--gF > div");

    public ProjectsPage() {
        header.shouldBe(Condition.visible, BASE_WAITING);
    }

    @Step("Open projects page")
    public static ProjectsPage open() {
        return Selenide.open(PROJECTS_URL, ProjectsPage.class);
    }

    private ElementsCollection getProjectsElements() {
        return $$(PROJECTS_LOCATOR);
    }

    @Step("Get visible project names from Projects page")
    public List<String> getVisibleProjectNames() {
        return getProjectsElements()
                .filter(Condition.visible)
                .texts();
    }



    @Step("Wait for project to appear: {name}")
    public void waitForProjectToAppear(String name) {
        getProjectsElements()
                .findBy(Condition.text(name))
                .shouldBe(Condition.visible, Duration.ofSeconds(5));
    }


    @Step("Get project tiles from Projects page")
    public List<ProjectElement> getProjects() {
        return generatePageElements(getProjectsElements(), ProjectElement::new);
    }
}
