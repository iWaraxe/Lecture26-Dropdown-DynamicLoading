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
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NoSuchElementException.class);
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void fluentWaitTest() {
        driver.findElement(START_BUTTON).click();
        WebElement message = fluentWait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                return driver.findElement(FINISH_MESSAGE);
            }
        });
        Assert.assertTrue(message.isDisplayed());
        Assert.assertEquals(message.getText(), "Hello World!");
    }

    public static void main(String[] args) {
        Ex03DynamicLoadingWithFluentWait test = new Ex03DynamicLoadingWithFluentWait();
        test.openBrowser();
        test.fluentWaitTest();
        test.closeBrowser();
    }
}
