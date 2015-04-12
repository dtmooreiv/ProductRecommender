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

import java.util.LinkedList;
import java.util.List;

public class indexTest{

    WebDriver driver;
    boolean setupDone = false;
    int numTests = 3;
    int testsDone = 0;

    @Before
    public void startSelenium(){

        driver = new FirefoxDriver();
        setupDone = true;

    }

    @Test
    public void canGetPage() {

        driver.get("localhost:8080");
        assertEquals(driver.getTitle(), "Recommendation Front End");
    }

    @Test
    public void defaultUserBased(){

        //tests getting recommendations with user based
        driver.get("localhost:8080");
        assertEquals(driver.getTitle(), "Recommendation Front End");

        WebElement theButton = driver.findElement(By.id("recButton"));
        theButton.click();
        WebElement theResults = driver.findElement(By.id("resultsContainer"));
        assertNotNull(theResults);
    }

    @Test
    public void resultsInOrder(){

        //tests that the results seem to be in the correct order
        driver.get("localhost:8080");
        assertEquals(driver.getTitle(), "Recommendation Front End");

        WebElement theButton = driver.findElement(By.id("recButton"));
        theButton.click();
        WebElement theResults = driver.findElement(By.id("resultsContainer"));
        assertNotNull(theResults);
        List<WebElement> scoreThings = driver.findElements(By.name("score"));
        //assert(scoreThings.size() == 10);
        for (int i = 0; i < scoreThings.size(); i++){

            if (i > 0){
                assert(Double.parseDouble(scoreThings.get(i).getText()) > Double.parseDouble(scoreThings.get(i - 1).getText()));
            }

        }
        testsDone++;

    }

    @After
    public void stopSelenium(){

         driver.quit();

    }

}