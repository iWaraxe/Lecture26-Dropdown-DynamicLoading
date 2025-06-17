# Debugging Dynamic Loading: Troubleshooting Skills and Diagnostic Techniques

## Problem Statement

Dynamic loading failures are often intermittent, environment-specific, and difficult to reproduce. Traditional debugging approaches that work for static content frequently fail with dynamic web applications. Understanding how to systematically diagnose, analyze, and resolve dynamic loading issues is essential for maintaining reliable automation and delivering high-quality web applications.

## Why It Matters

Dynamic loading debugging affects:
- **Issue Resolution Speed**: Faster identification and fixing of loading problems
- **Test Reliability**: Reducing flaky tests through better understanding of failure modes
- **Development Velocity**: Preventing loading issues from blocking development cycles
- **User Experience**: Ensuring loading behaviors work correctly for end users
- **Team Productivity**: Reducing time spent on debugging and support escalations

## Understanding Dynamic Loading Failure Patterns

### Common Failure Categories
1. **Timing Issues**: Race conditions, premature interactions, timeout problems
2. **State Management**: Inconsistent application state, cache issues
3. **Network Dependencies**: API failures, slow responses, connectivity issues
4. **Browser Differences**: Rendering variations, JavaScript engine differences
5. **Framework Conflicts**: Library incompatibilities, version mismatches

### Debugging Complexity Factors
- **Intermittent Nature**: Issues that occur unpredictably
- **Environment Sensitivity**: Works locally but fails in CI/CD
- **Multi-layer Dependencies**: UI, network, server, database interactions
- **Asynchronous Behavior**: Non-deterministic timing and order

## Multiple Solutions

### Solution 1: Systematic Diagnostic Framework

**When to Use**: Complex loading issues, team debugging processes, systematic investigation needs

