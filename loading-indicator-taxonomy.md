# Loading Indicator Taxonomy: Pattern Recognition for Reliable Waiting

## Problem Statement

Modern web applications use diverse loading indicators to communicate dynamic content status to users. Each type of loading indicator requires different waiting strategies for reliable automation. Misunderstanding loading indicator patterns leads to premature element interaction, false positive tests, and unreliable automation. Recognizing and properly handling different loading indicator types is essential for robust dynamic content testing.

## Why It Matters

Loading indicator handling affects:
- **Test Accuracy**: Waiting for wrong indicators leads to false test results
- **Reliability**: Incorrect timing causes intermittent test failures
- **User Experience Validation**: Tests should verify actual user experience
- **Performance Measurement**: Loading indicators provide timing benchmarks
- **Debugging Efficiency**: Understanding indicators accelerates failure analysis

## Understanding Loading Indicator Categories

### Visual Loading Indicators
1. **Spinners**: Rotating icons, animated elements
2. **Progress Bars**: Linear/circular progress visualization
3. **Skeleton Screens**: Content placeholders showing layout structure
4. **Overlay Loaders**: Full-screen or modal loading states
5. **Pulse/Shimmer Effects**: Animated placeholders

### Semantic Loading Indicators
1. **ARIA Live Regions**: Screen reader announcements
2. **Status Messages**: Text-based loading communication
3. **CSS Classes**: Loading state classes on elements
4. **Data Attributes**: HTML5 data attributes indicating state

### Programmatic Loading Indicators
1. **JavaScript Variables**: Window or application state variables
2. **Network Activity**: Active HTTP requests
3. **DOM Mutations**: Elements being added/removed
4. **Event Listeners**: Loading start/complete events

## Multiple Solutions

### Solution 1: Visual Indicator Detection Strategy

**When to Use**: User-facing applications, visual regression testing, accessibility compliance

