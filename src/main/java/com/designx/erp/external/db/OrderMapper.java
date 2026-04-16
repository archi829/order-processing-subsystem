package com.designx.erp.external.db;

import com.designx.erp.model.Order;
import com.designx.erp.model.OrderStatus;
import com.designx.erp.exception.*;

import java.util.Map;

/**
 * MEMBER 1 - Database Integration Layer
 * ======================================
 * 
 * ⭐ CREATIONAL PATTERN: Factory Pattern
 * 
 * Responsibility: Convert database data into Order domain objects
 * 
 * Factory Method: createOrderFromQuote()
 *   - Takes raw DB data (Map objects)
 *   - Validates data
 *   - Constructs Order domain object
 *   - Handles conversion errors
 * 
 * GRASP Principle: Creator - knows how to construct Order objects
 * SOLID Principle: S - Single Responsibility (mapping only)
 * 
 * No business logic here - pure data transformation.
 */
public class OrderMapper {

    private final OrderDataFetcher dataFetcher;

    /**
     * Constructor - receives OrderDataFetcher for data access.
     */
    public OrderMapper(OrderDataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
    }

    /**
     * Factory Method: Creates an Order from database quote data.
     * 
     * Process:
     *   1. Fetch quote, customer, and deal data
     *   2. Validate all required fields
     *   3. Construct Order object with DB data
     *   4. Initialize with CAPTURED status
     * 
     * @param quoteId the quote ID from database
     * @return Order object mapped from DB data
     * @throws OrderNotFoundException if quote not found
     * @throws InvalidCustomerDataException if customer data invalid
     * @throws NegativeOrderValueException if order amount invalid
     */
    public Order createOrderFromQuote(String quoteId) 
            throws OrderNotFoundException, InvalidCustomerDataException, NegativeOrderValueException {
        
        // Step 1: Fetch quote from database
        Map<String, Object> quoteData = dataFetcher.fetchQuoteById(quoteId);
        if (quoteData.isEmpty()) {
            throw new OrderNotFoundException("Quote ID: " + quoteId);
        }

        // Step 2: Extract and validate customer ID
        Integer customerId = (Integer) quoteData.get("customer_id");
        if (customerId == null || customerId <= 0) {
            throw new InvalidCustomerDataException("Quote " + quoteId + " has no valid customer ID");
        }

        // Step 3: Fetch customer data
        Map<String, Object> customerData = dataFetcher.fetchCustomer(String.valueOf(customerId));
        if (customerData.isEmpty()) {
            throw new InvalidCustomerDataException("Customer not found: " + customerId);
        }

        // Step 4: Validate customer-critical fields
        String customerName = (String) customerData.get("name");
        String customerEmail = (String) customerData.get("email");
        
        if (customerName == null || customerName.isBlank()) {
            throw new InvalidCustomerDataException("Customer " + customerId + " has no name");
        }

        // Step 5: Validate order amount
        Double totalAmount = (Double) quoteData.get("total_amount");
        if (totalAmount == null || totalAmount < 0) {
            throw new NegativeOrderValueException(totalAmount != null ? totalAmount : -1);
        }

        // Step 6: Construct Order object
        Order order = new Order(
                String.valueOf(customerId),       // Convert to String
                customerName,
                customerEmail,  // Using email as contact detail
                "QUOTE-" + quoteId,               // Using quoteId as order reference
                "Active",                         // Default status for new orders
                "Tata Nexon",                     // vehicle model (matching demo data)
                "XZ+",                            // vehicle variant
                "Flame Red",                      // vehicle color
                "Sunroof, Rear Camera",           // custom features
                "Order from Quote: " + quoteId,
                (Double) quoteData.get("final_amount")  // Use final_amount from quote
        );

        // Step 7: Fetch and attach quote items
        Map<String, Object>[] quoteItems = dataFetcher.fetchQuoteItems(quoteId);
        StringBuilder itemsDescription = new StringBuilder();
        for (Map<String, Object> item : quoteItems) {
            itemsDescription.append(item.get("product_name")).append(" (qty: ")
                    .append(item.get("quantity")).append("), ");
        }

        if (itemsDescription.length() > 0) {
            // Remove trailing comma
            String itemsStr = itemsDescription.toString();
            itemsStr = itemsStr.substring(0, itemsStr.length() - 2);
        }

        // Step 8: Order is already initialized with CAPTURED status in constructor
        // The history already has one entry from constructor, so we just continue

        System.out.println("[OrderMapper] Order created from quote: " + quoteId + 
                " | Customer: " + customerName + " | Amount: ₹" + totalAmount);

        return order;
    }

    /**
     * Validates that all required database fields are present and valid.
     * 
     * @param quoteData the quote map from database
     * @param customerData the customer map from database
     * @return true if all fields are valid
     * @throws InvalidCustomerDataException if validation fails
     */
    @SuppressWarnings("unused")
    private boolean validateData(Map<String, Object> quoteData, Map<String, Object> customerData)
            throws InvalidCustomerDataException {
        
        // Check quote fields
        if (!quoteData.containsKey("total_amount")) {
            throw new InvalidCustomerDataException("Quote missing total_amount field");
        }

        // Check customer fields
        String[] requiredFields = {"customer_id", "name", "email"};
        for (String field : requiredFields) {
            if (!customerData.containsKey(field) || customerData.get(field) == null) {
                throw new InvalidCustomerDataException("Customer missing " + field + " field");
            }
        }

        return true;
    }
}
