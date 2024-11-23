package com.scalableservices.orderservice.repository;

import com.scalableservices.orderservice.model.OrderDeliveryAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDeliveryAgentRepository extends JpaRepository<OrderDeliveryAgent, Long> {
    OrderDeliveryAgent findByDeliveryAgentId(Long deliveryAgentId);
    OrderDeliveryAgent findByOrderId(Long orderId);
    @Query("SELECT oda FROM OrderDeliveryAgent oda WHERE oda.deliveryAgentId = :deliveryAgentId")
    List<OrderDeliveryAgent> findAllByDeliveryAgentId(Long deliveryAgentId);
}