```java
public class VisualIndicatorDetectionStrategy {
    private WebDriver driver;
    private WebDriverWait wait;
    
    public enum LoadingIndicatorType {
        SPINNER(".spinner, .loading-spinner, .fa-spinner"),
        PROGRESS_BAR(".progress-bar, .progress, .loading-progress"),
        SKELETON(".skeleton, .skeleton-loader, .loading-skeleton"),
        OVERLAY(".loading-overlay, .modal-loading, .overlay-spinner"),
        SHIMMER(".shimmer, .loading-shimmer, .pulse"),
        GENERIC_LOADING(".loading, .loader, [data-loading='true']");
        
        final String cssSelector;
        
        LoadingIndicatorType(String cssSelector) {
            this.cssSelector = cssSelector;
        }
    }
    
    public void waitForLoadingIndicatorToDisappear(LoadingIndicatorType... indicatorTypes) {
        List<LoadingIndicatorType> typesToCheck = indicatorTypes.length > 0 
            ? Arrays.asList(indicatorTypes) 
            : Arrays.asList(LoadingIndicatorType.values());
        
        for (LoadingIndicatorType type : typesToCheck) {
            try {
                System.out.printf("Checking for %s indicators: %s%n", type.name(), type.cssSelector);
                
                // Wait for indicator to appear first (optional - some may not appear)
                try {
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
                    shortWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(type.cssSelector)));
                    System.out.printf("  %s indicator detected%n", type.name());
                } catch (TimeoutException e) {
                    System.out.printf("  %s indicator not found, skipping%n", type.name());
                    continue;
                }
                
                // Wait for indicator to disappear
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(type.cssSelector)));
                System.out.printf("  %s indicator disappeared%n", type.name());
                
            } catch (TimeoutException e) {
                System.out.printf("  WARNING: %s indicator did not disappear within timeout%n", type.name());
                // Continue checking other indicators
            }
        }
    }
    
    public void waitForProgressBarCompletion(By progressBarLocator) {
        System.out.println("Monitoring progress bar completion...");
        
        // Wait for progress bar to appear
        WebElement progressBar = wait.until(ExpectedConditions.presenceOfElementLocated(progressBarLocator));
        
        // Monitor progress until completion
        wait.until(driver -> {
            WebElement currentProgressBar = driver.findElement(progressBarLocator);
            
            // Check various progress indicators
            String ariaValueNow = currentProgressBar.getAttribute("aria-valuenow");
            String ariaValueMax = currentProgressBar.getAttribute("aria-valuemax");
            String styleWidth = currentProgressBar.getAttribute("style");
            String progressClass = currentProgressBar.getAttribute("class");
            
            // Method 1: ARIA progress values
            if (ariaValueNow != null && ariaValueMax != null) {
                try {
                    double current = Double.parseDouble(ariaValueNow);
                    double max = Double.parseDouble(ariaValueMax);
                    boolean isComplete = current >= max;
                    System.out.printf("  Progress: %.0f/%.0f (%s)%n", current, max, 
                        isComplete ? "COMPLETE" : "IN_PROGRESS");
                    return isComplete;
                } catch (NumberFormatException e) {
                    // Fall through to other methods
                }
            }
            
            // Method 2: CSS width style
            if (styleWidth != null && styleWidth.contains("width")) {
                boolean isComplete = styleWidth.contains("100%") || styleWidth.contains("width: 100");
                System.out.printf("  Progress style: %s (%s)%n", styleWidth, 
                    isComplete ? "COMPLETE" : "IN_PROGRESS");
                return isComplete;
            }
            
            // Method 3: CSS class indicators
            if (progressClass != null) {
                boolean isComplete = progressClass.contains("complete") || progressClass.contains("done");
                System.out.printf("  Progress class: %s (%s)%n", progressClass, 
                    isComplete ? "COMPLETE" : "IN_PROGRESS");
                return isComplete;
            }
            
            return false; // Unable to determine completion
        });
        
        // Wait for progress bar to disappear after completion
        wait.until(ExpectedConditions.invisibilityOf(progressBar));
        System.out.println("Progress bar completed and disappeared");
    }
    
    public void waitForSkeletonScreenToResolve(By skeletonLocator, By contentLocator) {
        System.out.println("Waiting for skeleton screen to resolve to actual content...");
        
        // First, ensure skeleton screen is present
        wait.until(ExpectedConditions.presenceOfElementLocated(skeletonLocator));
        System.out.println("  Skeleton screen detected");
        
        // Wait for actual content to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(contentLocator));
        System.out.println("  Actual content appeared");
        
        // Wait for skeleton to disappear (content replacement)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(skeletonLocator));
        System.out.println("  Skeleton screen disappeared");
        
        // Verify content is not empty/placeholder
        WebElement content = driver.findElement(contentLocator);
        wait.until(driver -> {
            String contentText = content.getText().trim();
            boolean hasRealContent = !contentText.isEmpty() && 
                                   !contentText.toLowerCase().contains("loading") &&
                                   !contentText.toLowerCase().contains("placeholder");
            
            System.out.printf("  Content validation: '%s' (%s)%n", 
                contentText.length() > 50 ? contentText.substring(0, 50) + "..." : contentText,
                hasRealContent ? "REAL_CONTENT" : "PLACEHOLDER");
            
            return hasRealContent;
        });
    }
    
    public void detectAndWaitForAllLoadingIndicators() {
        System.out.println("Performing comprehensive loading indicator detection...");
        
        // Scan for all possible loading indicators
        Map<LoadingIndicatorType, Boolean> detectedIndicators = new HashMap<>();
        
        for (LoadingIndicatorType type : LoadingIndicatorType.values()) {
            List<WebElement> indicators = driver.findElements(By.cssSelector(type.cssSelector));
            detectedIndicators.put(type, !indicators.isEmpty());
            
            if (!indicators.isEmpty()) {
                System.out.printf("  Detected %d %s indicator(s)%n", indicators.size(), type.name());
            }
        }
        
        // Wait for all detected indicators to disappear
        for (Map.Entry<LoadingIndicatorType, Boolean> entry : detectedIndicators.entrySet()) {
            if (entry.getValue()) {
                LoadingIndicatorType type = entry.getKey();
                try {
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(type.cssSelector)));
                    System.out.printf("  %s indicators cleared%n", type.name());
                } catch (TimeoutException e) {
                    System.out.printf("  WARNING: %s indicators persist after timeout%n", type.name());
                }
            }
        }
        
        System.out.println("Loading indicator detection complete");
    }
}
```

