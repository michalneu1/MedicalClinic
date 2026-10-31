# Lekcja 20. Spring Data JPA. Praca domowa

Termin: przed następnymi zajęciami. Pracujesz w medical-clinic, na stanie z dzisiejszych
ćwiczeń.

## Zadanie D1. Jedno pytanie zamiast całego wiersza

Serwis sprawdza duplikat e-maila, pobierając cały wiersz i budując z niego obiekt,
po to tylko, żeby stwierdzić, czy ten e-mail jest już zajęty. Zamień to na metodę, która pyta wprost.

Kryteria akceptacji:
- [ ] `PatientRepository` ma metodę `existsByEmail` zwracającą `boolean`.
- [ ] Serwis używa jej przy zakładaniu pacjenta, a `findByEmail` zostaje tam, gdzie
  naprawdę potrzebny jest cały pacjent.
- [ ] `POST` z zajętym e-mailem nadal zwraca 409 z tym samym komunikatem.
- [ ] W logu przy tym żądaniu widać zapytanie po jednym wierszu, a nie pobranie
  wszystkich kolumn pacjenta.

## Zadanie D2. Zapytanie, którego nie da się nazwać

Dopisz wyszukiwanie pacjentów po fragmencie imienia albo nazwiska, bez względu na
wielkość liter. Nazwa metody dla takiego warunku byłaby nie do przeczytania, więc napisz
zapytanie wprost w JPQL przez `@Query`. Wystaw je jako kolejny wariant ścieżki
`GET /patients?fragment=...`.

Kryteria akceptacji:
- [ ] `GET /patients?fragment=kow` zwraca 200 i pacjentów, których imię albo nazwisko
  zawiera ten fragment.
- [ ] Wielkość liter nie ma znaczenia: `KOW` i `kow` dają ten sam wynik.
- [ ] Zapytanie w `@Query` operuje na encji `Patient` i nazwach pól, a nie na tabeli
  `patients` i nazwach kolumn.
- [ ] Parametr jest nazwany (`:fragment`), a nie pozycyjny.

## Zadanie D3. Kolekcja Bruno rośnie o jedno żądanie

Dopisz do kolekcji kliniki żądanie wyszukiwania po fragmencie nazwiska. Potem skasuj
wolumen (`docker compose down -v`), podnieś bazę i przejdź całą kolekcję od początku,
na świeżych danych startowych.

Kryteria akceptacji:
- [ ] W kolekcji jest żądanie `GET /patients?fragment=...` z sensowną nazwą po polsku.
- [ ] Na świeżej bazie cała kolekcja przechodzi: 200, 201, 409, 400 i 404 tam, gdzie
  było tak w lekcji 14.
- [ ] Żądanie „Utwórz pacjenta" ma dane, których nie ma w `data.sql` (inny e-mail
  i inny numer dowodu), więc za pierwszym razem daje 201.
- [ ] `mvn test` przechodzi przy zatrzymanym kontenerze bazy.

## Jak sprawdzisz, że skończyłeś

Zatrzymaj aplikację, skasuj wolumen (`docker compose down -v`), podnieś bazę i uruchom
wszystko od nowa. Dane startowe wejdą z `data.sql`, a cała kolekcja Bruno ma przejść tak
samo jak przed restartem. Jeżeli coś działa tylko za pierwszym razem, znaczy to, że
gdzieś jeszcze trzymasz stan w pamięci.

## Lektury

- `docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html`, rozdział o tworzeniu
  zapytań z nazw metod. Przeczytaj tabelę słów kluczowych: to jest cały mini-język.
- Kompendium, rozdz. 20, s. 136-141. Pamiętaj, że serwis z kompendium ma
  `@Transactional` (lekcja 26), którego u nas jeszcze nie ma, więc zapis wołasz jawnie.