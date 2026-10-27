# MED-09, L29 Z1: klasyfikacja 12 testów

| # | Opis | Rodzaj | Dubler | Uzasadnienie |
|---|---|---|---|---|
| 1 | `PatientService` przez `new`, atrapa repozytorium zwraca „Jan Kowalski" | jednostkowy | stub | Jedna klasa w izolacji, atrapa tylko podaje gotowe dane, a test sprawdza wynik. |
| 2 | Cały kontekst Springa z H2, aplikacja wstaje | integracyjny | brak | Startuje wszystkie beany i bazę naraz, więc sprawdza współpracę wielu elementów, a nie jedną klasę. |
| 3 | Kasowanie, serwis zawołał `deleteById(5)` | jednostkowy | mock | Test weryfikuje interakcję z zależnością (`verify`), a nie zwracaną wartość. |
| 4 | Playwright: rejestracja, logowanie, wizyta w przeglądarce | E2E | brak | Przechodzi cały system od interfejsu użytkownika do bazy. |
| 5 | `VisitRepository` na prawdziwej bazie w kontenerze, nakładanie wizyt | integracyjny | brak | Sprawdza współpracę zapytania z prawdziwą bazą danych. |
| 6 | `VisitService` z zegarem zatrzymanym na 7.01.2030 10:00 | jednostkowy | stub | `Clock.fixed` zwraca zawsze ten sam czas, więc jest stubem, a serwis testujemy w izolacji. |
| 7 | Testerka godzinę wysyła do API dziwne dane | manualny | brak | Człowiek eksploruje system ręcznie, bez skryptu z asercjami. |
| 8 | `PatientService` z repozytorium na `HashMap` | jednostkowy | fake | Ręcznie napisana, działająca uproszczona implementacja repozytorium. |
| 9 | `POST /patients` przez warstwę HTTP bez serwera, serwis jest atrapą, 400 i `errors` | integracyjny (wycinek web) | mock | Działa prawdziwa warstwa Spring MVC (walidacja, JSON, advice), a serwis zastępuje atrapa, przez którą można sprawdzić, że nie został wywołany. |
| 10 | Folder `zastepstwa` w Bruno na działającej aplikacji z prawdziwą bazą | E2E (na poziomie API) | brak | Cała uruchomiona aplikacja z bazą, sprawdzana z zewnątrz przez HTTP. |
| 11 | Serwis zastępstw nigdy nie woła `findAllByDoctorId` | jednostkowy | mock | Sprawdza brak interakcji (`verify(..., never())`). |
| 12 | `Visit.isFree()` na obiekcie z buildera | jednostkowy | brak | Jedna metoda jednej klasy, bez żadnych zależności. |

Bez dublera: **2, 4, 5, 7, 10, 12**.

**Czemu 2 nie jest jednostkowy:** test jednostkowy sprawdza jedną klasę w izolacji, a `contextLoads`
podnosi cały kontekst Springa (wszystkie beany, JPA, bazę H2). Sprawdza, czy elementy dają się
poskładać razem, a do tego trwa kilka sekund zamiast milisekund.
