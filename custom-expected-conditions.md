# Custom Expected Conditions: Extending Selenium's Wait Framework

## Problem Statement

Selenium's built-in ExpectedConditions cover common scenarios, but real-world applications often require custom waiting logic for business-specific states, complex UI behaviors, and application-specific loading patterns. Understanding when and how to create custom Expected Conditions is essential for building robust, maintainable automation that can handle unique application behaviors that standard conditions cannot address.

## Why It Matters

Custom Expected Conditions impact:
- **Test Specificity**: Ability to wait for exact business logic conditions
- **Code Reusability**: Custom conditions can be shared across multiple tests
- **Maintainability**: Centralized waiting logic reduces code duplication
- **Readability**: Business-meaningful condition names improve test clarity
- **Performance**: Optimized conditions reduce unnecessary polling and checks

## Understanding Custom Expected Conditions

### When Built-in Conditions Fall Short
1. **Business Logic States**: Waiting for application-specific data states
2. **Complex UI Interactions**: Multi-step UI operations with dependencies
3. **Data Validation**: Waiting for specific content formats or calculations
4. **Animation Completion**: CSS animations and transitions
5. **Cross-Element Dependencies**: Conditions involving multiple elements

### Custom Condition Categories
- **Element State Conditions**: Custom element property checks
- **Content Validation Conditions**: Text format, data validation
- **Business Logic Conditions**: Application-specific state checks
- **Performance Conditions**: Timing and resource-based waits
- **Composite Conditions**: Combining multiple wait criteria

## Multiple Solutions

### Solution 1: Lambda-Based Custom Conditions

**When to Use**: Simple, one-off conditions, rapid prototyping, inline conditions

