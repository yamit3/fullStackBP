DROP TABLE IF EXISTS movement;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS person;

DROP SEQUENCE IF EXISTS movement_seq;
DROP SEQUENCE IF EXISTS account_seq;
DROP SEQUENCE IF EXISTS person_seq;

CREATE SEQUENCE person_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE account_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE movement_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE person (
    id BIGINT NOT NULL,
    person_type VARCHAR(31) NOT NULL,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    age INTEGER NOT NULL,
    identification VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    CONSTRAINT pk_person PRIMARY KEY (id),
    CONSTRAINT uk_person_identification UNIQUE (identification),
    CONSTRAINT chk_person_gender CHECK (gender IN ('MALE', 'FEMALE'))
);

CREATE TABLE client (
    id BIGINT NOT NULL,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT pk_client PRIMARY KEY (id),
    CONSTRAINT fk_client_person FOREIGN KEY (id) REFERENCES person (id)
);

CREATE TABLE account (
    id BIGINT NOT NULL,
    number VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    initial_balance NUMERIC(19, 2) NOT NULL,
    active BOOLEAN NOT NULL,
    client_id BIGINT NOT NULL,
    CONSTRAINT pk_account PRIMARY KEY (id),
    CONSTRAINT uk_account_number UNIQUE (number),
    CONSTRAINT fk_account_client FOREIGN KEY (client_id) REFERENCES client (id),
    CONSTRAINT chk_account_type CHECK (type IN ('SAVINGS', 'CHECKING'))
);

CREATE TABLE movement (
    id BIGINT NOT NULL,
    date TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL,
    account_id BIGINT NOT NULL,
    CONSTRAINT pk_movement PRIMARY KEY (id),
    CONSTRAINT fk_movement_account FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT chk_movement_type CHECK (type IN ('DEPOSIT', 'WITHDRAW'))
);

