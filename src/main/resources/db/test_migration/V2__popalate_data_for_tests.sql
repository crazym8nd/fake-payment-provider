INSERT INTO public.merchants (merchant_id, secret_key, created_at, updated_at, created_by, updated_by, status)
VALUES ('PROSELYTE','$2a$10$O4emg0ZIpUmPkzdMfd2pwuIht8aPgZsGkp.CspB/Ztisqnpm/LneC', now(), now(), 'SYSTEM', 'SYSTEM', 'ACTIVE'),
('TestMerchantID','$2a$10$IxqpZljDF4VBVqhetF/brOXfPhs7UWNoXjc67Mt1X8LeH8LvJUyiS', now(), now(), 'SYSTEM', 'SYSTEM', 'ACTIVE');

INSERT INTO public.transactions (transaction_id, payment_method, amount, currency, language, notification_url,
                                 card_number, transaction_type, created_at, updated_at, created_by, updated_by, status)
VALUES ('b192e414-0d12-4129-9747-4fead5e8b6df', 'CARD', 1000, 'USD', 'en', 'https://test-webhook.free.beeceptor.com',
        '4444555566668888', 'TOPUP', now(), now(), 'SYSTEM', 'SYSTEM',
        'IN_PROGRESS');

INSERT INTO public.webhooks (id, transaction_id, transaction_attempt, url_request, body_request, message, body_response,
                             status_response, created_at, updated_at, created_by, updated_by, status)
VALUES (DEFAULT, 'b192e414-0d12-4129-9747-4fead5e8b6df', 0, 'https://test-webhook.free.beeceptor.com', 'SOME_RESPONSE',
        'SOME_MESSAGE', 'SOME_BODY_RESPONSE', '200 OK', now(), now(),
        'SYSTEM', 'SYSTEM', 'IN_PROGRESS');

INSERT INTO public.customers (card_number, first_name, last_name, country, created_at, updated_at, created_by,
                              updated_by, status)
VALUES ('4444555566668888', 'FirstNameFromSQLScriptTest', 'LastNameFromSQLScriptTest', 'CN',
        now(), now(), 'SYSTEM', 'SYSTEM', 'ACTIVE');

INSERT INTO public.cards (card_number, exp_date, cvv, created_at, updated_at, created_by, updated_by, status)
VALUES ('4444555566668888', '10/24', '999', now(), now(), 'SYSTEM',
        'SYSTEM', 'ACTIVE');

INSERT INTO public.accounts (id, merchant_id, currency, amount, created_at, updated_at, created_by, updated_by, status)
VALUES (DEFAULT, 'TestMerchantID', 'USD', 1000, now(), now(), 'SYSTEM',
        'SYSTEM', 'ACTIVE');

