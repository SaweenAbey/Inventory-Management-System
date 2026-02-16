package com.Inventory.Backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Inventory.Backend.entity.Order;
import com.Inventory.Backend.entity.OrderStatus;
import com.Inventory.Backend.entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    List<Order> findByUser_UserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserAndStatus(User user, OrderStatus status);

    List<Order> findByInventoryItem_ItemId(Long itemId);
}