```java
public class LambdaBasedConditions {
    private WebDriver driver;
    private WebDriverWait wait;
    
    public void waitForDropdownOptionsToLoad(By dropdownLocator, int minimumOptions) {
        System.out.printf("Waiting for dropdown to have at least %d options%n", minimumOptions);
        
        wait.until(driver -> {
            try {
                WebElement dropdown = driver.findElement(dropdownLocator);
                
                if ("select".equals(dropdown.getTagName().toLowerCase())) {
                    Select select = new Select(dropdown);
                    int optionCount = select.getOptions().size();
                    
                    System.out.printf("  Current option count: %d%n", optionCount);
                    return optionCount >= minimumOptions;
                } else {
                    // Custom dropdown - count option elements
                    List<WebElement> options = dropdown.findElements(By.cssSelector(".option, .item, [role='option']"));
                    int optionCount = options.size();
                    
                    System.out.printf("  Current option count: %d%n", optionCount);
                    return optionCount >= minimumOptions;
                }
            } catch (Exception e) {
                System.out.printf("  Error checking dropdown options: %s%n", e.getMessage());
                return false;
            }
        });
        
        System.out.println("Dropdown options loaded successfully");
    }
    
    public void waitForElementTextToMatch(By locator, String regex) {
        System.out.printf("Waiting for element text to match pattern: %s%n", regex);
        
        Pattern pattern = Pattern.compile(regex);
        
        wait.until(driver -> {
            try {
                WebElement element = driver.findElement(locator);
                String text = element.getText().trim();
                
                boolean matches = pattern.matcher(text).matches();
                System.out.printf("  Current text: '%s' (matches: %s)%n", text, matches);
                
                return matches;
            } catch (Exception e) {
                System.out.printf("  Error checking element text: %s%n", e.getMessage());
                return false;
            }
        });
        
        System.out.println("Element text matches expected pattern");
    }
    
    public void waitForAnimationToComplete(By elementLocator) {
        System.out.println("Waiting for CSS animation to complete...");
        
        wait.until(driver -> {
            try {
                WebElement element = driver.findElement(elementLocator);
                
                // Method 1: Check CSS animation properties
                String animationName = element.getCssValue("animation-name");
                String animationPlayState = element.getCssValue("animation-play-state");
                
                boolean animationComplete = "none".equals(animationName) || 
                                          "paused".equals(animationPlayState);
                
                if (!animationComplete) {
                    System.out.printf("  Animation active: %s (state: %s)%n", animationName, animationPlayState);
                    return false;
                }
                
                // Method 2: Check transition properties
                String transition = element.getCssValue("transition");
                boolean transitionComplete = "none".equals(transition) || transition.isEmpty();
                
                if (!transitionComplete) {
                    System.out.printf("  Transition active: %s%n", transition);
                    return false;
                }
                
                System.out.println("  Animation and transitions complete");
                return true;
                
            } catch (Exception e) {
                System.out.printf("  Error checking animation: %s%n", e.getMessage());
                return false;
            }
        });
    }
    
    public void waitForProgressToReachPercentage(By progressLocator, double targetPercentage) {
        System.out.printf("Waiting for progress to reach %.1f%%...%n", targetPercentage);
        
        wait.until(driver -> {
            try {
                WebElement progress = driver.findElement(progressLocator);
                
                // Try different methods to get progress value
                double currentProgress = 0.0;
                
                // Method 1: aria-valuenow
                String ariaValueNow = progress.getAttribute("aria-valuenow");
                String ariaValueMax = progress.getAttribute("aria-valuemax");
                
                if (ariaValueNow != null && ariaValueMax != null) {
                    double value = Double.parseDouble(ariaValueNow);
                    double max = Double.parseDouble(ariaValueMax);
                    currentProgress = (value / max) * 100;
                } else {
                    // Method 2: CSS width percentage
                    String style = progress.getAttribute("style");
                    if (style != null && style.contains("width")) {
                        Pattern percentPattern = Pattern.compile("width:\\s*(\\d+(?:\\.\\d+)?)%");
                        Matcher matcher = percentPattern.matcher(style);
                        if (matcher.find()) {
                            currentProgress = Double.parseDouble(matcher.group(1));
                        }
                    }
                }
                
                System.out.printf("  Current progress: %.1f%% (target: %.1f%%)%n", 
                    currentProgress, targetPercentage);
                
                return currentProgress >= targetPercentage;
                
            } catch (Exception e) {
                System.out.printf("  Error checking progress: %s%n", e.getMessage());
                return false;
            }
        });
        
        System.out.printf("Progress reached %.1f%%\\n", targetPercentage);
    }
    
    public void waitForMultipleElementsInState(Map<By, String> elementStateMap) {
        System.out.println("Waiting for multiple elements to reach specified states...");
        
        wait.until(driver -> {
            for (Map.Entry<By, String> entry : elementStateMap.entrySet()) {
                By locator = entry.getKey();
                String expectedState = entry.getValue();
                
                try {
                    WebElement element = driver.findElement(locator);
                    String currentState = determineElementState(element);
                    
                    if (!expectedState.equals(currentState)) {
                        System.out.printf("  Element %s: current='%s', expected='%s'%n", 
                            locator, currentState, expectedState);
                        return false;
                    }
                } catch (Exception e) {
                    System.out.printf("  Element %s: not found or error%n", locator);
                    return false;
                }
            }
            
            System.out.println("  All elements in expected states");
            return true;
        });
    }
    
    private String determineElementState(WebElement element) {
        if (!element.isDisplayed()) return "hidden";
        if (!element.isEnabled()) return "disabled";
        
        String className = element.getAttribute("class");
        if (className != null) {
            if (className.contains("loading")) return "loading";
            if (className.contains("error")) return "error";
            if (className.contains("success")) return "success";
            if (className.contains("active")) return "active";
        }
        
        return "ready";
    }
}
```

**Pros:**
- **Simple implementation**: No additional classes or complex setup required
- **Inline logic**: Condition logic is visible where it's used
- **Flexible**: Easy to modify for specific scenarios
- **Quick prototyping**: Rapid development of custom wait logic
- **Context awareness**: Can access local variables and method parameters

**Cons:**
- **Not reusable**: Lambda conditions cannot be easily shared across tests
- **Limited testing**: Difficult to unit test lambda-based logic
- **Debugging challenges**: Stack traces may be less clear
- **Code duplication**: Similar logic may be repeated across tests
- **Maintenance difficulty**: Changes require finding and updating multiple locations

### Solution 2: Reusable Expected Condition Classes

