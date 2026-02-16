package com.Inventory.Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Inventory.Backend.entity.InventoryItem;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    // Basic Find Methods
    Optional<InventoryItem> findByItemName(String itemName);

    Optional<InventoryItem> findBySku(String sku);

    List<InventoryItem> findByCategory(String category);

    List<InventoryItem> findByIsActiveTrue();

    List<InventoryItem> findByIsActiveFalse();

    // Boolean Check Methods
    boolean existsByItemName(String itemName);

    boolean existsBySku(String sku);

    // ============ CUSTOM SQL QUERIES ============

    // Search by item name (case-insensitive)
    @Query("SELECT i FROM InventoryItem i WHERE LOWER(i.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))")
    List<InventoryItem> searchByItemNameContains(@Param("itemName") String itemName);

    // Search by item name or description
    @Query("SELECT i FROM InventoryItem i WHERE LOWER(i.itemName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<InventoryItem> searchByNameOrDescription(@Param("keyword") String keyword);

    // Find items by price range
    @Query("SELECT i FROM InventoryItem i WHERE i.price BETWEEN :minPrice AND :maxPrice AND i.isActive = true " +
           "ORDER BY i.price ASC")
    List<InventoryItem> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    // Get low stock items (quantity less than threshold)
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity < :threshold AND i.isActive = true " +
           "ORDER BY i.quantity ASC")
    List<InventoryItem> getLowStockItems(@Param("threshold") Integer threshold);

    // Get out of stock items
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity = 0 AND i.isActive = true")
    List<InventoryItem> getOutOfStockItems();

    // Find items by category and active status
    @Query("SELECT i FROM InventoryItem i WHERE i.category = :category AND i.isActive = :isActive " +
           "ORDER BY i.itemName ASC")
    List<InventoryItem> findByCategoryAndActiveStatus(@Param("category") String category, 
                                                       @Param("isActive") Boolean isActive);

    // Get items with quantity greater than
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity > :minQuantity AND i.isActive = true " +
           "ORDER BY i.quantity DESC")
    List<InventoryItem> getItemsWithMinimumQuantity(@Param("minQuantity") Integer minQuantity);

    // Find most expensive items
    @Query("SELECT i FROM InventoryItem i WHERE i.isActive = true ORDER BY i.price DESC LIMIT :limit")
    List<InventoryItem> getMostExpensiveItems(@Param("limit") Integer limit);

    // Find cheapest items
    @Query("SELECT i FROM InventoryItem i WHERE i.isActive = true ORDER BY i.price ASC LIMIT :limit")
    List<InventoryItem> getCheapestItems(@Param("limit") Integer limit);

    // Get total inventory value
    @Query("SELECT SUM(i.price * i.quantity) FROM InventoryItem i WHERE i.isActive = true")
    Double getTotalInventoryValue();

    // Get total items in stock
    @Query("SELECT SUM(i.quantity) FROM InventoryItem i WHERE i.isActive = true")
    Integer getTotalItemsInStock();

    // Get average price
    @Query("SELECT AVG(i.price) FROM InventoryItem i WHERE i.isActive = true")
    Double getAveragePrice();

    // Native SQL: Get all items with their total value
    @Query(value = "SELECT item_id, item_name, category, quantity, price, " +
           "(quantity * price) as total_value FROM inventory_items " +
           "WHERE is_active = true ORDER BY (quantity * price) DESC", 
           nativeQuery = true)
    List<?> getAllItemsWithTotalValue();

    // Search with multiple criteria
    @Query("SELECT i FROM InventoryItem i WHERE " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:minPrice IS NULL OR i.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR i.price <= :maxPrice) AND " +
           "(:isActive IS NULL OR i.isActive = :isActive) " +
           "ORDER BY i.itemName ASC")
    List<InventoryItem> advancedSearch(@Param("category") String category, 
                                       @Param("minPrice") Double minPrice,
                                       @Param("maxPrice") Double maxPrice,
                                       @Param("isActive") Boolean isActive);

    // Get recently created items
    @Query("SELECT i FROM InventoryItem i WHERE i.isActive = true " +
           "ORDER BY i.createdAt DESC LIMIT :limit")
    List<InventoryItem> getRecentlyAddedItems(@Param("limit") Integer limit);

    // Get recently updated items
    @Query("SELECT i FROM InventoryItem i WHERE i.isActive = true " +
           "ORDER BY i.updatedAt DESC LIMIT :limit")
    List<InventoryItem> getRecentlyUpdatedItems(@Param("limit") Integer limit);

    // Find items by category with sorting
    @Query("SELECT i FROM InventoryItem i WHERE i.category = :category AND i.isActive = true " +
           "ORDER BY i.quantity DESC")
    List<InventoryItem> findByCategoryOrderByQuantity(@Param("category") String category);

    // Count items by category
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.category = :category AND i.isActive = true")
    Integer countByCategory(@Param("category") String category);

    // Get all active categories
    @Query("SELECT DISTINCT i.category FROM InventoryItem i WHERE i.isActive = true ORDER BY i.category")
    List<String> getAllActiveCategories();

    // Native SQL: Get inventory summary
    @Query(value = "SELECT category, COUNT(*) as item_count, SUM(quantity) as total_quantity, " +
           "MIN(price) as min_price, MAX(price) as max_price, AVG(price) as avg_price " +
           "FROM inventory_items WHERE is_active = true GROUP BY category",
           nativeQuery = true)
    List<?> getInventorySummaryByCategory();

    // Get items with specific SKU pattern
    @Query("SELECT i FROM InventoryItem i WHERE i.sku LIKE :skuPattern AND i.isActive = true")
    List<InventoryItem> findBySkuPattern(@Param("skuPattern") String skuPattern);

    // Check item availability
    @Query("SELECT CASE WHEN i.quantity > 0 THEN true ELSE false END FROM InventoryItem i WHERE i.itemId = :itemId")
    Boolean isItemAvailable(@Param("itemId") Long itemId);

    // Get items created by specific admin
    @Query("SELECT i FROM InventoryItem i WHERE i.createdBy = :createdBy AND i.isActive = true " +
           "ORDER BY i.createdAt DESC")
    List<InventoryItem> getItemsCreatedBy(@Param("createdBy") String createdBy);

    // Update item quantity
    @Query("UPDATE InventoryItem i SET i.quantity = :newQuantity WHERE i.itemId = :itemId")
    void updateQuantity(@Param("itemId") Long itemId, @Param("newQuantity") Integer newQuantity);

    // Soft delete (mark as inactive)
    @Query("UPDATE InventoryItem i SET i.isActive = false WHERE i.itemId = :itemId")
    void softDeleteItem(@Param("itemId") Long itemId);

    // Activate item
    @Query("UPDATE InventoryItem i SET i.isActive = true WHERE i.itemId = :itemId")
    void activateItem(@Param("itemId") Long itemId);
}