```java
public class DynamicLoadingDiagnostics {
    private WebDriver driver;
    private JavascriptExecutor js;
    private List<DiagnosticEntry> diagnosticLog;
    
    public static class DiagnosticEntry {
        private final long timestamp;
        private final String category;
        private final String message;
        private final Map<String, Object> data;
        
        public DiagnosticEntry(String category, String message, Map<String, Object> data) {
            this.timestamp = System.currentTimeMillis();
            this.category = category;
            this.message = message;
            this.data = data != null ? new HashMap<>(data) : new HashMap<>();
        }
        
        @Override
        public String toString() {
            return String.format("[%d] %s: %s %s", timestamp, category, message, 
                data.isEmpty() ? "" : data.toString());
        }
    }
    
    public DynamicLoadingDiagnostics(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
        this.diagnosticLog = new ArrayList<>();
    }
    
    public void diagnoseDynamicLoadingIssue(Runnable loadingAction, By expectedElementLocator) {
        System.out.println("Starting comprehensive dynamic loading diagnosis...");
        
        // Phase 1: Pre-loading environment capture
        capturePreLoadingState();
        
        // Phase 2: Execute loading action with monitoring
        monitorLoadingExecution(loadingAction);
        
        // Phase 3: Post-loading analysis
        analyzePostLoadingState(expectedElementLocator);
        
        // Phase 4: Generate diagnostic report
        generateDiagnosticReport();
    }
    
    private void capturePreLoadingState() {
        addDiagnosticEntry("PRE-LOADING", "Capturing initial state", null);
        
        // Capture page state
        Map<String, Object> pageState = new HashMap<>();
        pageState.put("url", driver.getCurrentUrl());
        pageState.put("title", driver.getTitle());
        pageState.put("readyState", js.executeScript("return document.readyState"));
        
        addDiagnosticEntry("PAGE_STATE", "Initial page state", pageState);
        
        // Capture network state
        captureNetworkState("PRE-LOADING");
        
        // Capture JavaScript errors
        captureJavaScriptErrors("PRE-LOADING");
        
        // Capture performance metrics
        capturePerformanceMetrics("PRE-LOADING");
    }
    
    private void monitorLoadingExecution(Runnable loadingAction) {
        addDiagnosticEntry("EXECUTION", "Starting loading action", null);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute the loading action
            loadingAction.run();
            
            long endTime = System.currentTimeMillis();
            Map<String, Object> executionData = new HashMap<>();
            executionData.put("duration_ms", endTime - startTime);
            executionData.put("status", "SUCCESS");
            
            addDiagnosticEntry("EXECUTION", "Loading action completed", executionData);
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            Map<String, Object> executionData = new HashMap<>();
            executionData.put("duration_ms", endTime - startTime);
            executionData.put("status", "FAILED");
            executionData.put("exception", e.getClass().getSimpleName());
            executionData.put("message", e.getMessage());
            
            addDiagnosticEntry("EXECUTION", "Loading action failed", executionData);
        }
        
        // Monitor immediate post-execution state
        try {
            Thread.sleep(1000); // Allow immediate changes to settle
            captureNetworkState("POST-EXECUTION");
            captureJavaScriptErrors("POST-EXECUTION");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void analyzePostLoadingState(By expectedElementLocator) {
        addDiagnosticEntry("POST-LOADING", "Analyzing final state", null);
        
        // Check for expected element
        analyzeElementState(expectedElementLocator);
        
        // Check for loading indicators
        analyzeLoadingIndicators();
        
        // Check for error states
        analyzeErrorStates();
        
        // Final performance metrics
        capturePerformanceMetrics("POST-LOADING");
    }
    
    private void captureNetworkState(String phase) {
        try {
            String networkScript = """
                if (typeof window.performance === 'undefined') {
                    return { available: false };
                }
                
                var entries = window.performance.getEntriesByType('resource');
                var recent = entries.filter(function(entry) {
                    return (Date.now() - entry.responseEnd) < 5000;
                });
                
                return {
                    available: true,
                    totalRequests: entries.length,
                    recentRequests: recent.length,
                    pendingRequests: recent.filter(function(e) { return e.responseEnd === 0; }).length,
                    failedRequests: recent.filter(function(e) { return e.responseEnd === 0 && e.duration > 30000; }).length
                };
            """;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> networkData = (Map<String, Object>) js.executeScript(networkScript);
            
            addDiagnosticEntry("NETWORK", phase + " network state", networkData);
            
        } catch (Exception e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            addDiagnosticEntry("NETWORK", phase + " network capture failed", errorData);
        }
    }
    
    private void captureJavaScriptErrors(String phase) {
        try {
            String errorScript = """
                var errors = [];
                if (window.jsErrors) {
                    errors = window.jsErrors;
                } else {
                    // Setup error capturing if not already done
                    window.jsErrors = [];
                    window.addEventListener('error', function(e) {
                        window.jsErrors.push({
                            message: e.message,
                            filename: e.filename,
                            lineno: e.lineno,
                            timestamp: Date.now()
                        });
                    });
                }
                
                // Return recent errors
                var recent = errors.filter(function(error) {
                    return (Date.now() - error.timestamp) < 10000;
                });
                
                return recent;
            """;
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> errors = (List<Map<String, Object>>) js.executeScript(errorScript);
            
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("errorCount", errors.size());
            errorData.put("errors", errors);
            
            addDiagnosticEntry("JS_ERRORS", phase + " JavaScript errors", errorData);
            
        } catch (Exception e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("captureError", e.getMessage());
            addDiagnosticEntry("JS_ERRORS", phase + " error capture failed", errorData);
        }
    }
    
    private void capturePerformanceMetrics(String phase) {
        try {
            String performanceScript = """
                if (typeof window.performance === 'undefined') {
                    return { available: false };
                }
                
                var navigation = window.performance.getEntriesByType('navigation')[0];
                var memory = window.performance.memory;
                
                return {
                    available: true,
                    domContentLoaded: navigation ? navigation.domContentLoadedEventEnd - navigation.domContentLoadedEventStart : -1,
                    loadComplete: navigation ? navigation.loadEventEnd - navigation.loadEventStart : -1,
                    memoryUsed: memory ? memory.usedJSHeapSize : -1,
                    memoryTotal: memory ? memory.totalJSHeapSize : -1,
                    timestamp: performance.now()
                };
            """;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> performanceData = (Map<String, Object>) js.executeScript(performanceScript);
            
            addDiagnosticEntry("PERFORMANCE", phase + " metrics", performanceData);
            
        } catch (Exception e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            addDiagnosticEntry("PERFORMANCE", phase + " capture failed", errorData);
        }
    }
    
    private void analyzeElementState(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            
            Map<String, Object> elementData = new HashMap<>();
            elementData.put("elementsFound", elements.size());
            
            if (elements.isEmpty()) {
                elementData.put("status", "NOT_FOUND");
                
                // Try similar selectors
                List<String> similarSelectors = generateSimilarSelectors(locator);
                for (String selector : similarSelectors) {
                    List<WebElement> similarElements = driver.findElements(By.cssSelector(selector));
                    if (!similarElements.isEmpty()) {
                        elementData.put("similar_" + selector, similarElements.size());
                    }
                }
            } else {
                WebElement element = elements.get(0);
                elementData.put("status", "FOUND");
                elementData.put("isDisplayed", element.isDisplayed());
                elementData.put("isEnabled", element.isEnabled());
                elementData.put("tagName", element.getTagName());
                elementData.put("className", element.getAttribute("class"));
                elementData.put("text", element.getText().substring(0, Math.min(100, element.getText().length())));
            }
            
            addDiagnosticEntry("ELEMENT", "Expected element analysis", elementData);
            
        } catch (Exception e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            addDiagnosticEntry("ELEMENT", "Element analysis failed", errorData);
        }
    }
    
    private void analyzeLoadingIndicators() {
        String[] loadingSelectors = {
            ".loading", ".spinner", ".loader", 
            "[data-loading='true']", ".progress", 
            ".skeleton", ".shimmer"
        };
        
        Map<String, Object> loadingData = new HashMap<>();
        int totalIndicators = 0;
        
        for (String selector : loadingSelectors) {
            List<WebElement> indicators = driver.findElements(By.cssSelector(selector));
            if (!indicators.isEmpty()) {
                int visibleCount = (int) indicators.stream().filter(WebElement::isDisplayed).count();
                loadingData.put(selector, visibleCount);
                totalIndicators += visibleCount;
            }
        }
        
        loadingData.put("totalVisibleIndicators", totalIndicators);
        addDiagnosticEntry("LOADING_INDICATORS", "Active loading indicators", loadingData);
    }
    
    private void analyzeErrorStates() {
        String[] errorSelectors = {
            ".error", ".alert-danger", ".notification-error",
            "[role='alert']", ".toast-error", ".message-error"
        };
        
        Map<String, Object> errorData = new HashMap<>();
        List<String> errorMessages = new ArrayList<>();
        
        for (String selector : errorSelectors) {
            List<WebElement> errorElements = driver.findElements(By.cssSelector(selector));
            for (WebElement errorElement : errorElements) {
                if (errorElement.isDisplayed()) {
                    String message = errorElement.getText().trim();
                    if (!message.isEmpty()) {
                        errorMessages.add(message);
                    }
                }
            }
        }
        
        errorData.put("errorCount", errorMessages.size());
        errorData.put("messages", errorMessages);
        addDiagnosticEntry("ERROR_STATES", "Visible error messages", errorData);
    }
    
    private List<String> generateSimilarSelectors(By locator) {
        // Simple approach - generate variations of CSS selectors
        List<String> similar = new ArrayList<>();
        String originalSelector = locator.toString();
        
        if (originalSelector.contains("id=")) {
            String id = originalSelector.substring(originalSelector.indexOf("id=") + 3);
            similar.add("[id*='" + id + "']");
            similar.add("[id^='" + id + "']");
            similar.add("[id$='" + id + "']");
        }
        
        if (originalSelector.contains("class=")) {
            String className = originalSelector.substring(originalSelector.indexOf("class=") + 6);
            similar.add("[class*='" + className + "']");
            similar.add("." + className);
        }
        
        return similar;
    }
    
    private void addDiagnosticEntry(String category, String message, Map<String, Object> data) {
        DiagnosticEntry entry = new DiagnosticEntry(category, message, data);
        diagnosticLog.add(entry);
        System.out.println("DIAGNOSTIC: " + entry);
    }
    
    private void generateDiagnosticReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DYNAMIC LOADING DIAGNOSTIC REPORT");
        System.out.println("=".repeat(60));
        
        // Group entries by category
        Map<String, List<DiagnosticEntry>> categorized = diagnosticLog.stream()
            .collect(Collectors.groupingBy(entry -> entry.category));
        
        for (Map.Entry<String, List<DiagnosticEntry>> categoryGroup : categorized.entrySet()) {
            System.out.println("\n" + categoryGroup.getKey() + ":");
            System.out.println("-".repeat(30));
            
            for (DiagnosticEntry entry : categoryGroup.getValue()) {
                System.out.println("  " + entry.message);
                if (!entry.data.isEmpty()) {
                    entry.data.forEach((key, value) -> 
                        System.out.println("    " + key + ": " + value));
                }
                System.out.println();
            }
        }
        
        // Generate summary
        generateDiagnosticSummary(categorized);
    }
    
    private void generateDiagnosticSummary(Map<String, List<DiagnosticEntry>> categorized) {
        System.out.println("\nDIAGNOSTIC SUMMARY:");
        System.out.println("-".repeat(20));
        
        // Identify likely issues
        List<String> likelyIssues = new ArrayList<>();
        
        // Check for JavaScript errors
        List<DiagnosticEntry> jsErrors = categorized.getOrDefault("JS_ERRORS", Collections.emptyList());
        long totalJsErrors = jsErrors.stream()
            .flatMap(entry -> {
                Object errors = entry.data.get("errors");
                if (errors instanceof List) {
                    return ((List<?>) errors).stream();
                }
                return Stream.empty();
            }).count();
        
        if (totalJsErrors > 0) {
            likelyIssues.add("JavaScript errors detected (" + totalJsErrors + " errors)");
        }
        
        // Check for network issues
        List<DiagnosticEntry> networkEntries = categorized.getOrDefault("NETWORK", Collections.emptyList());
        boolean networkIssues = networkEntries.stream()
            .anyMatch(entry -> {
                Object pending = entry.data.get("pendingRequests");
                Object failed = entry.data.get("failedRequests");
                return (pending instanceof Number && ((Number) pending).intValue() > 0) ||
                       (failed instanceof Number && ((Number) failed).intValue() > 0);
            });
        
        if (networkIssues) {
            likelyIssues.add("Network request issues detected");
        }
        
        // Check for persistent loading indicators
        List<DiagnosticEntry> loadingEntries = categorized.getOrDefault("LOADING_INDICATORS", Collections.emptyList());
        boolean persistentLoading = loadingEntries.stream()
            .anyMatch(entry -> {
                Object total = entry.data.get("totalVisibleIndicators");
                return total instanceof Number && ((Number) total).intValue() > 0;
            });
        
        if (persistentLoading) {
            likelyIssues.add("Loading indicators still visible");
        }
        
        // Check for error states
        List<DiagnosticEntry> errorEntries = categorized.getOrDefault("ERROR_STATES", Collections.emptyList());
        boolean errorStates = errorEntries.stream()
            .anyMatch(entry -> {
                Object count = entry.data.get("errorCount");
                return count instanceof Number && ((Number) count).intValue() > 0;
            });
        
        if (errorStates) {
            likelyIssues.add("Error messages visible on page");
        }
        
        if (likelyIssues.isEmpty()) {
            System.out.println("No obvious issues detected. The problem may be timing-related or environment-specific.");
        } else {
            System.out.println("LIKELY ISSUES:");
            likelyIssues.forEach(issue -> System.out.println("  - " + issue));
        }
        
        System.out.println("\nRECOMMENDAT IONS:");
        generateRecommendations(likelyIssues);
    }
    
    private void generateRecommendations(List<String> issues) {
        if (issues.stream().anyMatch(issue -> issue.contains("JavaScript errors"))) {
            System.out.println("  - Check browser console for JavaScript errors");
            System.out.println("  - Verify all required JavaScript libraries are loaded");
        }
        
        if (issues.stream().anyMatch(issue -> issue.contains("Network"))) {
            System.out.println("  - Check network connectivity and API endpoint availability");
            System.out.println("  - Verify API responses are returning expected data");
            System.out.println("  - Consider increasing wait timeouts for slow network conditions");
        }
        
        if (issues.stream().anyMatch(issue -> issue.contains("Loading indicators"))) {
            System.out.println("  - Wait for all loading indicators to disappear before proceeding");
            System.out.println("  - Check if loading indicators are controlled by JavaScript");
        }
        
        if (issues.stream().anyMatch(issue -> issue.contains("Error messages"))) {
            System.out.println("  - Investigate error message content for specific failure reasons");
            System.out.println("  - Check application logs for server-side errors");
        }
        
        // General recommendations
        System.out.println("  - Verify test data and prerequisites are correctly set up");
        System.out.println("  - Try running the test in a different browser or environment");
        System.out.println("  - Add additional logging and screenshots at failure points");
    }
}
```

