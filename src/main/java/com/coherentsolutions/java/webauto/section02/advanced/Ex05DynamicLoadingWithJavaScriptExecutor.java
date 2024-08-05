package com.coherentsolutions.java.webauto.section02.advanced;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
 * Demonstrates handling dynamic loading using JavaScript Executor.
 */
public class Ex05DynamicLoadingWithJavaScriptExecutor {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor jsExecutor;

    private static final String URL = "https://the-internet.herokuapp.com/dynamic_loading/1";
    private static final By START_BUTTON = By.cssSelector("#start button");
    private static final By FINISH_MESSAGE = By.id("finish");

    @BeforeMethod
    public void openBrowser() {
        // Set up the WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Initialize the WebDriver instance
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        jsExecutor = (JavascriptExecutor) driver;
        driver.get(URL);
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void javascriptExecutorTest() {
        jsExecutor.executeScript("arguments[0].click();", driver.findElement(START_BUTTON));
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_MESSAGE));
        Assert.assertTrue(message.isDisplayed());
        Assert.assertEquals(message.getText(), "Hello World!");
    }

    public static void main(String[] args) {
        Ex05DynamicLoadingWithJavaScriptExecutor test = new Ex05DynamicLoadingWithJavaScriptExecutor();
        test.openBrowser();
        test.javascriptExecutorTest();
        test.closeBrowser();
    }
}
