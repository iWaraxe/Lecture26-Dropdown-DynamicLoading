package com.coherentsolutions.java.webauto.section02.advanced;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
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
 * Demonstrates handling timeout exceptions in dynamic loading scenarios.
 */
public class Ex04DynamicLoadingHandlingTimeout {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String URL = "https://the-internet.herokuapp.com/dynamic_loading/1";
    private static final By START_BUTTON = By.cssSelector("#start button");
    private static final By FINISH_MESSAGE = By.id("finish");

    @BeforeMethod
    public void openBrowser() {
        // Set up the WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Initialize the WebDriver instance
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        driver.get(URL);
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void handleTimeoutTest() {
        driver.findElement(START_BUTTON).click();
        try {
            WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_MESSAGE));
            Assert.assertTrue(message.isDisplayed());
            Assert.assertEquals(message.getText(), "Hello World!");
        } catch (TimeoutException e) {
            System.out.println("Timeout while waiting for the finish message to appear.");
        }
    }

    public static void main(String[] args) {
        Ex04DynamicLoadingHandlingTimeout test = new Ex04DynamicLoadingHandlingTimeout();
        test.openBrowser();
        test.handleTimeoutTest();
        test.closeBrowser();
    }
}