**Pros:**
- **Comprehensive analysis**: Captures multiple aspects of loading behavior simultaneously
- **Systematic approach**: Structured framework for consistent debugging
- **Rich context**: Provides detailed information for root cause analysis
- **Reusable framework**: Can be applied to different loading scenarios
- **Clear reporting**: Generates organized diagnostic reports

**Cons:**
- **Performance overhead**: Extensive monitoring slows down test execution
- **Complexity**: Requires understanding of multiple debugging techniques
- **Maintenance burden**: Framework needs updates as applications change
- **Information overload**: May generate too much data for simple issues
- **Browser dependencies**: Some diagnostic features may not work in all browsers

### Solution 2: Lightweight Debugging Utilities

**When to Use**: Quick debugging sessions, specific issue investigation, minimal overhead requirements

```java
public class QuickDebuggingUtilities {
    private WebDriver driver;
    private JavascriptExecutor js;
    
    public QuickDebuggingUtilities(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
    }
    
    public void capturePageSnapshot(String description) {
        System.out.println("\n--- PAGE SNAPSHOT: " + description + " ---");
        
        // Basic page info
        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("Title: " + driver.getTitle());
        
        // Page ready state
        String readyState = (String) js.executeScript("return document.readyState");
        System.out.println("Ready State: " + readyState);
        
        // Visible loading indicators
        List<WebElement> loadingElements = driver.findElements(
            By.cssSelector(".loading, .spinner, .loader, [data-loading='true']"));
        System.out.println("Loading Indicators: " + loadingElements.size() + " found");
        
        // Visible errors
        List<WebElement> errorElements = driver.findElements(
            By.cssSelector(".error, .alert-danger, [role='alert']"));
        System.out.println("Error Elements: " + errorElements.size() + " found");
        
        if (!errorElements.isEmpty()) {
            errorElements.forEach(error -> {
                if (error.isDisplayed()) {
                    System.out.println("  Error: " + error.getText().trim());
                }
            });
        }
        
        System.out.println("--- END SNAPSHOT ---\n");
    }
    
    public void waitWithLogging(ExpectedCondition<?> condition, int timeoutSeconds, String description) {
        System.out.println("WAITING: " + description);
        long startTime = System.currentTimeMillis();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        
        try {
            wait.until(condition);
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("SUCCESS: " + description + " (took " + duration + "ms)");
        } catch (TimeoutException e) {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("TIMEOUT: " + description + " (after " + duration + "ms)");
            
            // Capture diagnostic info on timeout
            capturePageSnapshot("TIMEOUT_ANALYSIS");
            throw e;
        }
    }
    
    public boolean isElementEventuallyPresent(By locator, int timeoutSeconds) {
        System.out.printf("Checking if element %s becomes present within %ds...%n", locator, timeoutSeconds);
        
        long endTime = System.currentTimeMillis() + (timeoutSeconds * 1000);
        int checkCount = 0;
        
        while (System.currentTimeMillis() < endTime) {
            checkCount++;
            List<WebElement> elements = driver.findElements(locator);
            
            if (!elements.isEmpty()) {
                System.out.printf("Element found on check #%d%n", checkCount);
                return true;
            }
            
            if (checkCount % 10 == 0) {
                System.out.printf("Still checking... (attempt #%d)%n", checkCount);
            }
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        System.out.printf("Element not found after %d checks%n", checkCount);
        return false;
    }
    
    public void monitorNetworkActivity(int durationSeconds) {
        System.out.printf("Monitoring network activity for %d seconds...%n", durationSeconds);
        
        String monitoringScript = """
            window.networkMonitor = {
                requests: [],
                startTime: Date.now(),
                
                logRequest: function(url, type) {
                    this.requests.push({
                        url: url,
                        type: type,
                        timestamp: Date.now() - this.startTime
                    });
                }
            };
            
            // Override fetch
            const originalFetch = window.fetch;
            window.fetch = function(...args) {
                window.networkMonitor.logRequest(args[0], 'fetch');
                return originalFetch.apply(this, args);
            };
            
            // Override XMLHttpRequest
            const originalXHR = window.XMLHttpRequest;
            function CustomXHR() {
                const xhr = new originalXHR();
                const originalOpen = xhr.open;
                xhr.open = function(method, url) {
                    window.networkMonitor.logRequest(url, 'xhr');
                    return originalOpen.apply(this, arguments);
                };
                return xhr;
            }
            CustomXHR.prototype = originalXHR.prototype;
            window.XMLHttpRequest = CustomXHR;
        """;
        
        js.executeScript(monitoringScript);
        
        try {
            Thread.sleep(durationSeconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Retrieve monitoring results
        String resultsScript = """
            return window.networkMonitor ? window.networkMonitor.requests : [];
        """;
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> requests = (List<Map<String, Object>>) js.executeScript(resultsScript);
        
        System.out.printf("Network activity summary: %d requests detected%n", requests.size());
        requests.forEach(request -> 
            System.out.printf("  %s: %s (at %sms)%n", 
                request.get("type"), request.get("url"), request.get("timestamp")));
    }
    
    public void checkForJavaScriptErrors() {
        System.out.println("Checking for JavaScript errors...");
        
        String errorCheckScript = """
            var errors = [];
            
            // Check if we've already set up error monitoring
            if (!window.errorMonitor) {
                window.errorMonitor = [];
                window.addEventListener('error', function(e) {
                    window.errorMonitor.push({
                        message: e.message,
                        filename: e.filename,
                        lineno: e.lineno,
                        timestamp: Date.now()
                    });
                });
            }
            
            return window.errorMonitor || [];
        """;
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) js.executeScript(errorCheckScript);
        
        if (errors.isEmpty()) {
            System.out.println("No JavaScript errors detected");
        } else {
            System.out.printf("%d JavaScript error(s) found:%n", errors.size());
            errors.forEach(error -> 
                System.out.printf("  %s at %s:%s%n", 
                    error.get("message"), error.get("filename"), error.get("lineno")));
        }
    }
    
    public void debugElementState(By locator) {
        System.out.println("ELEMENT DEBUG: " + locator);
        
        List<WebElement> elements = driver.findElements(locator);
        
        if (elements.isEmpty()) {
            System.out.println("  Status: NOT FOUND");
            
            // Try to find similar elements
            String selector = locator.toString();
            if (selector.contains("By.id:")) {
                String id = selector.substring(selector.indexOf("By.id:") + 6).trim();
                List<WebElement> partialMatches = driver.findElements(By.cssSelector("[id*='" + id + "']"));
                System.out.printf("  Partial ID matches: %d found%n", partialMatches.size());
            }
            
        } else {
            WebElement element = elements.get(0);
            System.out.println("  Status: FOUND");
            System.out.println("  Tag: " + element.getTagName());
            System.out.println("  Displayed: " + element.isDisplayed());
            System.out.println("  Enabled: " + element.isEnabled());
            System.out.println("  Class: " + element.getAttribute("class"));
            System.out.println("  Text: " + element.getText().substring(0, Math.min(50, element.getText().length())));
            
            // Check element position
            Point location = element.getLocation();
            Dimension size = element.getSize();
            System.out.printf("  Position: (%d, %d)%n", location.x, location.y);
            System.out.printf("  Size: %dx%d%n", size.width, size.height);
        }
    }
    
    public void takeScreenshotWithContext(String filename) {
        // Take screenshot
        if (driver instanceof TakesScreenshot) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                
                // Save screenshot (implementation depends on your setup)
                System.out.println("Screenshot captured: " + filename);
                
                // Also capture page source
                String pageSource = driver.getPageSource();
                System.out.println("Page source length: " + pageSource.length() + " characters");
                
            } catch (Exception e) {
                System.out.println("Failed to capture screenshot: " + e.getMessage());
            }
        }
    }
}

// Usage example
public class DebuggingExample {
    public void debugDynamicLoadingIssue() {
        QuickDebuggingUtilities debug = new QuickDebuggingUtilities(driver);
        
        debug.capturePageSnapshot("Before clicking start");
        
        driver.findElement(By.id("start")).click();
        
        debug.capturePageSnapshot("After clicking start");
        
        debug.waitWithLogging(
            ExpectedConditions.visibilityOfElementLocated(By.id("result")), 
            10, 
            "Result element to become visible"
        );
        
        debug.checkForJavaScriptErrors();
        debug.debugElementState(By.id("result"));
    }
}
```

