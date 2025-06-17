# Performance Monitoring Integration: Production-Aware Testing

## Problem Statement

Traditional automation testing focuses on functional correctness but often ignores performance characteristics that directly impact user experience. Dropdown and dynamic loading operations can be performance bottlenecks, causing user frustration and business impact. Integrating performance monitoring into automation testing enables early detection of performance regressions, realistic user experience validation, and data-driven optimization decisions.

## Why It Matters

Performance monitoring integration affects:
- **User Experience Quality**: Real-world performance impacts directly affect user satisfaction
- **Business Metrics**: Page load times and interaction responsiveness correlate with conversion rates
- **Production Readiness**: Early detection of performance issues before they reach users
- **Optimization Guidance**: Data-driven decisions about where to focus performance improvements
- **Regression Prevention**: Continuous monitoring prevents performance degradation over time

## Understanding Performance Monitoring in Automation Context

### Key Performance Metrics for Dropdowns and Dynamic Loading
1. **Time to Interactive (TTI)**: When dropdown becomes responsive to user interaction
2. **First Contentful Paint (FCP)**: When first dropdown option becomes visible
3. **Cumulative Layout Shift (CLS)**: Visual stability during loading
4. **Largest Contentful Paint (LCP)**: When main content finishes loading
5. **Response Time**: Server-side API response times for dynamic content

### Performance Monitoring Categories
- **Client-side Metrics**: Browser performance measurements
- **Network Metrics**: Request/response timing and throughput
- **Server-side Metrics**: Backend processing times and resource usage
- **User Experience Metrics**: Real user interaction timing
- **Comparative Metrics**: Performance across different conditions

## Multiple Solutions

### Solution 1: Browser Performance API Integration

**When to Use**: Real browser performance metrics, client-side optimization, user experience validation

