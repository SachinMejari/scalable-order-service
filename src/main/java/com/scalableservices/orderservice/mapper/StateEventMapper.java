package com.scalableservices.orderservice.mapper;

import com.scalableservices.orderservice.statemachine.OrderEvents;
import com.scalableservices.orderservice.statemachine.OrderStates;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StateEventMapper {

    private static final Map<OrderStates, OrderEvents> stateEventMap = new HashMap<>();

    static {
        stateEventMap.put(OrderStates.CONFIRMED, OrderEvents.CONFIRM);
        stateEventMap.put(OrderStates.PREPARING, OrderEvents.START_PREPARING);
        stateEventMap.put(OrderStates.READY, OrderEvents.MARK_READY);
        stateEventMap.put(OrderStates.ACCEPTED_BY_DELIVERY_AGENT, OrderEvents.ACCEPT_DELIVERY);
        stateEventMap.put(OrderStates.OUT_FOR_DELIVERY, OrderEvents.START_DELIVERY);
        stateEventMap.put(OrderStates.DELIVERED, OrderEvents.COMPLETE_DELIVERY);
        stateEventMap.put(OrderStates.CANCELLED, OrderEvents.CANCEL);
    }

    public static OrderEvents getEventForState(OrderStates state) {
        return stateEventMap.get(state);
    }
}
