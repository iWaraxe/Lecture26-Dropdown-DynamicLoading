# Network Condition Impact on Dynamic Loading Reliability

## Problem Statement

Network conditions significantly affect dynamic loading behavior and test reliability. Tests that pass consistently in local development environments often fail in CI/CD pipelines, different geographic regions, or varying network conditions. Understanding how network latency, bandwidth, packet loss, and connectivity patterns impact dynamic content loading is essential for building resilient automation that works across diverse environments.

## Why It Matters

Network condition considerations affect:
- **Test Environment Reliability**: Tests fail differently across local, staging, and production environments
- **Geographic Distribution**: Application performance varies by user location and CDN coverage
- **CI/CD Consistency**: Pipeline environments often have different network characteristics than development
- **Real-world Accuracy**: Tests should reflect actual user experience under various network conditions
- **Debugging Complexity**: Network-related failures are often intermittent and hard to reproduce

## Understanding Network Impact on Dynamic Loading

### Network Variables Affecting Automation
1. **Latency**: Round-trip time for HTTP requests
2. **Bandwidth**: Data transfer speed limitations
3. **Packet Loss**: Connection reliability and retry behavior
4. **Connection Stability**: Intermittent connectivity issues
5. **CDN Performance**: Content delivery network response times
6. **DNS Resolution**: Domain name lookup delays

### Dynamic Loading Scenarios Most Affected by Network
- **AJAX Data Fetching**: API calls for dynamic content
- **Resource Loading**: CSS, JavaScript, images, fonts
- **Chunked Loading**: Progressive content delivery
- **Real-time Updates**: WebSocket connections, polling
- **Third-party Integrations**: External service dependencies

## Multiple Solutions

### Solution 1: Adaptive Timeout Strategy

**When to Use**: Variable network environments, CI/CD pipelines, geographic distribution

```java
public class AdaptiveTimeoutStrategy {
    private WebDriver driver;
    private WebDriverWait wait;
    private NetworkMetrics networkMetrics;
    
    public class NetworkMetrics {
        private long baselineLatency;
        private double reliabilityFactor;
        private long lastMeasurement;
        
        public NetworkMetrics() {
            this.baselineLatency = measureBaselineLatency();
            this.reliabilityFactor = 1.0;
            this.lastMeasurement = System.currentTimeMillis();
        }
        
        private long measureBaselineLatency() {
            // Measure baseline network performance
            long startTime = System.currentTimeMillis();
            
            try {
                // Simple network test - load a lightweight resource
                ((JavascriptExecutor) driver).executeScript(
                    "return fetch('/favicon.ico').then(() => Date.now())");
                
                long endTime = System.currentTimeMillis();
                return endTime - startTime;
            } catch (Exception e) {
                // Default to conservative baseline
                return 1000; // 1 second baseline
            }
        }
        
        public void updateReliability(boolean wasSuccessful, long actualWaitTime, long expectedTime) {
            if (wasSuccessful && actualWaitTime <= expectedTime) {
                // Network performing well
                reliabilityFactor = Math.max(0.5, reliabilityFactor * 0.95);
            } else {
                // Network slower than expected
                reliabilityFactor = Math.min(3.0, reliabilityFactor * 1.1);
            }
            lastMeasurement = System.currentTimeMillis();
        }
        
        public long getAdaptiveTimeout(long baseTimeout) {
            long networkAdjustedTimeout = (long) (baseTimeout * reliabilityFactor);
            
            // Additional adjustment based on baseline latency
            if (baselineLatency > 500) {
                networkAdjustedTimeout = (long) (networkAdjustedTimeout * 1.5);
            }
            
            return Math.min(networkAdjustedTimeout, baseTimeout * 5); // Cap at 5x base
        }
    }
    
    public void waitForDynamicContentWithAdaptiveTimeout(By locator, long baseTimeoutSeconds) {
        long adaptiveTimeoutMs = networkMetrics.getAdaptiveTimeout(baseTimeoutSeconds * 1000);
        
        System.out.printf("Using adaptive timeout: %dms (base: %ds, factor: %.2f)%n", 
            adaptiveTimeoutMs, baseTimeoutSeconds, networkMetrics.reliabilityFactor);
        
        WebDriverWait adaptiveWait = new WebDriverWait(driver, Duration.ofMillis(adaptiveTimeoutMs));
        
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            adaptiveWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            success = true;
        } catch (TimeoutException e) {
            success = false;
            throw new RuntimeException("Element not visible after adaptive timeout: " + adaptiveTimeoutMs + "ms", e);
        } finally {
            long actualWaitTime = System.currentTimeMillis() - startTime;
            networkMetrics.updateReliability(success, actualWaitTime, adaptiveTimeoutMs);
        }
    }
    
    public void waitForNetworkIdleWithAdaptiveTimeout() {
        long baseTimeout = 10000; // 10 seconds base
        long adaptiveTimeout = networkMetrics.getAdaptiveTimeout(baseTimeout);
        
        WebDriverWait adaptiveWait = new WebDriverWait(driver, Duration.ofMillis(adaptiveTimeout));
        
        adaptiveWait.until(driver -> {
            String script = """
                if (typeof window.performance === 'undefined') return true;
                
                var entries = window.performance.getEntriesByType('resource');
                var recentRequests = entries.filter(entry => {
                    return (Date.now() - entry.responseEnd) < %d;
                });
                
                return recentRequests.length === 0;
            """.formatted(Math.max(500, networkMetrics.baselineLatency));
            
            return (Boolean) ((JavascriptExecutor) driver).executeScript(script);
        });
    }
}
```

