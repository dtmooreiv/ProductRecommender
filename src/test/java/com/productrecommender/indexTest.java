package com.productrecommender;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

public class indexTest {

    private Selenium selenium;

    @BeforeClass
    public void startSelenium(){

        this.selenium = new DefaultSelenium("localhost", 4444, "firefox", "localhost:8080");
        this.selenium.start();

    }

    @Test
    @Parameters
    public void testUserBased() {

    }

    @Test
    public void testProdBased() {

    }

    @Test
    public void testButtons() {

    }

    @AfterClass(alwaysRun = true)
    public void stopSelenium(){
        this.selenium.stop();
    }

}