**When to Use**: Frequently used conditions, team-shared logic, complex validation requirements

```java
public class CustomExpectedConditions {
    
    public static class DropdownOptionsLoaded implements ExpectedCondition<Boolean> {
        private final By locator;
        private final int minimumOptions;
        
        public DropdownOptionsLoaded(By locator, int minimumOptions) {
            this.locator = locator;
            this.minimumOptions = minimumOptions;
        }
        
        @Override
        public Boolean apply(WebDriver driver) {
            try {
                WebElement dropdown = driver.findElement(locator);
                
                if ("select".equals(dropdown.getTagName().toLowerCase())) {
                    Select select = new Select(dropdown);
                    return select.getOptions().size() >= minimumOptions;
                } else {
                    List<WebElement> options = dropdown.findElements(
                        By.cssSelector(".option, .item, [role='option']"));
                    return options.size() >= minimumOptions;
                }
            } catch (Exception e) {
                return false;
            }
        }
        
        @Override
        public String toString() {
            return String.format("dropdown at %s to have at least %d options", locator, minimumOptions);
        }
    }
    
    public static class ElementTextMatches implements ExpectedCondition<Boolean> {
        private final By locator;
        private final Pattern pattern;
        
        public ElementTextMatches(By locator, String regex) {
            this.locator = locator;
            this.pattern = Pattern.compile(regex);
        }
        
        @Override
        public Boolean apply(WebDriver driver) {
            try {
                WebElement element = driver.findElement(locator);
                String text = element.getText().trim();
                return pattern.matcher(text).matches();
            } catch (Exception e) {
                return false;
            }
        }
        
        @Override
        public String toString() {
            return String.format("element at %s to match pattern '%s'", locator, pattern.pattern());
        }
    }
    
    public static class ElementAttributeContains implements ExpectedCondition<Boolean> {
        private final By locator;
        private final String attributeName;
        private final String expectedValue;
        private final boolean partialMatch;
        
        public ElementAttributeContains(By locator, String attributeName, String expectedValue, boolean partialMatch) {
            this.locator = locator;
            this.attributeName = attributeName;
            this.expectedValue = expectedValue;
            this.partialMatch = partialMatch;
        }
        
        @Override
        public Boolean apply(WebDriver driver) {
            try {
                WebElement element = driver.findElement(locator);
                String actualValue = element.getAttribute(attributeName);
                
                if (actualValue == null) return false;
                
                return partialMatch ? 
                    actualValue.contains(expectedValue) : 
                    actualValue.equals(expectedValue);
            } catch (Exception e) {
                return false;
            }
        }
        
        @Override
        public String toString() {
            return String.format("element at %s to have attribute '%s' %s '%s'", 
                locator, attributeName, partialMatch ? "containing" : "equal to", expectedValue);
        }
    }
    
    public static class ProgressReachesPercentage implements ExpectedCondition<Boolean> {
        private final By locator;
        private final double targetPercentage;
        
        public ProgressReachesPercentage(By locator, double targetPercentage) {
            this.locator = locator;
            this.targetPercentage = targetPercentage;
        }
        
        @Override
        public Boolean apply(WebDriver driver) {
            try {
                WebElement progress = driver.findElement(locator);
                double currentProgress = getCurrentProgress(progress);
                return currentProgress >= targetPercentage;
            } catch (Exception e) {
                return false;
            }
        }
        
        private double getCurrentProgress(WebElement progress) {
            // Try ARIA values first
            String ariaValueNow = progress.getAttribute("aria-valuenow");
            String ariaValueMax = progress.getAttribute("aria-valuemax");
            
            if (ariaValueNow != null && ariaValueMax != null) {
                try {
                    double value = Double.parseDouble(ariaValueNow);
                    double max = Double.parseDouble(ariaValueMax);
                    return (value / max) * 100;
                } catch (NumberFormatException e) {
                    // Fall through to CSS method
                }
            }
            
            // Try CSS width percentage
            String style = progress.getAttribute("style");
            if (style != null && style.contains("width")) {
                Pattern percentPattern = Pattern.compile("width:\\s*(\\d+(?:\\.\\d+)?)%");
                Matcher matcher = percentPattern.matcher(style);
                if (matcher.find()) {
                    return Double.parseDouble(matcher.group(1));
                }
            }
            
            return 0.0; // Unable to determine progress
        }
        
        @Override
        public String toString() {
            return String.format("progress at %s to reach %.1f%%", locator, targetPercentage);
        }
    }
    
    public static class AllElementsInStates implements ExpectedCondition<Boolean> {
        private final Map<By, String> elementStates;
        
        public AllElementsInStates(Map<By, String> elementStates) {
            this.elementStates = elementStates;
        }
        
        @Override
        public Boolean apply(WebDriver driver) {
            for (Map.Entry<By, String> entry : elementStates.entrySet()) {
                By locator = entry.getKey();
                String expectedState = entry.getValue();
                
                try {
                    WebElement element = driver.findElement(locator);
                    String currentState = determineElementState(element);
                    
                    if (!expectedState.equals(currentState)) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        }
        
        private String determineElementState(WebElement element) {
            if (!element.isDisplayed()) return "hidden";
            if (!element.isEnabled()) return "disabled";
            
            String className = element.getAttribute("class");
            if (className != null) {
                if (className.contains("loading")) return "loading";
                if (className.contains("error")) return "error";
                if (className.contains("success")) return "success";
                if (className.contains("active")) return "active";
            }
            
            return "ready";
        }
        
        @Override
        public String toString() {
            return String.format("all elements to be in expected states: %s", elementStates);
        }
    }
    
    // Factory methods for easier usage
    public static ExpectedCondition<Boolean> dropdownOptionsLoaded(By locator, int minimumOptions) {
        return new DropdownOptionsLoaded(locator, minimumOptions);
    }
    
    public static ExpectedCondition<Boolean> elementTextMatches(By locator, String regex) {
        return new ElementTextMatches(locator, regex);
    }
    
    public static ExpectedCondition<Boolean> elementAttributeContains(By locator, String attribute, String value) {
        return new ElementAttributeContains(locator, attribute, value, true);
    }
    
    public static ExpectedCondition<Boolean> elementAttributeEquals(By locator, String attribute, String value) {
        return new ElementAttributeContains(locator, attribute, value, false);
    }
    
    public static ExpectedCondition<Boolean> progressReachesPercentage(By locator, double percentage) {
        return new ProgressReachesPercentage(locator, percentage);
    }
    
    public static ExpectedCondition<Boolean> allElementsInStates(Map<By, String> states) {
        return new AllElementsInStates(states);
    }
}

// Usage examples
public class CustomConditionUsageExamples {
    private WebDriver driver;
    private WebDriverWait wait;
    
    public void exampleUsage() {
        // Using factory methods for clean, readable code
        wait.until(CustomExpectedConditions.dropdownOptionsLoaded(By.id("dropdown"), 5));
        
        wait.until(CustomExpectedConditions.elementTextMatches(By.id("status"), "^(Complete|Done|Finished)$"));
        
        wait.until(CustomExpectedConditions.progressReachesPercentage(By.id("progress"), 75.0));
        
        // Multiple element states
        Map<By, String> expectedStates = new HashMap<>();
        expectedStates.put(By.id("submit-btn"), "ready");
        expectedStates.put(By.id("status"), "success");
        expectedStates.put(By.id("progress"), "hidden");
        
        wait.until(CustomExpectedConditions.allElementsInStates(expectedStates));
    }
}
```

