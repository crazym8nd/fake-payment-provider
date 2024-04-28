CREATE TABLE IF NOT EXISTS merchants (
                                        id SERIAL PRIMARY KEY,
                                        secret_key VARCHAR(256) NOT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        created_by VARCHAR(256) NOT NULL,
                                        updated_by VARCHAR(256) NOT NULL,
                                        status VARCHAR(256) DEFAULT 'IN_PROGRESS' NOT NULL
);
CREATE TABLE IF NOT EXISTS accounts (
                                        id SERIAL PRIMARY KEY,
                                        merchant_id BIGINT NOT NULL,
                                        currency VARCHAR(55) NOT NULL,
                                        amount BIGINT NOT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        created_by VARCHAR(256) NOT NULL,
                                        updated_by VARCHAR(256) NOT NULL,
                                        status VARCHAR(256) DEFAULT 'IN_PROGRESS' NOT NULL
);
CREATE TABLE IF NOT EXISTS cards (
                                     id SERIAL PRIMARY KEY,
                                     card_number VARCHAR(16) NOT NULL,
                                     expiration_date VARCHAR(5) NOT NULL,
                                     cvv VARCHAR(3) NOT NULL,
                                     customer_id BIGINT NOT NULL,
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
                                         card_id BIGINT NOT NULL,
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         created_by VARCHAR(256) NOT NULL,
                                         updated_by VARCHAR(256) NOT NULL,
                                         status VARCHAR(256) DEFAULT 'IN_PROGRESS' NOT NULL
);
CREATE TABLE IF NOT EXISTS transactions (
                                            id VARCHAR(36) PRIMARY KEY,
                                            payment_method VARCHAR(25) NOT NULL,
                                            amount BIGINT NOT NULL,
                                            currency VARCHAR(25) NOT NULL,
                                            language VARCHAR(25) NOT NULL,
                                            notification_url VARCHAR(256) NOT NULL,
                                            account_id BIGINT NOT NULL,
                                            card_id BIGINT NOT NULL,
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
ALTER TABLE accounts ADD CONSTRAINT accounts_merchant_id_fk
                                    FOREIGN KEY (merchant_id)
                                    REFERENCES merchants(id)
;
ALTER TABLE cards ADD CONSTRAINT cards_customer_id_fk
                                FOREIGN KEY (customer_id)
                                 REFERENCES customers(id)
;
ALTER TABLE transactions ADD CONSTRAINT transactions_account_id_fk
                                        FOREIGN KEY (account_id)
                                        REFERENCES accounts(id),
                         ADD CONSTRAINT transactions_card_id_fk
                                        FOREIGN KEY (card_id)
                                        REFERENCES cards(id)
;
