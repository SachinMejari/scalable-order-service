package com.scalableservices.orderservice.dto.order;

import com.scalableservices.orderservice.statemachine.OrderStates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateResponse {
    private Long orderId;
    private OrderStates orderStatus;
}
