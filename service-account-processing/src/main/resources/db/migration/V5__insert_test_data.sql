INSERT INTO account_processing.accounts
(id, client_id, product_id, balance, interest_rate, is_recalc, card_exist, status)
VALUES
    (1, 1, 1, 1000.00, 5.0, FALSE, TRUE, 'ACTIVE'),
    (2, 2, 2, 500.00, 3.5, FALSE, FALSE, 'ACTIVE')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO account_processing.transactions (id, account_id, amount, timestamp, type, status)
VALUES
    (1, 1, 200.00, '2024-09-01 10:00:00', 'DEPOSIT', 'COMPLETED'),
    (2, 1, -50.00, '2024-09-05 15:30:00', 'WITHDRAWAL', 'COMPLETED'),
    (3, 2, 500.00, '2024-09-03 09:45:00', 'DEPOSIT', 'COMPLETED')
    ON CONFLICT (id) DO NOTHING;
