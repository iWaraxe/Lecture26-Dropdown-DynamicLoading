package com.coherentsolutions.java.webauto.section01.advanced;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

/**
 * Demonstrates handling a multi-select dropdown.
 */
public class Ex04DropdownMultiSelect {

    private WebDriver driver;

    private static final String URL = "https://example.com/multiselect-dropdown";

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
    public void dropdownMultiSelectTest() {
        WebElement dropdownElement = driver.findElement(By.cssSelector("#multi-select-dropdown"));
        Select dropdown = new Select(dropdownElement);

        // Select multiple options
        dropdown.selectByIndex(0);
        dropdown.selectByIndex(1);

        // Verify multiple selections
        List<WebElement> selectedOptions = dropdown.getAllSelectedOptions();
        Assert.assertEquals(selectedOptions.size(), 2);

        // Deselect one option
        dropdown.deselectByIndex(0);

        // Verify remaining selection
        selectedOptions = dropdown.getAllSelectedOptions();
        Assert.assertEquals(selectedOptions.size(), 1);
    }

    public static void main(String[] args) {
        Ex04DropdownMultiSelect test = new Ex04DropdownMultiSelect();
        test.openBrowser();
        test.dropdownMultiSelectTest();
        test.closeBrowser();
    }
}
