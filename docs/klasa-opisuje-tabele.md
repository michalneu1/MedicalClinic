### Zadanie 1 (12 min). Klasa opisuje tabelę

Zamień `Patient` w encję: `@Entity`, `@Table(name = "patients")`, `@Id`
z `@GeneratedValue(strategy = GenerationType.IDENTITY)` oraz `@Column(nullable = false)`
przy polach, które w `schema.sql` miały `NOT NULL`. Skasuj `src/main/resources/schema.sql`:
od tej chwili strukturę opisuje klasa. Do `application.properties` dopisz trzy wpisy:
generowanie schematu z klas, pokazywanie SQL-a w logu i przesunięcie `data.sql` na moment
po utworzeniu tabel.

Uwaga: przy tym uruchomieniu aplikacja **nie wstanie** i to jest poprawny stan tego
zadania. Tabela powstanie, a dlaczego start się nie udał, wyjaśnia zadanie 2.

Kryteria akceptacji:
- [ ] W projekcie nie ma pliku `schema.sql`.
- [ ] `mvn clean package` kończy się na `BUILD SUCCESS`.
- [ ] Po `docker compose down -v`, `docker compose up -d --wait` i próbie startu aplikacji
  w logu jest wiersz zaczynający się od `Hibernate: create table patients`.
- [ ] `docker compose exec postgres psql -U clinic -d clinic -c "\d patients"` pokazuje
  tabelę z kolumnami `id_card_no`, `first_name`, `last_name`, `phone_number`.

### Zadanie 2 (10 min). Ograniczenia, które zniknęły razem ze skryptem

Zajrzyj do `schema.sql` z poprzedniej lekcji: plik już skasowałeś, ale jest w historii
gita, więc wyświetl go przez `git show po-L18-pd:src/main/resources/schema.sql`. Sprawdź,
które ograniczenia unikalności tam były. Przepisz je do encji jako `unique = true`. Uruchom
aplikację i zobacz, czy dane startowe wchodzą.

Kryteria akceptacji:
- [ ] `\d patients` pokazuje dwa ograniczenia `UNIQUE CONSTRAINT`: na kolumnach
  `email` i `id_card_no`.
- [ ] Aplikacja startuje bez błędu, a `SELECT COUNT(*) FROM patients;` zwraca 3.
- [ ] Umiesz powiedzieć, czemu sama adnotacja nie wystarczyła i trzeba było skasować
  wolumen.