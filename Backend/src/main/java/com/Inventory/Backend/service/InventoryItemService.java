package com.Inventory.Backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Inventory.Backend.dto.CreateInventoryItemRequest;
import com.Inventory.Backend.dto.DeleteInventoryItemRequest;
import com.Inventory.Backend.dto.UpdateInventoryItemRequest;
import com.Inventory.Backend.entity.InventoryItem;
import com.Inventory.Backend.repository.InventoryItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryItemService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryItemService.class);

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryItem addItem(CreateInventoryItemRequest request, String adminUsername) {
        logger.info("Admin {} attempting to add new inventory item: {}", adminUsername, request.getItemName());

        // Check if item name already exists
        if (inventoryItemRepository.existsByItemName(request.getItemName())) {
            logger.warn("Add item failed: Item name already exists - {}", request.getItemName());
            throw new RuntimeException("Item with this name already exists");
        }

        // Check if SKU already exists
        if (inventoryItemRepository.existsBySku(request.getSku())) {
            logger.warn("Add item failed: SKU already exists - {}", request.getSku());
            throw new RuntimeException("Item with this SKU already exists");
        }

        InventoryItem item = InventoryItem.builder()
                .itemName(request.getItemName())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .category(request.getCategory())
                .sku(request.getSku())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(adminUsername)
                .updatedBy(adminUsername)
                .build();

        InventoryItem savedItem = inventoryItemRepository.save(item);
        logger.info("Inventory item added successfully by {}: {} (ID: {})", adminUsername, savedItem.getItemName(), savedItem.getItemId());

        return savedItem;
    }

    public InventoryItem updateItem(UpdateInventoryItemRequest request, String adminUsername) {
        logger.info("Admin {} attempting to update inventory item ID: {}", adminUsername, request.getItemId());

        InventoryItem item = inventoryItemRepository.findById(request.getItemId())
                .orElseThrow(() -> {
                    logger.error("Update item failed: Item not found with ID: {}", request.getItemId());
                    return new RuntimeException("Item not found with ID: " + request.getItemId());
                });

        // Check if new item name already exists (and is different from current)
        if (request.getItemName() != null && !request.getItemName().equals(item.getItemName())) {
            if (inventoryItemRepository.existsByItemName(request.getItemName())) {
                logger.warn("Update item failed: New item name already exists - {}", request.getItemName());
                throw new RuntimeException("Item with this name already exists");
            }
            item.setItemName(request.getItemName());
        }

        // Check if new SKU already exists (and is different from current)
        if (request.getSku() != null && !request.getSku().equals(item.getSku())) {
            if (inventoryItemRepository.existsBySku(request.getSku())) {
                logger.warn("Update item failed: New SKU already exists - {}", request.getSku());
                throw new RuntimeException("Item with this SKU already exists");
            }
            item.setSku(request.getSku());
        }

        // Update other fields
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }
        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            item.setCategory(request.getCategory());
        }
        if (request.getIsActive() != null) {
            item.setIsActive(request.getIsActive());
        }

        item.setUpdatedBy(adminUsername);

        InventoryItem updatedItem = inventoryItemRepository.save(item);
        logger.info("Inventory item updated successfully by {}: {} (ID: {})", adminUsername, updatedItem.getItemName(), updatedItem.getItemId());

        return updatedItem;
    }

    public InventoryItem deleteItem(DeleteInventoryItemRequest request, String adminUsername) {
        logger.info("Admin {} attempting to delete inventory item ID: {}", adminUsername, request.getItemId());

        InventoryItem item = inventoryItemRepository.findById(request.getItemId())
                .orElseThrow(() -> {
                    logger.error("Delete item failed: Item not found with ID: {}", request.getItemId());
                    return new RuntimeException("Item not found with ID: " + request.getItemId());
                });

        logger.info("Deleting inventory item by {}: {} (ID: {}). Reason: {}", adminUsername, item.getItemName(), item.getItemId(), request.getReason());

        inventoryItemRepository.delete(item);

        logger.info("Inventory item deleted successfully by {}: {} (ID: {})", adminUsername, item.getItemName(), request.getItemId());

        return item;
    }

    public InventoryItem getItemById(Long itemId) {
        logger.debug("Fetching inventory item with ID: {}", itemId);
        return inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    logger.error("Item not found with ID: {}", itemId);
                    return new RuntimeException("Item not found with ID: " + itemId);
                });
    }

    public List<InventoryItem> getAllItems() {
        logger.debug("Fetching all inventory items");
        return inventoryItemRepository.findAll();
    }

    public List<InventoryItem> getItemsByCategory(String category) {
        logger.debug("Fetching inventory items by category: {}", category);
        return inventoryItemRepository.findByCategory(category);
    }

    public List<InventoryItem> getActiveItems() {
        logger.debug("Fetching all active inventory items");
        return inventoryItemRepository.findByIsActiveTrue();
    }

    public List<InventoryItem> getInactiveItems() {
        logger.debug("Fetching all inactive inventory items");
        return inventoryItemRepository.findByIsActiveFalse();
    }
}