**Pros:**
- **Lightweight**: Minimal performance impact on test execution
- **Quick setup**: Easy to add to existing tests for debugging
- **Focused information**: Provides specific diagnostic data without overwhelming detail
- **Easy to understand**: Simple utility methods with clear purposes
- **Flexible usage**: Can be used selectively for specific debugging needs

**Cons:**
- **Limited scope**: Less comprehensive than full diagnostic frameworks
- **Manual selection**: Requires choosing which debugging utilities to use
- **Less structured**: Information may not be as organized as systematic approaches
- **Missing correlations**: May not capture relationships between different issues
- **Limited automation**: Requires manual interpretation of results

### Solution 3: Interactive Debugging Session Tools

**When to Use**: Live debugging sessions, exploratory investigation, collaborative troubleshooting

```java
public class InteractiveDebuggingSession {
    private WebDriver driver;
    private JavascriptExecutor js;
    private Scanner scanner;
    private boolean sessionActive;
    
    public InteractiveDebuggingSession(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
        this.scanner = new Scanner(System.in);
        this.sessionActive = false;
    }
    
    public void startInteractiveSession() {
        sessionActive = true;
        System.out.println("\n" + "=".repeat(50));
        System.out.println("INTERACTIVE DEBUGGING SESSION STARTED");
        System.out.println("=".repeat(50));
        System.out.println("Type 'help' for available commands, 'exit' to quit");
        
        while (sessionActive) {
            System.out.print("\nDebug> ");
            String command = scanner.nextLine().trim().toLowerCase();
            
            processCommand(command);
        }
    }
    
    private void processCommand(String command) {
        String[] parts = command.split("\\s+", 2);
        String action = parts[0];
        String parameter = parts.length > 1 ? parts[1] : "";
        
        try {
            switch (action) {
                case "help":
                    showHelp();
                    break;
                case "status":
                    showPageStatus();
                    break;
                case "find":
                    findElement(parameter);
                    break;
                case "wait":
                    waitForElement(parameter);
                    break;
                case "js":
                    executeJavaScript(parameter);
                    break;
                case "errors":
                    checkErrors();
                    break;
                case "network":
                    checkNetworkActivity();
                    break;
                case "screenshot":
                    takeScreenshot();
                    break;
                case "source":
                    viewPageSource();
                    break;
                case "loading":
                    checkLoadingIndicators();
                    break;
                case "pause":
                    pauseExecution(parameter);
                    break;
                case "exit":
                    sessionActive = false;
                    System.out.println("Debugging session ended.");
                    break;
                default:
                    System.out.println("Unknown command: " + action + ". Type 'help' for available commands.");
            }
        } catch (Exception e) {
            System.out.println("Error executing command: " + e.getMessage());
        }
    }
    
    private void showHelp() {
        System.out.println("\nAvailable Commands:");
        System.out.println("  help              - Show this help message");
        System.out.println("  status            - Show current page status");
        System.out.println("  find <selector>   - Find element by CSS selector");
        System.out.println("  wait <selector>   - Wait for element to appear");
        System.out.println("  js <script>       - Execute JavaScript");
        System.out.println("  errors            - Check for JavaScript errors");
        System.out.println("  network           - Check network activity");
        System.out.println("  screenshot        - Take a screenshot");
        System.out.println("  source            - View page source summary");
        System.out.println("  loading           - Check for loading indicators");
        System.out.println("  pause <seconds>   - Pause execution");
        System.out.println("  exit              - End debugging session");
    }
    
    private void showPageStatus() {
        System.out.println("\nPage Status:");
        System.out.println("  URL: " + driver.getCurrentUrl());
        System.out.println("  Title: " + driver.getTitle());
        System.out.println("  Ready State: " + js.executeScript("return document.readyState"));
        
        // Count different element types
        int totalElements = driver.findElements(By.xpath("//*")).size();
        int visibleElements = driver.findElements(By.xpath("//*")).stream()
            .mapToInt(el -> el.isDisplayed() ? 1 : 0).sum();
        
        System.out.println("  Total Elements: " + totalElements);
        System.out.println("  Visible Elements: " + visibleElements);
    }
    
    private void findElement(String selector) {
        if (selector.isEmpty()) {
            System.out.println("Please provide a CSS selector. Example: find #myButton");
            return;
        }
        
        try {
            List<WebElement> elements = driver.findElements(By.cssSelector(selector));
            
            System.out.printf("Found %d element(s) matching '%s':%n", elements.size(), selector);
            
            for (int i = 0; i < Math.min(elements.size(), 5); i++) {
                WebElement element = elements.get(i);
                System.out.printf("  [%d] %s - displayed: %s, enabled: %s%n", 
                    i, element.getTagName(), element.isDisplayed(), element.isEnabled());
                
                String text = element.getText().trim();
                if (!text.isEmpty()) {
                    System.out.printf("      Text: %s%n", 
                        text.length() > 50 ? text.substring(0, 50) + "..." : text);
                }
            }
            
            if (elements.size() > 5) {
                System.out.printf("  ... and %d more%n", elements.size() - 5);
            }
            
        } catch (Exception e) {
            System.out.println("Error finding element: " + e.getMessage());
        }
    }
    
    private void waitForElement(String selector) {
        if (selector.isEmpty()) {
            System.out.println("Please provide a CSS selector. Example: wait #myButton");
            return;
        }
        
        System.out.println("Waiting for element: " + selector + " (timeout: 10s)");
        
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
            
            System.out.println("Element found and visible!");
            System.out.printf("  Tag: %s%n", element.getTagName());
            System.out.printf("  Text: %s%n", element.getText().trim());
            
        } catch (TimeoutException e) {
            System.out.println("Element did not appear within 10 seconds");
            
            // Check if element exists but not visible
            List<WebElement> elements = driver.findElements(By.cssSelector(selector));
            if (!elements.isEmpty()) {
                System.out.println("Element exists but is not visible");
            } else {
                System.out.println("Element does not exist in DOM");
            }
        }
    }
    
    private void executeJavaScript(String script) {
        if (script.isEmpty()) {
            System.out.println("Please provide JavaScript code. Example: js return document.title");
            return;
        }
        
        try {
            Object result = js.executeScript(script);
            System.out.println("JavaScript result: " + result);
        } catch (Exception e) {
            System.out.println("JavaScript error: " + e.getMessage());
        }
    }
    
    private void checkErrors() {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> errors = (List<Map<String, Object>>) js.executeScript(
                "return window.jsErrors || []");
            
            if (errors.isEmpty()) {
                System.out.println("No JavaScript errors detected");
            } else {
                System.out.printf("%d JavaScript error(s):%n", errors.size());
                errors.forEach(error -> 
                    System.out.printf("  %s%n", error.get("message")));
            }
        } catch (Exception e) {
            System.out.println("Unable to check JavaScript errors: " + e.getMessage());
        }
    }
    
    private void checkNetworkActivity() {
        try {
            String networkScript = """
                if (!window.performance) return { available: false };
                
                var entries = window.performance.getEntriesByType('resource');
                var recent = entries.filter(function(entry) {
                    return (Date.now() - entry.responseEnd) < 5000;
                });
                
                return {
                    available: true,
                    total: entries.length,
                    recent: recent.length,
                    recentUrls: recent.slice(0, 5).map(function(e) { return e.name; })
                };
            """;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> networkInfo = (Map<String, Object>) js.executeScript(networkScript);
            
            if ((Boolean) networkInfo.get("available")) {
                System.out.printf("Network Activity:%n");
                System.out.printf("  Total requests: %s%n", networkInfo.get("total"));
                System.out.printf("  Recent requests: %s%n", networkInfo.get("recent"));
                
                @SuppressWarnings("unchecked")
                List<String> recentUrls = (List<String>) networkInfo.get("recentUrls");
                if (!recentUrls.isEmpty()) {
                    System.out.println("  Recent URLs:");
                    recentUrls.forEach(url -> System.out.println("    " + url));
                }
            } else {
                System.out.println("Performance API not available");
            }
        } catch (Exception e) {
            System.out.println("Unable to check network activity: " + e.getMessage());
        }
    }
    
    private void takeScreenshot() {
        if (driver instanceof TakesScreenshot) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                String filename = "debug_screenshot_" + System.currentTimeMillis() + ".png";
                System.out.println("Screenshot captured: " + filename);
                // In real implementation, save the screenshot to file
            } catch (Exception e) {
                System.out.println("Failed to capture screenshot: " + e.getMessage());
            }
        } else {
            System.out.println("Screenshot capability not available");
        }
    }
    
    private void viewPageSource() {
        String source = driver.getPageSource();
        System.out.printf("Page Source Summary:%n");
        System.out.printf("  Length: %d characters%n", source.length());
        System.out.printf("  Contains 'error': %s%n", source.toLowerCase().contains("error"));
        System.out.printf("  Contains 'loading': %s%n", source.toLowerCase().contains("loading"));
        System.out.printf("  Contains 'success': %s%n", source.toLowerCase().contains("success"));
        
        // Show first few lines
        String[] lines = source.split("\n");
        System.out.println("  First 5 lines:");
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();
            if (line.length() > 80) {
                line = line.substring(0, 80) + "...";
            }
            System.out.println("    " + line);
        }
    }
    
    private void checkLoadingIndicators() {
        String[] selectors = {".loading", ".spinner", ".loader", "[data-loading='true']"};
        
        System.out.println("Loading Indicators:");
        boolean anyFound = false;
        
        for (String selector : selectors) {
            List<WebElement> elements = driver.findElements(By.cssSelector(selector));
            if (!elements.isEmpty()) {
                long visibleCount = elements.stream().filter(WebElement::isDisplayed).count();
                System.out.printf("  %s: %d total, %d visible%n", selector, elements.size(), visibleCount);
                anyFound = true;
            }
        }
        
        if (!anyFound) {
            System.out.println("  No loading indicators found");
        }
    }
    
    private void pauseExecution(String seconds) {
        int pauseTime = 5; // default
        
        if (!seconds.isEmpty()) {
            try {
                pauseTime = Integer.parseInt(seconds);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, using default 5 seconds");
            }
        }
        
        System.out.printf("Pausing for %d seconds...%n", pauseTime);
        try {
            Thread.sleep(pauseTime * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Pause complete");
    }
}

// Usage example
public class InteractiveDebuggingExample {
    public void debugWithInteractiveSession() {
        InteractiveDebuggingSession session = new InteractiveDebuggingSession(driver);
        
        // Navigate to problematic page
        driver.get("https://example.com/problematic-page");
        
        // Start interactive debugging
        session.startInteractiveSession();
        
        // Session runs until user types 'exit'
    }
}
```

