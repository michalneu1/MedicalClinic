# Schemat z klasy Patient


| Pole        | Kolumna      | Typ w bazie            | Skąd                                          |
|-------------|--------------|------------------------|-----------------------------------------------|
| id          | id           | bigint, identity       | `@Id` + `@GeneratedValue(IDENTITY)`, typ Long |
| email       | email        | varchar(255) not null  | `@Column(nullable = false, unique = true)`    |
| password    | password     | varchar(255) not null  | `@Column(nullable = false)`                   |
| idCardNo    | id_card_no   | varchar(20) not null   | `@Column(nullable = false, unique = true, length = 20)` |
| firstName   | first_name   | varchar(100) not null  | `@Column(nullable = false, length = 100)`     |
| lastName    | last_name    | varchar(100) not null  | `@Column(nullable = false, length = 100)`     |
| phoneNumber | phone_number | varchar(20)            | `@Column(length = 20)`                        |
| birthday    | birthday     | date                   | brak adnotacji, typ LocalDate                 |

Czego nie pisałem ręcznie:
- snake_case: Spring Boot ma domyślną strategię nazw, która zamienia camelCase na snake_case.
- varchar(255): domyślne `length` w `@Column` to 255.
- kolejność: najpierw klucz główny, potem reszta alfabetycznie.

Nazwy w `\d patients` (uka370hmxgv0l5c9panryr1ji7d, uk3usu2gl5shfee2u3f4gfw4w3p) wymyślił Hibernate: prefiks UK + hash z nazwy tabeli i kolumny. Można ją ustawić przez `@Table(uniqueConstraints = @UniqueConstraint(name = "...", columnNames = "email"))`.
docker compose exec postgres psql -U clinic -d clinic -c "\d patients"

ddl-auto=update nie zmieni długości ani typu kolumny, która już istnieje.
