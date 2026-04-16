# ERP System - Interface Contracts for Backend Teams

This document defines the **contracts** between the UI subsystem and backend modules. Each team MUST implement their respective interface for successful integration.

---

## How Integration Works

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI SUBSYSTEM                            │
│  (Java Swing - This Repository)                                 │
│                                                                 │
│  Uses interfaces from: com.erp.service.interfaces.*             │
│  Uses models from:     com.erp.model.*                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ Calls interface methods
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SERVICE IMPLEMENTATIONS                       │
│  (Your team implements these)                                   │
│                                                                 │
│  Example: HRServiceImpl implements HRService                    │
│           CRMServiceImpl implements CRMService                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ Your implementation talks to
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         DATABASE / APIs                          │
│  (Your choice: MySQL, PostgreSQL, MongoDB, REST APIs, etc.)     │
└─────────────────────────────────────────────────────────────────┘
```

---

## Team Assignments & Interfaces

| Team | Module | Interface File | Models Used |
|------|--------|----------------|-------------|
| **HR Team** | HR Management | `HRService.java` | Employee, Attendance, LeaveRequest, Payroll |
| **CRM Team** | CRM | `CRMService.java` | Customer, SupportTicket |
| **Sales Team** | Sales Management | `SalesService.java` | Order, OrderItem, Customer, Product |
| **Order Team** | Order Processing | `OrderProcessingService.java` | Order, OrderItem |
| **Finance Team** | Financial Management | `FinanceService.java` | Invoice, Payment, Customer |
| **Supply Chain Team** | Supply Chain/Purchasing | `SupplyChainService.java` | Product, Vendor, PurchaseOrder, PurchaseOrderItem |
| **Manufacturing Team** | Manufacturing | `ManufacturingService.java` | BillOfMaterials, BOMItem, WorkOrder, Product |
| **Project Team** | Project Management | `ProjectService.java` | Project, ProjectTask |
| **Marketing Team** | Marketing | `MarketingService.java` | Campaign, Customer |
| **Reporting Team** | Reporting | `ReportingService.java` | (Uses data from all modules) |
| **Analytics Team** | Data Analytics & BI | `AnalyticsService.java` | (Uses data from all modules) |
| **Automation Team** | Automation | `AutomationService.java` | (Cross-module) |
| **Integration Team** | Integration | `IntegrationService.java` | (Cross-module) |

---

## Implementation Guidelines

### Step 1: Create Your Implementation Class

```java
package com.erp.service.impl;

import com.erp.service.interfaces.HRService;
import com.erp.model.*;
import java.util.List;

/**
 * YOUR TEAM implements this class.
 * This is where you connect to your database/backend.
 */
public class HRServiceImpl implements HRService {

    @Override
    public List<Employee> getAllEmployees() {
        // YOUR CODE HERE
        // Connect to database, fetch employees, return list
    }

    @Override
    public Employee getEmployeeById(int employeeId) {
        // YOUR CODE HERE
    }

    // ... implement ALL methods from HRService interface
}
```

### Step 2: Follow These Rules

| Rule | Description |
|------|-------------|
| **Return empty lists, not null** | If no data found, return `new ArrayList<>()` not `null` |
| **IDs are positive integers** | Valid IDs > 0. Return 0 or -1 for "not found" |
| **Use the model classes** | Don't create your own Employee, Order, etc. Use `com.erp.model.*` |
| **Handle exceptions internally** | Catch database errors, log them, return appropriate defaults |
| **Implement ALL methods** | Every method in the interface must be implemented |

### Step 3: Register Your Implementation

When we integrate, we'll use a Service Locator or Dependency Injection pattern:

```java
// In ServiceRegistry.java (will be created during integration)
public class ServiceRegistry {
    private static HRService hrService = new HRServiceImpl();
    private static CRMService crmService = new CRMServiceImpl();
    // ... other services

