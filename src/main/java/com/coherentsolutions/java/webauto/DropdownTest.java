package com.coherentsolutions.java.webauto;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DropdownTest {

    private WebDriver driver;

    private static final String URL = "https://the-internet.herokuapp.com/dropdown";
    private static final String EXPECTED_SELECTED_OPTION = "Option 1";
    private static final String VALUE = "1";

    private static final By DROPDOWN = By.cssSelector("#dropdown");

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
    public void dropdownTest() {
        WebElement dropdownElement = driver.findElement(DROPDOWN);
        Select dropdown = new Select(dropdownElement);

//        dropdown.selectByValue(VALUE);
        dropdown.selectByVisibleText(EXPECTED_SELECTED_OPTION);

        //dropdown.selectByIndex(1);
        String actualSelectedOption = dropdown.getFirstSelectedOption().getText();
        String actualSelectedOptionValue = dropdown.getFirstSelectedOption().getAttribute("value");

        Assert.assertEquals(actualSelectedOption, EXPECTED_SELECTED_OPTION);
        Assert.assertEquals(actualSelectedOptionValue, VALUE);
    }
}
