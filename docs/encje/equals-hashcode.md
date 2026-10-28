# equals i hashCode encji

1. **HashSet:** `hashCode` wybiera wiadro, `equals` porównuje w wiadrze. Cichy warunek: hash nie
   może się zmienić, gdy obiekt leży w zbiorze, a w encji `id` zmienia się z `null` przy zapisie.

2. **Złe wersje:**
   - domyślna (referencje): ten sam pacjent z dwóch transakcji jest w zbiorze dwa razy;
   - po wszystkich polach (`@Data`): zmiana nazwiska zmienia hash, `remove` nie działa, `LAZY` się ładuje;
   - hash z `id`: placówka dodana przed zapisem trafia do wiadra 0, `removeFacility` szuka w innym.

3. **Strategia** (`src/main/java/com/medicalclinic/model/`):
   ```java
   if (this == o) return true;
   if (!(o instanceof Patient other)) return false;
   return id != null && id.equals(other.getId());
   // hashCode:
   return Patient.class.hashCode(); // nie getClass(): proxy Hibernate ma inną klasę
   ```
   Testy: `model/PatientTest.java`, strażnik relacji `model/DoctorTest.java`.

4. **Klucz biznesowy vs UUID:** klucz biznesowy, gdy jest niezmienny od utworzenia obiektu; inaczej
   UUID z konstruktora. `idCardNo` się nie nadaje: zmienia się przy wymianie dowodu.

5. **toString:** wygenerowany przy `LAZY` daje `LazyInitializationException` poza transakcją,
   dodatkowe zapytania w niej i `StackOverflowError` przy relacji dwukierunkowej.