**Pros:**
- **Comprehensive coverage**: Detects wide variety of visual loading patterns
- **User experience alignment**: Waits for what users actually see
- **Flexible detection**: Configurable CSS selectors for different frameworks
- **Progress monitoring**: Can track loading progress, not just completion
- **Visual validation**: Ensures UI actually reflects loading state

**Cons:**
- **CSS dependency**: Relies on consistent CSS class naming conventions
- **Performance overhead**: Multiple DOM queries to detect all indicator types
- **Framework variation**: Different frameworks use different indicator patterns
- **False positives**: May detect unrelated elements with similar classes
- **Maintenance burden**: CSS selectors need updates when UI changes

### Solution 2: Semantic and Accessibility-Based Detection

**When to Use**: Accessibility-first applications, screen reader testing, semantic markup

```java
public class SemanticIndicatorDetectionStrategy {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    
    public void waitForAriaLiveRegionUpdates() {
        System.out.println("Monitoring ARIA live regions for loading updates...");
        
        // Find all ARIA live regions
        List<WebElement> liveRegions = driver.findElements(By.cssSelector("[aria-live]"));
        
        if (liveRegions.isEmpty()) {
            System.out.println("  No ARIA live regions found");
            return;
        }
        
        System.out.printf("  Found %d ARIA live region(s)%n", liveRegions.size());
        
        // Monitor each live region for loading-related announcements
        for (int i = 0; i < liveRegions.size(); i++) {
            final int regionIndex = i;
            WebElement region = liveRegions.get(i);
            
            String ariaLive = region.getAttribute("aria-live");
            System.out.printf("  Monitoring region %d (aria-live: %s)%n", regionIndex + 1, ariaLive);
            
            // Wait for loading announcement to clear
            wait.until(driver -> {
                WebElement currentRegion = driver.findElements(By.cssSelector("[aria-live]")).get(regionIndex);
                String content = currentRegion.getText().toLowerCase().trim();
                
                boolean isLoadingAnnouncement = content.contains("loading") || 
                                               content.contains("please wait") || 
                                               content.contains("processing");
                
                System.out.printf("    Region %d content: '%s' (%s)%n", regionIndex + 1, 
                    content.isEmpty() ? "(empty)" : content,
                    isLoadingAnnouncement ? "LOADING" : "READY");
                
                return !isLoadingAnnouncement;
            });
        }
        
        System.out.println("All ARIA live regions indicate loading complete");
    }
    
    public void waitForStatusMessageClearing(String... loadingKeywords) {
        String[] keywords = loadingKeywords.length > 0 ? loadingKeywords : 
            new String[]{"loading", "please wait", "processing", "fetching", "updating"};
        
        System.out.printf("Waiting for status messages to clear (keywords: %s)%n", 
            Arrays.toString(keywords));
        
        // Look for elements with role="status" or common status classes
        String statusSelector = "[role='status'], .status, .message, .notification, .alert";
        
        wait.until(driver -> {
            List<WebElement> statusElements = driver.findElements(By.cssSelector(statusSelector));
            
            for (WebElement element : statusElements) {
                String text = element.getText().toLowerCase().trim();
                
                if (!text.isEmpty()) {
                    for (String keyword : keywords) {
                        if (text.contains(keyword.toLowerCase())) {
                            System.out.printf("  Status element still shows loading: '%s'%n", text);
                            return false;
                        }
                    }
                }
            }
            
            return true; // No loading status messages found
        });
        
        System.out.println("All status messages cleared");
    }
    
    public void waitForLoadingStateAttributes() {
        System.out.println("Monitoring HTML5 data attributes and ARIA states...");
        
        // Common loading state attributes
        String[] loadingSelectors = {
            "[data-loading='true']",
            "[data-state='loading']", 
            "[aria-busy='true']",
            "[data-fetching='true']",
            "[data-pending='true']"
        };
        
        for (String selector : loadingSelectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                
                if (!elements.isEmpty()) {
                    System.out.printf("  Found %d element(s) with loading state: %s%n", 
                        elements.size(), selector);
                    
                    // Wait for these elements to clear their loading state
                    wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(selector), 0));
                    System.out.printf("  Loading state cleared: %s%n", selector);
                }
            } catch (TimeoutException e) {
                System.out.printf("  WARNING: Loading state persisted: %s%n", selector);
            }
        }
        
        System.out.println("All loading state attributes cleared");
    }
    
    public void waitForAccessibleLoadingCompletion() {
        System.out.println("Performing comprehensive accessible loading detection...");
        
        // Step 1: ARIA live regions
        waitForAriaLiveRegionUpdates();
        
        // Step 2: Status messages
        waitForStatusMessageClearing();
        
        // Step 3: Loading state attributes
        waitForLoadingStateAttributes();
        
        // Step 4: Focus management (loading often affects focus)
        waitForFocusStability();
        
        System.out.println("Accessible loading completion verified");
    }
    
    private void waitForFocusStability() {
        System.out.println("  Verifying focus stability...");
        
        wait.until(driver -> {
            // Check if focus is on a loading-related element
            WebElement activeElement = driver.switchTo().activeElement();
            
            if (activeElement != null) {
                String tagName = activeElement.getTagName().toLowerCase();
                String className = activeElement.getAttribute("class");
                String ariaLabel = activeElement.getAttribute("aria-label");
                
                // Check if focused element indicates loading
                boolean isFocusOnLoading = (className != null && className.contains("loading")) ||
                                         (ariaLabel != null && ariaLabel.toLowerCase().contains("loading")) ||
                                         "progress".equals(tagName);
                
                if (isFocusOnLoading) {
                    System.out.println("    Focus still on loading element, waiting...");
                    return false;
                }
            }
            
            return true;
        });
        
        System.out.println("    Focus stability confirmed");
    }
    
    public void validateAccessibleLoadingPatterns() {
        System.out.println("Validating accessible loading pattern implementation...");
        
        // Check for proper ARIA attributes
        validateAriaLoadingAttributes();
        
        // Check for semantic markup
        validateSemanticLoadingMarkup();
        
        // Check for proper announcements
        validateLoadingAnnouncements();
    }
    
    private void validateAriaLoadingAttributes() {
        System.out.println("  Checking ARIA loading attributes...");
        
        // Look for elements that should have aria-busy
        List<WebElement> interactiveElements = driver.findElements(
            By.cssSelector("button, input, select, textarea, [role='button'], [role='textbox']"));
        
        for (WebElement element : interactiveElements) {
            String ariaBusy = element.getAttribute("aria-busy");
            String dataLoading = element.getAttribute("data-loading");
            
            if ("true".equals(dataLoading) && !"true".equals(ariaBusy)) {
                System.out.printf("    WARNING: Element has data-loading='true' but missing aria-busy: %s%n", 
                    element.getTagName());
            }
        }
    }
    
    private void validateSemanticLoadingMarkup() {
        System.out.println("  Checking semantic loading markup...");
        
        // Check for proper progress elements
        List<WebElement> progressElements = driver.findElements(By.tagName("progress"));
        for (WebElement progress : progressElements) {
            String ariaLabel = progress.getAttribute("aria-label");
            String ariaLabelledBy = progress.getAttribute("aria-labelledby");
            
            if (ariaLabel == null && ariaLabelledBy == null) {
                System.out.println("    WARNING: Progress element missing aria-label or aria-labelledby");
            }
        }
    }
    
    private void validateLoadingAnnouncements() {
        System.out.println("  Checking loading announcements...");
        
        List<WebElement> liveRegions = driver.findElements(By.cssSelector("[aria-live]"));
        
        if (liveRegions.isEmpty()) {
            System.out.println("    WARNING: No ARIA live regions found for loading announcements");
        } else {
            System.out.printf("    Found %d ARIA live region(s) for announcements%n", liveRegions.size());
        }
    }
}
```

