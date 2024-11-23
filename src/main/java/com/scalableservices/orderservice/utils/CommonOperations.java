package com.scalableservices.orderservice.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalableservices.orderservice.dto.order.OrderMenuDetails;
import com.scalableservices.orderservice.statemachine.OrderEvents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class CommonOperations {

    public boolean isEligibleOrderEvent(String role, OrderEvents orderEvent) {
        Set<OrderEvents> eligibleEvents;

        switch (role) {
            case "ADMIN":
                eligibleEvents = EnumSet.of(OrderEvents.CONFIRM, OrderEvents.START_PREPARING, OrderEvents.MARK_READY, OrderEvents.ACCEPT_DELIVERY, OrderEvents.START_DELIVERY, OrderEvents.COMPLETE_DELIVERY, OrderEvents.CANCEL);
                break;
            case "DELIVERY_AGENT":
                eligibleEvents = EnumSet.of(OrderEvents.ACCEPT_DELIVERY, OrderEvents.START_DELIVERY, OrderEvents.COMPLETE_DELIVERY);
                break;
            case "CUSTOMER":
                eligibleEvents = EnumSet.of(OrderEvents.CANCEL);
                break;
            case "RESTAURANT_OWNER":
                eligibleEvents = EnumSet.of(OrderEvents.CONFIRM, OrderEvents.START_PREPARING, OrderEvents.MARK_READY);
                break;
            default:
                eligibleEvents = EnumSet.noneOf(OrderEvents.class);
        }

        return eligibleEvents.contains(orderEvent);
    }

    public List<OrderMenuDetails> getOrderMenuDetails(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Check if the JSON string is wrapped in double quotes (escaped JSON)
            if (jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
                // Remove the starting and ending quotes and unescape the JSON string
                jsonString = jsonString.substring(1, jsonString.length() - 1).replace("\\\"", "\"");
            }

            // Parse the JSON array to a List<OrderMenuDetails>
            return objectMapper.readValue(jsonString, new TypeReference<List<OrderMenuDetails>>() {});
        } catch (Exception e) {
            log.error("Error in converting json to object: {}", e.getMessage());
            return null;
        }
    }

    public String getOrderMenuDetailsJson(List<OrderMenuDetails> orderMenuDetails) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(orderMenuDetails);
        } catch (Exception e) {
            log.error("Error in converting object to json: {}", e.getMessage());
            return null;
        }
    }

    public boolean isOrderEventValid(String orderStatus) {
        return EnumSet.allOf(OrderEvents.class)
                .stream()
                .anyMatch(state -> state.name().equals(orderStatus));
    }
}
