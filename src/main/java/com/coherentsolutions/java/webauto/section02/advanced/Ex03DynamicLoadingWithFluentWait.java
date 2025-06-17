package com.coherentsolutions.java.webauto.section02.advanced;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.function.Function;

/**
 * Demonstrates handling dynamic loading using FluentWait.
 */
public class Ex03DynamicLoadingWithFluentWait {

    private WebDriver driver;
    private FluentWait<WebDriver> fluentWait;

    private static final String URL = "https://the-internet.herokuapp.com/dynamic_loading/1";
    private static final By START_BUTTON = By.cssSelector("#start button");
    private static final By FINISH_MESSAGE = By.id("finish");

    @BeforeMethod
    public void openBrowser() {
        // Set up the WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Initialize the WebDriver instance
        driver = new ChromeDriver();
        driver.get(URL);
        fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class);
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void fluentWaitTest() {
        // Click the Start button after ensuring it is clickable
        fluentWait.until(ExpectedConditions.elementToBeClickable(START_BUTTON)).click();

        // Wait for the loading spinner to disappear
        fluentWait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loading")));

        // Wait for the finish message to become visible
        WebElement message = fluentWait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_MESSAGE));

        // Log the message text for debugging
        System.out.println("Message Text: " + message.getText());

        // Validate the message visibility and content
        Assert.assertTrue(message.isDisplayed(), "Finish message is not displayed.");
        Assert.assertEquals(message.getText().trim(), "Hello World!", "Finish message text is incorrect.");
    }

    public static void main(String[] args) {
        Ex03DynamicLoadingWithFluentWait test = new Ex03DynamicLoadingWithFluentWait();
        test.openBrowser();
        test.fluentWaitTest();
        test.closeBrowser();
    }
}
