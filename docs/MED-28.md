# Sortowanie, fasada i logowanie w medical-clinic: zadania z lekcji 35-37

| MED | Co robisz | Gdzie | Lekcja | Stan startowy |
|---|---|---|---|---|
| MED-28 cz. 2 | biała lista pól sortowania, testy, Bruno | `VisitService`, `VisitServiceTest`, `bruno/wizyty-strony` | L35 | `po-L34-pd` |
| MED-29 | fasada rezerwacji z podsumowaniem | pakiet `facade`, `VisitController`, `VisitBookingFacadeTest`, `VisitControllerTest` | L36 | `po-L35-pd` |
| MED-30 cz. 1 | logi w serwisach wizyt i pacjentów, próg DEBUG | `VisitService`, `PatientService`, `application.properties` | L37 | `po-L36-pd` |
| MED-30 cz. 2 | logi w całej aplikacji, MDC, plik logu, audyt | pozostałe serwisy, `config/RequestIdFilter` | L37 | stan po MED-30 cz. 1 |

`export BASE=http://localhost:8080`.

---

## MED-28 cz. 2: biała lista sortowania

Przed startem: baza i aplikacja jak w L34, kilkanaście wizyt doktora 1 (pętla z L34). Lista pól
do sortowania pochodzi z `docs/paginacja/plan-get-visits.md`.

**L35.1. Co dziś przechodzi** (przed jakąkolwiek zmianą)
- Wyślij `GET /visits?sort=abrakadabra`, `sort=patient.user.password`, `sort=patient.user.pesel`
  i zapisz statusy w komentarzu w planie.
- Umiesz powiedzieć, czemu pole z hasłem przechodzi, a pole, którego nie ma, daje 500, i czemu
  500 za literówkę to wina serwera, a nie klienta.

**L35.2. Biała lista w serwisie**
- Wyjątek `InvalidSortPropertyException` (dziedziczy po `MedicalClinicException`, status 400).
- `VisitService.findAll` sprawdza każde pole z `pageable.getSort()`, zanim zawoła repozytorium.
- Dozwolone pola jako `List.of(...)`, nie `Set.of(...)`.
- Komunikat nazywa odrzucone pole i wymienia dozwolone: `cannot sort by 'abrakadabra', allowed: startsAt, endsAt`.
- Bez nowego handlera w advice: wyjątek domenowy obsługuje piętro pierwsze.

**L35.3. Sprawdź**
- `sort=abrakadabra` i `sort=patient.user.password` → 400 z listą dozwolonych pól.
- `sort=startsAt,desc` → 200, wizyty od najpóźniejszej.
- Przy odrzuconym sortowaniu w logu nie ma `select ... from visits`.

**L35.4. Testy serwisu** (`VisitServiceTest`, dwa testy `findAll`)
- Pole z listy: serwis oddaje tę samą instancję `Page` co repozytorium (`assertSame`).
- Pole spoza listy: `assertThrows` z typem i pełnym komunikatem +
  `verify(visitRepository, never()).findAll(any(PageRequest.class))`.
- `Pageable` przez `PageRequest.of(...)` z `Sort.by(...)`.

**L35.5. Kolekcja Bruno** `bruno/wizyty-strony`, pięć żądań z asercjami
- Strona 0 i strona 1 po 5: 200, `res.body.page` i `res.body.size` zgodne z żądaniem.
- Sortowanie po `startsAt` rosnąco i malejąco: 200 i test w bloku `tests` na kolejność `startsAt` w `content`.
- Zły sort (`patient.user.password`): 400, `detail` zawiera listę dozwolonych pól.
- Zobacz porażkę asercji: zakomentuj sprawdzanie białej listy, przebuduj, zrestartuj, folder
  czerwony; przywróć, folder zielony.

Gotowe, gdy:
- `GET /visits?sort=abrakadabra` → 400, w `detail` lista dozwolonych pól
- `GET /visits?size=3&sort=startsAt,desc` → 200, trzy najpóźniejsze wizyty
- `./mvnw test` → `BUILD SUCCESS`
- `cd bruno && npx @usebruno/cli@4.1.0 run wizyty-strony --env local` → `Status ✓ PASS`, pięć żądań

---

## MED-29: fasada rezerwacji

Przed startem: baza od zera (`docker compose down -v`, `docker compose up -d --wait`), build
(`./mvnw -q -DskipTests package`) i aplikacja z logiem w pliku:
`java -jar target/medical-clinic-0.0.1-SNAPSHOT.jar > app.log 2>&1`.

