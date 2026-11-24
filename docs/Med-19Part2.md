                                                             # Lekcja 25. Logika biznesowa na danych: sloty wizyt. Ćwiczenia

## Na zajęciach (30 min): MED-19 część 2

### Zadanie 1 (10 min). Reguły bez bazy

1. Utwórz wyjątek `InvalidVisitException` ze statusem 400.
2. W `VisitService.create`, zanim sięgniesz do bazy, sprawdź po kolei: start nie jest
   w przeszłości, start wypada na pełnym kwadransie (bez sekund), a długość
   `endsAt - startsAt` jest dodatnią wielokrotnością 15 minut. Każda złamana reguła rzuca
   `InvalidVisitException` z komunikatem, który mówi, co poprawić.

Kryteria akceptacji:
- [ ] `w-2020.json`, `w-1005-1035.json` i `w-1000-1020.json` dają 400, każdy z innym `detail`.
- [ ] `w-1000-1030.json` daje 201.
- [ ] Reguły stoją w serwisie przed `doctorRepository.findById`.

### Zadanie 2 (12 min). Nakładanie jednym zapytaniem

1. W `VisitRepository` dopisz metodę, której nazwa buduje zapytanie i która odpowiada, czy doktor ma wizytę
   nachodzącą na podany przedział: istniejąca wizyta zaczyna się przed końcem nowej
   i kończy po jej początku.
2. Utwórz `VisitOverlapsException` ze statusem 409 i rzuć go w `VisitService.create`, gdy
   metoda zwróci `true`.
3. Dopisz 409 do dokumentacji `POST /visits` w kontrolerze.
4. Przed sprawdzeniem wyczyść bazę (`docker compose down -v`, `docker compose up -d --wait`),
   bo slot 10:00-10:30 z zadania 1 dalej w niej stoi, i uruchom aplikację.

Kryteria akceptacji:
- [ ] Po `w-1000-1030.json` żądanie `w-1015-1045.json` daje 409, a `w-1030-1100.json` 201.
- [ ] W logu zapytanie ma ostre nierówności: `starts_at<?` i `ends_at>?` (z aliasem tabeli
  przed kolumną, np. `v1_0.starts_at<?`).
- [ ] Umiesz wskazać, który argument metody porównujesz z którą kolumną.

### Zadanie 3 (8 min). Zegar jako bean

1. Utwórz klasę `ClockConfig` z `@Configuration` i beanem `Clock`
   (`Clock.systemDefaultZone()`).
2. Wstrzyknij `Clock` do `VisitService` i sprawdzaj przeszłość przez
   `LocalDateTime.now(clock)`.
3. Przesuń daty w folderze `wizyty` w Bruno na 14 stycznia 2030. Daty z października 2026
   złamią regułę przeszłości, gdy miną, a 7 stycznia zajmują twoje sloty z zadań 1-2.

Kryteria akceptacji:
- [ ] `grep -rn 'now(' src/main/java` pokazuje w kodzie tylko `LocalDateTime.now(clock)`
  (nie licząc komentarzy).
- [ ] Folder `wizyty` w Bruno przechodzi (4 z 4) przy pierwszym przebiegu po
  `docker compose down -v`.

## Jak sprawdzisz, że skończyłeś

Na czystej bazie (`docker compose down -v`, `docker compose up -d --wait`, start aplikacji):

- trzy złe sloty → 400, każdy z innym `detail`
- `w-1000-1030.json` → 201, `w-1015-1045.json` → 409, `w-1030-1100.json` → 201
- `./mvnw test` → `BUILD SUCCESS`

## Podpowiedzi (czytaj dopiero, gdy utkniesz)

- Zadanie 1: minuty `start.getMinute() % 15`, a sekundy i nanosekundy mają być zerem.
  Długość: `Duration.between(start, end)`; `toMinutes()` obetnie sekundy, więc resztę
  z dzielenia przez kwadrans licz w nanosekundach.
- Zadanie 2: `existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(doctorId, end, start)`.
  Kolejność argumentów idzie za kolejnością warunków w nazwie: `StartsAtBefore` dostaje
  koniec nowej wizyty.
- Zadanie 2: `Before` i `After` to ostre nierówności. `LessThanEqual` zrobiłby ze styku
  końców konflikt.
- Zadanie 3: serwis z `@RequiredArgsConstructor` dostanie zegar, gdy dopiszesz pole
  `private final Clock clock;`.
