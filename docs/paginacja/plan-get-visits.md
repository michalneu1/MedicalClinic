# Plan: paginacja GET /visits

1. **Stronicuję** `GET /visits`, bo wizyt stale przybywa. Listy zostają: `?doctorId=`, `/patients`, `/doctors` (mało rekordów).
2. **Domyślnie** 20 wizyt, `startsAt` rosnąco, limit 100.
3. **Sortowanie** po `startsAt`, `endsAt`, `id`.
4. **`Page`**, bo paginator potrzebuje `totalPages`, a `count` na jednej tabeli jest tani.
5. **Odpowiedź:** `content`, `page`, `size`, `totalElements`, `totalPages`.
6. **Testy L35:** serwis przekazuje `Pageable` do repozytorium i mapuje wynik na `VisitDto`. Kontroler: bez parametrów dostaje rozmiar 20 i sortowanie po `startsAt`, `size=5000` daje stronę o rozmiarze 100, `sort=startsAt,desc` zwraca najpóźniejsze wizyty pierwsze, a sortowanie po polu spoza białej listy kończy się błędem 400.
