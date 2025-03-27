package com.example.teamcity;

import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.TestData;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.spec.request.RequestSpecs;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.asserts.SoftAssert;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;
import static io.qameta.allure.Allure.step;

@Listeners({io.qameta.allure.testng.AllureTestNg.class})
public class BaseTest {
    protected SoftAssert softy;
    protected TestData testData;
    protected CheckedRequest superUserCheckRequests = new CheckedRequest(RequestSpecs.superUserAuthSpec());


//    @BeforeMethod(alwaysRun = true)
//    public void beforeTest() {
//        try {
//            softy = new SoftAssert();
//            testData = generate();
//        } catch (Exception e) {
//            System.err.println("Ошибка в генерации данных beforeTest: " + e.getMessage());
//        }
//
//    }


    @BeforeMethod(alwaysRun = true)
    public void beforeTest() {
        try {
            System.out.println("🎯 beforeTest: Running test setup");
            step("🔧 Инициализация тестовых данных");
            softy = new SoftAssert();
            testData = generate();
        } catch (Exception e) {
            System.err.println("❌ Ошибка в генерации данных beforeTest: " + e.getMessage());
        }
    }



    @AfterMethod(alwaysRun = true)
    public void afterTest() {
        try {
            softy.assertAll();
            TestDataStorage.getInstance().deleteCreatedEntities(); // Удаление созданных сущностей
        } catch (AssertionError e) {
            System.err.println("Ошибка в softAssert.assertAll() " + e.getMessage());
        }
    }



}
