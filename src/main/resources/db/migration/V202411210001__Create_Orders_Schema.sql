CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    customer_address VARCHAR(255) NOT NULL,
    restaurant_id BIGINT NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    order_details JSON NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    is_archived BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_customer_orders_customer_id` (`customer_id`),
    KEY `idx_customer_orders_restaurant_id` (`restaurant_id`)
);

CREATE TABLE order_logs (
    id BIGINT AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    remarks VARCHAR(500) NOT NULL,
    entered_by VARCHAR(50) NOT NULL,
    entered_by_id BIGINT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    is_archived BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_logs_order_id` (`order_id`),
    KEY `idx_order_logs_entered_by_id` (`entered_by_id`),
    CONSTRAINT `fk_order_logs_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
);

CREATE TABLE order_delivery_agents (
    id BIGINT AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    delivery_agent_id BIGINT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    is_archived BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_delivery_agents_order_id` (`order_id`),
    KEY `idx_order_delivery_agents_delivery_agent_id` (`delivery_agent_id`),
    CONSTRAINT `fk_order_delivery_agents_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
);