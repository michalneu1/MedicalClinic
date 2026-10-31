# Liquibase i Spring Security w medical-clinic: zadania z lekcji 38-41

| MED | Co robisz | Gdzie | Lekcja | Stan startowy |
|---|---|---|---|---|
| MED-14 | Liquibase zamiast `ddl-auto=update`, przepisy na cofnięcie, poprawki z code review historii | `pom.xml`, `liquibase-changelog/`, `application.properties` (główny i testów) | L38 | `po-L37-pd` |
| MED-15 | basic auth, konta z tabeli, hasła w BCrypcie, testy z zabezpieczeniami | `config/SecurityConfig`, `AccountUserDetailsService`, `PatientService`, `UserService`, `data.sql`, testy | L39 | `po-L38-pd` |
| MED-16 i MED-17 | rola ADMIN, admin z historii schematu, reguły ról, testy | `Role`, `versions/004-admin.yaml`, `SecurityConfig`, testy | L40 | `po-L39-pd` |
| MED-20 | reguły dla doktorów i placówek | `SecurityConfig` | L40 | stan po MED-16 i MED-17 |
| MED-18 | identyfikatory kroków od 1, kolekcja Bruno na trzech kontach, kolekcja Postman pod `/postman/patients` | `versions/001-initial.yaml`, `bruno/`, `PostmanController`, `SecurityConfig` | L38, L40 | stan po MED-20 |
| MED-32 | logowanie pod `POST /auth/token`, JWT zamiast basic auth | `JwtConfig`, `TokenService`, `AuthController`, `SecurityConfig`, testy, `bruno/` | L41 | `po-L40-pd` |

Baza od zera: `docker compose down -v`, potem `docker compose up -d --wait`. Build:
`./mvnw -q -DskipTests package`, aplikacja z logiem w pliku:
`java -jar target/medical-clinic-0.0.1-SNAPSHOT.jar > app.log 2>&1`. Po każdej zmianie zatrzymaj
aplikację, przebuduj ją i uruchom tak samo.

Konta z `data.sql`: pacjentka `anna.nowak@example.com` (`silne123`), doktor
`katarzyna.wrobel@example.com` (`silne654`, w `data.sql` to doktor 1). Admin `admin` (`admin`)
dochodzi w MED-17.

---

## MED-14: Liquibase

Przed startem: aplikacja zatrzymana, baza od zera.

**L38.1. Schemat z bazy**
- Build i start na świeżej bazie (tabele zakłada jeszcze Hibernate); gdy `grep 'Started' app.log`
  coś pokaże, zatrzymaj aplikację.
- Sprawdź, jakie ograniczenia check (listy dozwolonych wartości) Hibernate założył na kolumnach z enumem:
  `docker exec clinic-db psql -U clinic -d clinic -c "SELECT conrelid::regclass AS tabela, conname FROM pg_constraint WHERE contype = 'c' AND conrelid <> 0 ORDER BY conname"`.
- Generator Liquibase'a zapisuje schemat jako pierwszy krok historii (polecenie niżej).
- `grep -c 'changeSet:' $PLIK` → 18, każdy krok ma `author: kursant`.
- Porównaj ograniczenia check w bazie i w pliku (`grep -ci 'check' $PLIK`) i zapisz, których brakuje: wrócą w L38.5.

```bash
PLIK=src/main/resources/liquibase-changelog/versions/001-initial.yaml
./mvnw -q org.liquibase:liquibase-maven-plugin:5.0.3:generateChangeLog \
  -Dliquibase.url=jdbc:postgresql://localhost:5432/clinic \
  -Dliquibase.username=clinic -Dliquibase.password=clinic \
  -Dliquibase.changeSetAuthor=kursant \
  -Dliquibase.outputChangeLogFile=$PLIK
```

**L38.2. Liquibase rządzi, Hibernate pilnuje**
- `spring-boot-starter-liquibase` w `pom.xml` pod sterownikiem PostgreSQL, bez wersji.
- Spis `liquibase-changelog/liquibase-changelog.yaml` z jawnym `include` pliku `versions/001-initial.yaml`
  (`relativeToChangelogFile: true`).
