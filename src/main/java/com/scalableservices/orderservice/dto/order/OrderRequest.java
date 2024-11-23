package com.scalableservices.orderservice.dto.order;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotBlank(message = "customerId is mandatory")
    private Long customerId;
    @NotBlank(message = "restaurantId is mandatory")
    private Long restaurantId;
    @NotBlank(message = "items is mandatory")
    private List<OrderMenuDetails> items;
    @NotBlank(message = "totalPrice is mandatory")
    private String deliveryAddress;
    @NotBlank(message = "totalAmount is mandatory")
    private BigDecimal totalAmount;
}
