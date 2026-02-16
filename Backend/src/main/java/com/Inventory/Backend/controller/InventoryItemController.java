package com.Inventory.Backend.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Inventory.Backend.dto.CreateInventoryItemRequest;
import com.Inventory.Backend.dto.DeleteInventoryItemRequest;
import com.Inventory.Backend.dto.UpdateInventoryItemRequest;
import com.Inventory.Backend.entity.InventoryItem;
import com.Inventory.Backend.repository.UserRepository;
import com.Inventory.Backend.service.InventoryItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin
public class InventoryItemController {
    private static final Logger logger = LoggerFactory.getLogger(InventoryItemController.class);

    private final InventoryItemService inventoryItemService;
    private final UserRepository userRepository;

    // Helper method to check if user is admin
    private boolean isAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(user -> "ADMIN".equals(user.getRole().toString()))
                .orElse(false);
    }

    // Helper method to get current authenticated username
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    @PostMapping("/items/add")
    public ResponseEntity<?> addInventoryItem(@Valid @RequestBody CreateInventoryItemRequest request) {
        logger.info("Request to add inventory item");
        
        try {
            String username = getCurrentUsername();
            
            if (username == null) {
                logger.warn("Add item request without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            if (!isAdmin(username)) {
                logger.warn("Non-admin user {} attempted to add inventory item", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only admin users can add inventory items");
            }

            InventoryItem item = inventoryItemService.addItem(request, username);
            logger.info("Inventory item added successfully: {}", item.getItemId());
            return ResponseEntity.status(HttpStatus.CREATED).body(item);

        } catch (RuntimeException e) {
            logger.error("Error adding inventory item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error adding inventory item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @PutMapping("/items/update")
    public ResponseEntity<?> updateInventoryItem(@Valid @RequestBody UpdateInventoryItemRequest request) {
        logger.info("Request to update inventory item ID: {}", request.getItemId());
        
        try {
            String username = getCurrentUsername();
            
            if (username == null) {
                logger.warn("Update item request without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            if (!isAdmin(username)) {
                logger.warn("Non-admin user {} attempted to update inventory item", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only admin users can update inventory items");
            }

            InventoryItem item = inventoryItemService.updateItem(request, username);
            logger.info("Inventory item updated successfully: {}", item.getItemId());
            return ResponseEntity.ok(item);

        } catch (RuntimeException e) {
            logger.error("Error updating inventory item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating inventory item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @DeleteMapping("/items/delete")
    public ResponseEntity<?> deleteInventoryItem(@Valid @RequestBody DeleteInventoryItemRequest request) {
        logger.info("Request to delete inventory item ID: {}", request.getItemId());
        
        try {
            String username = getCurrentUsername();
            
            if (username == null) {
                logger.warn("Delete item request without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            if (!isAdmin(username)) {
                logger.warn("Non-admin user {} attempted to delete inventory item", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only admin users can delete inventory items");
            }

            InventoryItem item = inventoryItemService.deleteItem(request, username);
            logger.info("Inventory item deleted successfully: {}", item.getItemId());
            return ResponseEntity.ok("Inventory item deleted successfully");

        } catch (RuntimeException e) {
            logger.error("Error deleting inventory item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error deleting inventory item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<?> getItem(@PathVariable Long itemId) {
        logger.info("Request to fetch inventory item ID: {}", itemId);
        
        try {
            InventoryItem item = inventoryItemService.getItemById(itemId);
            logger.info("Inventory item fetched successfully: {}", itemId);
            return ResponseEntity.ok(item);

        } catch (RuntimeException e) {
            logger.error("Error fetching inventory item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/items")
    public ResponseEntity<?> getAllItems() {
        logger.info("Request to fetch all inventory items");
        
        try {
            List<InventoryItem> items = inventoryItemService.getAllItems();
            logger.info("Fetched {} inventory items", items.size());
            return ResponseEntity.ok(items);

        } catch (Exception e) {
            logger.error("Error fetching inventory items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @GetMapping("/items/category/{category}")
    public ResponseEntity<?> getItemsByCategory(@PathVariable String category) {
        logger.info("Request to fetch inventory items by category: {}", category);
        
        try {
            List<InventoryItem> items = inventoryItemService.getItemsByCategory(category);
            logger.info("Fetched {} inventory items for category: {}", items.size(), category);
            return ResponseEntity.ok(items);

        } catch (Exception e) {
            logger.error("Error fetching inventory items by category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @GetMapping("/items/active/only")
    public ResponseEntity<?> getActiveItems() {
        logger.info("Request to fetch active inventory items");
        
        try {
            List<InventoryItem> items = inventoryItemService.getActiveItems();
            logger.info("Fetched {} active inventory items", items.size());
            return ResponseEntity.ok(items);

        } catch (Exception e) {
            logger.error("Error fetching active inventory items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @GetMapping("/items/inactive/only")
    public ResponseEntity<?> getInactiveItems() {
        logger.info("Request to fetch inactive inventory items");
        
        try {
            String username = getCurrentUsername();
            
            if (username == null) {
                logger.warn("Get inactive items request without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            if (!isAdmin(username)) {
                logger.warn("Non-admin user {} attempted to view inactive inventory items", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only admin users can view inactive inventory items");
            }

            List<InventoryItem> items = inventoryItemService.getInactiveItems();
            logger.info("Fetched {} inactive inventory items by admin: {}", items.size(), username);
            return ResponseEntity.ok(items);

        } catch (Exception e) {
            logger.error("Error fetching inactive inventory items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }
}
