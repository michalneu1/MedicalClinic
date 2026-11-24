# MedicalClinic

## Uruchomienie

Wymagania: Java 21 lub nowsza i Docker.

1. Baza danych (PostgreSQL 16 na porcie 5432):

   ```
   docker compose up -d
   ```

2. Build (testy biegną na H2, bez Dockera) i start aplikacji na http://localhost:8080:

   ```
   ./mvnw clean package
   java -jar target/medical-clinic-1.0-SNAPSHOT.jar
   ```

   Albo bez budowania jara: `./mvnw spring-boot:run`.

3. Logowanie: `POST /auth/token` z e-mailem i hasłem zwraca token, który wysyłasz w nagłówku
   `Authorization: Bearer <token>`. Token jest ważny godzinę.

   ```
   curl -X POST http://localhost:8080/auth/token -H "Content-Type: application/json" \
        -d '{"email":"admin","password":"admin"}'
   ```

Konta dostępne po pierwszym starcie (admin z Liquibase, pozostałe z `data.sql`):

| E-mail | Hasło | Rola |
|---|---|---|
| `admin` | `admin` | ADMIN |
| `jan.kowalski@example.com` | `haslo123` | PATIENT |
| `anna.nowak@example.com` | `silne123` | PATIENT |
| `piotr.zielinski@example.com` | `haslo123` | PATIENT |
| `katarzyna.wrobel@example.com` | `silne654` | DOCTOR |

Rejestracja nowego pacjenta (`POST /patients`) nie wymaga tokena.

## Skrypty

Oba skrypty działają na uruchomionej aplikacji, logują się przez `POST /auth/token` i biorą adres ze
zmiennej `BASE_URL` (domyślnie `http://localhost:8080`).

- `bash scripts/wyscig.sh`: zakłada 20 terminów jako admin, a potem dwóch pacjentów rezerwuje każdy
  z nich jednocześnie. Oczekiwany wynik: `20 200` i `20 409`.
- `bash scripts/wyscig-terminy.sh`: wysyła naraz 15 żądań zakładających ten sam termin.
  Oczekiwany wynik: `1 201` i `14 409`. Z `STAGGER=1` co drugie żądanie zaczyna się kwadrans później;
  wtedy mogą przejść dwa nachodzące się terminy, bo tej reguły baza jeszcze nie pilnuje
  (opis w `liquibase-changelog.yaml`).

## Dokumentacja API


- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Opis OpenAPI (JSON): http://localhost:8080/v3/api-docs

Na prodzie jest wyłączona. Robi to profil `prod` (`application-prod.properties`), który ustawia
`springdoc.api-docs.enabled=false` i `springdoc.swagger-ui.enabled=false`. Odpalamy z
`java -jar target/medical-clinic-1.0-SNAPSHOT.jar --spring.profiles.active=prod` i oba adresy dają 404.

Uwaga: dokumentacja pokazuje tylko to, co API deklaruje (adnotacje w kodzie), a nie to, co
naprawdę robi. Jak ktoś wpisze w `@ApiResponse` zły kod, Swagger i tak go pokaże.
To, czy API faktycznie tak działa, sprawdza kolekcja Bruno.

## Kolekcja Bruno

Kolekcja jest w folderze `bruno/` i działa na uruchomionej aplikacji z danymi startowymi:

```
cd bruno
npx @usebruno/cli run --env local
```

Folder `logowanie` idzie pierwszy i zapisuje tokeny admina, pacjenta i lekarza, z których korzystają
pozostałe foldery, więc pojedynczy folder uruchamiaj razem z nim, np.
`npx @usebruno/cli run logowanie wizyty --env local`. Adres aplikacji bierze się ze zmiennej `baseUrl`
w `environments/local.bru`. Kolekcję można puszczać wielokrotnie na tej samej bazie: e-maile, nazwy
i daty terminów są losowane przy każdym uruchomieniu.

## Baza danych

Schemat bazy tworzy Liquibase (`src/main/resources/liquibase-changelog`). Changesety mają
identyfikatory 1, 2, 3... w kolejności wykonania.

Uwaga: pierwsze 17 changesetów zostało przenumerowanych (wcześniej miały identyfikatory z generatora,
np. `1793365497947-1`). Liquibase rozpoznaje wykonane kroki po identyfikatorze, więc w bazie
postawionej przed tą zmianą uzna je za nowe i spróbuje wykonać jeszcze raz, co kończy się błędem
przy starcie aplikacji. Taką bazę deweloperską trzeba postawić od zera:

```
docker compose down -v
docker compose up -d
```

`down -v` usuwa wolumen z danymi PostgreSQL, więc wszystkie dane z lokalnej bazy przepadają.
Dane startowe wrócą z `data.sql` przy starcie aplikacji.

## Logi

Na co dzień aplikacja loguje na poziomie INFO do konsoli i do pliku `logs/medical-clinic.log`.
Poziom jest ustawiony osobno dla trzech obszarów w `application.properties`:

```
logging.level.com.medicalclinic.service=INFO
logging.level.com.medicalclinic.facade=INFO
logging.level.com.medicalclinic.exception=INFO
```

Gdy szukasz problemu, włącz DEBUG tylko dla jednego obszaru i tylko na czas diagnozy, najprościej
przy starcie, bez zmiany pliku:

```
./mvnw spring-boot:run -Dspring-boot.run.arguments=--logging.level.com.medicalclinic.service=DEBUG
```

Tak samo włącza się tryb diagnostyczny transakcji (początek, commit i rollback każdej transakcji):
`--logging.level.org.springframework.orm.jpa.JpaTransactionManager=DEBUG`. Zapytania SQL przez
logger, a nie na konsolę: `--logging.level.org.hibernate.SQL=DEBUG`. Żadne z nich nie jest włączone
na stałe.