**L36.1. Podsumowanie i wolne sloty**
- Rekord `dto/BookingSummaryDto(VisitDto visit, PatientDto patient, DoctorDto doctor, long freeSlotsThatDay)`.
- W `VisitRepository` zapytanie `@Query`: liczba wizyt doktora bez pacjenta w przedziale czasu.
- `VisitService.countFreeSlots(Long doctorId, LocalDate day)` z `@Transactional(readOnly = true)`;
  dzień = od `day.atStartOfDay()` włącznie do `day.plusDays(1).atStartOfDay()` wyłącznie.
- Sprawdź: `./mvnw -q compile`.

**L36.2. `VisitBookingFacade`** (pakiet `facade`, `@Service`)
- `BookingSummaryDto book(Long visitId, Long patientId)`: `VisitService.book`, odczyt pacjenta
  i doktora przez ich serwisy, wolne sloty doktora w dniu wizyty, podsumowanie przez istniejące mapery.
- `@Transactional` na metodzie fasady; wolne sloty liczysz po rezerwacji.
- `PATCH /visits/{id}/book/{patientId}` woła wyłącznie fasadę, pozostałe endpointy wizyt zostają przy serwisie.
- Żaden serwis nie wstrzykuje innego serwisu.

**L36.3. Sprawdź** (przebuduj i zrestartuj aplikację)
- Doktor 1: dwa wolne sloty 4.03.2030, 10:00-10:30 i 11:00-11:30 (`POST /visits`).
- `PATCH /visits/1/book/2` → 200 z `visit`, `patient`, `doctor`, `freeSlotsThatDay` = 1.
- W `app.log` jedna linia `Creating new transaction` z metodą fasady, potem `Initiating transaction commit`.
- Druga rezerwacja tego samego slotu → 409, pacjent, którego nie ma → 404.
- Folder Bruno `sloty` oblewa teraz jedną asercję (`res.body.patientId`); poprawka w L36.6.

**L36.4. `VisitBookingFacadeTest`** (`src/test/java/.../facade`)
- Serwisy jako `@Mock`, mapery prawdziwe (`Mappers.getMapper`), fasada składana w `@BeforeEach`.
- Poprawna rezerwacja: `InOrder` przypina tylko `visitService.book` przed `countFreeSlots`,
  asercje na czterech polach podsumowania. Odczyty pacjenta i doktora w dowolnej kolejności.
- Odmowa: `book` rzuca `VisitAlreadyBookedException`, fasada go przepuszcza, serwisy pacjenta
  i doktora niewywołane (`verifyNoInteractions`).

**L36.5. `VisitControllerTest`** z `@WebMvcTest(VisitController.class)`
- `@MockitoBean` na fasadę i na serwis wizyt (inne endpointy), `@Import(VisitMapperImpl.class)`.
- Rezerwacja: 200 i `$.visit.patientId`, `$.patient.fullName`, `$.doctor.lastName`,
  `$.freeSlotsThatDay`; `verifyNoInteractions` na serwisie wizyt.
- Błędy: 409 z `detail` dla zajętego slotu, 404 dla pacjenta, którego nie ma.

**L36.6. Bruno** `bruno/sloty/zarezerwuj-slot.bru` (reszty folderu nie ruszasz)
- Asercje: 200, `res.body.visit.id: eq {{slotId}}`, pacjent, doktor, `freeSlotsThatDay` jest liczbą.

Gotowe, gdy:
- na czystej bazie dwa sloty doktora 1 → 201, 201
- `PATCH /visits/1/book/2` → 200, podsumowanie z `freeSlotsThatDay`
- `grep -oE 'Creating new transaction[^:]*|Initiating.*' app.log | tail -2` → transakcja fasady i commit
- `PATCH /visits/1/book/3` → 409, `PATCH /visits/2/book/99` → 404
- `./mvnw test` → `BUILD SUCCESS` (w tym `VisitBookingFacadeTest` i `VisitControllerTest`)
- na świeżej bazie `cd bruno && npx @usebruno/cli@4.1.0 run sloty --env local` → `Status ✓ PASS`, cztery żądania

---

## MED-30 cz. 1: logi w serwisach

Przed startem: baza od zera. Aplikację uruchamiasz dopiero w L37.3.
Zasady dla każdego wpisu: placeholdery `{}` (bez sklejania plusem), same identyfikatory (żaden
obiekt ani Command w `{}`), komunikaty po polsku bez diakrytyków (`zarezerwowano wizyte …`).

**L37.1. `VisitService`** (`@Slf4j`)
- INFO: założenie wizyty (wizyta, doktor, początek, koniec), rezerwacja (wizyta, pacjent),
  zastępstwo (liczba wizyt, obaj doktorzy).
