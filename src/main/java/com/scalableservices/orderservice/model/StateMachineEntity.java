package com.scalableservices.orderservice.model;

import javax.persistence.*;

@Entity
@Table(name = "state_machine_entity")
public class StateMachineEntity {

    @Id
    private String id; // This would be the unique identifier (e.g., Order ID)

    private String state; // Store the current state
    private String event; // Store the current event

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