```java
public class BrowserPerformanceMonitor {
    private WebDriver driver;
    private JavascriptExecutor js;
    private List<PerformanceMetric> metrics;
    
    public static class PerformanceMetric {
        private final String name;
        private final String type;
        private final double value;
        private final long timestamp;
        private final Map<String, Object> context;
        
        public PerformanceMetric(String name, String type, double value, Map<String, Object> context) {
            this.name = name;
            this.type = type;
            this.value = value;
            this.timestamp = System.currentTimeMillis();
            this.context = context != null ? new HashMap<>(context) : new HashMap<>();
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s (%s): %.2fms %s", 
                new Date(timestamp), name, type, value, 
                context.isEmpty() ? "" : context.toString());
        }
        
        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public double getValue() { return value; }
        public long getTimestamp() { return timestamp; }
        public Map<String, Object> getContext() { return context; }
    }
    
    public BrowserPerformanceMonitor(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
        this.metrics = new ArrayList<>();
    }
    
    public void measureDropdownPerformance(By dropdownLocator, String optionText) {
        System.out.println("Starting dropdown performance measurement...");
        
        // Capture baseline performance
        captureBaselineMetrics();
        
        // Measure dropdown opening performance
        long startTime = System.currentTimeMillis();
        measureDropdownOpeningPerformance(dropdownLocator);
        
        // Perform selection with timing
        measureSelectionPerformance(dropdownLocator, optionText);
        
        // Capture post-interaction metrics
        capturePostInteractionMetrics();
        
        // Calculate and report performance analysis
        analyzePerformanceData(System.currentTimeMillis() - startTime);
    }
    
    private void captureBaselineMetrics() {
        try {
            // Navigation Timing API
            Map<String, Long> navigationTiming = getNavigationTiming();
            for (Map.Entry<String, Long> entry : navigationTiming.entrySet()) {
                addMetric("navigation_timing", entry.getKey(), entry.getValue().doubleValue(), null);
            }
            
            // Performance Observer entries
            List<Map<String, Object>> performanceEntries = getPerformanceEntries();
            for (Map<String, Object> entry : performanceEntries) {
                String name = (String) entry.get("name");
                String entryType = (String) entry.get("entryType");
                Double duration = ((Number) entry.get("duration")).doubleValue();
                
                Map<String, Object> context = new HashMap<>();
                context.put("entryType", entryType);
                context.put("startTime", entry.get("startTime"));
                
                addMetric("performance_entry", name, duration, context);
            }
            
        } catch (Exception e) {
            System.out.printf("Error capturing baseline metrics: %s%n", e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Long> getNavigationTiming() {
        String script = """
            var timing = window.performance.timing;
            return {
                'domainLookupStart': timing.domainLookupStart,
                'domainLookupEnd': timing.domainLookupEnd,
                'connectStart': timing.connectStart,
                'connectEnd': timing.connectEnd,
                'requestStart': timing.requestStart,
                'responseStart': timing.responseStart,
                'responseEnd': timing.responseEnd,
                'domLoading': timing.domLoading,
                'domInteractive': timing.domInteractive,
                'domContentLoadedEventStart': timing.domContentLoadedEventStart,
                'domContentLoadedEventEnd': timing.domContentLoadedEventEnd,
                'loadEventStart': timing.loadEventStart,
                'loadEventEnd': timing.loadEventEnd
            };
        """;
        
        return (Map<String, Long>) js.executeScript(script);
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getPerformanceEntries() {
        String script = """
            return window.performance.getEntries().map(function(entry) {
                return {
                    name: entry.name,
                    entryType: entry.entryType,
                    startTime: entry.startTime,
                    duration: entry.duration
                };
            });
        """;
        
        return (List<Map<String, Object>>) js.executeScript(script);
    }
    
    private void measureDropdownOpeningPerformance(By dropdownLocator) {
        try {
            System.out.println("  Measuring dropdown opening performance...");
            
            // Mark the start of dropdown interaction
            js.executeScript("window.performance.mark('dropdown-interaction-start');");
            
            WebElement dropdown = driver.findElement(dropdownLocator);
            
            // Measure time to become clickable
            long clickableStart = System.currentTimeMillis();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.elementToBeClickable(dropdownLocator));
            long clickableTime = System.currentTimeMillis() - clickableStart;
            
            addMetric("dropdown_timing", "time_to_clickable", clickableTime, 
                Map.of("locator", dropdownLocator.toString()));
            
            // Click and measure expansion
            long expansionStart = System.currentTimeMillis();
            dropdown.click();
            
            // Wait for dropdown to expand (different approaches based on dropdown type)
            if ("select".equals(dropdown.getTagName().toLowerCase())) {
                // Standard HTML select - measure option availability
                wait.until(driver -> {
                    try {
                        Select select = new Select(dropdown);
                        return select.getOptions().size() > 1;
                    } catch (Exception e) {
                        return false;
                    }
                });
            } else {
                // Custom dropdown - look for expanded state
                wait.until(driver -> {
                    try {
                        String expanded = dropdown.getAttribute("aria-expanded");
                        return "true".equals(expanded);
                    } catch (Exception e) {
                        return false;
                    }
                });
            }
            
            long expansionTime = System.currentTimeMillis() - expansionStart;
            addMetric("dropdown_timing", "expansion_time", expansionTime,
                Map.of("locator", dropdownLocator.toString(), "type", dropdown.getTagName()));
            
            // Mark the end of dropdown opening
            js.executeScript("window.performance.mark('dropdown-opened');");
            
        } catch (Exception e) {
            System.out.printf("  Error measuring dropdown opening: %s%n", e.getMessage());
        }
    }
    
    private void measureSelectionPerformance(By dropdownLocator, String optionText) {
        try {
            System.out.println("  Measuring selection performance...");
            
            // Mark selection start
            js.executeScript("window.performance.mark('dropdown-selection-start');");
            
            WebElement dropdown = driver.findElement(dropdownLocator);
            long selectionStart = System.currentTimeMillis();
            
            if ("select".equals(dropdown.getTagName().toLowerCase())) {
                Select select = new Select(dropdown);
                select.selectByVisibleText(optionText);
            } else {
                // Custom dropdown selection
                WebElement option = dropdown.findElement(By.xpath(".//option[text()='" + optionText + "']"));
                option.click();
            }
            
            long selectionTime = System.currentTimeMillis() - selectionStart;
            addMetric("dropdown_timing", "selection_time", selectionTime,
                Map.of("locator", dropdownLocator.toString(), "optionText", optionText));
            
            // Mark selection complete
            js.executeScript("window.performance.mark('dropdown-selection-complete');");
            
            // Measure time for UI to reflect selection
            long uiUpdateStart = System.currentTimeMillis();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            
            wait.until(driver -> {
                try {
                    if ("select".equals(dropdown.getTagName().toLowerCase())) {
                        Select select = new Select(dropdown);
                        return optionText.equals(select.getFirstSelectedOption().getText());
                    } else {
                        return dropdown.getText().contains(optionText);
                    }
                } catch (Exception e) {
                    return false;
                }
            });
            
            long uiUpdateTime = System.currentTimeMillis() - uiUpdateStart;
            addMetric("dropdown_timing", "ui_update_time", uiUpdateTime,
                Map.of("locator", dropdownLocator.toString(), "optionText", optionText));
            
        } catch (Exception e) {
            System.out.printf("  Error measuring selection performance: %s%n", e.getMessage());
        }
    }
    
    private void capturePostInteractionMetrics() {
        try {
            // Capture any new performance entries
            String script = """
                var entries = window.performance.getEntriesByName('dropdown-interaction-start');
                if (entries.length > 0) {
                    var startTime = entries[0].startTime;
                    var endTime = window.performance.now();
                    return {
                        totalInteractionTime: endTime - startTime,
                        paintEntries: window.performance.getEntriesByType('paint'),
                        measureEntries: window.performance.getEntriesByType('measure')
                    };
                }
                return null;
            """;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> postMetrics = (Map<String, Object>) js.executeScript(script);
            
            if (postMetrics != null) {
                Double totalTime = ((Number) postMetrics.get("totalInteractionTime")).doubleValue();
                addMetric("interaction_summary", "total_interaction_time", totalTime, null);
            }
            
        } catch (Exception e) {
            System.out.printf("Error capturing post-interaction metrics: %s%n", e.getMessage());
        }
    }
    
    private void analyzePerformanceData(long totalTestTime) {
        System.out.println("\n=== Performance Analysis ===");
        
        // Group metrics by type
        Map<String, List<PerformanceMetric>> metricsByType = metrics.stream()
            .collect(Collectors.groupingBy(PerformanceMetric::getType));
        
        for (Map.Entry<String, List<PerformanceMetric>> entry : metricsByType.entrySet()) {
            String type = entry.getKey();
            List<PerformanceMetric> typeMetrics = entry.getValue();
            
            System.out.printf("\n%s Metrics (%d entries):%n", type.toUpperCase(), typeMetrics.size());
            
            // Calculate statistics
            DoubleSummaryStatistics stats = typeMetrics.stream()
                .mapToDouble(PerformanceMetric::getValue)
                .summaryStatistics();
            
            System.out.printf("  Average: %.2fms%n", stats.getAverage());
            System.out.printf("  Min: %.2fms%n", stats.getMin());
            System.out.printf("  Max: %.2fms%n", stats.getMax());
            System.out.printf("  Count: %d%n", stats.getCount());
            
            // Show individual metrics if few enough
            if (typeMetrics.size() <= 10) {
                typeMetrics.forEach(metric -> 
                    System.out.printf("    %s: %.2fms%n", metric.getName(), metric.getValue()));
            }
        }
        
        // Performance thresholds and warnings
        checkPerformanceThresholds();
        
        System.out.printf("\nTotal test execution time: %dms%n", totalTestTime);
    }
    
    private void checkPerformanceThresholds() {
        System.out.println("\n=== Performance Threshold Analysis ===");
        
        // Define performance thresholds (milliseconds)
        Map<String, Double> thresholds = Map.of(
            "time_to_clickable", 100.0,
            "expansion_time", 200.0,
            "selection_time", 50.0,
            "ui_update_time", 100.0,
            "total_interaction_time", 500.0
        );
        
        for (PerformanceMetric metric : metrics) {
            String metricName = metric.getName();
            double value = metric.getValue();
            
            if (thresholds.containsKey(metricName)) {
                double threshold = thresholds.get(metricName);
                if (value > threshold) {
                    System.out.printf("⚠️  WARNING: %s (%.2fms) exceeds threshold (%.2fms)%n", 
                        metricName, value, threshold);
                } else {
                    System.out.printf("✅ %s (%.2fms) within threshold (%.2fms)%n", 
                        metricName, value, threshold);
                }
            }
        }
    }
    
    private void addMetric(String type, String name, double value, Map<String, Object> context) {
        metrics.add(new PerformanceMetric(name, type, value, context));
    }
    
    public List<PerformanceMetric> getMetrics() {
        return new ArrayList<>(metrics);
    }
    
    public void exportMetrics(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("timestamp,type,name,value,context");
            
            for (PerformanceMetric metric : metrics) {
                writer.printf("%d,%s,%s,%.2f,\"%s\"%n",
                    metric.getTimestamp(),
                    metric.getType(),
                    metric.getName(),
                    metric.getValue(),
                    metric.getContext().toString().replace("\"", "\"\"")
                );
            }
            
            System.out.printf("Performance metrics exported to: %s%n", filename);
            
        } catch (IOException e) {
            System.out.printf("Error exporting metrics: %s%n", e.getMessage());
        }
    }
}
```

