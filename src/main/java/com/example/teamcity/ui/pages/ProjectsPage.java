package com.example.teamcity.ui.pages;

import com.codeborne.selenide.*;
import com.example.teamcity.ui.elements.ProjectElement;
import io.qameta.allure.Step;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ProjectsPage extends BasePage {
    private static final String PROJECTS_URL = "/favorite/projects";

    private ElementsCollection projectsElements = $$("div[class*='Subproject_container']");
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

}
