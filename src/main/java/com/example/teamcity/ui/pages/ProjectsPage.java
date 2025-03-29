package com.example.teamcity.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.ui.elements.ProjectElement;
import io.qameta.allure.Step;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ProjectsPage extends BasePage {
    private static final String PROJECTS_URL = "/favorite/projects";
    private ElementsCollection projectsElements = $$("span[class*='MiddleEllipsis']");

    private SelenideElement spanFavoriteProjects = $("span[class='ProjectPageHeader__title--ih']");
    private SelenideElement header = $(".MainPanel__router--gF > div");

    @Step("Open projects page")
    public static ProjectsPage open() {
        return Selenide.open(PROJECTS_URL, ProjectsPage.class);
    }

    public ProjectsPage() {
        header.shouldBe(Condition.visible, BASE_WAITING);
    }

    public List<ProjectElement> getProjects() {
        return generatePageElements(projectsElements, ProjectElement::new);
    }

    @Step("Log all visible project blocks")
    public void logVisibleProjects() {
        getProjects().forEach(project -> {
            String name = project.getName().text();
            System.out.println("> " + name);
        });
    }

    @Step("Wait for project with name: {name}")
    public void waitForProjectToAppear(String name) {
        projectsElements.findBy(Condition.text(name))
                .shouldBe(Condition.visible, Duration.ofSeconds(5));
    }


}

