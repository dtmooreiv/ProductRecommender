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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.openqa.selenium.WebDriver;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

public class indexTest{

    //WebDriver driver;

    public static void main(String[] args){

        WebDriver driver = new FirefoxDriver();
        driver.get("http://www.google.com");

        WebElement element = driver.findElement(By.name("q"));
        element.sendKeys("Cheese!");
        element.submit();
        System.out.println("Title of the page is " + driver.getTitle());

        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>(){
                                                 public Boolean apply(WebDriver d) {
                                                     return d.getTitle().toLowerCase().startsWith("Cheese!");
                                                 }
                                              });


        driver.quit();
    }

    /*@Before
    public void startSelenium(){

        driver = new FirefoxDriver();

    }

    @Test
    public void testUserBased() {

    }

    @Test
    public void testProdBased() {

    }

    @Test
    public void testButtons() {

        driver.get("localhost:8080");
        assertEquals(driver.getTitle(), "Recommendation Front End");

    }

    @After
    public void stopSelenium(){

        driver.quit();

    }*/

}