**Pros:**
- **Accessibility compliance**: Ensures tests work with assistive technologies
- **Semantic accuracy**: Uses meaningful markup rather than visual cues
- **Screen reader compatible**: Tests what screen reader users experience
- **Stable selectors**: ARIA attributes are more stable than CSS classes
- **Standards-based**: Follows W3C accessibility guidelines

**Cons:**
- **Implementation dependency**: Requires proper accessibility implementation
- **Limited adoption**: Not all applications use semantic loading indicators
- **Complexity**: Accessibility patterns can be complex to detect
- **Browser support**: Some ARIA features have inconsistent browser support
- **Developer awareness**: Requires team understanding of accessibility patterns

### Solution 3: Programmatic Loading Detection

**When to Use**: JavaScript-heavy applications, precise timing requirements, framework integration

```java
public class ProgrammaticLoadingDetectionStrategy {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    
    public void waitForJavaScriptLoadingVariables() {
        System.out.println("Monitoring JavaScript loading variables...");
        
        // Common loading variable patterns
        String[] loadingVariables = {
            "window.isLoading",
            "window.APP_LOADING", 
            "window.loadingState",
            "document.body.dataset.loading",
            "window.LOADING_MANAGER && window.LOADING_MANAGER.isLoading"
        };
        
        for (String variable : loadingVariables) {
            try {
                System.out.printf("  Checking: %s%n", variable);
                
                wait.until(driver -> {
                    String script = "return " + variable + ";";
                    Object result = js.executeScript(script);
                    
                    boolean isLoading = false;
                    if (result instanceof Boolean) {
                        isLoading = (Boolean) result;
                    } else if (result instanceof String) {
                        isLoading = "true".equals(result) || "loading".equals(result);
                    }
                    
                    System.out.printf("    %s = %s (%s)%n", variable, result, 
                        isLoading ? "LOADING" : "READY");
                    
                    return !isLoading;
                });
                
                System.out.printf("  %s indicates loading complete%n", variable);
                
            } catch (TimeoutException e) {
                System.out.printf("  WARNING: %s did not clear within timeout%n", variable);
            } catch (JavascriptException e) {
                System.out.printf("  %s not available or accessible%n", variable);
            }
        }
    }
    
    public void waitForNetworkActivity() {
        System.out.println("Monitoring network activity for completion...");
        
        wait.until(driver -> {
            String script = """
                if (typeof window.performance === 'undefined') {
                    return true; // Assume ready if performance API not available
                }
                
                // Check for active requests
                var entries = window.performance.getEntriesByType('resource');
                var now = performance.now();
                
                // Find requests that started recently but haven't finished
                var activeRequests = entries.filter(function(entry) {
                    return entry.responseEnd === 0 || (now - entry.fetchStart) < 100;
                });
                
                // Check for pending XHR/Fetch requests (if monitored by application)
                var pendingXHR = 0;
                if (window.activeRequests !== undefined) {
                    pendingXHR = window.activeRequests;
                }
                
                var isNetworkIdle = activeRequests.length === 0 && pendingXHR === 0;
                
                if (!isNetworkIdle) {
                    console.log('Network activity detected:', {
                        activeRequests: activeRequests.length,
                        pendingXHR: pendingXHR
                    });
                }
                
                return isNetworkIdle;
            """;
            
            return (Boolean) js.executeScript(script);
        });
        
        System.out.println("Network activity completed");
    }
    
    public void waitForDOMStability() {
        System.out.println("Monitoring DOM stability...");
        
        // Take initial DOM snapshot
        String initialHTML = driver.getPageSource();
        
        wait.until(driver -> {
            try {
                Thread.sleep(500); // Allow time for changes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return true;
            }
            
            String currentHTML = driver.getPageSource();
            boolean isStable = initialHTML.equals(currentHTML);
            
            if (!isStable) {
                System.out.println("  DOM changes detected, continuing to monitor...");
                // Update comparison point for next check
                Thread.currentThread().setName(currentHTML); // Store for next iteration
            }
            
            return isStable;
        });
        
        System.out.println("DOM stability achieved");
    }
    
    public void waitForFrameworkSpecificLoading() {
        System.out.println("Detecting framework-specific loading patterns...");
        
        // React loading detection
        if (detectReactApp()) {
            waitForReactLoadingCompletion();
        }
        
        // Angular loading detection
        if (detectAngularApp()) {
            waitForAngularLoadingCompletion();
        }
        
        // Vue loading detection
        if (detectVueApp()) {
            waitForVueLoadingCompletion();
        }
        
        // jQuery loading detection
        if (detectjQueryUsage()) {
            waitForjQueryLoadingCompletion();
        }
    }
    
    private boolean detectReactApp() {
        try {
            String script = """
                return typeof window.React !== 'undefined' || 
                       document.querySelector('[data-reactroot]') !== null ||
                       document.querySelector('div[id="root"]') !== null;
            """;
            return (Boolean) js.executeScript(script);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void waitForReactLoadingCompletion() {
        System.out.println("  Waiting for React application loading completion...");
        
        wait.until(driver -> {
            String script = """
                // Check if React is still rendering
                if (window.React && window.React.__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED) {
                    var internals = window.React.__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED;
                    var isUpdating = internals.ReactCurrentDispatcher && 
                                   internals.ReactCurrentDispatcher.current !== null;
                    if (isUpdating) return false;
                }
                
                // Check for common React loading indicators
                var loadingElements = document.querySelectorAll('.react-loading, [data-loading], .spinner');
                return loadingElements.length === 0;
            """;
            
            return (Boolean) js.executeScript(script);
        });
        
        System.out.println("  React loading completed");
    }
    
    private boolean detectAngularApp() {
        try {
            String script = """
                return typeof window.angular !== 'undefined' || 
                       typeof window.ng !== 'undefined' ||
                       document.querySelector('[ng-app]') !== null ||
                       document.querySelector('app-root') !== null;
            """;
            return (Boolean) js.executeScript(script);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void waitForAngularLoadingCompletion() {
        System.out.println("  Waiting for Angular application loading completion...");
        
        wait.until(driver -> {
            String script = """
                // Angular Universal or Angular Elements
                if (typeof window.getAllAngularRootElements === 'function') {
                    var elements = window.getAllAngularRootElements();
                    return elements.length > 0;
                }
                
                // Check for Angular loading indicators
                var loadingElements = document.querySelectorAll('mat-spinner, mat-progress-bar, .angular-loading');
                return loadingElements.length === 0;
            """;
            
            return (Boolean) js.executeScript(script);
        });
        
        System.out.println("  Angular loading completed");
    }
    
    private boolean detectVueApp() {
        try {
            String script = "return typeof window.Vue !== 'undefined' || document.querySelector('[data-v-]') !== null;";
            return (Boolean) js.executeScript(script);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void waitForVueLoadingCompletion() {
        System.out.println("  Waiting for Vue application loading completion...");
        
        wait.until(driver -> {
            String script = """
                // Check for Vue loading indicators
                var loadingElements = document.querySelectorAll('.vue-loading, [v-loading], .v-progress-circular');
                return loadingElements.length === 0;
            """;
            
            return (Boolean) js.executeScript(script);
        });
        
        System.out.println("  Vue loading completed");
    }
    
    private boolean detectjQueryUsage() {
        try {
            String script = "return typeof window.jQuery !== 'undefined' || typeof window.$ !== 'undefined';";
            return (Boolean) js.executeScript(script);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void waitForjQueryLoadingCompletion() {
        System.out.println("  Waiting for jQuery AJAX completion...");
        
        wait.until(driver -> {
            String script = """
                if (typeof window.jQuery !== 'undefined') {
                    return window.jQuery.active === 0;
                }
                return true;
            """;
            
            return (Boolean) js.executeScript(script);
        });
        
        System.out.println("  jQuery AJAX requests completed");
    }
    
    public void performComprehensiveLoadingDetection() {
        System.out.println("Performing comprehensive programmatic loading detection...");
        
        // Step 1: JavaScript variables
        waitForJavaScriptLoadingVariables();
        
        // Step 2: Network activity
        waitForNetworkActivity();
        
        // Step 3: Framework-specific loading
        waitForFrameworkSpecificLoading();
        
        // Step 4: DOM stability (final check)
        waitForDOMStability();
        
        System.out.println("Comprehensive loading detection completed");
    }
}
```

