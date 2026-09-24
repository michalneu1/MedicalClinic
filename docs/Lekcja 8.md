# Lekcja 8. Pierwszy kontroler: praca domowa

## MED-03, część 2: dokończ CRUD pacjentów

### Zadanie 1. Pełna aktualizacja pacjenta
Dodaj `PUT /patients/{id}`. Body ma ten sam kształt co przy tworzeniu. Serwis dostaje
`id` i nowe dane, podmienia wszystkie pola istniejącego pacjenta i zwraca go; gdy
pacjenta nie ma, kontroler odpowiada 404.
Po restarcie lista jest pusta, więc zanim sprawdzisz kryteria zadań 1 i 2, wyślij `POST`
z Anną z ćwiczeń na zajęciach (dostanie `id` równe 1).

Kryteria akceptacji:
- [ ] `PUT /patients/1` z nowym nazwiskiem zwraca 200 i pacjenta z nowym nazwiskiem.
- [ ] `GET /patients/1` pokazuje zmienione dane.
- [ ] `PUT /patients/99` zwraca 404.

### Zadanie 2. Usuwanie pacjenta
Dodaj `DELETE /patients/{id}`. Gdy pacjent istniał i został usunięty, odpowiedź to 204
bez body. Gdy nie istniał, 404. Anna musi być na liście (patrz uwaga o restarcie w zadaniu 1). Repozytorium z lekcji 7 ma już metodę `deleteById(Long)`,
która zwraca `boolean` (czy coś usunięto); użyj jej w nowej metodzie serwisu.

Kryteria akceptacji:
- [ ] `DELETE /patients/1` zwraca 204, a `GET /patients` nie zawiera już Anny.
- [ ] Drugie `DELETE /patients/1` zwraca 404.

### Zadanie 3. Duplikat e-maila ma dostać 409
Serwis rzuca `PatientAlreadyExistsException`, gdy e-mail jest zajęty, a klient dostaje 500.
To nasz błąd, nie klienta. Oznacz klasę wyjątku adnotacją `@ResponseStatus(HttpStatus.CONFLICT)`.
Od tej chwili Spring sam zamienia ten wyjątek na 409. Docelową obsługę błędów zrobimy
w lekcji 14, dziś wystarczy ta jedna adnotacja.

Kryteria akceptacji:
- [ ] Drugi `POST /patients` z tym samym e-mailem zwraca 409.
- [ ] Pierwszy `POST` nadal zwraca 201.

### Zadanie 4. Kolekcja Bruno i data
- [ ] W folderze `patients` kolekcji są wszystkie żądania: lista, jeden po id, dodanie,
  pełna aktualizacja, usunięcie; każde wysłane i działające na środowisku `local`.
- [ ] Data urodzenia w odpowiedziach ma format ISO (`"1990-05-12"`), bez konfiguracji.
  Zanotuj w jednym zdaniu, dlaczego nie musiałeś nic ustawiać (rozdział 4 materiału
  do lekcji 8).
- [ ] Commit w Twoim repo z opisem zaczynającym się od `MED-03`.

Lektury z kompendium na następne zajęcia: „Spring Boot: Query Parameter vs Path Variable"
(Daryl Goh, medium.com) oraz tomaytotomato.com/overloading-rest-endpoints-on-spring-boot-2.
Zaczniemy od pytań do nich.


## Jak sprawdzisz, że skończyłeś
- `POST /patients` (nowy e-mail) → 201 · `POST /patients` (ten sam e-mail) → 409
- `PUT /patients/1` → 200 z nowymi danymi · `PUT /patients/99` → 404
- `DELETE /patients/1` → 204 · drugi raz → 404
- `GET /patients` → 200 i lista bez usuniętego pacjenta

## Podpowiedzi (czytaj dopiero, gdy utkniesz)
- Zadanie 1: metoda w serwisie może zwracać `Optional<Patient>` i użyć `map` na wyniku
  `findById`, żeby ustawić pola tylko wtedy, gdy pacjent istnieje. Kontroler mapuje `Optional`
  tak samo jak w `GET /patients/{id}`.
- Zadanie 2: `ResponseEntity<Void>` i wyrażenie warunkowe z `noContent()` albo `notFound()`.
  Repozytorium z lekcji 7 ma już `deleteById(Long)` zwracające `boolean`.
- Zadanie 3: adnotacja idzie na klasę wyjątku, nie na metodę kontrolera. Import
  z `org.springframework.web.bind.annotation`.
- Po każdej zmianie kodu zrestartuj aplikację; po restarcie lista jest pusta, więc zanim
  sprawdzisz PUT i DELETE, wyślij POST z Anną (dostanie `id` równe 1).