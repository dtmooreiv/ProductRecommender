package com.productrecommender;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import junit.framework.TestCase;
import org.junit.runners.Parameterized;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

public class indexTest{

    private static Selenium selenium;
    private int testInt = 1;

    @Before
    public void startSelenium(){
        //set up redis?
        this.selenium = new DefaultSelenium("localhost", 8080, "Firefox", "localhost:8080");
        this.selenium.start();
        this.testInt = 2;

    }

    @Test
    public void testUserBased() {

    }

    @Test
    public void testProdBased() {

    }

    @Test
    public void testButtons() {

        assertEquals(2, this.testInt);
        this.selenium.click("identifier=recButton");
        this.selenium.waitForPageToLoad("30000");
        assertEquals("Recommendation Front End", this.selenium.getTitle());
        assert(true);

    }

    @After
    public void stopSelenium(){
        this.selenium.stop();
    }

}