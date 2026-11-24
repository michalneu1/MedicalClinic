# Rollback przy zastępstwie

`POST /doctors/1/substitutions {"substituteId": 4}`. Zastępca ma wizytę o 10:30, więc konflikt
jest przy drugiej wizycie → 409.

## Przed zastępstwem

```
 id | doctor_id |      starts_at
----+-----------+---------------------
  1 |         1 | 2030-01-14 10:00:00   ← wizyta doktora 1
  2 |         1 | 2030-01-14 10:30:00   ← wizyta doktora 1, koliduje z 3
  3 |         4 | 2030-01-14 10:30:00   ← własna wizyta zastępcy
```

## Wariant A: bez `@Transactional`, `save` w pętli

```
 id | doctor_id |      starts_at
----+-----------+---------------------
  1 |         4 | 2030-01-14 10:00:00
  2 |         1 | 2030-01-14 10:30:00
  3 |         4 | 2030-01-14 10:30:00
```

## Wariant B: `@Transactional`, bez `save`

```
 id | doctor_id |      starts_at
----+-----------+---------------------
  1 |         1 | 2030-01-14 10:00:00
  2 |         1 | 2030-01-14 10:30:00
  3 |         4 | 2030-01-14 10:30:00
```

```
Creating new transaction with name [com.medicalclinic.service.DoctorService.substitute]
Hibernate: update visits set doctor_id=?,ends_at=?,patient_id=?,starts_at=? where id=?
Initiating transaction rollback
Rolling back JPA transaction on EntityManager [SessionImpl(532518805<open>)]
```

## Dlaczego

- **A:** każdy `save` to osobna transakcja z commitem. Pierwsza wizyta jest już zapisana,
  zanim poleci wyjątek, więc nie ma czego cofnąć.
- **B:** cała metoda to jedna transakcja. Wyjątek → rollback wszystkiego.
- **`update` w logu:** Hibernate robi flush przed zapytaniem o konflikt, ale wewnątrz
  niezatwierdzonej transakcji. Rollback go anuluje, więc w bazie nic się nie zmienia.