**Pros:**
- **Real-time exploration**: Interactive investigation of live application state
- **Flexible investigation**: Can adapt debugging approach based on findings
- **Learning tool**: Helps understand application behavior through experimentation
- **Collaborative debugging**: Multiple team members can participate in investigation
- **Immediate feedback**: See results of debugging commands instantly

**Cons:**
- **Manual process**: Requires human interaction and cannot be automated
- **Time consuming**: Interactive sessions can take significant time
- **Not suitable for CI/CD**: Cannot be used in automated pipelines
- **Requires expertise**: Effectiveness depends on debugger's knowledge and experience
- **Limited reproducibility**: Sessions are unique and difficult to replay

## Decision Framework

### Choose Systematic Diagnostic Framework When:
- Dealing with complex, multi-faceted loading issues
- Need comprehensive documentation of failure scenarios
- Building team debugging processes and standards
- Issue requires deep analysis of multiple system layers
- Preparing reports for stakeholders or development teams

### Choose Lightweight Debugging Utilities When:
- Need quick investigation of specific issues
- Working with time constraints or performance requirements
- Debugging simple, focused problems
- Adding debugging capability to existing tests
- Working in environments where extensive monitoring isn't practical

### Choose Interactive Debugging Session When:
- Exploring unknown or complex application behaviors
- Collaborative debugging with team members
- Learning how application responds to different conditions
- Investigating intermittent issues that require real-time observation
- Need to experiment with different debugging approaches

