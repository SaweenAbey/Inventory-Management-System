package com.Inventory.Backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Inventory.Backend.dto.PurchaseItemRequest;
import com.Inventory.Backend.entity.InventoryItem;
import com.Inventory.Backend.entity.Order;
import com.Inventory.Backend.entity.OrderStatus;
import com.Inventory.Backend.entity.User;
import com.Inventory.Backend.repository.InventoryItemRepository;
import com.Inventory.Backend.repository.OrderRepository;
import com.Inventory.Backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseService {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    private final OrderRepository orderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;

    public Order purchaseItem(PurchaseItemRequest request, String buyerUsername) {
        logger.info("Buyer {} attempting to purchase item ID: {} with quantity: {}", buyerUsername, request.getItemId(), request.getQuantity());

        // Get buyer user
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> {
                    logger.error("Buyer not found: {}", buyerUsername);
                    return new RuntimeException("Buyer not found");
                });

        // Get inventory item
        InventoryItem item = inventoryItemRepository.findById(request.getItemId())
                .orElseThrow(() -> {
                    logger.error("Item not found with ID: {}", request.getItemId());
                    return new RuntimeException("Item not found with ID: " + request.getItemId());
                });

        // Check if item is active
        if (!item.getIsActive()) {
            logger.warn("Buyer {} attempted to purchase inactive item: {}", buyerUsername, item.getItemName());
            throw new RuntimeException("Item is not available for purchase");
        }

        // Check quantity availability
        if (item.getQuantity() < request.getQuantity()) {
            logger.warn("Insufficient inventory for item {}: available={}, requested={}", 
                    item.getItemName(), item.getQuantity(), request.getQuantity());
            throw new RuntimeException("Insufficient inventory. Available quantity: " + item.getQuantity());
        }

        // Calculate total price
        Double totalPrice = item.getPrice() * request.getQuantity();

        // Create order
        Order order = Order.builder()
                .inventoryItem(item)
                .user(buyer)
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .notes(request.getNotes())
                .build();

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully: Order ID: {}, Item: {}, Quantity: {}", 
                savedOrder.getOrderId(), item.getItemName(), request.getQuantity());

        // Update item quantity
        item.setQuantity(item.getQuantity() - request.getQuantity());
        item.setUpdatedBy(buyerUsername);
        inventoryItemRepository.save(item);

        logger.info("Inventory updated for item {}: New quantity: {}", item.getItemName(), item.getQuantity());
        logger.info("Purchase completed by {}: Order ID: {}, Item: {}, Total: ${}", 
                buyerUsername, savedOrder.getOrderId(), item.getItemName(), totalPrice);

        return savedOrder;
    }

    public Order getOrderById(Long orderId) {
        logger.debug("Fetching order with ID: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Order not found with ID: " + orderId);
                });
    }

    public List<Order> getBuyerOrders(String buyerUsername) {
        logger.debug("Fetching orders for buyer: {}", buyerUsername);
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> {
                    logger.error("Buyer not found: {}", buyerUsername);
                    return new RuntimeException("Buyer not found");
                });

        List<Order> orders = orderRepository.findByUser(buyer);
        logger.info("Fetched {} orders for buyer: {}", orders.size(), buyerUsername);
        return orders;
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        logger.debug("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status);
    }

    public Order confirmOrder(Long orderId) {
        logger.info("Confirming order ID: {}", orderId);
        Order order = getOrderById(orderId);
        order.setStatus(OrderStatus.CONFIRMED);
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order confirmed: {}", orderId);
        return updatedOrder;
    }

    public Order cancelOrder(Long orderId, String username) {
        logger.info("Admin {} attempting to cancel order ID: {}", username, orderId);
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            logger.warn("Order {} is already cancelled", orderId);
            throw new RuntimeException("Order is already cancelled");
        }

        // Refund inventory if not delivered
        if (order.getStatus() != OrderStatus.DELIVERED) {
            InventoryItem item = order.getInventoryItem();
            item.setQuantity(item.getQuantity() + order.getQuantity());
            item.setUpdatedBy(username);
            inventoryItemRepository.save(item);
            logger.info("Inventory refunded for cancelled order: {} quantity restored", order.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        logger.info("Order cancelled successfully by {}: {}", username, orderId);
        return cancelledOrder;
    }
}