- DEBUG: sprawdzanie nakładania przed zapisem, wynik liczenia wolnych slotów.
- Odmów (409, 400) serwis nie loguje, robi to advice.

**L37.2. `PatientService`** (`@Slf4j`)
- INFO: rejestracja (numer zapisanego pacjenta, bez `command`), zmiana danych, zmiana hasła
  (bez hasła), usunięcie.
- `./mvnw test` zielony. Test tworzenia pada z `"saved" is null`? Dopisz
  `when(patientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));`
  (to samo dla `visitRepository` w teście tworzenia wizyty).

**L37.3. Próg i sprawdzenie**
- `logging.level.com.github.kurs.medicalclinic=DEBUG` w `application.properties`, build,
  aplikacja z `> app.log 2>&1`.
- Rejestracja pacjenta, dwa sloty doktora 1 (4.03.2030, 10:00 i 11:00), rezerwacja pierwszego
  dla pacjenta 2, druga próba dla pacjenta 3.
- `grep -E 'service\.(Visit|Patient)Service +:' app.log`: rejestracja, dwa założenia wizyty
  (każde po DEBUG o nakładaniu), rezerwacja, DEBUG z wolnymi slotami.
- Druga rezerwacja: jeden WARN z advice (`blad domenowy: …`), bez stosu.
- W `GlobalExceptionHandler` siatka na `Exception` woła `log.error("…", exception)` (5xx z pełnym stosem).
- Spring i Hibernate nie piszą DEBUG (poza menedżerem transakcji z L26).

Gotowe, gdy:
- `POST /patients` → 201 i wpis `zarejestrowano pacjenta …`
- dwa sloty doktora 1 → 201, 201 i dwa wpisy `zalozono wizyte …`
- `PATCH /visits/1/book/2` → 200 i wpisy `zarezerwowano …` oraz `wolne sloty …`
- `PATCH /visits/1/book/3` → 409 i jeden WARN z advice
- `./mvnw test` → `BUILD SUCCESS`

---

## MED-30 cz. 2: logi w całej aplikacji, MDC i audyt

Ciąg dalszy MED-30 (L37), te same zasady wpisów co w cz. 1.

**L37.4. Pozostałe serwisy i fasada**
- INFO w `DoctorService` (utworzenie, usunięcie, przypisanie i odpięcie placówki),
  `UserService` (utworzenie i usunięcie konta), `FacilityService` (utworzenie placówki).
- `VisitBookingFacade`: jeden DEBUG o złożeniu podsumowania (wizyta, doktor). Rezerwacji
  i wolnych slotów nie powtarzasz, zapisuje je `VisitService`.
- `./mvnw test` zielony.

**L37.5. `RequestIdFilter`** (pakiet `config`, `@Component`, dziedziczy po `OncePerRequestFilter`)
- Krótki identyfikator żądania (np. 8 znaków UUID) w MDC pod kluczem `requestId` i w nagłówku
  odpowiedzi `X-Request-Id`; `MDC.remove("requestId")` w `finally`.
- `logging.pattern.correlation=[%X{requestId}] ` (spacja na końcu, ginie przy kopiowaniu z PDF-u).
- `logging.file.name=logs/medical-clinic.log`, katalog `logs/` w `.gitignore`.
- Każda linia logu z żądania ma identyfikator w nawiasie; puste `[]` tylko przy wpisach ze startu.

**L37.6. Audyt pliku logu**
- Zarejestruj pacjenta, zmień mu hasło (`PATCH /patients/{id}/password`), załóż placówkę i konto (`POST /users`).
- `grep -riE 'password=|idCardNo|id_card_no' logs/` nic nie zwraca; nie ma też wysłanych haseł
  ani numeru dowodu (szukaj ich wartości).
- Sprawdź, czy w pliku jest e-mail (np. z WARN przy 409), i zapisz w komentarzu do pracy, co z tym zrobić.
- Audyt dotyczy `logs/`, nie `app.log` (tam SQL z `show-sql` pisany printlnem na konsolę).

Gotowe, gdy (czysta baza, `rm -rf logs`, build, aplikacja):
- `POST /patients` → 201, w odpowiedzi nagłówek `X-Request-Id`
- `grep -E 'utworzono|zarejestrowano' logs/medical-clinic.log` → wpis o rejestracji z tym samym identyfikatorem
- `grep -riE 'password=|idCardNo|id_card_no|<twoje-haslo>|<twoj-nr-dowodu>' logs/ | wc -l` → 0
- `git status` nie pokazuje `logs/`
- `./mvnw test` → `BUILD SUCCESS`