## Real-world Examples from Codebase Context

### Enhanced Debugging for Current Tests

**DynamicLoadingTest.java** with debugging:
```java
@Test
public void hiddenElementTestWithDebugging() {
    QuickDebuggingUtilities debug = new QuickDebuggingUtilities(driver);
    
    debug.capturePageSnapshot("Initial state");
    
    driver.findElement(EXAMPLE_1_LINK).click();
    debug.capturePageSnapshot("After clicking Example 1");
    
    driver.findElement(START_BUTTON).click();
    debug.capturePageSnapshot("After clicking Start");
    
    // Monitor the loading process
    debug.monitorNetworkActivity(3);
    
    WebElement message = driver.findElement(FINISH_MESSAGE);
    debug.waitWithLogging(
        ExpectedConditions.visibilityOf(message), 
        10, 
        "Finish message to become visible"
    );
    
    debug.checkForJavaScriptErrors();
    debug.debugElementState(FINISH_MESSAGE);
    
    String messageText = message.getText();
    Assert.assertTrue(message.isDisplayed());
    Assert.assertEquals(messageText, "Hello World!");
}
```

**For investigating flaky test failures:**
```java
@Test
public void investigateFlakiness() {
    DynamicLoadingDiagnostics diagnostics = new DynamicLoadingDiagnostics(driver);
    
    // Run diagnostic analysis on the problematic operation
    diagnostics.diagnoseDynamicLoadingIssue(
        () -> {
            driver.findElement(EXAMPLE_2_LINK).click();
            driver.findElement(START_BUTTON).click();
        },
        FINISH_MESSAGE
    );
    
    // The diagnostic framework will generate a comprehensive report
    // helping identify why the test sometimes fails
}
```

