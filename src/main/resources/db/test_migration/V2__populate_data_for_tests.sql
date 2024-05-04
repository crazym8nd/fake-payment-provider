INSERT INTO public.customers (id, first_name, last_name, country, card_id, created_at, updated_at, created_by,
                              updated_by, status)
VALUES (1, 'Vitaly', 'Dyagilev', 'RUS', 1, '2024-05-03 13:26:30.000000', '2024-05-03 13:26:31.000000', 'SYSTEM',
        'SYSTEM', 'ACTIVE');

INSERT INTO public.cards (id, card_number, expiration_date, cvv, customer_id, created_at, updated_at, created_by,
                          updated_by, status)
VALUES (1, '4028017173446679', '2024-05-03', '525', 1, '2024-05-03 13:16:28.000000', '2024-05-03 13:16:29.000000',
        'SYSTEM', 'SYSTEM', 'ACTIVE');

INSERT INTO public.merchants (id, secret_key, created_at, updated_at, created_by, updated_by, status)
VALUES (DEFAULT, 'b2eeea3e27834b7499dd7e01143a23dd', '2024-05-03 13:14:36.000000', '2024-05-03 13:14:37.000000',
        'SYSTEM', 'SYSTEM', 'ACTIVE');

INSERT INTO public.accounts (id, merchant_id, currency, amount, created_at, updated_at, created_by, updated_by, status)
VALUES (1, 1, 'USD', 1, '2024-05-03 13:13:24.000000', '2024-05-03 13:13:26.000000', 'SYSTEM', 'SYSTEM', 'ACTIVE');

INSERT INTO public.transactions (uuid, payment_method, amount, currency, language, notification_url, account_id, card_id,
                                 created_at, updated_at, created_by, updated_by, status)
VALUES ('018f3dee-b6ac-72f3-913d-0cad8241e58d', 'CARD', 1000, 'USD', 'EN', 'https://proselyte.net/webhook/transaction',
        1, 1, '2024-05-03 13:11:02.000000', '2024-05-03 13:11:06.000000', 'SYSTEM', 'SYSTEM', 'ACTIVE');