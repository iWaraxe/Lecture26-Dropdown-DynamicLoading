# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java QA Automation project focused on Selenium WebDriver techniques for handling dropdowns and dynamic loading. It's part of a web automation course (Lecture 26) and serves as educational material for demonstrating advanced Selenium concepts.

## Technology Stack

- **Java 21** - Primary programming language
- **Maven** - Build and dependency management
- **Selenium WebDriver 4.22.0** - Web automation framework
- **TestNG 7.10.2** - Testing framework
- **WebDriverManager 5.9.1** - Automatic driver management

## Project Structure

The codebase is organized into two main learning sections:

### Section 01: Dropdown Handling
- **Basic examples**: `Ex01DropdownSelectByIndex.java`, `Ex02DropdownSelectByValue.java`, `Ex03DropdownSelectByVisibleText.java`
- **Advanced examples**: `Ex04DropdownMultiSelect.java`, `Ex05DropdownHandlingExceptions.java`, `Ex06DropdownGetAllOptions.java`
- **Consolidated test**: `DropdownTest.java`

### Section 02: Dynamic Loading
- **Basic examples**: `Ex01DynamicLoadingExample1.java`, `Ex02DynamicLoadingExample2.java`
- **Advanced examples**: `Ex03DynamicLoadingWithFluentWait.java`, `Ex04DynamicLoadingHandlingTimeout.java`, `Ex05DynamicLoadingWithJavaScriptExecutor.java`
- **Consolidated test**: `DynamicLoadingTest.java`

## Common Development Commands

### Build and Test
```bash
mvn clean compile                    # Compile the project
mvn test                            # Run all tests
mvn test -Dtest=DropdownTest        # Run specific test class
mvn test -Dtest=DynamicLoadingTest  # Run dynamic loading tests
```

### Run Individual Examples
Each example class has a `main` method for standalone execution:
```bash
mvn exec:java -Dexec.mainClass="com.coherentsolutions.java.webauto.section01.Ex01DropdownSelectByIndex"
mvn exec:java -Dexec.mainClass="com.coherentsolutions.java.webauto.section02.Ex01DynamicLoadingExample1"
```

## Key Patterns and Conventions

### Test Structure
- All test classes follow TestNG pattern with `@BeforeMethod` and `@AfterMethod` annotations
- WebDriver setup using WebDriverManager for automatic driver management
- Consistent use of constants for URLs and locators
- Both TestNG test methods and standalone main methods for flexibility

### Selenium Patterns
- **Dropdown handling**: Uses Selenium's `Select` class for standard dropdowns
- **Dynamic loading**: Implements WebDriverWait and FluentWait patterns
- **Locator strategy**: Primarily uses CSS selectors and IDs
- **Wait strategies**: Explicit waits with ExpectedConditions

### Target Test Sites
- Primary test site: `https://the-internet.herokuapp.com/`
- Dropdown tests: `/dropdown` endpoint
- Dynamic loading tests: `/dynamic_loading/1` and `/dynamic_loading/2` endpoints

## Important Implementation Notes

### Dropdown Handling
- Always wrap dropdown WebElements with `Select` class
- Use appropriate selection methods: `selectByIndex()`, `selectByValue()`, `selectByVisibleText()`
- Verify selections using `getFirstSelectedOption()` or `getAllSelectedOptions()`

### Dynamic Loading
- Use explicit waits (`WebDriverWait`) for predictable timing
- Implement FluentWait for complex polling scenarios
- Wait for visibility conditions using `ExpectedConditions.visibilityOf()` or `ExpectedConditions.visibilityOfElementLocated()`
- Handle both hidden elements (Example 1) and non-existent elements (Example 2)

### Best Practices Implemented
- WebDriverManager eliminates need for manual driver setup
- Consistent teardown with `driver.quit()` in @AfterMethod
- Proper exception handling in advanced examples
- Clear naming conventions for constants and methods

## Educational Materials

This project includes 15 comprehensive educational markdown files organized in a progressive learning structure from foundational to expert level:

### Foundational Level (01-03)
- **01-dropdown-fundamentals.md**: Architectural decision frameworks and patterns
- **02-wait-strategies.md**: Performance analysis of WebDriverWait vs FluentWait  
- **03-timing-issues.md**: Race condition prevention patterns and debugging techniques

### Practical Level (04-06)
- **04-custom-dropdowns.md**: Framework-specific handling (React, Angular, Bootstrap)
- **05-spa-loading.md**: Single Page Application loading patterns and challenges
- **06-error-handling.md**: Retry patterns and error recovery strategies

### Performance Level (07-09)
- **07-performance-optimization.md**: Benchmarking and optimization strategies
- **08-network-conditions.md**: Adaptive timeout strategies for different environments
- **09-loading-indicators.md**: Classification and detection of loading indicators

### Advanced Level (10-12)
- **10-custom-conditions.md**: Extending Selenium's wait framework
- **11-debugging-techniques.md**: Systematic diagnostic techniques and troubleshooting
- **12-cross-browser-testing.md**: Browser-specific compatibility strategies

### Expert Level (13-15)
- **13-mobile-automation.md**: Touch-optimized strategies and responsive testing
- **14-accessibility-testing.md**: WCAG compliance and assistive technology support
- **15-performance-monitoring.md**: Production-aware testing with RUM integration

See **README-Educational-Materials.md** for detailed learning paths, prerequisites, and time estimates for each topic.