## Common Debugging Patterns

### 1. The Before/After Comparison
```java
public void debugWithBeforeAfterComparison() {
    // Capture state before action
    String beforeSource = driver.getPageSource();
    List<WebElement> beforeElements = driver.findElements(By.xpath("//*"));
    
    // Perform action
    driver.findElement(By.id("trigger")).click();
    
    // Capture state after action
    String afterSource = driver.getPageSource();
    List<WebElement> afterElements = driver.findElements(By.xpath("//*"));
    
    // Compare states
    System.out.println("Page source length changed: " + 
        (beforeSource.length() != afterSource.length()));
    System.out.println("Element count changed: " + 
        (beforeElements.size() != afterElements.size()));
}
```

### 2. The Progressive Wait Pattern
```java
public void debugWithProgressiveWaits(By locator) {
    int[] waitTimes = {1, 2, 5, 10, 15};
    
    for (int waitTime : waitTimes) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(waitTime));
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            System.out.println("Element appeared after " + waitTime + " seconds");
            return;
        } catch (TimeoutException e) {
            System.out.println("Element not visible after " + waitTime + " seconds");
            // Capture diagnostic info at each timeout
            captureElementDiagnostics(locator);
        }
    }
}

private void captureElementDiagnostics(By locator) {
    List<WebElement> elements = driver.findElements(locator);
    if (elements.isEmpty()) {
        System.out.println("  Element not found in DOM");
    } else {
        WebElement element = elements.get(0);
        System.out.printf("  Element found but visible: %s, enabled: %s%n", 
            element.isDisplayed(), element.isEnabled());
    }
}
```

