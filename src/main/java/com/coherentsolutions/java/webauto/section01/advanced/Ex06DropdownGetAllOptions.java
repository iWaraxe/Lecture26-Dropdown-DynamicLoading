package com.coherentsolutions.java.webauto.section01.advanced;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

/**
 * Demonstrates how to retrieve all options from a dropdown.
 */
public class Ex06DropdownGetAllOptions {

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
    public void dropdownGetAllOptionsTest() {
        WebElement dropdownElement = driver.findElement(By.cssSelector("#dropdown"));
        Select dropdown = new Select(dropdownElement);

        // Retrieve all options from the dropdown
        List<WebElement> options = dropdown.getOptions();
        for (WebElement option : options) {
            System.out.println("Option: " + option.getText());
        }
    }

    public static void main(String[] args) {
        Ex06DropdownGetAllOptions test = new Ex06DropdownGetAllOptions();
        test.openBrowser();
        test.dropdownGetAllOptionsTest();
        test.closeBrowser();
    }
}