**Pros:**
- **Real browser metrics**: Uses actual browser Performance API for accurate measurements
- **Comprehensive coverage**: Captures multiple aspects of dropdown performance
- **Threshold monitoring**: Automated detection of performance regressions
- **Data export**: Enables historical analysis and trend monitoring
- **Fine-grained timing**: Detailed breakdown of interaction phases

**Cons:**
- **Browser dependency**: Requires JavaScript-enabled browsers
- **Complexity**: More complex setup and interpretation
- **Overhead**: Performance monitoring itself adds test execution time
- **Limited cross-browser**: Performance API support varies across browsers
- **Data volume**: Can generate large amounts of performance data

### Solution 2: Network Performance Monitoring

**When to Use**: API-driven dropdowns, network-dependent loading, server performance analysis

```java
public class NetworkPerformanceMonitor {
    private WebDriver driver;
    private List<NetworkMetric> networkMetrics;
    
    public static class NetworkMetric {
        private final String url;
        private final String method;
        private final long requestTime;
        private final long responseTime;
        private final int statusCode;
        private final long responseSize;
        private final String resourceType;
        private final Map<String, String> headers;
        
        public NetworkMetric(String url, String method, long requestTime, long responseTime, 
                           int statusCode, long responseSize, String resourceType, Map<String, String> headers) {
            this.url = url;
            this.method = method;
            this.requestTime = requestTime;
            this.responseTime = responseTime;
            this.statusCode = statusCode;
            this.responseSize = responseSize;
            this.resourceType = resourceType;
            this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        }
        
        public long getDuration() {
            return responseTime - requestTime;
        }
        
        @Override
        public String toString() {
            return String.format("%s %s -> %d (%dms, %d bytes)", 
                method, url, statusCode, getDuration(), responseSize);
        }
        
        // Getters
        public String getUrl() { return url; }
        public String getMethod() { return method; }
        public long getRequestTime() { return requestTime; }
        public long getResponseTime() { return responseTime; }
        public int getStatusCode() { return statusCode; }
        public long getResponseSize() { return responseSize; }
        public String getResourceType() { return resourceType; }
        public Map<String, String> getHeaders() { return headers; }
    }
    
    public NetworkPerformanceMonitor(WebDriver driver) {
        this.driver = driver;
        this.networkMetrics = new ArrayList<>();
        
        // Enable network logging if using Chrome
        if (driver instanceof ChromeDriver) {
            enableNetworkLogging();
        }
    }
    
    private void enableNetworkLogging() {
        try {
            // Enable Chrome DevTools Protocol for network monitoring
            ChromeDriver chromeDriver = (ChromeDriver) driver;
            chromeDriver.executeCdpCommand("Network.enable", new HashMap<>());
            chromeDriver.executeCdpCommand("Runtime.enable", new HashMap<>());
            
        } catch (Exception e) {
            System.out.printf("Warning: Could not enable network logging: %s%n", e.getMessage());
        }
    }
    
    public void monitorDropdownNetworkActivity(By dropdownLocator, String optionText) {
        System.out.println("Starting network performance monitoring...");
        
        // Clear existing network data
        clearNetworkHistory();
        
        // Start monitoring
        startNetworkCapture();
        
        try {
            // Perform dropdown interaction
            WebElement dropdown = driver.findElement(dropdownLocator);
            dropdown.click();
            
            // Wait for any network activity to complete
            Thread.sleep(1000);
            
            // Select option
            if ("select".equals(dropdown.getTagName().toLowerCase())) {
                Select select = new Select(dropdown);
                select.selectByVisibleText(optionText);
            } else {
                WebElement option = dropdown.findElement(By.xpath(".//option[text()='" + optionText + "']"));
                option.click();
            }
            
            // Wait for any additional network activity
            Thread.sleep(2000);
            
        } catch (Exception e) {
            System.out.printf("Error during network monitoring: %s%n", e.getMessage());
        }
        
        // Capture network metrics
        captureNetworkMetrics();
        
        // Analyze network performance
        analyzeNetworkPerformance();
    }
    
    private void clearNetworkHistory() {
        try {
            if (driver instanceof ChromeDriver) {
                ((ChromeDriver) driver).executeCdpCommand("Network.clearBrowserCache", new HashMap<>());
            }
        } catch (Exception e) {
            System.out.printf("Could not clear network history: %s%n", e.getMessage());
        }
    }
    
    private void startNetworkCapture() {
        // Implementation would depend on the specific WebDriver and capabilities
        // This is a conceptual approach - actual implementation would require
        // browser-specific network monitoring setup
        
        if (driver instanceof ChromeDriver) {
            try {
                // Set up network domain listeners
                Map<String, Object> params = new HashMap<>();
                params.put("maxTotalBufferSize", 10000000);
                params.put("maxResourceBufferSize", 5000000);
                
                ((ChromeDriver) driver).executeCdpCommand("Network.setBufferSize", params);
            } catch (Exception e) {
                System.out.printf("Could not start network capture: %s%n", e.getMessage());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void captureNetworkMetrics() {
        try {
            if (driver instanceof ChromeDriver) {
                // Get network logs using JavaScript
                String script = """
                    var entries = window.performance.getEntriesByType('resource');
                    return entries.map(function(entry) {
                        return {
                            name: entry.name,
                            startTime: entry.startTime,
                            responseEnd: entry.responseEnd,
                            duration: entry.duration,
                            transferSize: entry.transferSize,
                            decodedBodySize: entry.decodedBodySize,
                            initiatorType: entry.initiatorType
                        };
                    });
                """;
                
                List<Map<String, Object>> resourceEntries = 
                    (List<Map<String, Object>>) ((JavascriptExecutor) driver).executeScript(script);
                
                for (Map<String, Object> entry : resourceEntries) {
                    String url = (String) entry.get("name");
                    Double startTime = ((Number) entry.get("startTime")).doubleValue();
                    Double responseEnd = ((Number) entry.get("responseEnd")).doubleValue();
                    Double duration = ((Number) entry.get("duration")).doubleValue();
                    Long transferSize = entry.get("transferSize") != null ? 
                        ((Number) entry.get("transferSize")).longValue() : 0L;
                    String initiatorType = (String) entry.get("initiatorType");
                    
                    NetworkMetric metric = new NetworkMetric(
                        url, "GET", 
                        startTime.longValue(), 
                        responseEnd.longValue(),
                        200, // Assume success for resource timing entries
                        transferSize,
                        initiatorType,
                        new HashMap<>()
                    );
                    
                    networkMetrics.add(metric);
                }
            }
        } catch (Exception e) {
            System.out.printf("Error capturing network metrics: %s%n", e.getMessage());
        }
    }
    
    private void analyzeNetworkPerformance() {
        System.out.println("\n=== Network Performance Analysis ===");
        
        if (networkMetrics.isEmpty()) {
            System.out.println("No network activity detected during dropdown interaction.");
            return;
        }
        
        // Group by resource type
        Map<String, List<NetworkMetric>> metricsByType = networkMetrics.stream()
            .collect(Collectors.groupingBy(NetworkMetric::getResourceType));
        
        for (Map.Entry<String, List<NetworkMetric>> entry : metricsByType.entrySet()) {
            String resourceType = entry.getKey();
            List<NetworkMetric> typeMetrics = entry.getValue();
            
            System.out.printf("\n%s Resources (%d requests):%n", 
                resourceType.toUpperCase(), typeMetrics.size());
            
            // Calculate statistics
            DoubleSummaryStatistics durationStats = typeMetrics.stream()
                .mapToDouble(NetworkMetric::getDuration)
                .summaryStatistics();
            
            long totalSize = typeMetrics.stream()
                .mapToLong(NetworkMetric::getResponseSize)
                .sum();
            
            System.out.printf("  Average Duration: %.2fms%n", durationStats.getAverage());
            System.out.printf("  Total Size: %d bytes%n", totalSize);
            System.out.printf("  Slowest Request: %.2fms%n", durationStats.getMax());
            System.out.printf("  Fastest Request: %.2fms%n", durationStats.getMin());
            
            // Show slow requests
            typeMetrics.stream()
                .filter(metric -> metric.getDuration() > 1000) // Slower than 1 second
                .forEach(metric -> System.out.printf("  ⚠️  Slow request: %s%n", metric));
        }
        
        // Overall network performance summary
        analyzeOverallNetworkPerformance();
        
        // Check for specific issues
        checkNetworkIssues();
    }
    
    private void analyzeOverallNetworkPerformance() {
        System.out.println("\n=== Overall Network Summary ===");
        
        long totalRequests = networkMetrics.size();
        double totalDuration = networkMetrics.stream()
            .mapToDouble(NetworkMetric::getDuration)
            .sum();
        long totalSize = networkMetrics.stream()
            .mapToLong(NetworkMetric::getResponseSize)
            .sum();
        
        System.out.printf("Total Requests: %d%n", totalRequests);
        System.out.printf("Total Duration: %.2fms%n", totalDuration);
        System.out.printf("Total Size: %d bytes (%.2f KB)%n", totalSize, totalSize / 1024.0);
        
        if (totalRequests > 0) {
            System.out.printf("Average Request Duration: %.2fms%n", totalDuration / totalRequests);
        }
        
        // Parallel vs sequential analysis
        analyzeRequestConcurrency();
    }
    
    private void analyzeRequestConcurrency() {
        if (networkMetrics.size() < 2) return;
        
        // Sort by start time
        List<NetworkMetric> sortedMetrics = networkMetrics.stream()
            .sorted(Comparator.comparing(NetworkMetric::getRequestTime))
            .collect(Collectors.toList());
        
        int concurrentRequests = 0;
        for (int i = 0; i < sortedMetrics.size() - 1; i++) {
            NetworkMetric current = sortedMetrics.get(i);
            NetworkMetric next = sortedMetrics.get(i + 1);
            
            // Check if requests overlap
            if (next.getRequestTime() < current.getResponseTime()) {
                concurrentRequests++;
            }
        }
        
        System.out.printf("Concurrent Requests: %d out of %d%n", 
            concurrentRequests, sortedMetrics.size());
    }
    
    private void checkNetworkIssues() {
        System.out.println("\n=== Network Issue Detection ===");
        
        // Check for failed requests
        long failedRequests = networkMetrics.stream()
            .filter(metric -> metric.getStatusCode() >= 400)
            .count();
        
        if (failedRequests > 0) {
            System.out.printf("❌ %d failed requests detected%n", failedRequests);
        }
        
        // Check for slow requests
        long slowRequests = networkMetrics.stream()
            .filter(metric -> metric.getDuration() > 2000)
            .count();
        
        if (slowRequests > 0) {
            System.out.printf("🐌 %d slow requests (>2s) detected%n", slowRequests);
        }
        
        // Check for large responses
        long largeResponses = networkMetrics.stream()
            .filter(metric -> metric.getResponseSize() > 100000) // >100KB
            .count();
        
        if (largeResponses > 0) {
            System.out.printf("📊 %d large responses (>100KB) detected%n", largeResponses);
        }
        
        // Check for unnecessary requests
        checkForUnnecessaryRequests();
    }
    
    private void checkForUnnecessaryRequests() {
        // Look for duplicate requests to the same URL
        Map<String, Long> urlCounts = networkMetrics.stream()
            .collect(Collectors.groupingBy(NetworkMetric::getUrl, Collectors.counting()));
        
        long duplicateRequests = urlCounts.values().stream()
            .filter(count -> count > 1)
            .mapToLong(Long::longValue)
            .sum();
        
        if (duplicateRequests > 0) {
            System.out.printf("🔄 %d duplicate requests detected%n", duplicateRequests - urlCounts.size());
        }
    }
    
    public List<NetworkMetric> getNetworkMetrics() {
        return new ArrayList<>(networkMetrics);
    }
    
    public void exportNetworkMetrics(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("url,method,requestTime,responseTime,duration,statusCode,responseSize,resourceType");
            
            for (NetworkMetric metric : networkMetrics) {
                writer.printf("\"%s\",%s,%d,%d,%d,%d,%d,%s%n",
                    metric.getUrl().replace("\"", "\"\""),
                    metric.getMethod(),
                    metric.getRequestTime(),
                    metric.getResponseTime(),
                    metric.getDuration(),
                    metric.getStatusCode(),
                    metric.getResponseSize(),
                    metric.getResourceType()
                );
            }
            
            System.out.printf("Network metrics exported to: %s%n", filename);
            
        } catch (IOException e) {
            System.out.printf("Error exporting network metrics: %s%n", e.getMessage());
        }
    }
}
```

