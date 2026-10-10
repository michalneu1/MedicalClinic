# Lekcja 9. Endpointy w praktyce: praca domowa

Termin: przed następnymi zajęciami. Pracujesz na swoim projekcie medical-clinic,
na stanie po dzisiejszych ćwiczeniach.

## Zadanie D1. Przegląd własnych ścieżek

Przejdź po wszystkich adnotacjach mapujących w swoim projekcie i sprawdź je listą
z zajęć. Ścieżka nazywa zasób rzeczownikiem, operację niesie metoda HTTP,
a identyfikatorem w ścieżce jest `{id}`, nie e-mail. Popraw to, co nie przechodzi.

W terminalu, w katalogu projektu, ta komenda wypisze wszystkie twoje mapowania naraz:

```bash
grep -rhoE "@(Get|Post|Put|Patch|Delete)Mapping(\([^)]*\))?" src/main/java | sort -u
```

Kryteria akceptacji:
- [ ] Żaden adres nie zawiera słowa opisującego operację (`/all`, `/create`, `/update`,
  `/delete`); operację niesie metoda HTTP.
- [ ] Żaden adres nie zawiera e-maila ani innych danych osobowych.
- [ ] Wyszukiwanie po e-mailu jest parametrem zapytania, nie ścieżką.
- [ ] Aplikacja startuje, a wszystkie żądania z lekcji 8 nadal działają.

## Zadanie D2. Kolekcja Bruno z kompletem żądań

Uzupełnij folder `pacjenci` z zajęć tak, żeby zawierał wszystkie żądania projektu:
tworzenie, listę, pacjenta po `id`, pacjenta po e-mailu, pełną aktualizację, zmianę
hasła i usunięcie. Każde żądanie ma używać zmiennej `{{baseUrl}}` ze środowiska `local`.
Kolejność przejścia ustawia pole `seq` w plikach `.bru`: tworzenie pacjenta pierwsze,
usunięcie ostatnie.

Uruchom całą kolekcję od góry na świeżo uruchomionej aplikacji i zapisz, jaki status
wrócił dla każdego żądania. Jeżeli masz zainstalowany Node, cały folder uruchomisz
jedną komendą z katalogu `bruno`:

```bash
npx --yes @usebruno/cli@4.1.0 run pacjenci --env local
```

Kryteria akceptacji:
- [ ] Folder `pacjenci` jest w repozytorium razem z resztą projektu.
- [ ] Kolejność żądań (`seq`) pozwala przejść kolekcję od góry bez ręcznych poprawek.
- [ ] Statusy zgadzają się z tym, czego oczekujesz: 201, 200, 200, 200, 200, 200, 204.

## Zadanie D3. Notatka z krokowania doDispatch

Postaw breakpoint w klasie `DispatcherServlet`, w metodzie `doDispatch` (w IntelliJ
znajdziesz klasę skrótem do wyszukiwania klas). Uruchom aplikację w trybie debug
i wyślij jedno żądanie `GET /patients/1` z Bruno. Przechodź krokami, aż wykonanie
zatrzyma się w twojej metodzie kontrolera.

Zapisz notatkę w pliku `docs/dispatcher/droga-zadania.md`. Ma odpowiadać na trzy pytania:
przez które trzy klasy Springa przeszło żądanie, zanim trafiło do twojej metody; w którym
momencie tekst `"1"` z adresu stał się liczbą; co widać w oknie zmiennych, gdy wykonanie
stoi w `doDispatch`.

Kryteria akceptacji:
- [ ] Notatka wymienia trzy klasy z pełnymi nazwami i metodami, w kolejności od wejścia.
- [ ] Jest w niej zdanie o tym, gdzie dzieje się konwersja argumentu.
- [ ] Plik jest w repozytorium projektu.

## Lektury

Do przeczytania przed następnymi zajęciami (obie pozycje w całości):

- `tomaytotomato.com/overloading-rest-endpoints-on-spring-boot-2`: kilka metod pod jedną
  ścieżką rozróżnianych parametrami, czyli dzisiejsze `params` z innej strony.
- `medium.com`, „Spring Boot: Query Parameter vs Path Variable" (Daryl Goh): utrwalenie
  różnicy między parametrem zapytania a zmienną ścieżkową, tym razem w adnotacjach.

## Jak sprawdzisz, że skończyłeś

- Komenda z zadania D1 wypisuje same adresy bez słów opisujących operację.
- Cała kolekcja `pacjenci` przechodzi od góry na świeżo uruchomionej aplikacji.
- Plik `docs/dispatcher/droga-zadania.md` istnieje i wymienia trzy klasy.
- Umiesz opowiedzieć drogę żądania od gniazda sieciowego do swojej metody bez patrzenia
  w notatkę. Od tego zaczniemy następne zajęcia.