**Pros:**
- **Self-adjusting**: Learns from network performance and adapts timeouts accordingly
- **Environment aware**: Adjusts to local vs CI/CD vs production network characteristics  
- **Performance optimized**: Shorter waits when network is fast, longer when slow
- **Failure reduction**: Reduces timeout-related failures in slow network conditions
- **Data-driven**: Uses actual network measurements rather than fixed assumptions

**Cons:**
- **Implementation complexity**: Requires network measurement and adaptation logic
- **Initial calibration**: May need several runs to establish accurate baseline
- **Memory overhead**: Maintains network performance history and metrics
- **Debugging complexity**: Variable timeouts make failure reproduction harder
- **Potential over-adaptation**: May mask actual application performance issues

### Solution 2: Network Condition Simulation and Testing

**When to Use**: Testing across different network conditions, performance validation, user experience simulation

```java
public class NetworkConditionSimulator {
    private WebDriver driver;
    private ChromeDriver chromeDriver;
    
    public enum NetworkCondition {
        FAST_3G(500, 1600, 750, 0),
        SLOW_3G(2000, 500, 2000, 0),
        SLOW_2G(20000, 250, 5000, 0),
        OFFLINE(0, 0, 0, 100),
        LOSSY_NETWORK(100, 1000, 1000, 5);
        
        final long latency;
        final long downloadThroughput;
        final long uploadThroughput;
        final double packetLoss;
        
        NetworkCondition(long latency, long downloadThroughput, long uploadThroughput, double packetLoss) {
            this.latency = latency;
            this.downloadThroughput = downloadThroughput;
            this.uploadThroughput = uploadThroughput;
            this.packetLoss = packetLoss;
        }
    }
    
    public void setNetworkCondition(NetworkCondition condition) {
        if (!(driver instanceof ChromeDriver)) {
            System.out.println("Network condition simulation only supported with ChromeDriver");
            return;
        }
        
        chromeDriver = (ChromeDriver) driver;
        
        Map<String, Object> networkConditions = new HashMap<>();
        networkConditions.put("offline", condition == NetworkCondition.OFFLINE);
        networkConditions.put("latency", condition.latency);
        networkConditions.put("download_throughput", condition.downloadThroughput * 1024); // Convert to bytes
        networkConditions.put("upload_throughput", condition.uploadThroughput * 1024);
        
        chromeDriver.executeCdpCommand("Network.emulateNetworkConditions", networkConditions);
        
        System.out.printf("Set network condition: %s (latency: %dms, download: %d KB/s)%n", 
            condition.name(), condition.latency, condition.downloadThroughput);
    }
    
    public void testDynamicLoadingUnderNetworkConditions(By locator, List<NetworkCondition> conditions) {
        Map<NetworkCondition, Long> loadingTimes = new HashMap<>();
        Map<NetworkCondition, Boolean> successResults = new HashMap<>();
        
        for (NetworkCondition condition : conditions) {
            System.out.println("Testing under " + condition.name() + " conditions...");
            
            // Set network condition
            setNetworkCondition(condition);
            
            // Refresh page to start clean
            driver.navigate().refresh();
            
            long startTime = System.currentTimeMillis();
            boolean success = false;
            
            try {
                // Use condition-appropriate timeout
                long timeoutMs = calculateTimeoutForCondition(condition);
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(timeoutMs));
                
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                success = true;
                
                long endTime = System.currentTimeMillis();
                loadingTimes.put(condition, endTime - startTime);
                
            } catch (TimeoutException e) {
                loadingTimes.put(condition, -1L); // Indicate failure
                System.out.printf("Failed to load under %s conditions%n", condition.name());
            }
            
            successResults.put(condition, success);
        }
        
        // Reset to normal conditions
        setNetworkCondition(NetworkCondition.FAST_3G);
        
        // Report results
        generateNetworkTestReport(loadingTimes, successResults);
    }
    
    private long calculateTimeoutForCondition(NetworkCondition condition) {
        // Base timeout adjustment based on network condition
        long baseTimeout = 10000; // 10 seconds
        
        switch (condition) {
            case FAST_3G:
                return baseTimeout;
            case SLOW_3G:
                return baseTimeout * 2;
            case SLOW_2G:
                return baseTimeout * 5;
            case LOSSY_NETWORK:
                return baseTimeout * 3;
            case OFFLINE:
                return 5000; // Fail fast for offline
            default:
                return baseTimeout;
        }
    }
    
    private void generateNetworkTestReport(Map<NetworkCondition, Long> loadingTimes, 
                                         Map<NetworkCondition, Boolean> successResults) {
        System.out.println("\nNetwork Condition Test Report");
        System.out.println("=============================");
        
        for (NetworkCondition condition : NetworkCondition.values()) {
            Long loadTime = loadingTimes.get(condition);
            Boolean success = successResults.get(condition);
            
            if (success != null) {
                if (success && loadTime != null && loadTime > 0) {
                    System.out.printf("%-15s: SUCCESS (%,d ms)%n", condition.name(), loadTime);
                } else {
                    System.out.printf("%-15s: FAILED%n", condition.name());
                }
            }
        }
        
        // Calculate success rate
        long successCount = successResults.values().stream().mapToLong(b -> b ? 1 : 0).sum();
        double successRate = (double) successCount / successResults.size() * 100;
        
        System.out.printf("%nOverall success rate: %.1f%% (%d/%d conditions)%n", 
            successRate, successCount, successResults.size());
    }
    
    public void simulateIntermittentConnectivity(By locator, int cycles) {
        System.out.println("Testing intermittent connectivity resilience...");
        
        for (int i = 0; i < cycles; i++) {
            System.out.printf("Cycle %d/%d:%n", i + 1, cycles);
            
            // Go offline
            setNetworkCondition(NetworkCondition.OFFLINE);
            System.out.println("  Network: OFFLINE");
            
            try {
                Thread.sleep(2000); // 2 seconds offline
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Come back online
            setNetworkCondition(NetworkCondition.SLOW_3G);
            System.out.println("  Network: BACK ONLINE");
            
            // Test if application recovers
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                System.out.println("  Result: RECOVERED");
            } catch (TimeoutException e) {
                System.out.println("  Result: FAILED TO RECOVER");
            }
            
            try {
                Thread.sleep(3000); // 3 seconds online
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Reset to normal
        setNetworkCondition(NetworkCondition.FAST_3G);
    }
}
```

