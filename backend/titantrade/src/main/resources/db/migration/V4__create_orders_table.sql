CREATE TABLE orders (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,
    stock_id UUID NOT NULL,

    trade_type VARCHAR(20) NOT NULL,
    order_status VARCHAR(20) NOT NULL,

    quantity NUMERIC(19,6) NOT NULL,
    requested_price NUMERIC(19,4) NOT NULL,
    execution_price NUMERIC(19,4),
    total_amount NUMERIC(19,4),

    rejection_reason VARCHAR(500),
    idempotency_key UUID NOT NULL,

    created_on TIMESTAMP NOT NULL,
    updated_on TIMESTAMP NOT NULL,
    executed_at TIMESTAMP,

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_orders_stock
        FOREIGN KEY (stock_id)
        REFERENCES stocks(id),

    CONSTRAINT uk_order_user_idempotency
        UNIQUE (user_id, idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_orders_user_created_on
ON orders (user_id, created_on DESC);

CREATE INDEX IF NOT EXISTS idx_orders_user_status_created_on
ON orders (user_id, order_status, created_on DESC);

CREATE INDEX IF NOT EXISTS idx_orders_user_stock_created_on
ON orders (user_id, stock_id, created_on DESC);