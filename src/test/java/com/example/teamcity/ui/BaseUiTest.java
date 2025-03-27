package com.example.teamcity.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.example.teamcity.BaseTest;
import com.example.teamcity.api.config.Config;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.User;
import com.example.teamcity.ui.pages.LoginPage;
import io.qameta.allure.selenide.AllureSelenide;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import java.util.Map;

import static io.qameta.allure.Allure.step;

public class BaseUiTest extends BaseTest {
    @BeforeSuite(alwaysRun = true)
    public void setupUiTest() {
        SelenideLogger.addListener("allure", new AllureSelenide().screenshots(true).savePageSource(true));
        Configuration.browser = Config.getProperty("browser");
        Configuration.baseUrl = "http://" + Config.getProperty("host");
        Configuration.remote= Config.getProperty("remote");
        Configuration.browserSize=Config.getProperty("browserSize");
        Configuration.browserCapabilities.setCapability("selenoid:options", Map.of("enableVNC", true, "enableLog", true));
    }

    @AfterMethod
    public void closeWebDriver(){
        Selenide.closeWebDriver();
    }
    protected void loginAs(User user){
        superUserCheckRequests.getRequest(ApiEndpoint.USERS).create(testData.getUser());
        LoginPage.open().login(testData.getUser());
    }
}