- W `application.properties`: `spring.liquibase.change-log=classpath:liquibase-changelog/liquibase-changelog.yaml`,
  `spring.jpa.hibernate.ddl-auto=validate`; linię `spring.jpa.defer-datasource-initialization=true`
  usuń razem z komentarzem nad nią. `spring.sql.init.mode=always` zostaje: `data.sql` wykona się po Liquibasie.
- Trzy starty: na bazie z L38.1 pada z `relation "doctors" already exists`; na świeżej bazie
  `Run: 18` i 4 pacjentów; jeszcze raz bez czyszczenia `Run: 0`, `Previously run: 18`.
- Licznik kroków w logu: `grep -E 'liquibase.util +: (Run|Previously run|Total change sets):' app.log | sed 's/.* : //'`.
- Na świeżej bazie `Schema validation: missing table [...]`? W pliku została flaga `defer-datasource-initialization`.

**L38.3. Testy jadą historią**
- `./mvnw test` → `Errors: 2` i `no changelog could be found at 'classpath:/db/changelog/db.changelog-master.yaml'`.
- Na końcu `src/test/resources/application.properties` ten sam wpis spisu i `spring.jpa.hibernate.ddl-auto=validate`
  (plik testów zastępuje główny, a nie uzupełnia go) → `Tests run: 51`, `BUILD SUCCESS`.

**L38.4. Przepisy na cofnięcie**
- Każdy krok w `001-initial.yaml` dostaje sekcję `rollback`: `dropTable` do `createTable`,
  `dropUniqueConstraint` do `addUniqueConstraint`, `dropForeignKeyConstraint` do `addForeignKeyConstraint`.
  Nazwy tabel i ograniczeń przepisz z kroku, który cofasz.
- Identyfikatorów z generatora jeszcze nie ruszasz: porządkujesz je w MED-18.

**L38.5. Code review historii**
- Każda poprawka to nowy krok w osobnym pliku w `versions/`, z wpisem na końcu spisu i z sekcją `rollback`.
- `002-enum-checks.yaml`, krok `19`: ograniczenia check, które zgubił generator. Zmiana `sql`
  (`ALTER TABLE ... ADD CONSTRAINT ... CHECK (... IN (...))`, wartości z enumów w kodzie); jej
  Liquibase nie cofnie sam, więc przepis piszesz wprost.
- `003-...-index.yaml`, krok `20` (nazwę dokończ sam): indeks pod kolumną, po której filtruje jedno
  z zapytań `VisitRepository`, a indeksu nie ma (`\di` w `psql` pokaże istniejące).
- Komentarz w spisie: której reguły wizyt baza nie pilnuje i kto ją dziś sprawdza. Bez udawania gwarancji.

Gotowe, gdy (aplikacja zatrzymana, baza od zera, build, start):
- `./mvnw test` → `Tests run: 51`, `BUILD SUCCESS`
- `grep -c 'rollback:' src/main/resources/liquibase-changelog/versions/001-initial.yaml` → 18
- licznik kroków w logu → `Run: 20`, a `SELECT count(*) FROM patients` → 4
- `SELECT id, filename FROM databasechangelog WHERE orderexecuted IN (19, 20) ORDER BY orderexecuted` → `19` i `20` w nowych plikach
- zapytanie o ograniczenia check z L38.1 → wśród nich `doctors_specialization_check` i `users_role_check`
- na 20 000 wizyt plan zapytania o wizyty pacjenta używa twojego indeksu (polecenia niżej, aplikacja zatrzymana)
- cofnięcie 20 kroków zostawia tylko `databasechangelog` i `databasechangeloglock`, a następny start znowu daje `Run: 20` i 4 pacjentów

```bash
docker exec clinic-db psql -U clinic -d clinic -c "INSERT INTO visits \
  (starts_at, ends_at, version, doctor_id, patient_id) SELECT timestamp '2031-01-01 06:00' \
  + n * interval '1 hour', timestamp '2031-01-01 06:30' + n * interval '1 hour', 0, 1, \
  1 + n % 4 FROM generate_series(0, 19999) n" -c 'ANALYZE visits'
docker exec clinic-db psql -U clinic -d clinic \
  -c 'EXPLAIN (COSTS OFF) SELECT * FROM visits WHERE patient_id = 2'

./mvnw -q org.liquibase:liquibase-maven-plugin:5.0.3:rollback \
  -Dliquibase.url=jdbc:postgresql://localhost:5432/clinic \
  -Dliquibase.username=clinic -Dliquibase.password=clinic \
  -Dliquibase.searchPath=src/main/resources \
  -Dliquibase.changeLogFile=liquibase-changelog/liquibase-changelog.yaml \
  -Dliquibase.rollbackCount=20
docker exec clinic-db psql -U clinic -d clinic -c '\dt'
```

