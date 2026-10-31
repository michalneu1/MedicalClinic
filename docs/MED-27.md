# Encje i paginacja w medical-clinic: zadania z lekcji 33-34

| MED | Co robisz | Gdzie | Lekcja | Stan startowy |
|---|---|---|---|---|
| MED-27 | `equals`/`hashCode` encji + test strażnik | pakiet `model`, `PatientTest`, `DoctorTest` | L33 | `po-L32-pd` |
| MED-28 cz. 1 | stronicowanie `GET /visits` | `VisitService`, `VisitController`, `dto/PageResponse` | L34 | `po-L33-pd` |

Oznaczenia: **Z** = na zajęciach, **D** = praca domowa. Numer lekcji wskazuje PDF z teorią.
MED-28 cz. 2 (sortowanie z białą listą pól, testy) przychodzi w L35.

---

## MED-27: equals i hashCode encji

Strategia: dwa obiekty są równe tylko z tym samym, nadanym id; `hashCode` stały dla klasy.
`equals` przez `instanceof` (nie `getClass()`) i getter (`other.getId()`, nie pole).
Na encjach żadnego `@EqualsAndHashCode` ani `@Data`.

**L33 Z1. Obstaw i sprawdź** (`src/test/java/.../model/PatientTest.java`, na obecnej klasie `Patient`)
- Pacjent dodany do `HashSet` przed `setId(42L)` jest w nim po nadaniu id.
- Dwa obiekty `Patient` z tym samym id są równe.
- Przed uruchomieniem zapisz w komentarzu, który test padnie; potem wyjaśnij, czemu.

**L33 Z2.** `equals` i `hashCode` w `Patient` według strategii. Oba testy z Z1 zielone.

**L33 Z3.** Trzeci test: dwaj niezapisani pacjenci (bez id) z tym samym imieniem nie są równi.
Umiesz powiedzieć, co się stanie z testem z setem po zamianie `hashCode` na `Objects.hashCode(id)`.

**L33 D1. Wszystkie encje:** ta sama strategia w `User`, `Doctor`, `Facility` i `Visit`.
Testy z lekcji 30-32 nadal zielone.

**L33 D2. Test strażnik relacji:** `DoctorTest`
- Placówka dodana przez `addFacility` przed nadaniem id daje się usunąć przez `removeFacility`
  po nadaniu id (bez `getFacilities().add(...)`).
- Na końcu set placówek doktora jest pusty.

**L33 D3. Notatka** `docs/encje/equals-hashcode.md`, pięć punktów:
1. Jak `HashSet` używa `hashCode` i `equals` (wiadra) i jaki cichy warunek kontraktu łamią encje.
2. Trzy złe wersje (domyślna, po wszystkich polach, hash z id), każda ze scenariuszem porażki.
3. Nasza strategia: kod i test strażnik (ścieżki plików w repo).
4. Klucz biznesowy kontra UUID: kiedy co wygrywa; czy `idCardNo` nadaje się na klucz pacjenta.
5. `toString` encji: co zrobi wygenerowany `toString` przy relacjach `LAZY`.

Gotowe, gdy: `PatientTest` (3) i `DoctorTest` (1) zielone, `./mvnw test` → `BUILD SUCCESS`,
notatka w `docs/encje/`.

---

## MED-28 cz. 1: paginacja GET /visits

Przed startem: baza na czysto (`docker compose down -v`, `docker compose up -d --wait`) i aplikacja.

**L34 Z1. Plan** `docs/paginacja/plan-get-visits.md`, sześć odpowiedzi (pokaż prowadzącemu przed
kodowaniem):
1. Które endpointy stronicujesz, które zostają listami, i dlaczego.
2. Domyślny rozmiar i domyślne sortowanie.
3. Pola, po których klient może sortować.
4. `Page` czy `Slice`, jednym zdaniem.
5. Kształt odpowiedzi (pola widoczne dla klienta).
6. Jakie testy napiszesz w L35.

**L34 Z2. `GET /visits` z `Pageable`**
- `VisitService.findAll(Pageable)` z `@Transactional(readOnly = true)`, woła `visitRepository.findAll(pageable)`.
- Endpoint w `VisitController`: `@PageableDefault` według planu, zwraca `Page<VisitDto>`
  przez `Page.map(visitMapper::toDto)`.
- `GET /visits?doctorId=1` i `GET /visits/patient/{id}` działają jak wcześniej.

**L34 Z3. Sprawdź w logu:** dodaj kilkanaście wizyt doktora 1, wyślij `GET /visits?page=1&size=5`.
- Odpowiedź: 5 wizyt, pola `totalElements`, `totalPages`, `number`.
- W logu dwa zapytania: strona (`offset ... fetch first`) i `count`.
- Ostrzeżenie „Serializing PageImpl instances as-is is not supported": umiesz powiedzieć, skąd jest.

**L34 D1. Limit rozmiaru strony**
- `spring.data.web.pageable.max-page-size=100` w `src/main/resources/application.properties`
  i w `src/test/resources/application.properties`, obok komentarz, po co limit.
- `GET /visits?size=5000` oddaje stronę o rozmiarze 100.

**L34 D2. `PageResponse` zamiast surowego `Page`**
- Rekord `PageResponse<T>` w pakiecie `dto`: pola `content`, `page`, `size`, `totalElements`,
  `totalPages` i statyczne `from(Page<T>)`.
- `GET /visits` oddaje dokładnie te pięć pól, `content` z `VisitDto` (encje nie wychodzą).
- Ostrzeżenie o `PageImpl` znika z logu.

Gotowe, gdy:
- `GET /visits?page=1&size=5` → 200, `page` 1, `size` 5, pięć wizyt w `content`
- `GET /visits?size=2&sort=startsAt,desc` → 200, dwie najpóźniejsze wizyty
- `GET /visits?size=5000` → 200, `size` 100
- `./mvnw test` → `BUILD SUCCESS`