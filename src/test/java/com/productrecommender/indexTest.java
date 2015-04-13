package com.productrecommender;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;
import java.util.List;

public class indexTest{

    WebDriver driver;

    @Before
    public void startSelenium(){

        driver = new FirefoxDriver();
        driver.get("localhost:8080");

    }

    @Test
    public void canGetPage() {

        assertEquals(driver.getTitle(), "Recommendation Front End");
    }

    @Test
    public void defaultUserBased(){

        //tests getting recommendations with user based
        assertEquals(driver.getTitle(), "Recommendation Front End");

        //click the button
        WebElement theButton = driver.findElement(By.id("recButton"));
        theButton.click();

        //check out the results
        WebElement theResults = driver.findElement(By.id("resultsContainer"));
        assertNotNull(theResults);
        List<WebElement> resultPics = driver.findElements(By.className("img-responsive"));
        assertEquals(10, resultPics.size());

        List<WebElement> scoreThings = driver.findElements(By.id("scoreLabel"));
        assertEquals(10, scoreThings.size());
        inOrder(scoreThings);
    }

    @Test
    public void checkGoodUserBasedResults(){

        WebElement siteInput = driver.findElement(By.id("siteIdInput"));
        siteInput.clear();

        assertEquals("", siteInput.getText());

        WebElement contactInput = driver.findElement(By.id("contactIdInput"));
        contactInput.clear();

        assertEquals("", contactInput.getText());

        siteInput.sendKeys("13703");
        contactInput.sendKeys("83605079");
        WebElement theButton = driver.findElement(By.id("recButton"));
        theButton.click();

        WebElement theResults = driver.findElement(By.id("resultsContainer"));
        assertNotNull(theResults);
        List<WebElement> resultPics = driver.findElements(By.className("img-responsive"));
        assertEquals(10, resultPics.size());

        List<WebElement> scoreThings = driver.findElements(By.id("scoreLabel"));
        assertEquals(10, scoreThings.size());
        inOrder(scoreThings);
    }

    @Test
    public void checkItemBasedResults(){

        assertEquals(driver.getTitle(), "Recommendation Front End");

        //select item based
        WebElement typeLabel = driver.findElement(By.id("typeLabel"));
        assertEquals(typeLabel.getText(), "Contact ID:");
        List<WebElement> radioButtons = driver.findElements(By.name("recType"));
        radioButtons.get(1).click();
        assertEquals(typeLabel.getText(), "Product ID:");


        //change the inputs for item based

        WebElement siteInput = driver.findElement(By.id("siteIdInput"));
        siteInput.clear();

        assertEquals("", siteInput.getText());

        WebElement contactInput = driver.findElement(By.id("contactIdInput"));
        contactInput.clear();

        assertEquals("", contactInput.getText());

        siteInput.sendKeys("13703");
        contactInput.sendKeys("706348");
        WebElement theButton = driver.findElement(By.id("recButton"));
        theButton.click();

        WebElement theResults = driver.findElement(By.id("resultsContainer"));
        assertNotNull(theResults);
        List<WebElement> resultPics = driver.findElements(By.className("img-responsive"));
        assertEquals(10, resultPics.size());

        List<WebElement> scoreThings = driver.findElements(By.id("scoreLabel"));
        assertEquals(10, scoreThings.size());
        inOrder(scoreThings);

    }

    @Test
    public void testUserResultNumber() {

        //using default siteId and contactId
        WebElement countInput = driver.findElement(By.id("countInput"));
        countInput.clear();
        countInput.sendKeys("5");

        //get the results
        WebElement theButton = driver.findElement(By.id("recButton"));
        theButton.click();

        //check that there are results
        WebElement theResults = driver.findElement(By.id("resultsContainer"));
        assertNotNull(theResults);

        //check how many there are
        List<WebElement> resultPics = driver.findElements(By.className("img-responsive"));
        assertEquals(5, resultPics.size());

        //try with more than 10
        countInput.clear();
        countInput.sendKeys("12");
        theButton.click();

        //check that there are results
        WebElement theResults2 = driver.findElement(By.id("resultsContainer"));
        assertNotNull(theResults2);

        //check how many there are
        List<WebElement> resultPics2 = driver.findElements(By.className("img-responsive"));
        assertEquals(12, resultPics2.size());

    }

    @Test
    public void testItemResultNumber() {

        //setting it to item based mode
        WebElement typeLabel = driver.findElement(By.id("typeLabel"));
        assertEquals(typeLabel.getText(), "Contact ID:");
        List<WebElement> radioButtons = driver.findElements(By.name("recType"));
        radioButtons.get(1).click();
        assertEquals(typeLabel.getText(), "Product ID:");

        //change the inputs for item based
        WebElement siteInput = driver.findElement(By.id("siteIdInput"));
        siteInput.clear();

        assertEquals("", siteInput.getText());

        WebElement contactInput = driver.findElement(By.id("contactIdInput"));
        contactInput.clear();

        assertEquals("", contactInput.getText());

        siteInput.sendKeys("13703");
        contactInput.sendKeys("706348");

        //setting low count
        WebElement countInput = driver.findElement(By.id("countInput"));
        countInput.clear();
        countInput.sendKeys("5");

        //get the results
        WebElement theButton = driver.findElement(By.id("recButton"));
        theButton.click();

        //check that there are results
        WebElement theResults = driver.findElement(By.id("resultsContainer"));
        assertNotNull(theResults);

        //check how many there are
        List<WebElement> resultPics = driver.findElements(By.className("img-responsive"));
        assertEquals(5, resultPics.size());

        //try with more than 10
        countInput.clear();
        countInput.sendKeys("12");
        theButton.click();

        //check that there are results
        WebElement theResults2 = driver.findElement(By.id("resultsContainer"));
        assertNotNull(theResults2);

        //check how many there are
        List<WebElement> resultPics2 = driver.findElements(By.className("img-responsive"));
        assertEquals(12, resultPics2.size());

    }

    public void inOrder(List<WebElement> scoreThings){

        double[] rawScores = new double[scoreThings.size()];
        double[] sortedScores = new double[scoreThings.size()];

        for (int i = 0; i < 10; i++){

            rawScores[i] = Double.parseDouble(scoreThings.get(i).getText());
            sortedScores[i] = rawScores[i];

        }
        Arrays.sort(sortedScores);
        for (int j = 0; j < 10; j++) {
            assertEquals(rawScores[j], sortedScores[9 - j], .000001);
        }

    }

    @After
    public void stopSelenium(){

         driver.quit();

    }

}