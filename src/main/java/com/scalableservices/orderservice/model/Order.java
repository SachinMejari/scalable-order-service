package com.scalableservices.orderservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.scalableservices.orderservice.statemachine.OrderStates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(name="order_status", nullable = false)
    private OrderStates orderStatus;

    @Column(name="order_details", nullable = false, columnDefinition = "json")
    private String orderDetails; // Using String to represent JSON data

    @Column(name="total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name="is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name="is_archived", nullable = false)
    private Boolean isArchived = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "customer_address", nullable = false)
    private String customerAddress;

    // Getters and setters
    public OrderStates getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStates orderStatus) {
        this.orderStatus = orderStatus;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
