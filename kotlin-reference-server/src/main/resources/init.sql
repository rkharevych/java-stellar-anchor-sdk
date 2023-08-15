DROP TABLE IF EXISTS customer;
CREATE TABLE customer
(
    id                  VARCHAR(255),
    stellar_account     VARCHAR(255),
    memo                VARCHAR(255),
    memo_type           VARCHAR(255),
    first_name          VARCHAR(255),
    last_name           VARCHAR(255),
    email               VARCHAR(255),
    bank_account_number VARCHAR(255),
    bank_account_type   VARCHAR(255),
    bank_routing_number VARCHAR(255),
    clabe_number        VARCHAR(255),
    CONSTRAINT pk_customer PRIMARY KEY (id)
);

DROP TABLE IF EXISTS quote;
CREATE TABLE quote
(
    id VARCHAR(255),
    price VARCHAR(255),
    total_price VARCHAR(255),
    expires_at VARCHAR(255),
    created_at VARCHAR(255),
    sell_asset VARCHAR(255),
    sell_amount VARCHAR(255),
    sell_delivery_method VARCHAR(255),
    buy_asset VARCHAR(255),
    buy_amount VARCHAR(255),
    buy_delivery_method VARCHAR(255),
    country_code VARCHAR(255),
    client_id VARCHAR(255),
    transaction_id VARCHAR(255),
    fee VARCHAR(255)
)