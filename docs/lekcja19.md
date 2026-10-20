# Lekcja 19. Encje JPA. Praca domowa

Termin: przed następnymi zajęciami. Pracujesz w medical-clinic, na stanie z dzisiejszych
ćwiczeń.

## Zadanie D1. Kolumny tak wąskie jak dane

Wygenerowany schemat dał każdemu polu tekstowemu `varchar(255)`, bo tyle wynosi wartość
domyślna. W `schema.sql` z lekcji 18 numer dowodu miał 20 znaków, imię i nazwisko po 100,
a numer telefonu 20. Przywróć te szerokości, tym razem adnotacjami przy polach.

Kryteria akceptacji:
- [ ] `id_card_no` i `phone_number` mają w bazie `character varying(20)`, a `first_name`
  i `last_name` `character varying(100)`.
- [ ] `email` i `password` zostają na `varchar(255)`: tych nie zawężamy.
- [ ] Aplikacja startuje, a `SELECT COUNT(*) FROM patients;` nadal zwraca 3.
- [ ] W notatce z zadania D2 jest napisane, czy zawężenie zadziałało na istniejącej
  tabeli, czy trzeba było skasować wolumen. Sprawdź to u siebie, nie zgaduj.

## Zadanie D2. Notatka o schemacie z klas

Załóż plik `docs/baza/schemat-z-klas.md` i opisz w nim tabelę, która powstała z twojej
klasy. Notatka ma odpowiadać na pytanie „skąd wzięła się każda kolumna", a nie
powtarzać treści lekcji.

1. Wklej z logu wiersz `Hibernate: create table patients (...)` (włącz
   `spring.jpa.show-sql=true`, jeżeli jeszcze go nie masz).
2. Zrób tabelkę: pole klasy, nazwa kolumny, typ w bazie, adnotacja, z której to wynika.
3. Dopisz trzy obserwacje o tym, czego nikt nie pisał ręcznie: skąd wzięły się nazwy
   w snake_case, skąd `varchar(255)` i w jakiej kolejności Hibernate ustawił kolumny.
4. Wyjaśnij, skąd wzięły się nazwy ograniczeń unikalności, które pokazuje
   `\d patients`.
5. Na koniec jedno zdanie o tym, czego `ddl-auto=update` nie zrobi.

Kryteria akceptacji:
- [ ] W notatce jest prawdziwy wydruk z twojego logu, nie przepisany z lekcji.
- [ ] Tabelka pokrywa wszystkie pola klasy `Patient`.
- [ ] Przy ograniczeniach unikalności jest napisane, skąd się wzięły ich dziwne nazwy.
- [ ] Notatka mieści się na jednym ekranie.

## Jak sprawdzisz, że skończyłeś

Skasuj wolumen (`docker compose down -v`), podnieś bazę i uruchom aplikację. Wygenerowany
`create table` w logu ma się zgadzać co do znaku z tym, co wkleiłeś do notatki, a
`\d patients` ma pokazać zawężone kolumny. Jeżeli coś się różni, to notatka jest z innego
stanu projektu niż kod.

## Lektury

- `baeldung.com/jpa-entities`, wymagania i adnotacje encji. Całość, artykuł jest krótki.
- Kompendium, rozdz. 19, s. 130-135. Pamiętaj, że kompendium stawia
  `ddl-auto=create-drop` (bo pracuje na bazie w pamięci), a my mamy `update`.
- Dokumentacja Spring Boota, „Data Initialization" (`docs.spring.io`, Boot 4.x): akapit
  o tym, kiedy wykonują się `schema.sql` i `data.sql` przy generowaniu schematu z klas.
  Nieobowiązkowe.