Cofnięcie staje na `No inverse to liquibase.change.core.RawSQLChange created`? Krok ze zmianą `sql`
nie ma sekcji `rollback`.

---

## MED-15: uwierzytelnianie basic auth

Przed startem: baza od zera. Do L39.3 budujesz z `-DskipTests`: po włączeniu Spring Security testy padają.

**L39.1. Zależności i łańcuch filtrów**
- `spring-boot-starter-security` pod starterem Liquibase'a i `spring-boot-starter-security-test`
  ze `<scope>test</scope>` pod starterem testów web.
- `config/SecurityConfig`, bean `SecurityFilterChain`: bez CSRF, bez sesji (`SessionCreationPolicy.STATELESS`),
  `POST /patients` tylko dla anonima (`anonymous()`), reszta `authenticated()`, `httpBasic(Customizer.withDefaults())`.
- Bean `PasswordEncoder`: `new BCryptPasswordEncoder()`.
- `curl -s -i http://localhost:8080/patients` → 401 z `WWW-Authenticate: Basic realm="Realm"`, bez `Set-Cookie`.

**L39.2. Konta z tabeli**
- `UserRepository`: `Optional<User> findByEmail(String email)`.
- `service/AccountUserDetailsService` (`@Service`, `implements UserDetailsService`): konto po e-mailu
  z nagłówka, `UserDetails` z hasłem i rolą; konta brak → `UsernameNotFoundException`. Nazwę `User`
  zajmuje twoja encja, więc klasę Spring Security piszesz z pełnym pakietem.
- `PatientService.create` zapisuje `passwordEncoder.encode(...)` (`PasswordEncoder` przez konstruktor).
- `grep -c 'generated security password' app.log` → 0.
- Sprawdź: rejestracja Oli (`ola.kowal@example.com`, `tajne123`) bez nagłówka → 201, jej konto na
  `GET /patients?email=ola.kowal@example.com` → 200, w bazie hash `$2a$10$`; złe hasło → 401; konto
  Anny → 401 (w bazie czysty tekst, poprawka w L39.4); rejestracja z nagłówkiem Oli → 403.
- Nowe konto dostaje 401? Rejestracja zapisała czysty tekst. Po poprawce baza od zera i rejestracja jeszcze raz.

**L39.3. Testy z zabezpieczeniami** (`./mvnw test`: 16 porażek i 2 błędy)
- `PatientControllerTest` i `VisitControllerTest`: `SecurityConfig.class` w `@Import` (wycinek
  `@WebMvcTest` sam go nie ładuje), `@WithMockUser` przy testach zalogowanego; testy rejestracji bez adnotacji.
- `PatientApiIntegrationTest`: po rejestracji loguje się nowym kontem przez `Authorization: Basic`
  (e-mail i hasło sklejone dwukropkiem, w Base64), tym samym hasłem, którym zarejestrował.
- `PatientServiceTest`: atrapa `PasswordEncoder`, a w `create_newEmail_savesPatientWithAccount` stub
  `encode`; w koncie ląduje to, co oddał `PasswordEncoder`.
