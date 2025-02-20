package com.example.teamcity;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.asserts.SoftAssert;

import java.lang.ref.SoftReference;

public class BaseTest {
    //сохраняем ассерты в переменной
    protected SoftAssert softy;
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
