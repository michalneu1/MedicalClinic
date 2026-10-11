INSERT INTO patients (email, password, id_card_no, first_name, last_name, phone_number, birthday)
VALUES
('jan.kowalski@example.com', 'haslo123', 'ABC123456', 'Jan', 'Kowalski', '500100200', '1985-03-12'),
('anna.nowak@example.com',   'haslo123', 'DEF654321', 'Anna', 'Nowak',   '500300400', '1990-07-01'),
('piotr.zielinski@example.com', 'haslo123', 'GHI112233', 'Piotr', 'Zieliński', NULL, '1978-11-23')
    ON CONFLICT DO NOTHING;