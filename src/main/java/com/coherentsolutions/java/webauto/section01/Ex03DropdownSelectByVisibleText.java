package com.coherentsolutions.java.webauto.section01;

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

/**
 * Demonstrates selecting a dropdown option by visible text.
 */
public class Ex03DropdownSelectByVisibleText {

    private WebDriver driver;

    private static final String URL = "https://the-internet.herokuapp.com/dropdown";
    private static final String EXPECTED_SELECTED_OPTION = "Option 1";

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
    public void dropdownSelectByVisibleTextTest() {
        WebElement dropdownElement = driver.findElement(By.cssSelector("#dropdown"));
        Select dropdown = new Select(dropdownElement);

        // Select option by visible text
        dropdown.selectByVisibleText(EXPECTED_SELECTED_OPTION);

        String actualSelectedOption = dropdown.getFirstSelectedOption().getText();
        Assert.assertEquals(actualSelectedOption, EXPECTED_SELECTED_OPTION);
    }

    public static void main(String[] args) {
        Ex03DropdownSelectByVisibleText test = new Ex03DropdownSelectByVisibleText();
        test.openBrowser();
        test.dropdownSelectByVisibleTextTest();
        test.closeBrowser();
    }
}