    public static HRService getHRService() { return hrService; }
    public static CRMService getCRMService() { return crmService; }
}
```

---

## Model Classes Reference

All model classes are in `src/com/erp/model/`. Here's what each contains:

### Core Entities

| Model | Key Fields | Used By |
|-------|------------|---------|
| `Employee` | employeeId, firstName, lastName, department, position, salary | HR, Project, All |
| `Customer` | customerId, companyName, contactName, email, customerType | CRM, Sales, Finance |
| `Product` | productId, sku, name, unitPrice, quantityInStock | Inventory, Sales, Manufacturing |
| `Vendor` | vendorId, companyName, contactName, paymentTerms | Supply Chain |

### Transaction Entities

| Model | Key Fields | Used By |
|-------|------------|---------|
| `Order` | orderId, orderNumber, customerId, status, totalAmount | Sales, Order Processing |
| `OrderItem` | orderItemId, productId, quantity, unitPrice | Sales, Order Processing |
| `PurchaseOrder` | poId, poNumber, vendorId, status, totalAmount | Supply Chain |
| `Invoice` | invoiceId, invoiceNumber, customerId, status, totalAmount | Finance |
| `Payment` | paymentId, amount, paymentMethod, status | Finance |

### HR Entities

| Model | Key Fields | Used By |
|-------|------------|---------|
| `Attendance` | attendanceId, employeeId, checkInTime, checkOutTime, status | HR |
| `LeaveRequest` | leaveRequestId, employeeId, leaveType, startDate, endDate, status | HR |
| `Payroll` | payrollId, employeeId, payPeriod, grossPay, netPay | HR |

### Other Entities

| Model | Key Fields | Used By |
|-------|------------|---------|
| `Project` | projectId, projectCode, name, status, percentComplete | Project Management |
| `ProjectTask` | taskId, projectId, name, assignedToId, status | Project Management |
| `Campaign` | campaignId, name, type, status, budget, leadsGenerated | Marketing |
| `SupportTicket` | ticketId, customerId, subject, priority, status | CRM |
| `BillOfMaterials` | bomId, productId, components | Manufacturing |
| `WorkOrder` | workOrderId, productId, quantityOrdered, status | Manufacturing |

---

## Status Values (Use These Exact Strings)

### Common Statuses
- **Active states**: `ACTIVE`, `IN_PROGRESS`, `OPEN`
- **Completion states**: `COMPLETED`, `CLOSED`, `RESOLVED`
- **Pending states**: `PENDING`, `DRAFT`, `PLANNED`
- **Negative states**: `CANCELLED`, `REJECTED`, `BLOCKED`

### Order Status Flow
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
                ↓
            CANCELLED
```

### Leave Request Status
```
PENDING → APPROVED → (taken)
    ↓
REJECTED
```

### Work Order Status
```
PLANNED → RELEASED → IN_PROGRESS → COMPLETED
                          ↓
                      CANCELLED
```

---

## Integration Checklist

Before integration day, verify:

- [ ] All interface methods are implemented
- [ ] No methods return `null` (use empty collections)
- [ ] All model classes from `com.erp.model` are used correctly
- [ ] Status strings match exactly (case-sensitive)
- [ ] Code compiles without errors: `javac -d out @sources.txt`
- [ ] Basic manual testing done for CRUD operations

---

## File Structure for Your Implementation

```
your-team-repo/
├── src/
│   └── com/
│       └── erp/
│           └── service/
│               └── impl/
│                   └── YourServiceImpl.java   ← Your implementation
├── lib/                                        ← Any dependencies
└── README.md                                   ← How to build your code
```

---

## Questions?

- **Integration issues**: Contact the UI team lead
- **Interface unclear**: Check the JavaDoc comments in the interface files
- **Need new methods**: Request through the integration team

---

## Change Log

| Date | Change |
|------|--------|
| Initial | Created all interfaces and models |

---

*This document is authoritative. If the code differs from this document, the code wins.*
