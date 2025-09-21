INSERT INTO client_processing.users (id, login, password, email)
VALUES (1, 'ivan_petrov', 'password123', 'ivan.petrov@example.com'),
       (2, 'anna_ivanova', 'qwerty', 'anna.ivanova@example.com');

INSERT INTO client_processing.clients
(id, client_id, user_id, first_name, middle_name, last_name, date_of_birth, document_type, document_id, document_prefix, document_suffix)
VALUES (1, 'CL001', 1, 'Ivan', 'Petrovich', 'Petrov', '1985-03-12', 'PASSPORT', '123456789', null, null),
       (2, 'CL002', 2, 'Anna', 'Ivanovna', 'Ivanova', '1990-07-21', 'PASSPORT', '987654321', null, null);

INSERT INTO client_processing.products (id, name, key, create_date)
VALUES
    (1, 'Product A', 'PRDA', NOW()),
    (2, 'Product B', 'PRDB', NOW());
