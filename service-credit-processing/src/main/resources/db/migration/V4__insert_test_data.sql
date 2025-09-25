INSERT INTO account_processing.accounts
(id, client_id, product_id, balance, interest_rate, is_recalc, card_exist, status)
VALUES
    (1, 1, 1, 1000.00, 5.0, FALSE, TRUE, 'ACTIVE'),
    (2, 2, 2, 500.00, 3.5, FALSE, FALSE, 'ACTIVE')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO credit_processing.product_registry (id, client_id, account_id, product_id, interest_rate, open_date)
VALUES
    (1, 1, 1, 1, 12.5, '2024-01-01 10:00:00'),
    (2, 2, 2, 2, 10.0, '2024-06-01 09:00:00')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO credit_processing.payment_registry (id, product_registry_id, payment_date, amount, interest_rate_amount, debt_amount, expired, payment_expiration_date)
VALUES
    (1, 1, '2024-02-01 12:00:00', 1000.00, 125.00, 9000.00, FALSE, '2024-02-28 23:59:59'),
    (2, 1, '2024-03-01 12:00:00', 1000.00, 115.00, 8000.00, FALSE, '2024-03-31 23:59:59'),
    (3, 2, '2024-07-01 12:00:00', 500.00, 50.00, 4500.00, FALSE, '2024-07-31 23:59:59')
    ON CONFLICT (id) DO NOTHING;