**Pros:**
- **Precise detection**: Direct access to application state and network activity
- **Framework awareness**: Specific handling for popular JavaScript frameworks
- **Performance insight**: Provides detailed timing and activity information
- **Programmatic control**: Can integrate with application's own loading management
- **Debugging capability**: Rich information for troubleshooting loading issues

**Cons:**
- **JavaScript dependency**: Requires applications to expose loading state programmatically
- **Framework coupling**: Framework-specific code needs maintenance
- **Browser limitations**: Some APIs may not be available in all browsers
- **Security restrictions**: Cross-origin and security policies may limit access
- **Implementation variability**: Different applications expose different loading APIs

## Decision Framework

### Choose Visual Indicator Detection When:
- Testing user-facing loading experience
- Working with traditional web applications using standard UI patterns
- Visual regression testing is important
- Loading indicators follow consistent CSS conventions
- Team prioritizes maintainable, readable test code

### Choose Semantic/Accessibility Detection When:
- Application has strong accessibility requirements
- Testing with assistive technologies
- Semantic markup is well-implemented
- Compliance with accessibility standards is required
- User experience includes screen reader users

### Choose Programmatic Detection When:
- Working with JavaScript-heavy applications or SPAs
- Need precise timing measurements
- Application exposes loading state programmatically
- Framework-specific loading patterns are used
- Performance testing requires detailed loading metrics

