package com.scalableservices.orderservice.service;

import com.scalableservices.orderservice.dto.order.*;
import com.scalableservices.orderservice.enums.UserTypes;
import com.scalableservices.orderservice.exception.ServiceException;
import com.scalableservices.orderservice.model.Order;
import com.scalableservices.orderservice.model.OrderDeliveryAgent;
import com.scalableservices.orderservice.model.OrderLog;
import com.scalableservices.orderservice.repository.OrderDeliveryAgentRepository;
import com.scalableservices.orderservice.repository.OrderLogRepository;
import com.scalableservices.orderservice.repository.OrderRepository;
import com.scalableservices.orderservice.statemachine.OrderEvents;
import com.scalableservices.orderservice.statemachine.OrderStates;
import com.scalableservices.orderservice.statemachine.ProcessOrderEvent;
import com.scalableservices.orderservice.utils.CommonOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDeliveryAgentRepository orderDeliveryAgentRepository;
    @Autowired
    private OrderLogRepository orderLogRepository;
    @Autowired
    private CommonOperations commonOperations;
    @Autowired
    private ProcessOrderEvent processOrderEvent;


    public Order getOrderByOrderId(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "Order not found with id: " + orderId));
    }

    public void addOrderDeliveryAgentMapping(Long orderId, Long deliveryAgentId) {
        try {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "Order not found with id: " + orderId));
            OrderDeliveryAgent existingMapping = orderDeliveryAgentRepository.findByDeliveryAgentId(deliveryAgentId);
            if (existingMapping != null) {
                existingMapping.setDeliveryAgentId(deliveryAgentId);
                orderDeliveryAgentRepository.save(existingMapping);
            } else {
                orderDeliveryAgentRepository.save(OrderDeliveryAgent.builder()
                        .order(order)
                        .deliveryAgentId(deliveryAgentId)
                        .isDeleted(false)
                        .isArchived(false)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }
        catch (Exception e) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while adding order delivery agent mapping");
        }
    }

    public void updateOrder(Order order) {
        try {
            orderRepository.save(order);
        }
        catch (Exception e) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating order");
        }
    }

    public void updateOrderLog(OrderLog orderLog) {
        try {
            orderLogRepository.save(orderLog);
        }
        catch (Exception e) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating order log");
        }
    }

    public List<Order> getReadyForDeliveryOrders(Long deliveryAgentId, Long restaurantId) {
        List<Order> eligibleOrders = orderRepository.getReadyForDeliveryOrders(deliveryAgentId, restaurantId);
        if(eligibleOrders.isEmpty()) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "No orders found ready for delivery agent: " + deliveryAgentId);
        }
        return eligibleOrders;
    }

    public OrderResponse createOrder(OrderRequest orderRequest){
        try{
            Order order = Order.builder()
                    .orderDetails(commonOperations.getOrderMenuDetailsJson(orderRequest.getItems()))
                    .restaurantId(orderRequest.getRestaurantId())
                    .customerId(orderRequest.getCustomerId())
                    .orderStatus(OrderStates.PLACED)
                    .totalAmount(orderRequest.getTotalAmount())
                    .customerAddress(orderRequest.getDeliveryAddress())
                    .isDeleted(false)
                    .isArchived(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
            return OrderResponse.builder().orderId(order.getId()).orderStatus(order.getOrderStatus().name()).build();
        }catch (ServiceException e){
            throw e;
        }
        catch (Exception e) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while creating order");
        }
    }

    public Order trackOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "Order not found with id: " + orderId));
    }

    public List<OrderLogResponse> getOrderLogs(Long orderId) {
        try {
            List<OrderLog> orderLogs = orderLogRepository.findByOrderId(orderId);
            if (orderLogs.isEmpty()) {
                throw new ServiceException(HttpStatus.BAD_REQUEST, "No logs found for order: " + orderId);
            }
            List<OrderLogResponse> orderLogResponses = new ArrayList<>();

            for (OrderLog orderLog : orderLogs) {
                orderLogResponses.add(OrderLogResponse.builder()
                        .orderStatus(orderLog.getOrderSubStatus())
                        .remarks(orderLog.getRemarks())
                        .userType(orderLog.getEnteredBy())
                        .userId(orderLog.getEnteredById())
                        .timestamp(orderLog.getCreatedAt().toString())
                        .build());
            }
            return orderLogResponses;
        }catch (ServiceException e){
            throw e;
        }
        catch (Exception e) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while fetching order logs");
        }
    }

    public OrderStatusUpdateResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest requestPayload, UserTypes userType) {
        try {

            Order order = orderRepository.findById(orderId).orElseThrow(
                    () -> new ServiceException(HttpStatus.NOT_FOUND, "Order with ID " + orderId + " not found"));

            if(!commonOperations.isEligibleOrderEvent(userType.name(), OrderEvents.valueOf(requestPayload.getOrderStatus().getOrderEvent()))) {
                log.error("Restaurant owner is not eligible to perform this action");
                throw new ServiceException(HttpStatus.BAD_REQUEST, "Restaurant owner is not eligible to perform this action");
            }
            if(!commonOperations.isOrderEventValid(requestPayload.getOrderStatus().getOrderEvent())) {
                log.error("Invalid order status: {}", requestPayload.getOrderStatus().getOrderEvent());
                throw new ServiceException(HttpStatus.BAD_REQUEST, "Invalid order status: " + requestPayload.getOrderStatus().getOrderEvent());
            }
            if (order.getOrderStatus() == OrderStates.DELIVERED || order.getOrderStatus() == OrderStates.CANCELLED) {
                log.error("Order with id {} is {}", orderId, order.getOrderStatus());
                throw new ServiceException(HttpStatus.BAD_REQUEST, "Order with id " + orderId + " is " + order.getOrderStatus());
            }
            OrderEvents orderEvent = OrderEvents.valueOf(requestPayload.getOrderStatus().getOrderEvent());
            OrderStates orderStates = processOrderEvent.process(order.getId(), orderEvent);
            if(order.getOrderStatus().ordinal() >= orderStates.ordinal()) {
                log.error("Order state transition failed for order with id: {}", orderId);
                throw new ServiceException(HttpStatus.BAD_REQUEST, "Order state transition failed for order with id: " + orderId);
            }
            else{
                order.setOrderStatus(orderStates);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                OrderLog orderLog = OrderLog.builder()
                        .order(order)
                        .orderSubStatus(orderStates.name())
                        .remarks(requestPayload.getOrderStatus().getComment())
                        .enteredBy(userType.name())
                        .enteredById(requestPayload.getUserId())
                        .isDeleted(false)
                        .isArchived(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                orderLogRepository.save(orderLog);
            }
            order = getOrderByOrderId(orderId);
            log.info("Order states changed successfully by owner: {}", requestPayload.getUserId());
            return OrderStatusUpdateResponse.builder()
                    .orderId(orderId)
                    .orderStatus(order.getOrderStatus())
                    .build();
        } catch (ServiceException e) {
            log.error("Error while updating order status: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error while updating order status: {}", e.getMessage());
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    public OrderDeliveryAgentMappingResponse mapOrderToDeliveryAgent(Long orderId, Long deliveryAgentId) {
        try {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "Order with ID " + orderId + " not found"));
            if(order.getOrderStatus().equals(OrderStates.DELIVERED) || order.getOrderStatus().equals(OrderStates.CANCELLED)) {
                throw new ServiceException(HttpStatus.BAD_REQUEST, "Order with id " + orderId + " is " + order.getOrderStatus());
            }
            if(!order.getOrderStatus().equals(OrderStates.READY)) {
                throw new ServiceException(HttpStatus.BAD_REQUEST, "Order with id " + orderId + " is not ready for delivery");
            }
            OrderDeliveryAgent existingMapping = orderDeliveryAgentRepository.findByOrderId(orderId);
            if (existingMapping != null) {
                existingMapping.setDeliveryAgentId(deliveryAgentId);
                orderDeliveryAgentRepository.save(existingMapping);
            } else {
                orderDeliveryAgentRepository.save(OrderDeliveryAgent.builder()
                        .order(order)
                        .deliveryAgentId(deliveryAgentId)
                        .isDeleted(false)
                        .isArchived(false)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
            return OrderDeliveryAgentMappingResponse.builder()
                    .orderId(orderId)
                    .deliveryAgentId(deliveryAgentId)
                    .build();
        } catch (ServiceException e) {
            log.error("Error while mapping order to delivery agent: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error while mapping order to delivery agent: {}", e.getMessage());
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    public List<Order> getAllOrdersMappedToDeliveryAgent(Long deliveryAgentId) {
        try {
            List<OrderDeliveryAgent> orderDeliveryAgents = orderDeliveryAgentRepository.findAllByDeliveryAgentId(deliveryAgentId);
            if (orderDeliveryAgents.isEmpty()) {
                throw new ServiceException(HttpStatus.BAD_REQUEST, "No orders found for delivery agent: " + deliveryAgentId);
            }
            List<Order> orders = new ArrayList<>();
            for (OrderDeliveryAgent orderDeliveryAgent : orderDeliveryAgents) {
                orders.add(orderDeliveryAgent.getOrder());
            }
            return orders;
        } catch (ServiceException e) {
            log.error("Error while fetching orders mapped to delivery agent: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error while fetching orders mapped to delivery agent: {}", e.getMessage());
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }
}
