package com.coherentsolutions.java.webauto.section02;

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
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

/**
 * Demonstrates handling dynamic loading with non-existent elements initially.
 */
public class Ex02DynamicLoadingExample2 {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String URL = "https://the-internet.herokuapp.com/dynamic_loading/2";
    private static final By START_BUTTON = By.cssSelector("#start button");
    private static final By FINISH_MESSAGE = By.id("finish");

    @BeforeMethod
    public void openBrowser() {
        // Set up the WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Initialize the WebDriver instance
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void nonExistElementTest() {
        driver.findElement(START_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_MESSAGE));
        WebElement message = driver.findElement(FINISH_MESSAGE);
        String messageText = message.getText();
        Assert.assertTrue(message.isDisplayed());
        Assert.assertEquals(messageText, "Hello World!");
    }

    public static void main(String[] args) {
        Ex02DynamicLoadingExample2 test = new Ex02DynamicLoadingExample2();
        test.openBrowser();
        test.nonExistElementTest();
        test.closeBrowser();
    }
}
