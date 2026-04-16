# Multi-Threaded Order Processing - Quick Reference

## 📌 Quick Start (2 minutes)

### Step 1: Create Processor
```java
MultiThreadedOrderProcessor processor = 
    new MultiThreadedOrderProcessor(controller, 4);  // 4 threads
```

### Step 2: Submit Orders
```java
processor.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red",
    "Options", "Description", 1_450_000.0,
    Payment.PaymentMethod.BANK_TRANSFER, "TXN-001");

processor.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White",
    "None", "Description", 3_200_000.0,
    Payment.PaymentMethod.LOAN_FINANCING, "TXN-002");
```

### Step 3: Process in Parallel
```java
List<Payment> results = processor.processAndGetResults();
```

### Step 4: Check Results
```java
System.out.println("Total: " + processor.getTotalOrders());
System.out.println("Success: " + processor.getSuccessCount());
System.out.println("Time: " + processor.getExecutionTimeMs() + " ms");
```

---

## 📁 Files Created

| File | Location | Purpose |
|------|----------|---------|
| **MultiThreadedOrderProcessor.java** | `controller/` | Main orchestrator |
| **OrderProcessingTask.java** | `controller/` | Per-thread task |
| **MultiThreadedOrderProcessingDemo.java** | `src/main/java/com/designx/erp/` | Demo comparison |
| **MultiThreadedDBOrderProcessingDemo.java** | `src/main/java/com/designx/erp/` | Database demo |
| **MULTITHREADING_IMPLEMENTATION_GUIDE.md** | `root/` | Full guide |
| **MULTITHREADING_QUICK_REFERENCE.md** | `root/` | This file |

---

## 🎛️ Thread Pool Sizing

```java
// Small (testing)
new MultiThreadedOrderProcessor(controller, 2);

// Medium (normal production)
new MultiThreadedOrderProcessor(controller, 4-8);

// Large (high volume)
new MultiThreadedOrderProcessor(controller, 16);

// Formula: (CPU_cores × 2) + 1
// Example: 4-core = 9 threads, 8-core = 17 threads
```

---

## ⏱️ Timeout Options

```java
// Default: 5 minutes
processor.processAndGetResults();

// Custom timeout
processor.processAndGetResults(10, TimeUnit.MINUTES);
processor.processAndGetResults(30, TimeUnit.SECONDS);
processor.processAndGetResults(2, TimeUnit.HOURS);
```

---

## 📊 Key Methods

```java
// Submission
processor.submitOrder(customerId, vehicle, variant, color,
                     features, details, price, method, transactionId);

// Processing
List<Payment> results = processor.processAndGetResults();
List<Payment> results = processor.processAndGetResults(timeout, unit);

// Metrics
processor.getTotalOrders()           // Total submitted
processor.getSuccessCount()          // Successfully processed
processor.getFailureCount()          // Failed/rejected
processor.getExecutionTimeMs()       // Total time in milliseconds

// Lifecycle
processor.clear()                    // Reset for new batch
processor.shutdown()                 // Graceful shutdown
processor.shutdownNow()              // Force shutdown
```

---

## 🔀 Sequential vs Parallel

### Sequential (Original)
```java
Payment p1 = controller.processOrder(...);  // ~1000ms
Payment p2 = controller.processOrder(...);  // ~1000ms
Payment p3 = controller.processOrder(...);  // ~1000ms
// Total: ~3000ms
```

### Parallel (New)
```java
MultiThreadedOrderProcessor processor = 
    new MultiThreadedOrderProcessor(controller, 4);

processor.submitOrder(...);  // Submit immediately
processor.submitOrder(...);  // Submit immediately
processor.submitOrder(...);  // Submit immediately

List<Payment> results = processor.processAndGetResults();
// Total: ~1200ms (all running in parallel)
```

---

## 💾 Batch Processing Pattern

```java
List<OrderDTO> allOrders = fetchFromDatabase();  // 10,000 orders

int batchSize = 100;
int threadCount = 8;

for (int i = 0; i < allOrders.size(); i += batchSize) {
    MultiThreadedOrderProcessor batchProcessor = 
        new MultiThreadedOrderProcessor(controller, threadCount);
    
    List<OrderDTO> batch = allOrders.subList(i, 
        Math.min(i + batchSize, allOrders.size()));
    
    for (OrderDTO order : batch) {
        batchProcessor.submitOrder(order.customerId, order.vehicle, ...);
    }
    
    List<Payment> results = batchProcessor.processAndGetResults();
    saveToDB(results);
    
    System.out.println("Processed batch: " + (i + batchSize) + "/" + 
        allOrders.size());
}
```

---

## 🚀 Running Demos