**Pros:**
- **Reusability**: Can be used across multiple tests and projects
- **Testability**: Each condition can be unit tested independently
- **Clear errors**: toString() methods provide meaningful failure messages
- **Type safety**: Strong typing helps catch errors at compile time
- **Documentation**: Class and method names serve as documentation

**Cons:**
- **Implementation overhead**: More code required for each condition
- **Class proliferation**: Can lead to many small condition classes
- **Maintenance burden**: More files to maintain and update
- **Learning curve**: Team needs to understand custom condition patterns
- **Debugging complexity**: May require stepping through multiple classes

### Solution 3: Business Domain-Specific Conditions

**When to Use**: Application-specific workflows, domain expertise integration, business rule validation

```java
public class BusinessDomainConditions {
    
    // E-commerce specific conditions
    public static class ShoppingCartConditions {
        
        public static class CartItemCountEquals implements ExpectedCondition<Boolean> {
            private final int expectedCount;
            
            public CartItemCountEquals(int expectedCount) {
                this.expectedCount = expectedCount;
            }
            
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    // Multiple ways to find cart count
                    WebElement cartBadge = driver.findElement(By.cssSelector(".cart-badge, .cart-count"));
                    String countText = cartBadge.getText().trim();
                    
                    if (countText.isEmpty()) {
                        return expectedCount == 0;
                    }
                    
                    int actualCount = Integer.parseInt(countText);
                    return actualCount == expectedCount;
                    
                } catch (Exception e) {
                    // Fallback: count cart items directly
                    try {
                        List<WebElement> cartItems = driver.findElements(By.cssSelector(".cart-item"));
                        return cartItems.size() == expectedCount;
                    } catch (Exception fallbackError) {
                        return false;
                    }
                }
            }
            
            @Override
            public String toString() {
                return String.format("shopping cart to contain %d items", expectedCount);
            }
        }
        
        public static class CartTotalEquals implements ExpectedCondition<Boolean> {
            private final BigDecimal expectedTotal;
            private final String currencySymbol;
            
            public CartTotalEquals(BigDecimal expectedTotal, String currencySymbol) {
                this.expectedTotal = expectedTotal;
                this.currencySymbol = currencySymbol;
            }
            
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    WebElement totalElement = driver.findElement(
                        By.cssSelector(".cart-total, .total-amount, #cart-total"));
                    
                    String totalText = totalElement.getText().trim();
                    
                    // Remove currency symbol and parse
                    String numericTotal = totalText.replace(currencySymbol, "").replace(",", "").trim();
                    BigDecimal actualTotal = new BigDecimal(numericTotal);
                    
                    return actualTotal.compareTo(expectedTotal) == 0;
                    
                } catch (Exception e) {
                    return false;
                }
            }
            
            @Override
            public String toString() {
                return String.format("cart total to equal %s%s", currencySymbol, expectedTotal);
            }
        }
    }
    
    // Form validation conditions
    public static class FormValidationConditions {
        
        public static class AllRequiredFieldsCompleted implements ExpectedCondition<Boolean> {
            
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    List<WebElement> requiredFields = driver.findElements(
                        By.cssSelector("input[required], select[required], textarea[required]"));
                    
                    for (WebElement field : requiredFields) {
                        String value = field.getAttribute("value");
                        if (value == null || value.trim().isEmpty()) {
                            return false;
                        }
                        
                        // Special handling for select elements
                        if ("select".equals(field.getTagName().toLowerCase())) {
                            Select select = new Select(field);
                            WebElement selectedOption = select.getFirstSelectedOption();
                            String selectedValue = selectedOption.getAttribute("value");
                            if (selectedValue == null || selectedValue.trim().isEmpty()) {
                                return false;
                            }
                        }
                    }
                    
                    return true;
                    
                } catch (Exception e) {
                    return false;
                }
            }
            
            @Override
            public String toString() {
                return "all required form fields to be completed";
            }
        }
        
        public static class FormSubmissionCompleted implements ExpectedCondition<Boolean> {
            private final By successIndicatorLocator;
            
            public FormSubmissionCompleted(By successIndicatorLocator) {
                this.successIndicatorLocator = successIndicatorLocator;
            }
            
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    // Check for success indicator
                    WebElement successElement = driver.findElement(successIndicatorLocator);
                    if (!successElement.isDisplayed()) return false;
                    
                    // Ensure no error messages are present
                    List<WebElement> errorElements = driver.findElements(
                        By.cssSelector(".error, .alert-danger, .form-error"));
                    
                    for (WebElement error : errorElements) {
                        if (error.isDisplayed()) return false;
                    }
                    
                    // Ensure loading indicators are gone
                    List<WebElement> loadingElements = driver.findElements(
                        By.cssSelector(".loading, .submitting, .form-loading"));
                    
                    for (WebElement loading : loadingElements) {
                        if (loading.isDisplayed()) return false;
                    }
                    
                    return true;
                    
                } catch (Exception e) {
                    return false;
                }
            }
            
            @Override
            public String toString() {
                return String.format("form submission to complete with success indicator at %s", successIndicatorLocator);
            }
        }
    }
    
    // Data loading and validation conditions
    public static class DataConditions {
        
        public static class TableRowCountEquals implements ExpectedCondition<Boolean> {
            private final By tableLocator;
            private final int expectedCount;
            
            public TableRowCountEquals(By tableLocator, int expectedCount) {
                this.tableLocator = tableLocator;
                this.expectedCount = expectedCount;
            }
            
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    WebElement table = driver.findElement(tableLocator);
                    List<WebElement> rows = table.findElements(By.cssSelector("tbody tr"));
                    
                    // Filter out loading/empty rows
                    long dataRows = rows.stream()
                        .filter(row -> !row.getAttribute("class").contains("loading"))
                        .filter(row -> !row.getAttribute("class").contains("empty"))
                        .filter(row -> !row.getText().trim().toLowerCase().contains("loading"))
                        .count();
                    
                    return dataRows == expectedCount;
                    
                } catch (Exception e) {
                    return false;
                }
            }
            
            @Override
            public String toString() {
                return String.format("table at %s to have %d data rows", tableLocator, expectedCount);
            }
        }
        
        public static class DataFormatValid implements ExpectedCondition<Boolean> {
            private final By locator;
            private final DataFormat format;
            
            public enum DataFormat {
                EMAIL("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"),
                PHONE("^\\+?[1-9]\\d{1,14}$"),
                DATE_ISO("^\\d{4}-\\d{2}-\\d{2}$"),
                CURRENCY("^\\$?\\d+(\\.\\d{2})?$"),
                PERCENTAGE("^\\d+(\\.\\d+)?%$");
                
                final String regex;
                
                DataFormat(String regex) {
                    this.regex = regex;
                }
            }
            
            public DataFormatValid(By locator, DataFormat format) {
                this.locator = locator;
                this.format = format;
            }
            
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    WebElement element = driver.findElement(locator);
                    String text = element.getText().trim();
                    
                    Pattern pattern = Pattern.compile(format.regex);
                    return pattern.matcher(text).matches();
                    
                } catch (Exception e) {
                    return false;
                }
            }
            
            @Override
            public String toString() {
                return String.format("element at %s to have valid %s format", locator, format.name());
            }
        }
    }
    
    // Factory methods for business conditions
    public static ExpectedCondition<Boolean> cartItemCount(int count) {
        return new ShoppingCartConditions.CartItemCountEquals(count);
    }
    
    public static ExpectedCondition<Boolean> cartTotal(BigDecimal total, String currency) {
        return new ShoppingCartConditions.CartTotalEquals(total, currency);
    }
    
    public static ExpectedCondition<Boolean> allRequiredFieldsCompleted() {
        return new FormValidationConditions.AllRequiredFieldsCompleted();
    }
    
    public static ExpectedCondition<Boolean> formSubmissionCompleted(By successIndicator) {
        return new FormValidationConditions.FormSubmissionCompleted(successIndicator);
    }
    
    public static ExpectedCondition<Boolean> tableRowCount(By tableLocator, int count) {
        return new DataConditions.TableRowCountEquals(tableLocator, count);
    }
    
    public static ExpectedCondition<Boolean> dataFormatValid(By locator, DataConditions.DataFormat format) {
        return new DataConditions.DataFormatValid(locator, format);
    }
}
```

