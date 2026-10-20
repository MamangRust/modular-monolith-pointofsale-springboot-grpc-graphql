CREATE ALIAS IF NOT EXISTS MAKE_DATE AS $$
java.sql.Date makeDate(int year, int month, int day) {
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.clear();
    calendar.set(year, month - 1, day);
    return new java.sql.Date(calendar.getTimeInMillis());
}
$$;

-- Cross-module tables referenced by the category statistics native queries.
-- These tables are owned by other modules (product/order/order-item); the
-- category module only reads them, so the test tree creates them explicitly
-- and seeds them via JdbcTemplate.
CREATE TABLE IF NOT EXISTS products (
    product_id BIGINT PRIMARY KEY,
    merchant_id BIGINT,
    category_id BIGINT,
    name VARCHAR(255),
    description VARCHAR(255),
    price INT,
    count_in_stock INT,
    brand VARCHAR(255),
    weight INT,
    slug_product VARCHAR(255),
    image_product VARCHAR(255),
    barcode VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    order_id BIGINT PRIMARY KEY,
    cashier_id BIGINT,
    merchant_id BIGINT,
    total_price BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id BIGINT PRIMARY KEY,
    order_id BIGINT,
    product_id BIGINT,
    quantity INT,
    price INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
