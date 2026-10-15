INSERT INTO users (email, password, role)
VALUES
('jan.kowalski@example.com',    'haslo123', 'PATIENT'),
('anna.nowak@example.com',      'haslo123', 'PATIENT'),
('piotr.zielinski@example.com', 'haslo123', 'PATIENT')
    ON CONFLICT DO NOTHING;

INSERT INTO patients (user_id, id_card_no, first_name, last_name, phone_number, birthday)
VALUES
((SELECT id FROM users WHERE email = 'jan.kowalski@example.com'),    'ABC123456', 'Jan',   'Kowalski',  '500100200', '1985-03-12'),
((SELECT id FROM users WHERE email = 'anna.nowak@example.com'),      'DEF654321', 'Anna',  'Nowak',     '500300400', '1990-07-01'),
((SELECT id FROM users WHERE email = 'piotr.zielinski@example.com'), 'GHI112233', 'Piotr', 'Zieliński', NULL,        '1978-11-23')
    ON CONFLICT DO NOTHING;

INSERT INTO users (email, password, role)
VALUES ('katarzyna.wrobel@example.com', 'haslo123', 'DOCTOR')
    ON CONFLICT DO NOTHING;

INSERT INTO doctors (user_id, first_name, last_name, specialization)
VALUES ((SELECT id FROM users WHERE email = 'katarzyna.wrobel@example.com'), 'Katarzyna', 'Wróbel', 'CARDIOLOGIST')
    ON CONFLICT DO NOTHING;

INSERT INTO facilities (name, city, zip_code, street, number)
VALUES
('Przychodnia Centrum',    'Kraków', '31-001', 'Rynek Główny',    '1'),
('Przychodnia Nowa Huta',  'Kraków', '31-900', 'Plac Centralny',  '1')
    ON CONFLICT DO NOTHING;
