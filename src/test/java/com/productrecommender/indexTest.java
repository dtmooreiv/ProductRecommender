package com.productrecommender;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import junit.framework.TestCase;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.WebDriver;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

public class indexTest{

    WebDriver driver;
    boolean setupDone = false;

    @Before
    public void startSelenium(){

        driver = new FirefoxDriver();

    }

    @Test
    public void canGetPage() {

        driver.get("localhost:8080");
        assertEquals(driver.getTitle(), "Recommendation Front End");

    }

    @After
    public void stopSelenium(){

        driver.quit();

    }

}