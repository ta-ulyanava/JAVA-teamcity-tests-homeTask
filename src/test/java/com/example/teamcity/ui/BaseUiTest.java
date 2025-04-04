package com.example.teamcity.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.example.teamcity.BaseTest;
import com.example.teamcity.api.config.Config;
import com.example.teamcity.api.helpers.ApiBuildTypeHelper;
import com.example.teamcity.api.helpers.ApiProjectHelper;
import com.example.teamcity.api.helpers.ApiUserHelper;
import com.example.teamcity.api.models.User;
import com.example.teamcity.ui.helpers.UiBuildTypeHelper;
import com.example.teamcity.ui.helpers.UiLoginHelper;
import com.example.teamcity.ui.helpers.UiProjectHelper;
import io.qameta.allure.selenide.AllureSelenide;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.util.Map;

public class BaseUiTest extends BaseTest {

    protected UiProjectHelper uiProjectHelper;
    protected UiBuildTypeHelper uiBuildTypeHelper;
    protected UiLoginHelper uiLoginHelper;

    protected ApiProjectHelper projectHelper;
    protected ApiBuildTypeHelper buildTypeHelper;
    protected ApiUserHelper userHelper;

    @BeforeSuite(alwaysRun = true)
    public void setupUiSuite() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(true).savePageSource(true));
        Configuration.browser = Config.getProperty("browser");
        Configuration.baseUrl = "http://" + Config.getProperty("host");
        Configuration.remote = Config.getProperty("remote");
        Configuration.browserSize = Config.getProperty("browserSize");
        Configuration.browserCapabilities.setCapability("selenoid:options", Map.of("enableVNC", true, "enableLog", true));
    }

    @BeforeMethod(alwaysRun = true)
    public void setupUiTest() {
        projectHelper = new ApiProjectHelper();
        buildTypeHelper = new ApiBuildTypeHelper(superUserCheckRequests);
        userHelper = new ApiUserHelper(superUserCheckRequests);
        uiProjectHelper = new UiProjectHelper();
        uiBuildTypeHelper = new UiBuildTypeHelper(superUserCheckRequests);
        uiLoginHelper = new UiLoginHelper(superUserCheckRequests);
    }

    @AfterMethod(alwaysRun = true)
    public void closeWebDriver() {
        Selenide.closeWebDriver();
    }

    protected void loginAs(User user) {
        uiLoginHelper.loginAs(user);
    }
}