**Pros:**
- **Network visibility**: Detailed insight into all network requests
- **Resource analysis**: Identifies large, slow, or unnecessary requests
- **Concurrency analysis**: Understanding of parallel vs sequential loading
- **Issue detection**: Automatic identification of network performance problems
- **Real-world conditions**: Captures actual network behavior

**Cons:**
- **Browser limitations**: Requires specific browser capabilities for full monitoring
- **Implementation complexity**: Network monitoring setup is complex
- **Privacy concerns**: Captures detailed network activity
- **Performance overhead**: Monitoring itself can impact performance
- **Data interpretation**: Requires expertise to analyze network metrics effectively

### Solution 3: Real User Monitoring (RUM) Integration

**When to Use**: Production environments, user experience validation, long-term performance trends

```java
public class RealUserMonitoringIntegration {
    private WebDriver driver;
    private String rumEndpoint;
    private String applicationId;
    private Map<String, Object> sessionContext;
    
    public RealUserMonitoringIntegration(WebDriver driver, String rumEndpoint, String applicationId) {
        this.driver = driver;
        this.rumEndpoint = rumEndpoint;
        this.applicationId = applicationId;
        this.sessionContext = initializeSessionContext();
    }
    
    private Map<String, Object> initializeSessionContext() {
        Map<String, Object> context = new HashMap<>();
        
        try {
            Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();
            context.put("browserName", caps.getBrowserName());
            context.put("browserVersion", caps.getBrowserVersion());
            context.put("platformName", caps.getPlatformName());
            
            // Add viewport information
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Long viewportWidth = (Long) js.executeScript("return window.innerWidth;");
            Long viewportHeight = (Long) js.executeScript("return window.innerHeight;");
            context.put("viewportWidth", viewportWidth);
            context.put("viewportHeight", viewportHeight);
            
            // Add connection information if available
            @SuppressWarnings("unchecked")
            Map<String, Object> connection = (Map<String, Object>) js.executeScript(
                "return navigator.connection ? {" +
                "  effectiveType: navigator.connection.effectiveType," +
                "  downlink: navigator.connection.downlink," +
                "  rtt: navigator.connection.rtt" +
                "} : null;"
            );
            
            if (connection != null) {
                context.putAll(connection);
            }
            
        } catch (Exception e) {
            System.out.printf("Warning: Could not initialize full session context: %s%n", e.getMessage());
        }
        
        context.put("sessionId", UUID.randomUUID().toString());
        context.put("timestamp", System.currentTimeMillis());
        
        return context;
    }
    
    public void instrumentDropdownForRUM(By dropdownLocator, String optionText) {
        System.out.println("Instrumenting dropdown for Real User Monitoring...");
        
        try {
            // Inject RUM tracking code
            injectRUMTracking();
            
            // Start user interaction tracking
            startInteractionTracking("dropdown_selection", Map.of(
                "locator", dropdownLocator.toString(),
                "optionText", optionText
            ));
            
            // Perform dropdown interaction with timing
            performInstrumentedDropdownInteraction(dropdownLocator, optionText);
            
            // End interaction tracking
            endInteractionTracking("dropdown_selection");
            
            // Send metrics to RUM endpoint
            sendRUMMetrics();
            
        } catch (Exception e) {
            System.out.printf("Error during RUM instrumentation: %s%n", e.getMessage());
        }
    }
    
    private void injectRUMTracking() {
        String rumScript = """
            window.rumData = window.rumData || {
                interactions: [],
                metrics: [],
                errors: []
            };
            
            window.rumTracker = {
                startInteraction: function(type, details) {
                    window.rumData.interactions.push({
                        type: type,
                        details: details,
                        startTime: performance.now(),
                        id: Date.now() + Math.random()
                    });
                },
                
                endInteraction: function(type) {
                    var interaction = window.rumData.interactions
                        .filter(function(i) { return i.type === type && !i.endTime; })
                        .pop();
                    
                    if (interaction) {
                        interaction.endTime = performance.now();
                        interaction.duration = interaction.endTime - interaction.startTime;
                    }
                },
                
                addMetric: function(name, value, context) {
                    window.rumData.metrics.push({
                        name: name,
                        value: value,
                        context: context,
                        timestamp: performance.now()
                    });
                },
                
                captureError: function(error, context) {
                    window.rumData.errors.push({
                        message: error.message,
                        stack: error.stack,
                        context: context,
                        timestamp: performance.now()
                    });
                }
            };
            
            // Capture Core Web Vitals
            window.rumTracker.captureCoreWebVitals = function() {
                // Largest Contentful Paint
                new PerformanceObserver(function(list) {
                    var entries = list.getEntries();
                    var lastEntry = entries[entries.length - 1];
                    window.rumTracker.addMetric('LCP', lastEntry.startTime, {
                        element: lastEntry.element ? lastEntry.element.tagName : null
                    });
                }).observe({entryTypes: ['largest-contentful-paint']});
                
                // First Input Delay
                new PerformanceObserver(function(list) {
                    var entries = list.getEntries();
                    entries.forEach(function(entry) {
                        window.rumTracker.addMetric('FID', entry.processingStart - entry.startTime, {
                            eventType: entry.name
                        });
                    });
                }).observe({entryTypes: ['first-input']});
                
                // Cumulative Layout Shift
                var clsValue = 0;
                new PerformanceObserver(function(list) {
                    var entries = list.getEntries();
                    entries.forEach(function(entry) {
                        if (!entry.hadRecentInput) {
                            clsValue += entry.value;
                            window.rumTracker.addMetric('CLS', clsValue, {
                                sources: entry.sources ? entry.sources.length : 0
                            });
                        }
                    });
                }).observe({entryTypes: ['layout-shift']});
            };
            
            // Start capturing Core Web Vitals
            window.rumTracker.captureCoreWebVitals();
            
            console.log('RUM tracking initialized');
        """;
        
        ((JavascriptExecutor) driver).executeScript(rumScript);
    }
    
    private void startInteractionTracking(String interactionType, Map<String, Object> details) {
        String script = String.format(
            "window.rumTracker.startInteraction('%s', %s);",
            interactionType,
            mapToJsonString(details)
        );
        
        ((JavascriptExecutor) driver).executeScript(script);
    }
    
    private void endInteractionTracking(String interactionType) {
        String script = String.format("window.rumTracker.endInteraction('%s');", interactionType);
        ((JavascriptExecutor) driver).executeScript(script);
    }
    
    private void performInstrumentedDropdownInteraction(By dropdownLocator, String optionText) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        try {
            // Add custom timing marks
            js.executeScript("window.rumTracker.addMetric('dropdown_locate_start', performance.now(), {});");
            
            WebElement dropdown = driver.findElement(dropdownLocator);
            
            js.executeScript("window.rumTracker.addMetric('dropdown_located', performance.now(), {});");
            
            // Measure visibility timing
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOf(dropdown));
            
            js.executeScript("window.rumTracker.addMetric('dropdown_visible', performance.now(), {});");
            
            // Measure clickability timing
            wait.until(ExpectedConditions.elementToBeClickable(dropdown));
            
            js.executeScript("window.rumTracker.addMetric('dropdown_clickable', performance.now(), {});");
            
            // Perform click with timing
            dropdown.click();
            
            js.executeScript("window.rumTracker.addMetric('dropdown_clicked', performance.now(), {});");
            
            // Handle selection based on dropdown type
            if ("select".equals(dropdown.getTagName().toLowerCase())) {
                Select select = new Select(dropdown);
                select.selectByVisibleText(optionText);
            } else {
                // Custom dropdown - wait for options to be available
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath(".//option[text()='" + optionText + "']")));
                
                WebElement option = dropdown.findElement(By.xpath(".//option[text()='" + optionText + "']"));
                option.click();
            }
            
            js.executeScript("window.rumTracker.addMetric('option_selected', performance.now(), " +
                "{optionText: '" + optionText + "'});");
            
            // Wait for UI to update
            Thread.sleep(500);
            
            js.executeScript("window.rumTracker.addMetric('ui_update_complete', performance.now(), {});");
            
        } catch (Exception e) {
            // Capture error in RUM data
            String errorScript = String.format(
                "window.rumTracker.captureError({message: '%s', stack: ''}, {phase: 'dropdown_interaction'});",
                e.getMessage().replace("'", "\\'")
            );
            js.executeScript(errorScript);
            throw e;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void sendRUMMetrics() {
        try {
            // Retrieve RUM data from browser
            Map<String, Object> rumData = (Map<String, Object>) 
                ((JavascriptExecutor) driver).executeScript("return window.rumData;");
            
            if (rumData == null) {
                System.out.println("No RUM data available to send");
                return;
            }
            
            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("applicationId", applicationId);
            payload.put("sessionContext", sessionContext);
            payload.put("rumData", rumData);
            payload.put("timestamp", System.currentTimeMillis());
            
            // Send to RUM endpoint (simulated - would use HTTP client in real implementation)
            sendRUMPayload(payload);
            
            // Log summary
            logRUMSummary(rumData);
            
        } catch (Exception e) {
            System.out.printf("Error sending RUM metrics: %s%n", e.getMessage());
        }
    }
    
    private void sendRUMPayload(Map<String, Object> payload) {
        // In a real implementation, this would send HTTP POST to RUM endpoint
        // For demonstration, we'll just log the payload
        
        System.out.println("\n=== RUM Payload (would be sent to " + rumEndpoint + ") ===");
        System.out.println("Application ID: " + applicationId);
        System.out.println("Session Context: " + sessionContext);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> rumData = (Map<String, Object>) payload.get("rumData");
        
        if (rumData != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> interactions = (List<Map<String, Object>>) rumData.get("interactions");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> metrics = (List<Map<String, Object>>) rumData.get("metrics");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> errors = (List<Map<String, Object>>) rumData.get("errors");
            
            System.out.printf("Interactions: %d%n", interactions != null ? interactions.size() : 0);
            System.out.printf("Metrics: %d%n", metrics != null ? metrics.size() : 0);
            System.out.printf("Errors: %d%n", errors != null ? errors.size() : 0);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void logRUMSummary(Map<String, Object> rumData) {
        System.out.println("\n=== RUM Session Summary ===");
        
        List<Map<String, Object>> interactions = (List<Map<String, Object>>) rumData.get("interactions");
        if (interactions != null && !interactions.isEmpty()) {
            System.out.println("\nInteractions:");
            for (Map<String, Object> interaction : interactions) {
                String type = (String) interaction.get("type");
                Double duration = (Double) interaction.get("duration");
                System.out.printf("  %s: %.2fms%n", type, duration != null ? duration : 0.0);
            }
        }
        
        List<Map<String, Object>> metrics = (List<Map<String, Object>>) rumData.get("metrics");
        if (metrics != null && !metrics.isEmpty()) {
            System.out.println("\nKey Metrics:");
            
            // Group metrics by name
            Map<String, List<Map<String, Object>>> metricsByName = metrics.stream()
                .collect(Collectors.groupingBy(m -> (String) m.get("name")));
            
            for (Map.Entry<String, List<Map<String, Object>>> entry : metricsByName.entrySet()) {
                String metricName = entry.getKey();
                List<Map<String, Object>> metricList = entry.getValue();
                
                if (metricList.size() == 1) {
                    Double value = (Double) metricList.get(0).get("value");
                    System.out.printf("  %s: %.2fms%n", metricName, value);
                } else {
                    System.out.printf("  %s: %d occurrences%n", metricName, metricList.size());
                }
            }
        }
        
        List<Map<String, Object>> errors = (List<Map<String, Object>>) rumData.get("errors");
        if (errors != null && !errors.isEmpty()) {
            System.out.println("\nErrors:");
            for (Map<String, Object> error : errors) {
                String message = (String) error.get("message");
                System.out.printf("  Error: %s%n", message);
            }
        }
    }
    
    private String mapToJsonString(Map<String, Object> map) {
        // Simple JSON serialization for demonstration
        // In production, use a proper JSON library
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            } else {
                json.append(value.toString());
            }
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
}
```

