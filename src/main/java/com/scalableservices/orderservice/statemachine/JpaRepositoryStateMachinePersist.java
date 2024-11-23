package com.scalableservices.orderservice.statemachine;

import com.scalableservices.orderservice.mapper.StateEventMapper;
import com.scalableservices.orderservice.model.StateMachineEntity;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Repository
public class JpaRepositoryStateMachinePersist implements StateMachinePersist<OrderStates, OrderEvents, String> {

    private final EntityManager entityManager;

    // Constructor to inject the EntityManager
    public JpaRepositoryStateMachinePersist(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void write(StateMachineContext<OrderStates, OrderEvents> context, String contextObj) throws Exception {
        // Write the state machine context to the database
        StateMachineEntity stateMachineEntity = entityManager.find(StateMachineEntity.class, contextObj);
        if (stateMachineEntity == null) {
            stateMachineEntity = new StateMachineEntity();
            stateMachineEntity.setId(contextObj); // Set ID as the contextObj (usually a String identifier)
        }

        // Set the state
        stateMachineEntity.setState(context.getState().toString());

        // Check if event is null before calling toString
        if (context.getEvent() != null) {
            stateMachineEntity.setEvent(context.getEvent().toString());
        } else {
            // Handle the case where event is null (optional, based on your use case)
            stateMachineEntity.setEvent(StateEventMapper.getEventForState(context.getState()).toString());
        }

        // Persist the entity
        entityManager.persist(stateMachineEntity);
    }

    @Override
    public StateMachineContext<OrderStates, OrderEvents> read(String contextObj) throws Exception {
        // Read the state machine context from the database
        // Query the state machine entity using the provided contextObj
        StateMachineEntity stateMachineEntity = entityManager.find(StateMachineEntity.class, contextObj);
        if (stateMachineEntity == null) {
            return null; // Return null if the state machine context is not found
        }

        // Use DefaultStateMachineContext with explicit type arguments
        return new DefaultStateMachineContext<>(
                OrderStates.valueOf(stateMachineEntity.getState()), // Convert the state String to OrderStates enum
                OrderEvents.valueOf(stateMachineEntity.getEvent()), // Convert the event String to OrderEvents enum
                null, // Set the event
                new DefaultExtendedState() // Use DefaultExtendedState
        );
    }
}
