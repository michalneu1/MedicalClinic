### Zadanie 1 (12 min). Interfejs zamiast listy w pamięci

Napisz `PatientRepository`: interfejs w pakiecie `repository`, który dziedziczy po
`JpaRepository<Patient, Long>` i deklaruje jedną metodę: `findByEmail`. Przełącz
`PatientService` na to repozytorium i skasuj `InMemoryPatientRepository`. Metoda
`deleteById` z `JpaRepository` nic nie zwraca, więc sprawdź osobno, czy taki pacjent
istnieje.

Kryteria akceptacji:
- [ ] W projekcie nie ma już klasy `InMemoryPatientRepository`.
- [ ] `mvn clean package` kończy się na `BUILD SUCCESS`.
- [ ] `GET /patients` zwraca pacjentów z `data.sql`, a nie pustą tablicę.
- [ ] W logu aplikacji widać `Hibernate: select ... from patients ...`.
- [ ] `DELETE /patients/999` zwraca 404, a nie 204.

### Zadanie 2 (10 min). Zapis, o którym łatwo zapomnieć

Zmień nazwisko pacjenta przez `PUT /patients/1` i sprawdź w `psql`, czy zmiana jest
w tabeli. Jeżeli nie ma, popraw serwis tak, żeby była.

Kryteria akceptacji:
- [ ] Po `PUT` w tabeli `patients` jest nowe nazwisko, nie stare.
- [ ] W logu widać `Hibernate: update patients ...`.
- [ ] To samo działa dla zmiany hasła (`PATCH /patients/1/password`).
- [ ] Umiesz powiedzieć, czemu bez wywołania `save` zmiana ginie.

### Zadanie 3 (8 min). Kolekcja Bruno na trwałej bazie

Przejdź kolekcję kliniki i zobacz, co zmieniło się teraz, gdy dane nie znikają razem
z aplikacją. Uwaga na dwie rzeczy, które zachowują się inaczej niż tydzień temu:

1. Żądanie „Utwórz pacjenta" wysyła e-mail i numer dowodu, które są już w `data.sql`,
   więc dostaniesz 409 zamiast 201. Zmień w nim **oba** te pola (e-mail i `idCardNo`),
   zanim wyślesz.
2. Żądanie „Usuń pacjenta" kasuje wiersz na stałe. Po nim żądania po `id` dają 404, a po
   `docker compose down -v` i restarcie dane wracają z nowymi identyfikatorami, bo licznik
   klucza idzie dalej.

Kryteria akceptacji:
- [ ] `GET /patients` → 200, `GET /patients/{id}` → 200, `GET /patients?email=...` → 200.
- [ ] Twoje poprawione „Utwórz pacjenta" → 201, a wysłane drugi raz → 409.
- [ ] Żądania z celowym błędem nadal dają 400 i 404, tak jak w lekcji 14.
- [ ] Umiesz powiedzieć, czemu po `DELETE` żądanie po tym samym `id` daje teraz 404
  także po restarcie aplikacji.

## Jak sprawdzisz, że skończyłeś

- `GET /patients` → 200 i pacjenci z bazy
- log aplikacji → `Hibernate: select ... from patients`
- `PUT /patients/1` → 200, a w `psql` nowe nazwisko
- `DELETE /patients/999` → 404