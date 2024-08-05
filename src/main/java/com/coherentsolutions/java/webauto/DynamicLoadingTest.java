package com.coherentsolutions.java.webauto;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DynamicLoadingTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String URL = "https://the-internet.herokuapp.com/dynamic_loading";
    private static final By EXAMPLE_1_LINK = By.cssSelector("[href='/dynamic_loading/1']");
    private static final By EXAMPLE_2_LINK = By.cssSelector("[href='/dynamic_loading/2']");
    private static final By START_BUTTON = By.cssSelector("#start button");
    private static final By FINISH_MESSAGE = By.id("finish");

    @BeforeMethod
    public void openBrowser() {
        // Set up the WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Initialize the WebDriver instance
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(6));
        //driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(URL);
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void hiddenElementTest() {
        driver.findElement(EXAMPLE_1_LINK).click();
        driver.findElement(START_BUTTON).click();

        WebElement message = driver.findElement(FINISH_MESSAGE);
        wait.until(ExpectedConditions.visibilityOf(message));
        String messageText = message.getText();
        Assert.assertTrue(message.isDisplayed());
        Assert.assertEquals(messageText, "Hello World!");
    }

    @Test
    public void nonExistElementTest() {
        driver.findElement(EXAMPLE_2_LINK).click();
        driver.findElement(START_BUTTON).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_MESSAGE));
        WebElement message = driver.findElement(FINISH_MESSAGE);

        String messageText = message.getText();
        Assert.assertTrue(message.isDisplayed());
        Assert.assertEquals(messageText, "Hello World!");
    }
}