### Demo 1: Comparison (Sequential vs Parallel)
```bash
cd DesignX_OrderProcessing
java -cp bin com.designx.erp.MultiThreadedOrderProcessingDemo
```

**What it shows:**
- 3 orders: sequential vs parallel vs batch
- Performance comparison
- Throughput metrics

### Demo 2: Database Processing
```bash
java -cp bin com.designx.erp.MultiThreadedDBOrderProcessingDemo
```

**What it shows:**
- Processing quotes from MySQL in parallel
- Database integration
- Real-world scenario

---

## ✅ Performance Expectations

| Orders | Sequential | Parallel (4T) | Speedup |
|--------|-----------|---------------|---------|
| 3 | ~3000ms | ~1000ms | 3.0x |
| 10 | ~10000ms | ~2800ms | 3.6x |
| 50 | ~50000ms | ~12500ms | 4.0x |
| 100 | ~100000ms | ~24000ms | 4.2x |

*Times are approximate; actual results vary based on system and components*

---

## 🔒 Thread Safety Guarantees

✓ Safe for concurrent order submissions  
✓ Atomic counters prevent race conditions  
✓ Synchronized result collection  
✓ Per-thread exception isolation  
✓ Proper executor lifecycle management  

---

## ⚠️ Common Pitfalls

| Pitfall | Solution |
|---------|----------|
| OutOfMemory with large batches | Process in smaller batches (100-500 orders) |
| All orders fail | Check inventory, customer data, payment config |
| Timeout errors | Increase timeout or reduce thread count |
| Database connections exhausted | Reduce thread pool size or increase DB pool |
| Memory leaks | Call `processor.shutdown()` or use try-with-resources |

---

## 🎓 Integration Examples

### With Spring
```java
@Service
public class ParallelOrderService {
    @Autowired
    private OrderProcessingController controller;
    
    public List<Payment> processBatch(List<OrderDTO> orders) {
        MultiThreadedOrderProcessor processor = 
            new MultiThreadedOrderProcessor(controller, 8);
        
        orders.forEach(o -> processor.submitOrder(...));
        return processor.processAndGetResults();
    }
}
```

### With REST API
```java
@PostMapping("/api/orders/batch")
public ResponseEntity<List<Payment>> processBatch(
        @RequestBody List<OrderRequest> orders) {
    MultiThreadedOrderProcessor processor = 
        new MultiThreadedOrderProcessor(controller, 8);
    
    orders.forEach(o -> processor.submitOrder(...));
    return ResponseEntity.ok(processor.processAndGetResults());
}
```

### With Custom Service
```java
public class OrderBatchService {
    public void processOrdersFromFile(String filename, int threads) {
        List<Order> orders = readOrdersFromFile(filename);
        MultiThreadedOrderProcessor processor = 
            new MultiThreadedOrderProcessor(controller, threads);
        
        orders.forEach(o -> processor.submitOrder(...));
        List<Payment> results = processor.processAndGetResults();
        
        generateReport(results);
    }
}
```

---

## 📈 Monitoring & Logging

```java
// Log progress
System.out.println("Total orders: " + processor.getTotalOrders());
System.out.println("Completed: " + processor.getSuccessCount());
System.out.println("Failed: " + processor.getFailureCount());

// Calculate metrics
long executionTime = processor.getExecutionTimeMs();
double throughput = (processor.getTotalOrders() * 1000.0) / executionTime;
double successRate = (100.0 * processor.getSuccessCount()) / 
    processor.getTotalOrders();

System.out.println("Throughput: " + throughput + " orders/sec");
System.out.println("Success Rate: " + successRate + "%");
System.out.println("Avg Time/Order: " + 
    (executionTime / processor.getTotalOrders()) + " ms");
```

---

## 📞 Troubleshooting

**Q: Why are some orders null in results?**  
A: Null means the order was rejected during processing (validation/approval failed)

**Q: How do I know which order failed?**  
A: Results list maintains order of submissions. Cross-reference with submitted orders.

**Q: Can I process orders to different databases?**  
A: Yes, inject different adapters into each controller instance

**Q: What happens if a thread crashes?**  
A: Exception is captured in Future.get() and logged. Other threads continue.

**Q: Can I use the same controller for multiple processors?**  
A: Not recommended. Create a new controller per processor to avoid state conflicts.

---

## 📚 Related Files

- `MULTITHREADING_IMPLEMENTATION_GUIDE.md` - Full documentation
- `MultiThreadedOrderProcessingDemo.java` - Complete working example
- `MultiThreadedDBOrderProcessingDemo.java` - Database example
- `controller/MultiThreadedOrderProcessor.java` - Main implementation
- `controller/OrderProcessingTask.java` - Task implementation

---

**Last Updated:** April 2026  
**Version:** 1.0  
**Status:** Production Ready ✅
