-- Seeding Roles
MERGE INTO roles (role_name) KEY (role_name) VALUES ('ROLE_ADMIN');

MERGE INTO roles (role_name) KEY (role_name) VALUES ('ROLE_USER');

-- Seeding Default User for tests
MERGE INTO users (
    user_id,
    username,
    email,
    password,
    firstname,
    lastname
) KEY (user_id)
VALUES (
        1,
        'admin',
        'admin@example.com',
        '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOnu',
        'Admin',
        'User'
    );

MERGE INTO users (
    user_id,
    username,
    email,
    password,
    firstname,
    lastname
) KEY (user_id)
VALUES (
        2,
        'user',
        'user@example.com',
        '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOnu',
        'Regular',
        'User'
    );

-- Seeding Default Card and Saldo for WithdrawControllerTest
MERGE INTO cards (
    card_id,
    user_id,
    card_number,
    card_type,
    expire_date,
    cvv,
    card_provider
) KEY (card_number)
VALUES (
        1,
        1,
        '1234567890123456',
        'CREDIT',
        '2030-12-31',
        '123',
        'VISA'
    );

MERGE INTO saldos (
    saldo_id,
    card_number,
    total_balance
) KEY (card_number)
VALUES (
        1,
        '1234567890123456',
        1000000
    );