CREATE TABLE orders.items (
    id          VARCHAR(36)    PRIMARY KEY,
    id_order    VARCHAR(36)    NOT NULL REFERENCES orders.orders (id) ON DELETE CASCADE,
    id_product  VARCHAR(36)    NOT NULL,
    quantity    INTEGER        NOT NULL CHECK (quantity > 0),
    price       NUMERIC(12,2)  NOT NULL,
    total       NUMERIC(12,2)  NOT NULL
);
