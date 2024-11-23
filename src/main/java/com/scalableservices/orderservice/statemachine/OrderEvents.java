package com.scalableservices.orderservice.statemachine;

public enum OrderEvents {
    CONFIRM, START_PREPARING, MARK_READY, ACCEPT_DELIVERY, START_DELIVERY, COMPLETE_DELIVERY, CANCEL
}
