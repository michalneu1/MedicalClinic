Description:

Parameter 0 of constructor in com.medicalclinic.notification.VisitReminderService required a single bean, but 2 were found:
- emailReminderSender: defined in file [C:\Users\micha\IdeaProjects\MedicalClinic\target\classes\com\medicalclinic\notification\EmailReminderSender.class]
- smsReminderSender: defined in file [C:\Users\micha\IdeaProjects\MedicalClinic\target\classes\com\medicalclinic\notification\SmsReminderSender.class]

This may be due to missing parameter name information

Action:

Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans, or using @Qualifier to identify the bean that should be consumed


Beana potrzebował VisitReminderService a nie wybrał bo są 2 i nie wie który bean wybrać.
Ten problem może rozwiązać np @Primary bo nadaje priorytet;

1. Niczym dla springa dla czytajacego wskazuje warstwe;
2. Bo to cudzy kod dlatego nie ma gdzie wpisac @Component, rozwiazanie jest metoda ktora go zwraca w klasie konfiguracji
3. Nieaktualny build lub spring nie widzi klasy