- Żaden test nie wyłącza zabezpieczeń (bez `excludeAutoConfiguration` i bez konfiguracji „przepuść wszystko").

**L39.4. Hasło kodowane wszędzie**
- `PatientService.changePassword` koduje hasło; w `changePassword_patientExists_setsNewPassword` stub `encode` i asercja na hash.
- `UserService.create` (`POST /users`): maper przepisuje hasło z komendy, po mapowaniu podmień je na hash.
- `data.sql`: hashe BCrypt zamiast haseł, hasła pokazowe w komentarzu nad wpisami. Hash zrobisz
  aplikacją: zarejestruj pacjenta z hasłem z `data.sql` i skopiuj `password` jego konta z bazy.
- Na starej bazie `ON CONFLICT DO NOTHING` zostawi czyste hasła: baza od zera.

**L39.5. `X-Request-Id` także przy 401**
- `RequestIdFilter` z L37 stoi za filtrem Spring Security (kolejność -100), więc 401 wychodzi bez
  nagłówka. Przestaw filtr na początek łańcucha.

Gotowe, gdy (aplikacja zatrzymana, baza od zera, build, start):
- `./mvnw test` → `Tests run: 51, Failures: 0, Errors: 0`
- `GET /patients` bez nagłówka → 401 z `WWW-Authenticate: Basic` i `X-Request-Id`
- `GET /patients` kontem Anny → 200
- `PATCH /patients/1/password` z `{"password": "nowe-haslo-2030"}` → 200; potem stare hasło 401, nowe 200
- konto założone przez `POST /users` (np. `lena.lis@example.com`, `tajne456`, rola `DOCTOR`) → 201 i loguje się swoim hasłem (200)
- `SELECT email, left(password, 7) FROM users ORDER BY id` → wszędzie `$2a$10$`

---

## MED-16 i MED-17: role PATIENT i ADMIN

Przed startem: baza od zera. Do L40.4 budujesz z `-DskipTests`: po regułach z rolami testy padają.
Kolumna `role` jest w `users` od L21, więc skrypt z polem `role` z MED-16 nie jest potrzebny. Konsoli
H2 w aplikacji nie ma, więc „konsolę bazy" z MED-17 zastępuje Swagger UI.

**L40.1. Rola ADMIN i admin z historii schematu**
- `Role`: wartość `ADMIN` na końcu.
- `versions/004-admin.yaml` na końcu spisu, dwa kroki z `author: kursant` i z `rollback`.
- Krok `21`: usuń `users_role_check` i załóż go od nowa z `PATIENT`, `DOCTOR` i `ADMIN` (dwie zmiany `sql`, jak w kroku 19).
- Krok `22`: konto `admin` z hashem BCrypt hasła `admin` i rolą `ADMIN` (zmiana `insert`, cofnięcie
  `delete` z `where: email = 'admin'`). Hash policzysz w `jshell` (polecenie niżej), u ciebie będzie inny.
- Start pada na kroku 22 z `users_role_check`? Krok 21 musi stać wyżej i poszerzać ograniczenie.

```bash
cat > /tmp/hash.jsh <<'KONIEC'
import org.springframework.security.crypto.bcrypt.BCrypt;
System.out.println(BCrypt.hashpw("admin", BCrypt.gensalt()));
/exit
KONIEC
JAR=~/.m2/repository/org/springframework/security/spring-security-crypto/7.1.1
jshell --class-path $JAR/spring-security-crypto-7.1.1.jar /tmp/hash.jsh
```

**L40.2. Reguły** (w `SecurityConfig` zamiast `.anyRequest().authenticated()`, w tej kolejności, rejestracja zostaje na górze)
- `GET /patients` (lista i wyszukiwanie) tylko dla ADMIN.
- `/users/**` (konta) tylko dla ADMIN.
- Swagger UI (`/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`) tylko dla ADMIN.
- `POST /visits` (dostępne terminy wizyt, MED-19) tylko dla ADMIN.
- `/patients/**`, `/doctors/**`, `/facilities/**` i `/visits/**` dla PATIENT.
- Na końcu `anyRequest().denyAll()`.
- W `hasRole` podajesz samą nazwę roli, przedrostek `ROLE_` Spring Security dokleja sam.

**L40.3. Sprawdź** (przy każdym przypadku wiesz, która reguła zadecydowała)
- `GET /patients`: Anna 403, admin 200. `GET /patients/1`: Anna 200, admin 403.
- `GET /users`: Anna 403, admin 200.
- `GET /doctors` kontem doktor Katarzyny: 403. `GET /nie-ma-takiej` jako Anna: 403.
- `GET /swagger-ui/index.html`: Anna 403, admin 200.
- `POST /visits` z `{"doctorId": 1, "startsAt": "2030-03-04T10:00:00", "endsAt": "2030-03-04T10:30:00"}`: Anna 403, admin 201.
- Anna dostaje listę pacjentów? Reguła `/patients/**` stoi nad `GET /patients`: wygrywa pierwsza pasująca.

**L40.4. Testy pod role** (`./mvnw test`: 12 porażek, wszystkie z 403)
- `PatientControllerTest`: testy listy (`GET /patients`) z `@WithMockUser(roles = "ADMIN")`, pozostałe
  z `@WithMockUser` dostają `roles = "PATIENT"`, testy rejestracji bez adnotacji.
- `VisitControllerTest`: `@WithMockUser(roles = "PATIENT")` nad klasą.
- `PatientApiIntegrationTest`: nowy pacjent na wyszukiwaniu po e-mailu dostaje 403, a admin
  `admin:admin` z kroku 22 to samo wyszukiwanie dostaje 200. Admina w teście nie wstawiasz.

Gotowe, gdy (aplikacja zatrzymana, baza od zera, build, start):
- w `app.log` `…::21::kursant ran successfully` i `…::22::kursant ran successfully`
- `SELECT email, left(password, 7), role FROM users WHERE role = 'ADMIN'` → `admin | $2a$10$ | ADMIN`
- przypadki z L40.3 dają podane statusy
- `./mvnw test` → `Tests run: 51, Failures: 0, Errors: 0`

---

## MED-20: doktorzy i placówki

Przed startem: stan po MED-16 i MED-17. Z MED-20 zostały same wymagania ról.

**L40.5. Reguły** (w `SecurityConfig` pod regułą `POST /visits`, nad regułą dla PATIENT, w tej kolejności)
- `POST /doctors` i `POST /facilities` (nowi doktorzy i placówki) tylko dla ADMIN.
- `GET /doctors` (lista doktorów) tylko dla ADMIN.
- `GET /facilities/**` dla każdego zalogowanego (`authenticated()`).
- `/doctors/*/facilities/*` (przypisanie doktora do placówki i jego cofnięcie) dla DOCTOR.
- Doktor dostaje 403 na przypisaniu? Reguła `/doctors/**` dla PATIENT stoi nad regułą dla DOCTOR.

Gotowe, gdy (baza od zera, build, start):
- `GET /doctors`: Anna 403, admin 200
- `GET /facilities` kontem doktor Katarzyny: 200
- `POST /facilities` jako Anna: 403
- `POST /doctors/1/facilities/1`: Anna 403, doktor Katarzyna 200
- `./mvnw test` → `BUILD SUCCESS`

---

## MED-18: porządek w Liquibasie i kolekcja zapytań

Przed startem: stan po MED-20, aplikacja zatrzymana.

**L40.6. Identyfikatory od 1**
- Kroki w `001-initial.yaml` dostają identyfikatory `1` do `18` w kolejności z pliku (tak je wykonuje
  Liquibase); kroki `19`-`22` już je mają. Na stanie z tagu `po-L38-pd` lub późniejszym kroki są już
  ponumerowane, więc tylko sprawdzasz.
- Nowy identyfikator to dla Liquibase'a nowy krok, a nie stary pod nową nazwą: na starej bazie start
  pada z `relation "doctors" already exists`, więc baza od zera. Zmieniać identyfikatory wolno ci
  tylko dlatego, że historia nie wyszła poza twój komputer; na wspólnym środowisku wykonanych kroków się nie rusza.

**L40.7. Kolekcja Bruno na trzech kontach** (od MED-15 żądania kolekcji poza rejestracją dostają 401)
- `bruno/collection.bru`: basic auth ze zmiennych `{{email}}` i `{{haslo}}`.
- `bruno/environments/local.bru`: `email` i `haslo` Anny, `adminEmail` i `adminHaslo` admina,
  `doktorEmail` i `doktorHaslo` doktor Katarzyny.
- W żądaniach folderów aplikacji `auth: none` → `auth: inherit`, poza rejestracją (`utworz-pacjenta`, `zle-dane-400`).
- Adminem (`auth: basic` i blok `auth:basic` z `{{adminEmail}}` i `{{adminHaslo}}`): `lista-pacjentow`,
  `pacjent-po-emailu`, `szukaj-po-nazwisku`, `utworz-konto-doktora`, `konto-zastepcy`; siedem żądań
  `POST /visits`: `utworz-slot` w folderach `sloty` i `wizyty`, `nakladajacy-slot-409`,
  `slot-nieistniejacy-doktor-404`, `slot-doktora-1000`, `slot-doktora-1100`, `slot-zastepcy-1100`;
  siedem żądań z MED-20: `utworz-doktora`, `doktor-na-nieistniejace-konto-404`,
  `doktor-na-zajete-konto-409`, `lista-doktorow`, `doktor-zastepca`, `utworz-placowke`, `zajeta-nazwa-409`.
- Doktorem (`{{doktorEmail}}`, `{{doktorHaslo}}`): `przypisz-doktora` i `nieistniejaca-placowka-404`.
- Każdy folder aplikacji ma `folder.bru` z `auth { mode: inherit }`; folder `konta` z basic auth admina.
- `usun-konto-pacjenta-409` dostaje 204, a potem admin wszędzie 401? Na świeżej bazie admin ma `id` 1,
  bo Liquibase rusza przed `data.sql`, więc żądanie usuwało admina. Anna ma konto 2: `/users/1` → `/users/2`.
- Folder `users` to zewnętrzne API z L3 (środowisko `test`), dlatego polecenie wymienia foldery aplikacji po kolei.

**L40.8. Eksport do Postmana**
- W skryptach `res.body` → `res.getBody()`: Bruno rozumie oba, a konwerter poprawnie tłumaczy tylko drugi zapis.
- Z plików z blokiem `params:query` usuń ten blok, parametr zostaw w adresie.
- Bruno → Open Collection z katalogu `bruno` projektu → menu kolekcji → Share → Postman, prosto do
  `src/main/resources`, nazwa `medical-clinic.postman_collection` (Bruno dopisze `.json`). Zmienne
  trafiają do pliku bez wartości.

**L40.9. `GET /postman/patients`**
- `PostmanController` oddaje plik jako `String` czytany przez `InputStream` z `ClassPathResource`.
- `GET /postman/**` tylko dla ADMIN, nad `denyAll`.
- Brak pliku to `FileNotFoundException`. Nie łapiesz go: advice z L14 zamienia nieznany wyjątek na 500.
- Test wycinka kontrolera: admin 200, pacjent 403.

Gotowe, gdy (aplikacja zatrzymana, baza od zera, build, start):
- `SELECT id, author, filename FROM databasechangelog WHERE orderexecuted IN (1, 18, 19, 22) ORDER BY orderexecuted` → identyfikatory `1`, `18`, `19`, `22`
- `cd bruno && npx --yes @usebruno/cli@4.1.0 run doktorzy konta pacjenci placowki sloty wizyty wizyty-strony zastepstwa --env local` → `Status ✓ PASS`, `48 (48 Passed)`, `61/61` asercji
- `GET /postman/patients`: admin 200 z `Content-Type: application/json`, Anna 403, anonim 401
- plik przeniesiony poza resources (`mv src/main/resources/medical-clinic.postman_collection.json /tmp/`),
  `./mvnw -q clean package -DskipTests`, restart → admin 500; plik z powrotem, ten sam build, restart → 200
  (bez `clean` stary plik zostaje w `target/classes`)
- `./mvnw test` → `Tests run: 53, Failures: 0, Errors: 0`
- Newman (polecenie niżej): `requests` 48, `test-scripts` 9, `assertions` 3, w kolumnie `failed` same zera.
  Statusów Newman tu nie sprawdza (konwerter nie przenosi asercji Bruno), potwierdza je przebieg Bruno.

```bash
curl -s -u admin:admin http://localhost:8080/postman/patients > /tmp/kolekcja.json
npx --yes newman@6.2.2 run /tmp/kolekcja.json --folder doktorzy --folder konta \
  --folder pacjenci --folder placowki --folder sloty --folder wizyty --folder wizyty-strony \
  --folder zastepstwa --env-var baseUrl=http://localhost:8080 --env-var email=anna.nowak@example.com \
  --env-var haslo=silne123 --env-var adminEmail=admin --env-var adminHaslo=admin \
  --env-var doktorEmail=katarzyna.wrobel@example.com --env-var doktorHaslo=silne654
```

---

## MED-32: JWT zamiast basic auth

Przed startem: stan po MED-18 (albo `po-L40-pd`). Klient podaje hasło raz, pod `POST /auth/token`,
i dostaje token JWT ważny godzinę; dalej wysyła tylko `Authorization: Bearer <token>`. Klinika sama
wystawia token (podpis kluczem prywatnym RSA) i sama go sprawdza (kluczem publicznym), bez Keycloaka.
Bez zmian zostają tabela kont, hasła w BCrypcie, `UserDetailsService` z L39, rejestracja dla anonima
i reguły ról z MED-17, MED-19 i MED-20.

Jak ma działać po zmianie:
- `POST /auth/token` z `{"email": "…", "password": "…"}`: dobre dane → 200 i
  `{"token": "…", "tokenType": "Bearer", "expiresIn": 3600}`; złe hasło albo nieznany e-mail → 401
  z tym samym `wrong e-mail or password`; brak pola → 400 z listą błędów.
- Token RS256, claimy: `iss` = `medical-clinic`, `sub` = e-mail konta, `iat`, `exp` (godzina po `iat`)
  i `roles` z nazwami ról bez przedrostka, np. `["PATIENT"]`.
- Reszta API przyjmuje tylko token: bez tokenu, ze zmienionym tokenem i z nagłówkiem Basic → 401.
- Swagger UI widzi tylko admin, bez okna logowania w przeglądarce. JSON dokumentacji:
  `curl -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/v3/api-docs`.

**L41.1. Klucze, koder i dekoder**
- `spring-boot-starter-security-oauth2-resource-server` pod starterem Spring Security.
- `JwtConfig`: para kluczy RSA generowana przy starcie (`KeyPairGenerator.getInstance("RSA")`,
  `initialize(2048)`), bean `JwtEncoder` (`NimbusJwtEncoder.withKeyPair(...)`) i bean `JwtDecoder`
  (`NimbusJwtDecoder.withPublicKey(...)` z walidatorem `JwtValidators.createDefaultWithIssuer(...)`).
- Wystawca w publicznej stałej `JwtConfig.ISSUER`; korzystają z niej dekoder i serwis tokenów.
- Klucze żyją w pamięci, więc po restarcie stare tokeny przestają działać. Na produkcji para kluczy przychodzi z pliku albo z sejfu.

**L41.2. Logowanie pod `POST /auth/token`**
- Rekordy `LoginCommand` (`email`, `password`, oba wymagane) i `TokenDto` (`token`, `tokenType`, `expiresIn`).
- `TokenService`: e-mail i hasło sprawdza `AuthenticationManager`
  (`authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, password))`), czas
  wystawienia z beana `Clock` z L25, claimy przez `JwtClaimsSet.builder()`. Do `roles` bierzesz tylko
  uprawnienia z `ROLE_` i obcinasz przedrostek.
- Bean `AuthenticationManager` (`ProviderManager` z `DaoAuthenticationProvider` i `PasswordEncoder`)
  w osobnej klasie konfiguracji, nie w `SecurityConfig`: wycinki `@WebMvcTest` nie mają
  `UserDetailsService` i padają z `No qualifying bean`.
- `AuthController` z `POST /auth/token` i adnotacjami springdoc jak w MED-23 (200, 400, 401).
- Advice: handler `AuthenticationException` → 401 z `wrong e-mail or password`, nad piętrem czwartym,
  bez e-maila w logu (MED-30). Bez niego złe hasło kończy się 500.
- `SecurityConfig`: `POST /auth/token` z `permitAll()` zaraz pod regułą rejestracji. Basic auth zostaje do L41.3.
- `JwtPeek.java` z materiału L41 do `scripts/`; w komentarzu skreśl słowo „Keycloaka".
- Sprawdź: token Anny (polecenie niżej) → `java scripts/JwtPeek.java "$TOKEN"` pokazuje `"alg":"RS256"`,
  `"iss":"medical-clinic"`, `"sub":"anna.nowak@example.com"`, `"roles":["PATIENT"]`, `exp` większe od
  `iat` o 3600. Basic auth działa jak dotąd (`GET /patients/1` Anny → 200), a z tokenem to samo żądanie
  dostaje jeszcze 401.

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"email": "anna.nowak@example.com", "password": "silne123"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')
```

`ADMIN_TOKEN` zapisujesz tak samo, z danymi `admin`/`admin`.

**L41.3. Token zamiast basic auth**
- `SecurityConfig`: bez `httpBasic`, z `oauth2ResourceServer` i JWT (dekoder z `JwtConfig`); reguła logowania zostaje.
- `@Import(JwtConfig.class)` nad `SecurityConfig`: wycinki `@WebMvcTest` importują `SecurityConfig`,
  ale innych klas konfiguracji nie ładują, a łańcuch z resource serverem bez dekodera nie wstaje.
- `application.properties`: role w claimie `roles`, przedrostek `ROLE_` (dwa wpisy). `issuer-uri` nie
  jest potrzebne, dekoder budujesz sam.

**L41.4. Testy**
- `PatientApiIntegrationTest` loguje się jak klient: pod `POST /auth/token`, potem wysyła token
  (`JsonPath.read(body, "$.token")` z odpowiedzi logowania).
- `AuthApiIntegrationTest` (pełny kontekst, MockMvc), cztery testy: admin dostaje token na godzinę,
  złe hasło 401, basic auth na `GET /patients` 401, `Bearer abc` 401.
- Dwa wpisy mapowania ról powtórz w `src/test/resources/application.properties`; bez nich token
  w teście ma tylko uprawnienie `FACTOR_BEARER` i dostaje 403.

**L41.5. Bruno i Postman na tokenach**
- Folder `logowanie`, cztery żądania bez uwierzytelnienia (`auth { mode: none }` w `folder.bru`,
  `auth: none` w każdym żądaniu). Trzy logują Annę, admina i doktor Katarzynę i zapisują token
  w zmiennej (`bru.setVar("token", res.getBody().token)`, dalej `adminToken`, `doktorToken`); czwarte
  wysyła złe hasło i oczekuje 401.
- `collection.bru` loguje się tokenem Anny (`mode: bearer`, blok `auth:bearer` z `{{token}}`).
  Foldery i żądania, które logowały się adminem albo doktorem, używają `{{adminToken}}` albo
  `{{doktorToken}}`. W żądaniach zmień i tryb w bloku metody (`auth: bearer`), i blok
  (`auth:bearer`): sam blok bez trybu to dalej 401.
- `logowanie/folder.bru`: w bloku `meta` `seq: 1` zamiast numeru nadanego przez Bruno (nie dopisuj
  drugiej linii). Konwerter układa foldery alfabetycznie i bez tego `logowanie` wypadnie po `doktorzy`
  i `konta`, które bez tokenu admina dostaną 401.
- Eksport do `src/main/resources` ponownie, jak w MED-18 (L40.8); potem zatrzymaj aplikację, zbuduj
  i uruchom ją, bo aplikacja czyta plik z jara.

Gotowe, gdy (aplikacja zatrzymana, baza od zera, build, start):
- `POST /auth/token`: dobre dane 200 z tokenem, złe hasło i nieznany e-mail 401 z `"detail":"wrong e-mail or password"`, brak pola 400
- z tokenem Anny `GET /patients/1` → 200, `GET /patients` → 403; z tokenem admina `GET /patients` → 200
- bez tokenu i z `-u anna.nowak@example.com:silne123`: `GET /patients/1` → 401 z `WWW-Authenticate: Bearer`
- po restarcie aplikacji token sprzed restartu → 401 z `Invalid signature`, świeżo pobrany działa
- `./mvnw test` → `Tests run: 57, Failures: 0, Errors: 0`, bez działającej aplikacji i bez Dockera
- na świeżej bazie `npx --yes @usebruno/cli@4.1.0 run logowanie --env local` (w katalogu `bruno`) → `4 (4 Passed)`
- na świeżej bazie `npx --yes @usebruno/cli@4.1.0 run logowanie doktorzy konta pacjenci placowki sloty wizyty wizyty-strony zastepstwa --env local` → `52 (52 Passed)`, `68/68` asercji
- w pliku z `/postman/patients` `grep -c '"type": "basic"'` → 0; Newman na świeżej bazie (polecenie
  niżej, `ADMIN_TOKEN` pobrany po ostatnim restarcie) → `requests` 52, `test-scripts` 12,
  `assertions` 3, w kolumnie `failed` same zera

```bash
curl -s -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/postman/patients > /tmp/kolekcja.json
npx --yes newman@6.2.2 run /tmp/kolekcja.json --folder logowanie --folder doktorzy --folder konta \
  --folder pacjenci --folder placowki --folder sloty --folder wizyty --folder wizyty-strony \
  --folder zastepstwa --env-var baseUrl=http://localhost:8080 --env-var email=anna.nowak@example.com \
  --env-var haslo=silne123 --env-var adminEmail=admin --env-var adminHaslo=admin \
  --env-var doktorEmail=katarzyna.wrobel@example.com --env-var doktorHaslo=silne654
```

Newman odpowiada `collection could not be loaded`? Token był sprzed restartu i plik wyszedł pusty.
