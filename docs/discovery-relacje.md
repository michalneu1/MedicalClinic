# Discovery: relacje dwukierunkowe

## 1. Rekurencja w JSON-ie

- **Znalezisko:** kontrolery zwracają tylko DTO, więc pętli nie ma. Na encjach zamknęłaby się przez `Doctor.facilities` ↔ `Facility.doctors` (to pole usunięte w sekcji 3).
- **Sprawdzenie:** `grep -rn 'import com.medicalclinic.model' src/main/java/com/medicalclinic/controller`
- **Zmiana:** usunąłem nieużywany import `Visit` z `VisitController`.

## 2. Lombok

- **Znalezisko:** `@Data` na encjach generuje `toString`/`hashCode` z relacji, co daje pętlę `User` → `Patient` → `User` i `StackOverflowError`.
- **Sprawdzenie:** `grep -rnE '^ *@(Data|ToString)' src/main/java`
- **Zmiana:** zamieniłem `@Data` na `@Getter @Setter`.

## 3. Strony odwrotne

- **Znalezisko:** żadne pole z `mappedBy` nie miało czytelnika. Kod tylko je zapisywał (`addFacility`, `user.setPatient`) albo pomijał w mapperze (`ignore`). Zapytania w repozytoriach idą po stronie właściciela (`findByUserEmail`, `findByDoctorId`, `existsByDoctorId`).
- **Sprawdzenie:** `grep -rnE 'getVisits|getDoctors|getPatient\(' src/main/java` nic nie znajduje.

| Pole | Kto je czyta | Decyzja |
|---|---|---|
| `Doctor.visits` | nikt | usunięte razem z `ignore` w `DoctorMapper` |
| `Facility.doctors` | nikt | usunięte razem z `ignore` w `FacilityMapper` i drugą linijką `addFacility`/`removeFacility` |
| `User.patient` | nikt (kaskada `ALL` i tak blokowana 409 w `UserService`) | usunięte razem z `ignore` w `UserMapper` i `user.setPatient` |

- **Zmiana:** relacje są teraz jednokierunkowe, więc `grep -rn 'mappedBy' src/main/java` nic nie znajduje i nie ma już stron, które mogłyby się rozjechać.

## 4. Kaskady

| Pole | Kaskada | Test | Wynik |
|---|---|---|---|
| `Patient.user` | PERSIST | `POST /patients` | 201, insert users → patients |
| `Doctor.user` | PERSIST, MERGE | `DELETE /doctors/1` | 204, konto i pacjentka zostają |
| — | — | `DELETE /users/{id}` | 409 z `UserService`, nic nie znika |

- **Sprawdzenie:** `SELECT count(*)` z `users`, `patients` i `doctors` przed i po żądaniu.

Pomiar na czystej bazie, po `POST /doctors/1/facilities/1`. Katarzyna (konto 4) jest doktorem 1 i pacjentką `KLM222333`.

| Żądanie | Status | `delete` z logu | users | patients | doctors | doctor_facility |
|---|---|---|---|---|---|---|
| przed | | | 4 | 4 | 1 | 1 |
| `DELETE /doctors/1` | 204 | `delete from doctor_facility`, `delete from doctors` | 4 | 4 | 0 | 0 |
| `DELETE /users/4` | 409 | brak | 4 | 4 | 0 | 0 |