**Pros:**
- **Business alignment**: Conditions reflect actual business requirements and workflows
- **Domain expertise**: Captures business logic that developers might not consider
- **Self-documenting**: Condition names clearly express business intent
- **Validation integration**: Combines UI automation with business rule validation
- **Cross-functional**: Can be understood by both technical and business team members

**Cons:**
- **Domain coupling**: Tightly coupled to specific business domains
- **Limited reusability**: Less applicable across different applications or industries
- **Business logic changes**: Requires updates when business rules change
- **Complexity growth**: Can become complex as business rules evolve
- **Testing challenges**: May require business domain knowledge to test effectively

## Decision Framework

### Choose Lambda-Based Conditions When:
- Creating simple, one-off wait conditions
- Rapid prototyping or experimenting with wait logic
- Condition logic is specific to a single test scenario
- Quick fixes or temporary solutions are needed
- Team prefers inline logic for readability

### Choose Reusable Condition Classes When:
- Same waiting logic is needed across multiple tests
- Building a shared automation framework
- Team values strong typing and compile-time checking
- Conditions are complex enough to benefit from dedicated testing
- Clear error messages and documentation are important

### Choose Business Domain-Specific Conditions When:
- Testing applications with complex business workflows
- Business rules require specific validation logic
- Cross-functional teams need to understand test conditions
- Domain expertise is important for accurate testing
- Building long-term test suites for specific business domains