**Pros:**
- **Real user perspective**: Captures actual user experience metrics
- **Production insights**: Provides data from real user environments
- **Core Web Vitals**: Measures Google's user experience metrics
- **Comprehensive tracking**: Covers interactions, performance, and errors
- **Historical data**: Enables trend analysis and regression detection

**Cons:**
- **Infrastructure requirements**: Needs RUM service and endpoint setup
- **Privacy considerations**: Collects detailed user interaction data
- **Data volume**: Can generate significant amounts of telemetry data
- **Analysis complexity**: Requires expertise to interpret RUM data effectively
- **Cost implications**: RUM services typically have usage-based pricing

## Decision Framework

### Choose Browser Performance API When:
- Need detailed client-side performance metrics
- Optimizing front-end dropdown and loading performance
- Building performance regression test suites
- Team has JavaScript and browser performance expertise
- Testing specific browser performance characteristics

### Choose Network Performance Monitoring When:
- Dropdowns depend on API calls or dynamic data loading
- Need to optimize server-side response times
- Analyzing network-related performance bottlenecks
- Testing under different network conditions
- Building comprehensive performance monitoring systems

### Choose Real User Monitoring When:
- Deploying to production environments
- Need real user experience validation
- Building long-term performance trend analysis
- Compliance with performance SLAs or standards
- Cross-functional teams need business-friendly metrics

