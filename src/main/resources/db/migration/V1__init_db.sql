CREATE TABLE IF NOT EXISTS merchants (
                                        merchant_id VARCHAR(255) PRIMARY KEY,
                                        secret_key VARCHAR(256) NOT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        created_by VARCHAR(256) NOT NULL,
                                        updated_by VARCHAR(256) NOT NULL,
                                        status VARCHAR(256) DEFAULT 'ACTIVE' NOT NULL
);
CREATE TABLE IF NOT EXISTS accounts (
                                        id SERIAL PRIMARY KEY,
                                        merchant_id VARCHAR(255),
                                        currency VARCHAR(55) NOT NULL,
                                        amount BIGINT NOT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        created_by VARCHAR(256) NOT NULL,
                                        updated_by VARCHAR(256) NOT NULL,
                                        status VARCHAR(256) DEFAULT 'ACTIVE' NOT NULL
);
CREATE TABLE IF NOT EXISTS cards (
                                     card_number VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE,
                                     exp_date VARCHAR(25) NOT NULL,
                                     cvv VARCHAR(3) NOT NULL,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     created_by VARCHAR(256) NOT NULL,
                                     updated_by VARCHAR(256) NOT NULL,
                                     status VARCHAR(256) DEFAULT 'IN_PROGRESS' NOT NULL
);
CREATE TABLE IF NOT EXISTS customers (
                                         id SERIAL PRIMARY KEY,
                                         first_name VARCHAR(256) NOT NULL,
                                         last_name VARCHAR(256) NOT NULL,
                                         country VARCHAR(256) UNIQUE NOT NULL,
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         created_by VARCHAR(256) NOT NULL,
                                         updated_by VARCHAR(256) NOT NULL,
                                         status VARCHAR(256) DEFAULT 'ACTIVE' NOT NULL
);
CREATE TABLE IF NOT EXISTS transactions (
                                            transaction_id UUID PRIMARY KEY,
                                            payment_method VARCHAR(25) NOT NULL,
                                            amount BIGINT NOT NULL,
                                            currency VARCHAR(25) NOT NULL,
                                            language VARCHAR(25) NOT NULL,
                                            notification_url VARCHAR(256) NOT NULL,
                                            card_number VARCHAR(16) NOT NULL,
                                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            created_by VARCHAR(256) NOT NULL,
                                            updated_by VARCHAR(256) NOT NULL,
                                            status VARCHAR(256) DEFAULT 'IN_PROGRESS' NOT NULL
);
CREATE TABLE IF NOT EXISTS webhooks (
                                        id VARCHAR(36) PRIMARY KEY,
                                        transaction_attempt BIGINT NOT NULL,
                                        url_request VARCHAR(256) NOT NULL,
                                        body_request VARCHAR(256) NOT NULL,
                                        message VARCHAR(256) NOT NULL,
                                        body_response VARCHAR(256) NOT NULL,
                                        status_response VARCHAR(256) NOT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        created_by VARCHAR(256) NOT NULL,
                                        updated_by VARCHAR(256) NOT NULL,
                                        status VARCHAR(256) DEFAULT 'IN_PROGRESS' NOT NULL
);
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
ALTER TABLE accounts ADD CONSTRAINT accounts_merchant_id_fk
                                    FOREIGN KEY (merchant_id)
                                    REFERENCES merchants(merchant_id)
;

ALTER TABLE transactions ALTER COLUMN transaction_id SET DEFAULT uuid_generate_v4();
;