**Pros:**
- **Realistic testing**: Simulates actual user network conditions
- **Comprehensive coverage**: Tests across various network scenarios
- **Performance validation**: Identifies loading time bottlenecks
- **Resilience testing**: Validates application behavior under network stress
- **Debugging aid**: Reproduces network-related issues in controlled environment

**Cons:**
- **Browser dependency**: Network simulation limited to Chrome/Chromium
- **Environment limitations**: May not reflect real-world network complexity
- **Test execution time**: Network simulation significantly slows test execution
- **Setup complexity**: Requires additional configuration and teardown
- **Limited accuracy**: Simulated conditions may not match actual user experience

### Solution 3: Network-Aware Wait Strategies

**When to Use**: Production environments, real user condition testing, adaptive performance requirements

```java
public class NetworkAwareWaitStrategy {
    private WebDriver driver;
    private JavascriptExecutor js;
    
    public class NetworkPerformanceMonitor {
        public NetworkStats getCurrentNetworkStats() {
            String script = """
                if (!navigator.connection) {
                    return { effectiveType: 'unknown', downlink: -1, rtt: -1 };
                }
                
                return {
                    effectiveType: navigator.connection.effectiveType,
                    downlink: navigator.connection.downlink,
                    rtt: navigator.connection.rtt,
                    saveData: navigator.connection.saveData
                };
            """;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) js.executeScript(script);
            
            return new NetworkStats(
                (String) result.get("effectiveType"),
                ((Number) result.get("downlink")).doubleValue(),
                ((Number) result.get("rtt")).longValue(),
                (Boolean) result.getOrDefault("saveData", false)
            );
        }
        
        public long measureActualLatency() {
            String script = """
                var start = performance.now();
                return fetch('data:text/plain,test', { method: 'GET' })
                    .then(() => performance.now() - start)
                    .catch(() => -1);
            """;
            
            try {
                Object result = js.executeScript(script);
                return ((Number) result).longValue();
            } catch (Exception e) {
                return -1; // Unable to measure
            }
        }
    }
    
    public static class NetworkStats {
        final String effectiveType;
        final double downlink;
        final long rtt;
        final boolean saveData;
        
        NetworkStats(String effectiveType, double downlink, long rtt, boolean saveData) {
            this.effectiveType = effectiveType;
            this.downlink = downlink;
            this.rtt = rtt;
            this.saveData = saveData;
        }
        
        public long getRecommendedTimeout() {
            // Base timeout calculation based on network type
            long baseTimeout = 10000; // 10 seconds
            
            switch (effectiveType.toLowerCase()) {
                case "slow-2g":
                    return baseTimeout * 6;
                case "2g":
                    return baseTimeout * 4;
                case "3g":
                    return baseTimeout * 2;
                case "4g":
                    return baseTimeout;
                default:
                    // Unknown or very fast connection
                    return baseTimeout;
            }
        }
        
        public boolean shouldUseOptimizedWaiting() {
            // Use optimized waiting for slower connections
            return "slow-2g".equals(effectiveType) || "2g".equals(effectiveType) || saveData;
        }
    }
    
    public void waitForDynamicContentWithNetworkAwareness(By locator) {
        NetworkPerformanceMonitor monitor = new NetworkPerformanceMonitor();
        NetworkStats stats = monitor.getCurrentNetworkStats();
        
        System.out.printf("Network Stats - Type: %s, Downlink: %.1f Mbps, RTT: %d ms%n", 
            stats.effectiveType, stats.downlink, stats.rtt);
        
        long recommendedTimeout = stats.getRecommendedTimeout();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(recommendedTimeout));
        
        if (stats.shouldUseOptimizedWaiting()) {
            // For slow connections, use more patient waiting strategy
            waitWithProgressiveTimeout(locator, wait);
        } else {
            // Standard waiting for fast connections
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
    }
    
    private void waitWithProgressiveTimeout(By locator, WebDriverWait wait) {
        // Progressive timeout: short waits initially, longer waits as time progresses
        long[] timeoutSteps = {1000, 2000, 5000, 10000}; // 1s, 2s, 5s, 10s
        
        for (long stepTimeout : timeoutSteps) {
            try {
                WebDriverWait stepWait = new WebDriverWait(driver, Duration.ofMillis(stepTimeout));
                stepWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                return; // Success
            } catch (TimeoutException e) {
                System.out.printf("Element not ready after %dms, trying longer timeout...%n", stepTimeout);
                // Continue to next timeout step
            }
        }
        
        // Final attempt with full timeout
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    public void waitForNetworkDependentContent(By locator, String resourceUrl) {
        // Wait for specific network resource to complete loading
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        
        wait.until(driver -> {
            String script = """
                var entries = performance.getEntriesByType('resource');
                var targetResource = entries.find(entry => entry.name.includes('%s'));
                
                if (!targetResource) {
                    return false; // Resource hasn't started loading
                }
                
                // Check if resource finished loading
                return targetResource.responseEnd > 0;
            """.formatted(resourceUrl);
            
            Boolean resourceLoaded = (Boolean) js.executeScript(script);
            
            if (resourceLoaded) {
                // Resource loaded, now check if element is visible
                try {
                    return driver.findElement(locator).isDisplayed();
                } catch (Exception e) {
                    return false;
                }
            }
            
            return false;
        });
    }
    
    public void validateNetworkPerformanceForDynamicLoading() {
        NetworkPerformanceMonitor monitor = new NetworkPerformanceMonitor();
        NetworkStats stats = monitor.getCurrentNetworkStats();
        
        System.out.println("Network Performance Validation");
        System.out.println("==============================");
        System.out.printf("Connection Type: %s%n", stats.effectiveType);
        System.out.printf("Downlink Speed: %.1f Mbps%n", stats.downlink);
        System.out.printf("Round Trip Time: %d ms%n", stats.rtt);
        System.out.printf("Save Data Mode: %s%n", stats.saveData ? "ON" : "OFF");
        
        // Validate against performance thresholds
        if (stats.rtt > 1000) {
            System.out.println("WARNING: High latency detected - consider longer timeouts");
        }
        
        if (stats.downlink < 1.0) {
            System.out.println("WARNING: Low bandwidth detected - optimize for slow connections");
        }
        
        if (stats.saveData) {
            System.out.println("INFO: Save Data mode active - user prefers reduced data usage");
        }
        
        System.out.printf("Recommended timeout: %d ms%n", stats.getRecommendedTimeout());
    }
}
```