## Real-world Examples from Codebase Context

### Current Implementation Enhancement

**DynamicLoadingTest.java** enhanced with performance monitoring:

```java
@Test
public void dynamicLoadingWithPerformanceMonitoring() {
    BrowserPerformanceMonitor perfMonitor = new BrowserPerformanceMonitor(driver);
    
    driver.findElement(EXAMPLE_1_LINK).click();
    
    // Monitor dropdown performance
    perfMonitor.measureDropdownPerformance(START_BUTTON, "Start");
    
    // Export metrics for analysis
    perfMonitor.exportMetrics("dynamic_loading_performance.csv");
    
    // Validate performance thresholds
    List<BrowserPerformanceMonitor.PerformanceMetric> metrics = perfMonitor.getMetrics();
    Assert.assertTrue("Loading should complete within 2 seconds", 
        metrics.stream()
            .filter(m -> "total_interaction_time".equals(m.getName()))
            .allMatch(m -> m.getValue() < 2000));
}
```

**Integration with existing test framework:**

```java
public class PerformanceAwareDropdownTest extends DropdownTest {
    private BrowserPerformanceMonitor performanceMonitor;
    private NetworkPerformanceMonitor networkMonitor;
    
    @BeforeMethod
    public void setupPerformanceMonitoring() {
        performanceMonitor = new BrowserPerformanceMonitor(driver);
        networkMonitor = new NetworkPerformanceMonitor(driver);
    }
    
    @Override
    @Test
    public void dropdownSelectionWithMonitoring() {
        // Perform regular dropdown test with performance monitoring
        performanceMonitor.measureDropdownPerformance(
            By.id("dropdown"), "Option 2");
        
        // Run original test logic
        super.dropdownSelection();
        
        // Validate performance requirements
        validatePerformanceRequirements();
    }
    
    private void validatePerformanceRequirements() {
        List<BrowserPerformanceMonitor.PerformanceMetric> metrics = 
            performanceMonitor.getMetrics();
        
        // Business requirement: Dropdown should open within 100ms
        Optional<BrowserPerformanceMonitor.PerformanceMetric> expansionMetric = 
            metrics.stream()
                .filter(m -> "expansion_time".equals(m.getName()))
                .findFirst();
        
        expansionMetric.ifPresent(metric -> 
            Assert.assertTrue("Dropdown expansion too slow", metric.getValue() < 100));
    }
    
    @AfterMethod
    public void exportPerformanceData() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        performanceMonitor.exportMetrics("perf_" + timestamp + ".csv");
        networkMonitor.exportNetworkMetrics("network_" + timestamp + ".csv");
    }
}
```

