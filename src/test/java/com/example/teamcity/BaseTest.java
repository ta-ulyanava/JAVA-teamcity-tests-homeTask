package com.example.teamcity;

import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.spec.Specifications;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.asserts.SoftAssert;


public class BaseTest {
    //сохраняем ассерты в переменной
    protected SoftAssert softy;
 // Самый первый запрос - под супер юзером всегда, для создания первой сущности,
 // поэтому убираем юзер реквестер из теста и выносим его в BaseTEst
// Запрос будет использоваться в API и в UI
    protected CheckedRequests superUserCheckRequests = new CheckedRequests(Specifications.superUserAuthSpec());
//(alwaysRun = true)  -- выполняем даже если тест упал
    @BeforeMethod(alwaysRun = true)
    public void beforeTest() {
        softy = new SoftAssert();

    }
    @AfterMethod(alwaysRun = true)
    public void afterTest(){
        //накопили ассерты и можем падать
        softy.assertAll();
    }
}
