CREATE TABLE orders.orders (
    id          VARCHAR(36)    PRIMARY KEY,
    id_account  VARCHAR(36)    NOT NULL,
    date        TIMESTAMP      NOT NULL,
    total       NUMERIC(12,2)  NOT NULL
);

CREATE INDEX idx_orders_id_account ON orders.orders (id_account);