## Real-world Examples from Codebase Context

### Current Implementation Analysis

**DynamicLoadingTest.java** waits for element visibility without considering loading indicators:
```java
wait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_MESSAGE));
```

**Enhanced with loading indicator detection:**
```java
@Test
public void dynamicLoadingWithIndicatorDetection() {
    driver.findElement(START_BUTTON).click();
    
    // Step 1: Wait for loading indicator to appear
    try {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        shortWait.until(ExpectedConditions.presenceOfElementLocated(By.id("loading")));
        System.out.println("Loading indicator detected");
    } catch (TimeoutException e) {
        System.out.println("No loading indicator appeared");
    }
    
    // Step 2: Wait for loading indicator to disappear
    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loading")));
    System.out.println("Loading indicator disappeared");
    
    // Step 3: Wait for actual content
    wait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_MESSAGE));
    System.out.println("Content appeared");
    
    // Verify the loading sequence was proper
    WebElement message = driver.findElement(FINISH_MESSAGE);
    Assert.assertTrue(message.isDisplayed());
    Assert.assertEquals(message.getText(), "Hello World!");
}
```

**Ex03DynamicLoadingWithFluentWait.java** improved with indicator awareness:
```java
@Test
public void fluentWaitWithLoadingIndicators() {
    // Click start and wait for loading sequence
    fluentWait.until(ExpectedConditions.elementToBeClickable(START_BUTTON)).click();
    
    // Enhanced: Wait for loading spinner to disappear using multiple strategies
    VisualIndicatorDetectionStrategy visualStrategy = new VisualIndicatorDetectionStrategy(driver, fluentWait);
    visualStrategy.waitForLoadingIndicatorToDisappear(
        LoadingIndicatorType.SPINNER,
        LoadingIndicatorType.GENERIC_LOADING
    );
    
    // Wait for finish message with better error context
    WebElement message = fluentWait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_MESSAGE));
    
    System.out.println("Loading sequence completed with indicator detection");
    Assert.assertTrue(message.isDisplayed());
    Assert.assertEquals(message.getText().trim(), "Hello World!");
}
```