**Pros:**
- **Real-time adaptation**: Uses actual browser network information
- **User experience focus**: Adapts to real user network conditions
- **Progressive enhancement**: Optimizes for different connection types
- **Resource awareness**: Can wait for specific network dependencies
- **Standards compliant**: Uses browser Network Information API

**Cons:**
- **API availability**: Network Information API not supported in all browsers
- **Limited control**: Cannot simulate conditions, only react to actual ones
- **Privacy considerations**: Network information may be restricted for privacy
- **Accuracy limitations**: Browser-reported network stats may not be precise
- **Platform differences**: Network API behavior varies across browsers/devices

## Decision Framework

### Choose Adaptive Timeout Strategy When:
- Tests run across multiple environments with varying network characteristics
- CI/CD pipelines experience different network performance than development
- Need automatic adjustment to network conditions without manual configuration
- Performance optimization is important but manual tuning is impractical

### Choose Network Condition Simulation When:
- Need to validate application behavior under specific network conditions
- Testing performance across different user scenarios (2G, 3G, 4G, WiFi)
- Debugging network-related test failures in controlled environment
- Quality assurance requires validation across network condition spectrum

### Choose Network-Aware Wait Strategies When:
- Tests run in production or real user environments
- Application serves users with diverse network conditions globally
- Need to optimize wait times based on actual user network characteristics
- Browser compatibility with Network Information API is acceptable