## Real-world Examples from Codebase Context

### Current Implementation Enhancement

**DynamicLoadingTest.java** could benefit from custom conditions:
```java
// Current approach
wait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_MESSAGE));

// Enhanced with custom condition
wait.until(CustomExpectedConditions.elementTextMatches(FINISH_MESSAGE, "Hello World!"));
```

**Ex03DynamicLoadingWithFluentWait.java** enhanced:
```java
// Current approach
fluentWait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loading")));

// Enhanced with custom condition that's more specific
fluentWait.until(CustomExpectedConditions.elementAttributeEquals(
    By.id("finish"), "data-loaded", "true"));
```

### Example Custom Conditions for Current Codebase

```java
public class DynamicLoadingConditions {
    
    public static class LoadingSequenceCompleted implements ExpectedCondition<Boolean> {
        private final By startButtonLocator;
        private final By loadingIndicatorLocator; 
        private final By finishMessageLocator;
        
        public LoadingSequenceCompleted(By startButton, By loadingIndicator, By finishMessage) {
            this.startButtonLocator = startButton;
            this.loadingIndicatorLocator = loadingIndicator;
            this.finishMessageLocator = finishMessage;
        }
        
        @Override
        public Boolean apply(WebDriver driver) {
            try {
                // Verify start button is no longer the focus
                WebElement startButton = driver.findElement(startButtonLocator);
                if (startButton.equals(driver.switchTo().activeElement())) {
                    return false; // Still focused on start button
                }
                
                // Verify loading indicator is not visible
                List<WebElement> loadingElements = driver.findElements(loadingIndicatorLocator);
                for (WebElement loading : loadingElements) {
                    if (loading.isDisplayed()) return false;
                }
                
                // Verify finish message is visible and has expected content
                WebElement finishMessage = driver.findElement(finishMessageLocator);
                if (!finishMessage.isDisplayed()) return false;
                
                String messageText = finishMessage.getText().trim();
                return "Hello World!".equals(messageText);
                
            } catch (Exception e) {
                return false;
            }
        }
        
        @Override
        public String toString() {
            return "dynamic loading sequence to complete with finish message visible";
        }
    }
    
    // Usage in tests
    public static ExpectedCondition<Boolean> loadingSequenceCompleted() {
        return new LoadingSequenceCompleted(
            By.cssSelector("#start button"),
            By.id("loading"),
            By.id("finish")
        );
    }
}

// Enhanced test method
@Test
public void dynamicLoadingWithCustomCondition() {
    driver.findElement(EXAMPLE_1_LINK).click();
    driver.findElement(START_BUTTON).click();
    
    // Single condition that validates entire loading sequence
    wait.until(DynamicLoadingConditions.loadingSequenceCompleted());
    
    // Additional verification if needed
    WebElement message = driver.findElement(FINISH_MESSAGE);
    Assert.assertTrue(message.isDisplayed());
}
```