## Performance Monitoring Best Practices

### 1. Baseline Establishment
```java
public class PerformanceBaseline {
    public static void establishBaseline() {
        // Run tests multiple times to establish baseline
        List<Double> baselineMetrics = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            // Perform dropdown interaction
            double duration = measureDropdownInteraction();
            baselineMetrics.add(duration);
        }
        
        DoubleSummaryStatistics stats = baselineMetrics.stream()
            .mapToDouble(Double::doubleValue)
            .summaryStatistics();
        
        System.out.printf("Baseline established - Avg: %.2fms, StdDev: %.2fms%n",
            stats.getAverage(), calculateStandardDeviation(baselineMetrics));
    }
}
```

### 2. Performance Regression Detection
```java
public class PerformanceRegressionDetector {
    private static final double REGRESSION_THRESHOLD = 1.2; // 20% slower
    
    public boolean detectRegression(double currentMetric, double baselineMetric) {
        return currentMetric > (baselineMetric * REGRESSION_THRESHOLD);
    }
    
    public void validateAgainstBaseline(List<PerformanceMetric> currentMetrics) {
        // Compare against historical baseline
        Map<String, Double> baseline = loadBaseline();
        
        for (PerformanceMetric metric : currentMetrics) {
            String metricName = metric.getName();
            if (baseline.containsKey(metricName)) {
                double baselineValue = baseline.get(metricName);
                if (detectRegression(metric.getValue(), baselineValue)) {
                    System.out.printf("⚠️ REGRESSION: %s is %.2f%% slower than baseline%n",
                        metricName, ((metric.getValue() / baselineValue) - 1) * 100);
                }
            }
        }
    }
}
```