## Real-world Examples from Codebase Context

### Current Implementation Analysis

**DynamicLoadingTest.java** uses fixed timeouts:
```java
wait = new WebDriverWait(driver, Duration.ofSeconds(6));
```

**Potential network-related issues:**
- 6-second timeout may be insufficient for slow network conditions
- Same timeout used regardless of network environment
- No adaptation for CI/CD vs local development differences

**Network-aware improvement:**
```java
@BeforeMethod
public void openBrowser() {
    WebDriverManager.chromedriver().setup();
    driver = new ChromeDriver();
    
    // Determine appropriate timeout based on environment
    long timeoutSeconds = determineEnvironmentAppropriateTimeout();
    wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    
    driver.get(URL);
}

private long determineEnvironmentAppropriateTimeout() {
    String environment = System.getProperty("test.environment", "local");
    
    switch (environment.toLowerCase()) {
        case "ci":
        case "pipeline":
            return 15; // CI environments often have slower network
        case "staging":
            return 10; // Staging may have geographic latency
        case "production":
            return 20; // Production testing needs to handle real-world conditions
        default:
            return 6;  // Local development baseline
    }
}
```

### Network-Optimized Test Structure

```java
public class NetworkAwareDynamicLoadingTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private NetworkConditionSimulator networkSim;
    
    @BeforeMethod
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        networkSim = new NetworkConditionSimulator(driver);
        
        // Measure baseline network performance
        driver.get(URL);
        long baseline = measurePageLoadTime();
        
        // Set adaptive timeout based on baseline
        long adaptiveTimeout = Math.max(6, baseline / 1000 + 4); // Baseline + 4 seconds
        wait = new WebDriverWait(driver, Duration.ofSeconds(adaptiveTimeout));
        
        System.out.printf("Using adaptive timeout: %d seconds (baseline: %d ms)%n", 
            adaptiveTimeout, baseline);
    }
    
    private long measurePageLoadTime() {
        long startTime = System.currentTimeMillis();
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        return System.currentTimeMillis() - startTime;
    }
    
    @Test
    public void testDynamicLoadingAcrossNetworkConditions() {
        List<NetworkConditionSimulator.NetworkCondition> conditions = Arrays.asList(
            NetworkConditionSimulator.NetworkCondition.FAST_3G,
            NetworkConditionSimulator.NetworkCondition.SLOW_3G,
            NetworkConditionSimulator.NetworkCondition.SLOW_2G
        );
        
        networkSim.testDynamicLoadingUnderNetworkConditions(FINISH_MESSAGE, conditions);
    }
}
```

