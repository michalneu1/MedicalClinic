-- Passwords are BCrypt hashes. Demo passwords: jan.kowalski and piotr.zielinski: haslo123, anna.nowak: silne123
INSERT INTO users (email, password, role)
VALUES
('jan.kowalski@example.com',    '$2a$10$4vSmtV8afLa04EZiV80Lz.NUMHacZ/MRgPRaWUZjNC.jiD76m1yLG', 'PATIENT'),
('anna.nowak@example.com',      '$2a$10$9FEyH9A3xTSw615VKSnXGuHNFItMk/v/BaINHnt72scnCfoxSD9B6', 'PATIENT'),
('piotr.zielinski@example.com', '$2a$10$4vSmtV8afLa04EZiV80Lz.NUMHacZ/MRgPRaWUZjNC.jiD76m1yLG', 'PATIENT')
    ON CONFLICT DO NOTHING;

INSERT INTO patients (user_id, id_card_no, first_name, last_name, phone_number, birthday)
VALUES
((SELECT id FROM users WHERE email = 'jan.kowalski@example.com'),    'ABC123456', 'Jan',   'Kowalski',  '500100200', '1985-03-12'),
((SELECT id FROM users WHERE email = 'anna.nowak@example.com'),      'DEF654321', 'Anna',  'Nowak',     '500300400', '1990-07-01'),
((SELECT id FROM users WHERE email = 'piotr.zielinski@example.com'), 'GHI112233', 'Piotr', 'Zieliński', NULL,        '1978-11-23')
    ON CONFLICT DO NOTHING;

-- Demo password: silne654
INSERT INTO users (email, password, role)
VALUES ('katarzyna.wrobel@example.com', '$2a$10$tmD2UgXKS3zxiZut4Qlq8.KurQi3cQqY6S9TslbAMpYAR7knbcu46', 'DOCTOR')
    ON CONFLICT DO NOTHING;

INSERT INTO doctors (user_id, first_name, last_name, specialization)
VALUES ((SELECT id FROM users WHERE email = 'katarzyna.wrobel@example.com'), 'Katarzyna', 'Wróbel', 'CARDIOLOGIST')
    ON CONFLICT DO NOTHING;

INSERT INTO facilities (name, city, zip_code, street, number)
VALUES
('Przychodnia Centrum',    'Kraków', '31-001', 'Rynek Główny',    '1'),
('Przychodnia Nowa Huta',  'Kraków', '31-900', 'Plac Centralny',  '1')
    ON CONFLICT DO NOTHING;

INSERT INTO patients (user_id, id_card_no, first_name, last_name)
VALUES
((SELECT id FROM users WHERE email = 'katarzyna.wrobel@example.com'), 'KLM222333', 'Katarzyna', 'Wróbel')
    ON CONFLICT (user_id) DO NOTHING;