## Integration with CI/CD Pipelines

### Performance Gates
```java
public class PerformanceGate {
    public static boolean passesPerformanceGate(List<PerformanceMetric> metrics) {
        Map<String, Double> thresholds = Map.of(
            "dropdown_expansion", 200.0,
            "option_selection", 100.0,
            "ui_update", 150.0
        );
        
        for (PerformanceMetric metric : metrics) {
            if (thresholds.containsKey(metric.getName())) {
                if (metric.getValue() > thresholds.get(metric.getName())) {
                    System.out.printf("Performance gate FAILED: %s exceeded threshold%n", 
                        metric.getName());
                    return false;
                }
            }
        }
        
        return true;
    }
}
```

## Further Reading

- [Web Performance APIs](https://developer.mozilla.org/en-US/docs/Web/API/Performance_API)
- [Core Web Vitals](https://web.dev/vitals/)
- [Real User Monitoring Best Practices](https://www.datadoghq.com/knowledge-center/real-user-monitoring/)
- [Performance Testing in CI/CD](https://martinfowler.com/articles/practical-test-pyramid.html)

## Key Takeaways

- **Performance monitoring integration transforms functional tests into performance validation tools**
- **Browser Performance API provides detailed client-side metrics for dropdown interactions**
- **Network monitoring reveals server-side and connectivity-related performance issues**
- **Real User Monitoring bridges the gap between test environments and production reality**
- **Performance thresholds and regression detection enable automated quality gates**
- **Historical data and trend analysis guide optimization efforts**
- **Integration with CI/CD pipelines prevents performance regressions from reaching production**
- **Choose monitoring approach based on performance goals, infrastructure, and team expertise**