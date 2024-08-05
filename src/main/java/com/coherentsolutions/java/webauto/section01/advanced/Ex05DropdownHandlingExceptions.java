package com.coherentsolutions.java.webauto.section01.advanced;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Demonstrates handling exceptions when interacting with dropdowns.
 */
public class Ex05DropdownHandlingExceptions {

    private WebDriver driver;

    private static final String URL = "https://the-internet.herokuapp.com/dropdown";

    @BeforeMethod
    public void openBrowser() {
        // Set up the WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Initialize the WebDriver instance
        driver = new ChromeDriver();
        driver.get(URL);
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void dropdownHandlingExceptionsTest() {
        try {
            WebElement dropdownElement = driver.findElement(By.cssSelector("#dropdown"));
            Select dropdown = new Select(dropdownElement);

            // Attempt to select a non-existent option
            dropdown.selectByVisibleText("Non-Existent Option");
        } catch (NoSuchElementException e) {
            System.out.println("Option not found: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Ex05DropdownHandlingExceptions test = new Ex05DropdownHandlingExceptions();
        test.openBrowser();
        test.dropdownHandlingExceptionsTest();
        test.closeBrowser();
    }
}