## Testing Custom Expected Conditions

### Unit Testing Approach
```java
public class CustomExpectedConditionsTest {
    
    @Test
    public void testDropdownOptionsLoaded() {
        // Mock WebDriver and elements
        WebDriver mockDriver = mock(WebDriver.class);
        WebElement mockDropdown = mock(WebElement.class);
        WebElement mockOption1 = mock(WebElement.class);
        WebElement mockOption2 = mock(WebElement.class);
        WebElement mockOption3 = mock(WebElement.class);
        
        when(mockDriver.findElement(By.id("dropdown"))).thenReturn(mockDropdown);
        when(mockDropdown.getTagName()).thenReturn("select");
        
        Select mockSelect = mock(Select.class);
        when(mockSelect.getOptions()).thenReturn(Arrays.asList(mockOption1, mockOption2, mockOption3));
        
        // Test the condition
        CustomExpectedConditions.DropdownOptionsLoaded condition = 
            new CustomExpectedConditions.DropdownOptionsLoaded(By.id("dropdown"), 3);
        
        Boolean result = condition.apply(mockDriver);
        
        assertTrue("Condition should return true when dropdown has required options", result);
    }
    
    @Test
    public void testProgressReachesPercentage() {
        WebDriver mockDriver = mock(WebDriver.class);
        WebElement mockProgress = mock(WebElement.class);
        
        when(mockDriver.findElement(By.id("progress"))).thenReturn(mockProgress);
        when(mockProgress.getAttribute("aria-valuenow")).thenReturn("75");
        when(mockProgress.getAttribute("aria-valuemax")).thenReturn("100");
        
        CustomExpectedConditions.ProgressReachesPercentage condition = 
            new CustomExpectedConditions.ProgressReachesPercentage(By.id("progress"), 50.0);
        
        Boolean result = condition.apply(mockDriver);
        
        assertTrue("Condition should return true when progress exceeds target", result);
    }
}
```