## Advanced Loading Indicator Patterns

### 1. Composite Loading Detection
```java
public class CompositeLoadingStrategy {
    private VisualIndicatorDetectionStrategy visualStrategy;
    private SemanticIndicatorDetectionStrategy semanticStrategy;
    private ProgrammaticLoadingDetectionStrategy programmaticStrategy;
    
    public void waitForAllLoadingToComplete() {
        // Run all strategies in parallel for comprehensive coverage
        CompletableFuture<Void> visualFuture = CompletableFuture.runAsync(() -> 
            visualStrategy.detectAndWaitForAllLoadingIndicators());
        
        CompletableFuture<Void> semanticFuture = CompletableFuture.runAsync(() -> 
            semanticStrategy.waitForAccessibleLoadingCompletion());
        
        CompletableFuture<Void> programmaticFuture = CompletableFuture.runAsync(() -> 
            programmaticStrategy.performComprehensiveLoadingDetection());
        
        // Wait for all strategies to complete
        CompletableFuture.allOf(visualFuture, semanticFuture, programmaticFuture).join();
    }
}
```

### 2. Loading Performance Measurement
```java
public class LoadingPerformanceMeasurement {
    public LoadingMetrics measureLoadingPerformance(Runnable loadingTrigger) {
        long startTime = System.currentTimeMillis();
        
        // Trigger loading
        loadingTrigger.run();
        
        // Measure different loading phases
        long indicatorAppearTime = waitForLoadingIndicatorAppear();
        long indicatorDisappearTime = waitForLoadingIndicatorDisappear();
        long contentReadyTime = waitForContentReady();
        
        return new LoadingMetrics(
            startTime,
            indicatorAppearTime,
            indicatorDisappearTime,
            contentReadyTime
        );
    }
}
```

