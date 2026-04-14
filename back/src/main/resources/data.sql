-- =====================================================
-- PERSON and CLIENT Test Data
-- =====================================================

INSERT INTO person (id, person_type, name, gender, age, identification, address, phone) VALUES
(1, 'CLI', 'Juan Carlos Pérez', 'MALE', 35, '1234567890', 'Av. Principal 123, Quito', '0987654321'),
(2, 'CLI', 'María Elena García López', 'FEMALE', 28, '1234567891', 'Calle Secundaria 456, Guayaquil', '0987654322'),
(3, 'CLI', 'Roberto Andrés Mendoza Ruiz', 'MALE', 42, '1234567892', 'Avenida Central 789, Cuenca', '0987654323');

INSERT INTO client (id, password, active) VALUES
(1, '123:abc123def456ghi789jkl012mno345pqr678stu901vwx234yz', true),
(2, '456:xyz789abc012def345ghi678jkl901mno234pqr567stu890vwx', true),
(3, '789:pqr012stu345vwx678yz901abc234def567ghi890jkl123mno', true);

-- =====================================================
-- ACCOUNT Test Data (Account numbers auto-generated)
-- =====================================================

INSERT INTO account (id, number, type, initial_balance, current_balance, active, client_id) VALUES
(1, '100001', 'CHECKING', 5000.00, 5500.00, true, 1),
(2, '100002', 'SAVINGS', 10000.00, 10500.00, true, 1),
(3, '100003', 'CHECKING', 3500.50, 4000.50, true, 2),
(4, '100004', 'SAVINGS', 25000.00, 27000.00, true, 2),
(5, '100005', 'CHECKING', 7200.75, 7600.75, true, 3),
(6, '100006', 'SAVINGS', 50000.00, 55000.00, true, 3);

-- =====================================================
-- MOVEMENT Test Data
-- =====================================================

-- Movements for Account 1 (Juan's Checking)
INSERT INTO movement (id, date, type, amount, balance, active, account_id) VALUES
(1, '2024-04-01 10:30:00', 'DEPOSIT', 5000.00, 5000.00, true, 1),
(2, '2024-04-05 14:15:00', 'WITHDRAW', -500.00, 4500.00, true, 1),
(3, '2024-04-10 09:45:00', 'DEPOSIT', 1000.00, 5500.00, true, 1);

-- Movements for Account 2 (Juan's Savings)
INSERT INTO movement (id, date, type, amount, balance, active, account_id) VALUES
(4, '2024-04-02 11:20:00', 'DEPOSIT', 10000.00, 10000.00, true, 2),
(5, '2024-04-08 15:30:00', 'DEPOSIT', 2000.00, 12000.00, true, 2),
(6, '2024-04-12 16:45:00', 'WITHDRAW', -1500.00, 10500.00, true, 2);

-- Movements for Account 3 (María's Checking)
INSERT INTO movement (id, date, type, amount, balance, active, account_id) VALUES
(7, '2024-03-28 08:00:00', 'DEPOSIT', 3500.50, 3500.50, true, 3),
(8, '2024-04-03 13:20:00', 'WITHDRAW', -250.00, 3250.50, true, 3),
(9, '2024-04-11 10:15:00', 'DEPOSIT', 750.00, 4000.50, true, 3);

-- Movements for Account 4 (María's Savings)
INSERT INTO movement (id, date, type, amount, balance, active, account_id) VALUES
(10, '2024-03-25 09:30:00', 'DEPOSIT', 25000.00, 25000.00, true, 4),
(11, '2024-04-06 14:45:00', 'DEPOSIT', 5000.00, 30000.00, true, 4),
(12, '2024-04-09 11:00:00', 'WITHDRAW', -3000.00, 27000.00, true, 4);

-- Movements for Account 5 (Roberto's Checking)
INSERT INTO movement (id, date, type, amount, balance, active, account_id) VALUES
(13, '2024-03-30 10:00:00', 'DEPOSIT', 7200.75, 7200.75, true, 5),
(14, '2024-04-04 12:30:00', 'WITHDRAW', -800.00, 6400.75, true, 5),
(15, '2024-04-13 09:15:00', 'DEPOSIT', 1200.00, 7600.75, true, 5);

-- Movements for Account 6 (Roberto's Savings)
INSERT INTO movement (id, date, type, amount, balance, active, account_id) VALUES
(16, '2024-03-20 07:45:00', 'DEPOSIT', 50000.00, 50000.00, true, 6),
(17, '2024-04-07 15:20:00', 'DEPOSIT', 10000.00, 60000.00, true, 6),
(18, '2024-04-11 14:30:00', 'WITHDRAW', -5000.00, 55000.00, true, 6);

-- =====================================================
-- Reset Sequences (for next inserts)
-- =====================================================
ALTER SEQUENCE person_seq RESTART WITH 4;
ALTER SEQUENCE account_seq RESTART WITH 7;
ALTER SEQUENCE movement_seq RESTART WITH 19;

