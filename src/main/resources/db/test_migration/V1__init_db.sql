CREATE TABLE IF NOT EXISTS merchants (
                                        merchant_id VARCHAR(255) PRIMARY KEY,
                                        secret_key VARCHAR(256) NOT NULL,
                                        created_at TIMESTAMP,
                                        updated_at TIMESTAMP,
                                        created_by VARCHAR(256) NOT NULL,
                                        updated_by VARCHAR(256),
                                        status VARCHAR(256) NOT NULL
);
CREATE TABLE IF NOT EXISTS accounts (
                                        id SERIAL PRIMARY KEY,
                                        merchant_id VARCHAR(255),
                                        currency VARCHAR(55) NOT NULL,
                                        amount BIGINT NOT NULL,
                                        created_at TIMESTAMP,
                                        updated_at TIMESTAMP,
                                        created_by VARCHAR(256) NOT NULL,
                                        updated_by VARCHAR(256),
                                        status VARCHAR(256)NOT NULL
);
CREATE TABLE IF NOT EXISTS cards (
                                     card_number VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE,
                                     exp_date VARCHAR(25) NOT NULL,
                                     cvv VARCHAR(3) NOT NULL,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP,
                                     created_by VARCHAR(256) NOT NULL,
                                     updated_by VARCHAR(256),
                                     status VARCHAR(256) NOT NULL
);
CREATE TABLE IF NOT EXISTS customers (
                                         card_number VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE,
                                         first_name VARCHAR(256) NOT NULL,
                                         last_name VARCHAR(256) NOT NULL,
                                         country VARCHAR(256) NOT NULL,
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP,
                                         created_by VARCHAR(256) NOT NULL,
                                         updated_by VARCHAR(256),
                                         status VARCHAR(256) NOT NULL
);
CREATE TABLE IF NOT EXISTS transactions (
                                            transaction_id UUID PRIMARY KEY,
                                            payment_method VARCHAR(25) NOT NULL,
                                            amount BIGINT NOT NULL,
                                            currency VARCHAR(25) NOT NULL,
                                            language VARCHAR(25) NOT NULL,
                                            notification_url VARCHAR(256) NOT NULL,
                                            card_number VARCHAR(16) NOT NULL,
                                            account_id BIGINT NOT NULL,
                                            transaction_type VARCHAR(25) NOT NULL,
                                            created_at TIMESTAMP,
                                            updated_at TIMESTAMP,
                                            created_by VARCHAR(256) NOT NULL,
                                            updated_by VARCHAR(256),
                                            status VARCHAR(256) NOT NULL
);
CREATE TABLE IF NOT EXISTS webhooks (
                                        id SERIAL PRIMARY KEY ,
                                        transaction_id UUID NOT NULL,
                                        transaction_attempt BIGINT NOT NULL DEFAULT 0,
                                        url_request VARCHAR(256) NOT NULL,
                                        body_request TEXT NOT NULL,
                                        message VARCHAR(256),
                                        body_response VARCHAR(256),
                                        status_response VARCHAR(256),
                                        created_at TIMESTAMP,
                                        updated_at TIMESTAMP,
                                        created_by VARCHAR(256) NOT NULL,
                                        updated_by VARCHAR(256),
                                        status VARCHAR(256) NOT NULL
);
ALTER TABLE accounts ADD CONSTRAINT accounts_merchant_id_fk
                                    FOREIGN KEY (merchant_id)
                                    REFERENCES merchants(merchant_id)
;
ALTER TABLE accounts ADD CONSTRAINT accounts_unique_pair
                                    UNIQUE (merchant_id, currency)
;

