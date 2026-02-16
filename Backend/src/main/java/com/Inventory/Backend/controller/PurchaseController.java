package com.Inventory.Backend.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Inventory.Backend.dto.PurchaseItemRequest;
import com.Inventory.Backend.entity.Order;
import com.Inventory.Backend.entity.OrderStatus;
import com.Inventory.Backend.repository.UserRepository;
import com.Inventory.Backend.service.PurchaseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
@CrossOrigin
public class PurchaseController {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);

    private final PurchaseService purchaseService;
    private final UserRepository userRepository;

    // Helper method to check if user is admin
    private boolean isAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(user -> "ADMIN".equals(user.getRole().toString()))
                .orElse(false);
    }

    // Helper method to check if user is buyer
    private boolean isBuyer(String username) {
        return userRepository.findByUsername(username)
                .map(user -> "BUYER".equals(user.getRole().toString()))
                .orElse(false);
    }

    // Helper method to get current authenticated username
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    @PostMapping("/buy-item")
    public ResponseEntity<?> purchaseItem(@Valid @RequestBody PurchaseItemRequest request) {
        logger.info("Purchase request received");

        try {
            String username = getCurrentUsername();

            if (username == null) {
                logger.warn("Purchase request without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            if (!isBuyer(username)) {
                logger.warn("Non-buyer user {} attempted to purchase item", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only buyers can purchase items");
            }

            Order order = purchaseService.purchaseItem(request, username);
            logger.info("Purchase successful for user {}: Order ID: {}", username, order.getOrderId());

            return ResponseEntity.status(HttpStatus.CREATED).body(order);

        } catch (RuntimeException e) {
            logger.error("Purchase failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Purchase failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during purchase", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders() {
        logger.info("Request to fetch buyer's orders");

        try {
            String username = getCurrentUsername();

            if (username == null) {
                logger.warn("Get orders request without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            if (!isBuyer(username)) {
                logger.warn("Non-buyer user {} attempted to view orders", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only buyers can view their orders");
            }

            List<Order> orders = purchaseService.getBuyerOrders(username);
            logger.info("Fetched {} orders for buyer: {}", orders.size(), username);

            return ResponseEntity.ok(orders);

        } catch (Exception e) {
            logger.error("Error fetching buyer orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId) {
        logger.info("Request to fetch order details for ID: {}", orderId);

        try {
            String username = getCurrentUsername();

            if (username == null) {
                logger.warn("Get order details request without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            Order order = purchaseService.getOrderById(orderId);

            // Check if user is the order owner or admin
            if (!order.getUser().getUsername().equals(username) && !isAdmin(username)) {
                logger.warn("User {} attempted to view order {} they don't own", username, orderId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to view this order");
            }

            logger.info("Order details fetched successfully for ID: {}", orderId);
            return ResponseEntity.ok(order);

        } catch (RuntimeException e) {
            logger.error("Order not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching order details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @PutMapping("/order/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long orderId) {
        logger.info("Request to confirm order ID: {}", orderId);

        try {
            String username = getCurrentUsername();

            if (username == null) {
                logger.warn("Confirm order request without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            if (!isAdmin(username)) {
                logger.warn("Non-admin user {} attempted to confirm order", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only admins can confirm orders");
            }

            Order order = purchaseService.confirmOrder(orderId);
            logger.info("Order confirmed by admin {}: {}", username, orderId);

            return ResponseEntity.ok(order);

        } catch (RuntimeException e) {
            logger.error("Error confirming order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/order/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        logger.info("Request to cancel order ID: {}", orderId);

        try {
            String username = getCurrentUsername();

            if (username == null) {
                logger.warn("Cancel order request without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            if (!isAdmin(username)) {
                logger.warn("Non-admin user {} attempted to cancel order", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only admins can cancel orders");
            }

            Order order = purchaseService.cancelOrder(orderId, username);
            logger.info("Order cancelled by admin {}: {}", username, orderId);

            return ResponseEntity.ok(order);

        } catch (RuntimeException e) {
            logger.error("Error cancelling order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/orders/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        logger.info("Request to fetch orders by status: {}", status);

        try {
            String username = getCurrentUsername();

            if (username == null) {
                logger.warn("Get orders by status request without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            if (!isAdmin(username)) {
                logger.warn("Non-admin user {} attempted to view orders by status", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only admins can view all orders by status");
            }

            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                List<Order> orders = purchaseService.getOrdersByStatus(orderStatus);
                logger.info("Fetched {} orders with status: {} by admin: {}", orders.size(), status, username);
                return ResponseEntity.ok(orders);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid order status: {}", status);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid order status. Valid values: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED");
            }

        } catch (Exception e) {
            logger.error("Error fetching orders by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }
}
