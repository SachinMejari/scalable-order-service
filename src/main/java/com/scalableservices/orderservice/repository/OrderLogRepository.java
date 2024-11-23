package com.scalableservices.orderservice.repository;

import com.scalableservices.orderservice.model.OrderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLogRepository extends JpaRepository<OrderLog, Long> {
    public List<OrderLog> findByOrderIdAndIsDeletedFalse(Long orderId);
    List<OrderLog> findByOrderId(Long orderId);
}
