package com.designx.erp.model;

/**
 * MEMBER 2 - Order Lifecycle Management
 * ======================================
 * 
 * ⭐ BEHAVIORAL PATTERN: State Pattern
 * 
 * Responsibility: Define valid state transitions for order lifecycle
 * 
 * State Transitions:
 *   CAPTURED → VALIDATED → APPROVED → ALLOCATED → DISPATCHED → INVOICED → PAYMENT_PENDING → PAYMENT_SUCCESS
 *   
 *   With alternatives:
 *   - REJECTED: from VALIDATED, APPROVED
 *   - CANCELLED: from any state except PAYMENT_SUCCESS
 *   - FAILED: from any processing state
 * 
 * GRASP Principle: Information Expert - knows valid state transitions
 * SOLID Principle: O - Open/Closed for adding new states
 * 
 * Usage: Call isTransitionAllowed(from, to) before updating status.
 */
public class OrderStateTransition {

    /**
     * Validates if transition from one state to another is allowed.
     * Enforces business rules for order lifecycle state machine.
     * 
     * @param currentStatus the current order status
     * @param desiredStatus the desired new status
     * @return true if transition is allowed
     */
    public static boolean isTransitionAllowed(OrderStatus currentStatus, OrderStatus desiredStatus) {
        
        // Terminal states - cannot transition from these
        if (currentStatus == OrderStatus.PAYMENT_SUCCESS || currentStatus == OrderStatus.CANCELLED) {
            return false;  // No further transitions from terminal states
        }

        // Main workflow path
        switch (currentStatus) {
            case CAPTURED:
                return desiredStatus == OrderStatus.VALIDATED || 
                       desiredStatus == OrderStatus.REJECTED ||
                       desiredStatus == OrderStatus.CANCELLED;

            case VALIDATED:
                return desiredStatus == OrderStatus.APPROVED || 
                       desiredStatus == OrderStatus.REJECTED ||
                       desiredStatus == OrderStatus.CANCELLED;

            case APPROVED:
                return desiredStatus == OrderStatus.ALLOCATED || 
                       desiredStatus == OrderStatus.REJECTED ||
                       desiredStatus == OrderStatus.CANCELLED ||
                       desiredStatus == OrderStatus.FAILED;

            case REJECTED:
                return desiredStatus == OrderStatus.CAPTURED ||  // Re-open for modification
                       desiredStatus == OrderStatus.CANCELLED;

            case ALLOCATED:
                return desiredStatus == OrderStatus.DISPATCHED || 
                       desiredStatus == OrderStatus.CANCELLED ||
                       desiredStatus == OrderStatus.FAILED;

            case DISPATCHED:
                return desiredStatus == OrderStatus.INVOICED || 
                       desiredStatus == OrderStatus.FAILED;

            case INVOICED:
                return desiredStatus == OrderStatus.PAYMENT_PENDING || 
                       desiredStatus == OrderStatus.FAILED;

            case PAYMENT_PENDING:
                return desiredStatus == OrderStatus.PAYMENT_SUCCESS || 
                       desiredStatus == OrderStatus.FAILED;

            case FAILED:
                return desiredStatus == OrderStatus.CAPTURED ||  // Allow retry from beginning
                       desiredStatus == OrderStatus.CANCELLED;

            default:
                return false;
        }
    }

    /**
     * Determines if an order can be modified in its current state.
     * Modification allowed only before significant progress.
     * 
     * @param status the current order status
     * @return true if order can be modified
     */
    public static boolean canModifyInState(OrderStatus status) {
        return status == OrderStatus.CAPTURED || 
               status == OrderStatus.VALIDATED || 
               status == OrderStatus.REJECTED;
    }

    /**
     * Determines if an order can be cancelled in its current state.
     * Cannot cancel once payment is successful.
     * 
     * @param status the current order status
     * @return true if order can be cancelled
     */
    public static boolean canCancelInState(OrderStatus status) {
        return status != OrderStatus.PAYMENT_SUCCESS && 
               status != OrderStatus.CANCELLED;
    }

    /**
     * Determines if allocated stock should be released on cancellation.
     * 
     * @param status the current order status
     * @return true if stock should be released
     */
    public static boolean shouldReleaseStockOnCancel(OrderStatus status) {
        return status == OrderStatus.ALLOCATED || 
               status == OrderStatus.DISPATCHED;
    }
}