### 3. The Multi-Condition Debug Pattern
```java
public void debugMultipleConditions() {
    Map<String, ExpectedCondition<Boolean>> conditions = new HashMap<>();
    conditions.put("Element Visible", ExpectedConditions.visibilityOfElementLocated(By.id("target")));
    conditions.put("No Loading", ExpectedConditions.invisibilityOfElementLocated(By.className("loading")));
    conditions.put("No Errors", ExpectedConditions.not(ExpectedConditions.presenceOfElementLocated(By.className("error"))));
    
    for (Map.Entry<String, ExpectedCondition<Boolean>> entry : conditions.entrySet()) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(entry.getValue());
            System.out.println("✓ " + entry.getKey());
        } catch (TimeoutException e) {
            System.out.println("✗ " + entry.getKey());
        }
    }
}
```

## Further Reading

- [Selenium Debugging Best Practices](https://selenium.dev/documentation/test_practices/encouraged/debugging/)
- [Browser Developer Tools for Automation](https://developer.chrome.com/docs/devtools/)
- [JavaScript Error Monitoring](https://developer.mozilla.org/en-US/docs/Web/API/GlobalEventHandlers/onerror)
- [Web Performance Debugging](https://web.dev/debug-performance-in-the-field/)

## Key Takeaways

- **Systematic approaches provide comprehensive diagnosis but with performance overhead**
- **Lightweight utilities offer quick investigation with minimal impact**
- **Interactive sessions enable real-time exploration and collaborative debugging**
- **Choose debugging strategy based on issue complexity and time constraints**
- **Capture context before, during, and after problematic operations**
- **Combine multiple debugging techniques for complex issues**
- **Document debugging findings to improve team knowledge and prevent recurrence**
- **Regular debugging practice improves overall automation reliability**