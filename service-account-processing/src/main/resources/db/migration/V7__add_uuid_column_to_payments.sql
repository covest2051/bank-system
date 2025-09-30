ALTER TABLE payments
    ADD COLUMN uuid UUID DEFAULT gen_random_uuid();;