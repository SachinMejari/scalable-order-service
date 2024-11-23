package com.scalableservices.orderservice.repository;

import com.scalableservices.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    public List<Order> findByRestaurantIdAndIsDeletedFalse(Long restaurantId);
    @Query("SELECT o FROM Order o inner join OrderDeliveryAgent oda on o.id = oda.order.id WHERE oda.deliveryAgentId = :deliveryAgentId AND o.restaurantId = :restaurantId AND o.orderStatus = 'READY' order by o.id")
    List<Order> getReadyForDeliveryOrders(Long deliveryAgentId, Long restaurantId);
}
