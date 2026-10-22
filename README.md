# MedicalClinic

## Dokumentacja API


- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Opis OpenAPI (JSON): http://localhost:8080/v3/api-docs

Na prodzie jest wyłączona. Robi to profil `prod` (`application-prod.properties`), który ustawia
`springdoc.api-docs.enabled=false` i `springdoc.swagger-ui.enabled=false`. Odpalamy z
`java -jar target/MedicalClinic-1.0-SNAPSHOT.jar --spring.profiles.active=prod` i oba adresy dają 404.

Uwaga: dokumentacja pokazuje tylko to, co API deklaruje (adnotacje w kodzie), a nie to, co
naprawdę robi. Jak ktoś wpisze w `@ApiResponse` zły kod, Swagger i tak go pokaże.
To, czy API faktycznie tak działa, sprawdza kolekcja Bruno.