## Performance Considerations

### Efficient Custom Conditions
```java
public class OptimizedCustomConditions {
    
    // Efficient: Single DOM query with multiple checks
    public static class OptimizedElementState implements ExpectedCondition<Boolean> {
        private final By locator;
        private final String expectedClass;
        private final String expectedText;
        
        public OptimizedElementState(By locator, String expectedClass, String expectedText) {
            this.locator = locator;
            this.expectedClass = expectedClass;
            this.expectedText = expectedText;
        }
        
        @Override
        public Boolean apply(WebDriver driver) {
            try {
                WebElement element = driver.findElement(locator);
                
                // Single element query, multiple validations
                String className = element.getAttribute("class");
                String text = element.getText();
                boolean isDisplayed = element.isDisplayed();
                
                return isDisplayed &&
                       (expectedClass == null || (className != null && className.contains(expectedClass))) &&
                       (expectedText == null || expectedText.equals(text.trim()));
                
            } catch (Exception e) {
                return false;
            }
        }
    }
    
    // Inefficient: Multiple separate conditions
    public static void inefficientApproach(WebDriverWait wait, By locator) {
        // BAD: Multiple DOM queries
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        wait.until(ExpectedConditions.attributeContains(locator, "class", "ready"));
        wait.until(ExpectedConditions.textToBe(locator, "Complete"));
    }
    
    // Efficient: Single custom condition
    public static void efficientApproach(WebDriverWait wait, By locator) {
        // GOOD: Single DOM query
        wait.until(new OptimizedElementState(locator, "ready", "Complete"));
    }
}
```

## Further Reading

- [Selenium ExpectedConditions Documentation](https://selenium.dev/selenium/docs/api/java/org/openqa/selenium/support/ui/ExpectedConditions.html)
- [Custom Wait Conditions Best Practices](https://selenium.dev/documentation/webdriver/waits/)
- [Functional Interface Patterns in Java](https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html)
- [Test-Driven Development for Custom Conditions](https://martinfowler.com/bliki/TestDrivenDevelopment.html)

## Key Takeaways

- **Custom Expected Conditions extend Selenium's capabilities for application-specific needs**
- **Lambda-based conditions are quick for one-off scenarios but not reusable**
- **Class-based conditions provide reusability, testability, and clear error messages**
- **Business domain-specific conditions align automation with business requirements**
- **Performance optimization requires minimizing DOM queries within conditions**
- **Proper toString() implementation is crucial for meaningful error messages**
- **Unit testing custom conditions ensures reliability and maintainability**
- **Choose the approach based on reusability needs, complexity, and team preferences**