## Performance Optimization for Network Conditions

### 1. Resource Loading Optimization
```java
public void optimizeForSlowNetworks() {
    if (isSlowNetworkDetected()) {
        // Disable images to speed up loading
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.managed_default_content_settings.images", 2);
        options.setExperimentalOption("prefs", prefs);
        
        // Reinitialize driver with optimizations
        driver.quit();
        driver = new ChromeDriver(options);
    }
}

private boolean isSlowNetworkDetected() {
    try {
        String script = """
            if (navigator.connection) {
                return navigator.connection.effectiveType === 'slow-2g' || 
                       navigator.connection.effectiveType === '2g';
            }
            return false;
        """;
        
        return (Boolean) ((JavascriptExecutor) driver).executeScript(script);
    } catch (Exception e) {
        return false;
    }
}
```

### 2. Network Resilience Testing
```java
@Test
public void testNetworkResilienceScenarios() {
    // Test 1: Slow initial load
    networkSim.setNetworkCondition(NetworkCondition.SLOW_2G);
    driver.navigate().refresh();
    
    long startTime = System.currentTimeMillis();
    wait.until(ExpectedConditions.visibilityOfElementLocated(START_BUTTON));
    long slowLoadTime = System.currentTimeMillis() - startTime;
    
    // Test 2: Network recovery during operation
    driver.findElement(START_BUTTON).click();
    
    // Simulate network interruption
    networkSim.setNetworkCondition(NetworkCondition.OFFLINE);
    Thread.sleep(2000);
    
    // Network recovery
    networkSim.setNetworkCondition(NetworkCondition.FAST_3G);
    
    // Verify application recovers
    wait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_MESSAGE));
    
    System.out.printf("Slow load time: %d ms%n", slowLoadTime);
    System.out.println("Network recovery: SUCCESS");
}
```

## Common Network-Related Pitfalls

### 1. Fixed Timeout Assumptions
**Problem**: Using same timeout regardless of network conditions
```java
// BAD: Fixed timeout doesn't account for network variability
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
```

**Solution**: Environment-aware timeout configuration
```java
// GOOD: Adjust timeout based on environment and network conditions
long timeout = getEnvironmentAppropriateTimeout();
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
```

### 2. Ignoring CDN Performance
**Problem**: Not considering content delivery network impact
```java
// BAD: Assumes all resources load at same speed
wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
```

**Solution**: Wait for critical resources specifically
```java
// GOOD: Wait for CDN resources to complete
waitForCDNResources();
wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
```

### 3. Missing Network Error Handling
**Problem**: Not handling network-specific failures
```java
// BAD: Generic exception handling
try {
    wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
} catch (TimeoutException e) {
    throw new RuntimeException("Element not found");
}
```

**Solution**: Network-aware error classification
```java
// GOOD: Classify timeout based on network conditions
try {
    wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
} catch (TimeoutException e) {
    if (isNetworkRelatedTimeout()) {
        throw new NetworkTimeoutException("Timeout likely due to network conditions", e);
    } else {
        throw new ApplicationTimeoutException("Application response timeout", e);
    }
}
```

## Further Reading

- [Network Information API](https://developer.mozilla.org/en-US/docs/Web/API/Network_Information_API)
- [Chrome DevTools Network Emulation](https://developer.chrome.com/docs/devtools/network/)
- [Web Performance Best Practices](https://web.dev/performance/)
- [Selenium Network Interception](https://selenium.dev/documentation/webdriver/bidirectional/chrome_devtools/)

## Key Takeaways

- **Network conditions significantly impact dynamic loading reliability**
- **Fixed timeouts fail across different network environments**
- **Adaptive strategies improve test reliability without sacrificing performance**
- **Network simulation helps validate application behavior under various conditions**
- **Real-time network awareness provides the most accurate user experience testing**
- **Consider geographic distribution and CDN performance in test strategy**
- **Network-related failures require different handling than application failures**
- **CI/CD environments often have different network characteristics than development**