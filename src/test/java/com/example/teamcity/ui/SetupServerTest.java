package com.example.teamcity.ui;

import com.example.teamcity.ui.setup.FirstStartPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;

@Feature("Teamcity Setup")
public class SetupServerTest extends BaseUiTest {
    @Description("Verifies that user can complete initial TeamCity setup: license acceptance and DB config.")
    @Test(groups = {"Setup"})
    public void setupTeamCityServerTest() {
        FirstStartPage page = FirstStartPage.open();
        if (page.isAtFirstStartPage()) {
            page.setupFirstStart();
        }
    }
}
