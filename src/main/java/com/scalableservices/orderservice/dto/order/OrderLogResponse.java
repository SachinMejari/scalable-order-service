package com.scalableservices.orderservice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLogResponse {
    private String orderStatus;
    private String remarks;
    private String userType;
    private Long userId;
    private String timestamp;
}
