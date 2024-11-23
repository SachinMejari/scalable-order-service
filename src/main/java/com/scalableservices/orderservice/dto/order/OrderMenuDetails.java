package com.scalableservices.orderservice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMenuDetails {
    private Long menuId;
    private String menuName;
    private String menuDescription;
    private Double menuPrice;
    private Integer menuQuantity;
    private Double menuTotalPrice;
}
