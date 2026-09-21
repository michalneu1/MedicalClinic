### 1. Wymień trzy problemy `main`-a, który ręcznie tworzy wszystkie obiekty aplikacji.

Po pierwsze, taki `main` rośnie razem z aplikacją.
Po drugie, trzeba pilnować kolejności tworzenia i tego, żeby serwis
istniał w jednej instancji, a nie w kilku przypadkowych kopiach.


### 2. Po czym poznajesz klasę modelową, a po czym funkcjonalną? Które z nich oddajemy Springowi i dlaczego tylko te?

Klasa modelowa reprezentuje rzecz i przechowuje dane. Klasa funkcjonalna nie przechowuje stanu,
tylko wykonuje operacje, dlatego jedna instancja wystarcza całej aplikacji.
Springowi oddajemy klasy funkcjonalne, bo są bezstanowe, a ich
zależności są znane już w momencie startu aplikacji. Klas modelowych nie oddajemy, bo ich
liczba i wartości zależą od danych pojawiających się w trakcie działania.

### 3. Co to jest bean, a co kontener? Jedno zdanie na każde pojęcie.

Bean to obiekt, który tworzy i którym zarządza Spring. Kontener to ta część Springa, która
te obiekty przechowuje: wykrywa klasy oznaczone adnotacjami, tworzy z nich instancje,
rozwiązuje zależności między nimi i udostępnia je tam, gdzie są potrzebne.

### 4. Czym różni się wstrzykiwanie przez konstruktor od wstrzykiwania przez pole? Podaj dwie przewagi konstruktora.
Przez konstruktor zależność trafia do obiektu od razu, w chwili jego utworzenia, a w pole
Spring wstawia ją później, do już gotowego obiektu. Pierwsza przewaga konstruktora:
pole może być final, więc obiekt nigdy nie istnieje bez swoich zależności.
Druga: w teście mozna podac atrapę.

### 5. Czym różni się `new` od konstruktora?

`new` to operator,  wywołuje konstruktor obiektu.
Konstruktor  opisuje, jak obiekt ma zostać zainicjalizowany.

### 6. Opisz, jak mini-kontener z zajęć buduje kontroler: co dzieje się krok po kroku i w jakiej kolejności powstają obiekty.
Kontener najpierw skanuje pakiet i zapisuje, jakie klasy ma do dyspozycji,
ale żadnego obiektu jeszcze nie tworzy. Kiedy proszę o kontroler, sprawdza jego konstruktor:
kontroler potrzebuje serwisu, serwis repozytorium,
a repozytorium już niczego. Obiekty
powstają więc od końca — repozytorium, serwis, kontroler.

### 7. Czym różni się odwrócenie sterowania (IoC) od wstrzykiwania zależności (DI)?
 Odwrócenie sterowania oznacza, że o moim kodzie decyduje framework, sam nie tworzę obiektów i sam nie wywołuję swoich
 metod. Wstrzykiwanie zależności to ten sam pomysł zawężony do jednej rzeczy: klasa dostaje współpracowników,
 w konstruktorze. Dlatego DI jest rodzajem IoC.

