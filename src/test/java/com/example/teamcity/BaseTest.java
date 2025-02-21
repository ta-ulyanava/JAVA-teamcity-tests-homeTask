package com.example.teamcity;

import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.TestData;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.spec.Specifications;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.asserts.SoftAssert;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;

public class BaseTest {
    //сохраняем ассерты в переменной
    protected SoftAssert softy;
    protected TestData testData;
 // Самый первый запрос - под супер юзером всегда, для создания первой сущности,
 // поэтому убираем юзер реквестер из теста и выносим его в BaseTEst
// Запрос будет использоваться в API и в UI
    protected CheckedRequests superUserCheckRequests = new CheckedRequests(Specifications.superUserAuthSpec());
//(alwaysRun = true)  -- выполняем даже если тест упал
@BeforeMethod(alwaysRun = true)
public void beforeTest() {
    softy = new SoftAssert();
    testData = generate();
}
    @AfterMethod(alwaysRun = true)
    public void afterTest(){
        //накопили ассерты и можем падать
        softy.assertAll();
        TestDataStorage.getInstance().deleteCreatedEntities();
    }
}
