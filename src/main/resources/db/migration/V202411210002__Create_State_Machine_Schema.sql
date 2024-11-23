CREATE TABLE state_machine_entity (
    id VARCHAR(255) PRIMARY KEY,    -- Unique identifier (e.g., Order ID)
    state VARCHAR(255),             -- Current state (e.g., OrderStates enum)
    event VARCHAR(255)              -- Current event (e.g., OrderEvents enum)
);