package com.scalableservices.orderservice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveryAgentMappingResponse {
    private Long orderId;
    private Long deliveryAgentId;
}
