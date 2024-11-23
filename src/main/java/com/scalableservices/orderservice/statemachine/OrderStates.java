package com.scalableservices.orderservice.statemachine;

public enum OrderStates {
    PLACED, CONFIRMED, PREPARING, READY, ACCEPTED_BY_DELIVERY_AGENT, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
}
