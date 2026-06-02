CREATE TABLE dishes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    is_available BOOLEAN NOT NULL,
    restaurant_id BIGINT NOT NULL,
    CONSTRAINT fk_dishes_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);