## Common Loading Indicator Pitfalls

### 1. Waiting for Wrong Indicators
**Problem**: Waiting for indicators that don't actually represent loading state
```java
// BAD: Generic loading class may not indicate actual loading
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("loading")));
```

**Solution**: Verify indicator actually represents loading state
```java
// GOOD: Verify indicator appears before disappearing
if (isLoadingIndicatorRelevant(By.className("loading"))) {
    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("loading")));
}
```

### 2. Missing Multiple Loading Phases
**Problem**: Only waiting for one loading indicator when multiple phases exist
```java
// BAD: Only waits for spinner, misses progress bar
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("spinner")));
```

**Solution**: Wait for all loading phases
```java
// GOOD: Comprehensive loading detection
waitForLoadingIndicatorToDisappear(LoadingIndicatorType.SPINNER, LoadingIndicatorType.PROGRESS_BAR);
```

### 3. Premature Content Interaction
**Problem**: Interacting with content before loading is truly complete
```java
// BAD: Content may still be loading even if visible
wait.until(ExpectedConditions.visibilityOfElementLocated(contentLocator));
element.click(); // May fail if content still loading
```

**Solution**: Verify content is fully loaded
```java
// GOOD: Ensure content is complete and interactive
waitForContentFullyLoaded(contentLocator);
wait.until(ExpectedConditions.elementToBeClickable(contentLocator));
```

## Further Reading

- [Loading UX Patterns](https://uxdesign.cc/loading-patterns-designs-that-reduce-perceived-wait-time-30c18760f5c3)
- [ARIA Live Regions](https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Live_Regions)
- [Progressive Web App Loading](https://web.dev/loading-best-practices/)
- [Performance Timing API](https://developer.mozilla.org/en-US/docs/Web/API/Performance)

## Key Takeaways

- **Different loading indicators require different detection strategies**
- **Visual indicators align with user experience but may be fragile**
- **Semantic indicators provide accessibility compliance and stability**
- **Programmatic detection offers precision but requires application cooperation**
- **Comprehensive loading detection combines multiple strategies**
- **Loading sequence often has multiple phases that must be handled separately**
- **Proper loading indicator handling is essential for reliable dynamic content testing**
- **Framework-specific loading patterns require specialized detection logic**