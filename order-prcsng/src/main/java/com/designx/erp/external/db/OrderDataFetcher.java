package com.designx.erp.external.db;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * MEMBER 1 - Database Integration Layer
 * ======================================
 * 
 * Responsibility: Fetch order-related data from MySQL database
 * 
 * Methods:
 *   - fetchQuoteById() - retrieve quote from quotes table
 *   - fetchQuoteItems() - retrieve line items for a quote
 *   - fetchCustomer() - retrieve customer information
 *   - fetchDeal() - retrieve deal/opportunity data
 * 
 * GRASP Principle: Information Expert - knows database query logic
 * SOLID Principle: S - Single Responsibility (read-only DB access)
 * 
 * Important: Read-only access only. No INSERT/UPDATE/DELETE operations.
 */
public class OrderDataFetcher {

    private final Connection connection;

    /**
     * Constructor - accepts DBConnection to avoid duplicate connections.
     */
    public OrderDataFetcher(Connection connection) {
        this.connection = connection;
    }

    /**
     * Fetches quote data by ID.
     * Table: quotes (quote_id, customer_id, deal_id, total_amount, discount, final_amount, created_at)
     * 
     * @param quoteId the quote identifier
     * @return Map with quote data, empty if not found
     */
    public Map<String, Object> fetchQuoteById(String quoteId) {
        Map<String, Object> quoteData = new HashMap<>();
        String query = "SELECT quote_id, customer_id, deal_id, total_amount, discount, final_amount, created_at FROM quotes WHERE quote_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(quoteId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                quoteData.put("quote_id", rs.getInt("quote_id"));
                quoteData.put("customer_id", rs.getInt("customer_id"));
                quoteData.put("deal_id", rs.getInt("deal_id"));
                quoteData.put("total_amount", rs.getDouble("total_amount"));
                quoteData.put("discount", rs.getDouble("discount"));
                quoteData.put("final_amount", rs.getDouble("final_amount"));
                quoteData.put("created_at", rs.getTimestamp("created_at"));
                System.out.println("[OrderDataFetcher] Quote fetched: " + quoteId);
            }
        } catch (SQLException e) {
            System.err.println("[OrderDataFetcher] Error fetching quote: " + e.getMessage());
        }

        return quoteData;
    }

    /**
     * Fetches all line items for a given quote.
     * Table: quote_items (item_id, quote_id, product_name, quantity, price)
     * 
     * @param quoteId the quote identifier
     * @return array of maps, each representing a line item
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object>[] fetchQuoteItems(String quoteId) {
        String query = "SELECT item_id, quote_id, product_name, quantity, price FROM quote_items WHERE quote_id = ?";
        Map<String, Object>[] items = new Map[100]; // max 100 items per quote
        int count = 0;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(quoteId));
            ResultSet rs = stmt.executeQuery();

            while (rs.next() && count < 100) {
                Map<String, Object> item = new HashMap<>();
                item.put("item_id", rs.getInt("item_id"));
                item.put("quote_id", rs.getInt("quote_id"));
                item.put("product_name", rs.getString("product_name"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("price", rs.getDouble("price"));
                items[count++] = item;
            }

            System.out.println("[OrderDataFetcher] Quote items fetched: " + count + " items");
        } catch (SQLException e) {
            System.err.println("[OrderDataFetcher] Error fetching quote items: " + e.getMessage());
        }

        // Return only populated items
        Map<String, Object>[] result = new Map[count];
        System.arraycopy(items, 0, result, 0, count);
        return result;
    }

    /**
     * Fetches customer data by ID.
     * Table: customers (customer_id, name, email, phone, region, created_at)
     * 
     * @param customerId the customer identifier
     * @return Map with customer data, empty if not found
     */
    public Map<String, Object> fetchCustomer(String customerId) {
        Map<String, Object> customerData = new HashMap<>();
        String query = "SELECT customer_id, name, email, phone, region, created_at FROM customers WHERE customer_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(customerId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                customerData.put("customer_id", rs.getInt("customer_id"));
                customerData.put("name", rs.getString("name"));
                customerData.put("email", rs.getString("email"));
                customerData.put("phone", rs.getString("phone"));
                customerData.put("region", rs.getString("region"));
                customerData.put("created_at", rs.getTimestamp("created_at"));
                System.out.println("[OrderDataFetcher] Customer fetched: " + customerId);
            }
        } catch (SQLException e) {
            System.err.println("[OrderDataFetcher] Error fetching customer: " + e.getMessage());
        }

        return customerData;
    }

    /**
     * Fetches deal (opportunity) data.
     * Table: deals (deal_id, quote_id, stage, probability, expected_close_date)
     * 
     * @param quoteId the quote identifier associated with the deal
     * @return Map with deal data, empty if not found
     */
    public Map<String, Object> fetchDeal(String quoteId) {
        Map<String, Object> dealData = new HashMap<>();
        String query = "SELECT deal_id, quote_id, stage, probability, expected_close_date FROM deals WHERE quote_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, quoteId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                dealData.put("deal_id", rs.getString("deal_id"));
                dealData.put("quote_id", rs.getString("quote_id"));
                dealData.put("stage", rs.getString("stage"));
                dealData.put("probability", rs.getDouble("probability"));
                dealData.put("expected_close_date", rs.getTimestamp("expected_close_date"));
                System.out.println("[OrderDataFetcher] Deal fetched for quote: " + quoteId);
            }
        } catch (SQLException e) {
            System.err.println("[OrderDataFetcher] Error fetching deal: " + e.getMessage());
        }

        return dealData;
    }
}
