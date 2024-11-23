package com.scalableservices.orderservice.controller;

import com.scalableservices.orderservice.dto.common.ApiResponse;
import com.scalableservices.orderservice.dto.common.ErrorMessage;
import com.scalableservices.orderservice.dto.order.*;
import com.scalableservices.orderservice.enums.UserTypes;
import com.scalableservices.orderservice.exception.ServiceException;
import com.scalableservices.orderservice.model.Order;
import com.scalableservices.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/place-order")
    public ApiResponse<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest, @RequestHeader(value = "X-UserType", required = true) String userType) {
        try{
            if(!userType.equalsIgnoreCase("customer")) {
                throw new ServiceException(HttpStatus.UNAUTHORIZED, "Only customers are allowed to place orders");
            }
            OrderResponse orderResponse = orderService.createOrder(orderRequest);
            return ApiResponse.<OrderResponse>builder().status("success").data(orderResponse).build();
        }catch (ServiceException e) {
            log.error("ServiceException occured while placing order for customer with id : {}", orderRequest.getCustomerId(), e);
            return ApiResponse.<OrderResponse>builder().status("failed")
                    .error(ErrorMessage.builder().error("Error while placing order").description(e.getMessage()).build())
                    .build();
        }
        catch (Exception e) {
            log.error("Exception occured while placing order for customer with id : {}", orderRequest.getCustomerId(), e);
            return ApiResponse.<OrderResponse>builder().status("failed").error(
                    ErrorMessage.builder().error("Error while placing order").description(e.getMessage()).build()
            ).build();
        }
    }

    // Track Order
    @GetMapping("/{orderId}/status")
    public ApiResponse<Order> trackOrder(@PathVariable Long orderId, @RequestHeader(value = "X-UserType", required = true) String userType) {
        try {
            if(!userType.equalsIgnoreCase("customer") && !userType.equalsIgnoreCase("restaurant_owner")) {
                throw new ServiceException(HttpStatus.UNAUTHORIZED, "Only customer and restaurant are allowed to track orders");
            }
            return ApiResponse.<Order>builder().status("success").data(orderService.getOrderByOrderId(orderId)).build();
        }catch (ServiceException e) {
            log.error("ServiceException occured while getting order with id: {}", orderId, e);
            return ApiResponse.<Order>builder().status("failed")
                    .error(ErrorMessage.builder().error("Error while getting order").description(e.getMessage()).build())
                    .build();
        }
        catch (Exception e) {
            log.error("Exception occured while getting order with id: {}", orderId, e);
            return ApiResponse.<Order>builder().status("failed").error(
                    ErrorMessage.builder().error("Error while getting order").description(e.getMessage()).build()
            ).build();
        }
    }

    // View Order History
    @GetMapping("/{orderId}/history")
    public ApiResponse<List<OrderLogResponse>> getOrderHistory(@PathVariable("orderId") Long orderId, @RequestHeader(value = "X-UserType", required = true) String userType) {
        try{
            if(!userType.equalsIgnoreCase("customer") && !userType.equalsIgnoreCase("restaurant_owner")) {
                throw new ServiceException(HttpStatus.UNAUTHORIZED, "Only customer and restaurant are allowed to view order history");
            }
            return ApiResponse.<List<OrderLogResponse>>builder().status("success").data(orderService.getOrderLogs(orderId)).build();
        }catch (ServiceException e) {
            log.error("ServiceException occured while getting order logs with id: {}", orderId, e);
            return ApiResponse.<List<OrderLogResponse>>builder().status("failed")
                    .error(ErrorMessage.builder().error("Error while getting order logs").description(e.getMessage()).build())
                    .build();
        }
        catch (Exception e) {
            log.error("Exception occured while getting order logs with id: {}", orderId, e);
            return ApiResponse.<List<OrderLogResponse>>builder().status("failed").error(
                    ErrorMessage.builder().error("Error while getting order logs").description(e.getMessage()).build()
            ).build();
        }
    }

    @PostMapping("/{orderId}/update-status")
    public ApiResponse<OrderStatusUpdateResponse> processDelivery(@RequestBody OrderStatusUpdateRequest orderStatusUpdateRequest, @PathVariable("orderId") Long orderId
            , @RequestHeader(value = "X-UserType", required = true) String userType) {
        try {
            if(!userType.equalsIgnoreCase("delivery_agent") && !userType.equalsIgnoreCase("restaurant_owner")) {
                throw new ServiceException(HttpStatus.UNAUTHORIZED, "Only delivery agents are allowed to process delivery");
            }
            OrderStatusUpdateResponse response = orderService.updateOrderStatus(orderId, orderStatusUpdateRequest, UserTypes.valueOf(userType.toUpperCase()));
            return ApiResponse.<OrderStatusUpdateResponse>builder().status("success").data(response).build();
        } catch (ServiceException e) {
            log.error("ServiceException occured while processing delivery for order: {}", orderId, e);
            return ApiResponse.<OrderStatusUpdateResponse>builder().status("failed")
                    .error(ErrorMessage.builder().error(e.getMessage()).description("Error while processing delivery").build())
                    .build();
        }
        catch (Exception e) {
            log.error("Exception occured while processing delivery for order: {}", orderId, e);
            return ApiResponse.<OrderStatusUpdateResponse>builder().status("failed").error(
                    ErrorMessage.builder().error("Error while processing delivery").description(e.getMessage()).build()
            ).build();
        }
    }

    @PostMapping("/{orderId}/delivery-agent-assign/{deliveryAgentId}")
    public ApiResponse<OrderDeliveryAgentMappingResponse> assignDeliveryAgent(@PathVariable("orderId") Long orderId, @PathVariable("deliveryAgentId") Long deliveryAgentId
            , @RequestHeader(value = "X-UserType", required = true) String userType) {
        try {
            if(!userType.equalsIgnoreCase(UserTypes.RESTAURANT_OWNER.name())) {
                throw new ServiceException(HttpStatus.UNAUTHORIZED, "Only restaurant owners are allowed to assign delivery agents");
            }
            OrderDeliveryAgentMappingResponse response = orderService.mapOrderToDeliveryAgent(orderId, deliveryAgentId);
            return ApiResponse.<OrderDeliveryAgentMappingResponse>builder().status("success").data(response).build();
        } catch (ServiceException e) {
            log.error("ServiceException occured while assigning delivery agent for order: {}", orderId, e);
            return ApiResponse.<OrderDeliveryAgentMappingResponse>builder().status("failed")
                    .error(ErrorMessage.builder().error(e.getMessage()).description("Error while assigning delivery agent").build())
                    .build();
        }
        catch (Exception e) {
            log.error("Exception occured while assigning delivery agent for order: {}", orderId, e);
            return ApiResponse.<OrderDeliveryAgentMappingResponse>builder().status("failed").error(
                    ErrorMessage.builder().error("Error while assigning delivery agent").description(e.getMessage()).build()
            ).build();
        }
    }

    @GetMapping("/ready-for-pickup/{deliverAgentId}")
    public ApiResponse<List<Order>> getOrdersReadyForPickup(@PathVariable("deliverAgentId") Long deliverAgentId
            , @RequestHeader(value = "X-UserType", required = true) String userType) {
        try {
            if(!userType.equalsIgnoreCase(UserTypes.DELIVERY_AGENT.name())) {
                throw new ServiceException(HttpStatus.UNAUTHORIZED, "Only delivery agents are allowed to view orders ready for pickup");
            }
            return ApiResponse.<List<Order>>builder().status("success").data(orderService.getAllOrdersMappedToDeliveryAgent(deliverAgentId)).build();
        } catch (ServiceException e) {
            log.error("ServiceException occured while getting orders ready for pickup for delivery agent: {}", deliverAgentId, e);
            return ApiResponse.<List<Order>>builder().status("failed")
                    .error(ErrorMessage.builder().error(e.getMessage()).description("Error while getting orders ready for pickup").build())
                    .build();
        }
        catch (Exception e) {
            log.error("Exception occured while getting orders ready for pickup for delivery agent: {}", deliverAgentId, e);
            return ApiResponse.<List<Order>>builder().status("failed").error(
                    ErrorMessage.builder().error("Error while getting orders ready for pickup").description(e.getMessage()).build()
            ).build();
        }
    }
}
