# Prezentacja i obrona (około 5–7 minut)

1. **Cel (30 s):** lokalny dziennik treningowy jednej osoby. Zamiast konta i serwera: prostota, praca offline, prywatna baza.
2. **Plan (1 min):** utwórz „Trening A”, dodaj przysiad: 3 serie, 10 powtórzeń, 20 kg. Pokaż odrzucenie pustej nazwy lub ujemnego ciężaru. Zapisz.
3. **Wykonanie (1 min):** wybierz „Zapisz wykonanie”, zmień rzeczywisty ciężar, czas i datę. Opcjonalnie dodaj lokalizację. Zapisz, pokaż historię, edytuj trening.
4. **Statystyki (45 s):** wskaż objętość: 3 × 10 × 20 = 600 kg. Pokaż wykres; porównuje objętość, nie kondycję.
5. **Urządzenie (45 s):** pokaż zgodę na lokalizację i możliwość pracy bez niej. W ustawieniach włącz przypomnienie, wybierz porę i użyj powiadomienia testowego.
6. **Technika (1 min):** pokaż diagram MVVM, encje Room, transakcję i Flow. Obróć urządzenie w trakcie edycji. Wyjaśnij niezależność historii od planów.
7. **Jakość (1 min):** pokaż testy i raport wyników, historię Git, prywatną bazę oraz dokumentację.

## Najczęstsze pytania

**Dlaczego MVVM?** Interfejs nie wykonuje zapytań SQL. ViewModel zarządza stanem ekranu i akcjami, repozytorium odpowiada za zapis. To wystarcza dla małej aplikacji.

**Czym jest Room?** Biblioteka nad SQLite. Encje opisują tabele, DAO zapytania, a kompilator generuje kod i sprawdza zgodność zapytań.

**Po co transakcja?** Plan i jego lista ćwiczeń muszą być zapisane razem. Nie może zostać połowa nowej listy po błędzie.

**Po co Coroutines i Flow?** Coroutines służą do operacji asynchronicznych, Flow dostarcza kolejne wersje danych z bazy, a Compose aktualizuje widok.

**Co po usunięciu planu?** Historia zostaje, bo trening ma własną kopię nazwy i ćwiczeń. CASCADE usuwa tylko pozycje należące do usuwanego rekordu.

**Co po obrocie telefonu?** Stan formularza jest w ViewModel i SavedStateHandle, a nie wyłącznie w polach widoku.

**Czy przypomnienie przyjdzie dokładnie o 18:00?** Nie ma takiej gwarancji. WorkManager wykonuje zadania zgodnie z ograniczeniami Androida; UI o tym informuje. Nie prosimy o uprawnienie do dokładnych alarmów.

**Czy aplikacja śledzi użytkownika?** Nie. Odczytuje punkt lokalizacji na żądanie w otwartym formularzu, z limitem czasu i możliwością odmowy.

**Czy baza jest szyfrowana?** Nie stosujemy dodatkowego szyfrowania aplikacyjnego. Korzystamy z prywatnego katalogu i izolacji Androida; zalecana jest blokada urządzenia. Kopie i transfer danych są wyłączone.

**Co oznacza 0 kg?** Ćwiczenie bez zewnętrznego obciążenia. Jego objętość w kg wynosi zero, ale serie i czas aktywności nadal są uwzględniane.

**Jak rozbudować aplikację?** Można dodać katalog ćwiczeń, eksport, osobne wpisy każdej serii i synchronizację. Wymaga to rozszerzenia modelu i migracji bazy.

**Jak przebiegały prace?** Projekt rozwijano etapami. Po kolejnych częściach uruchamiano testy jednostkowe i testy na emulatorze.

## Przed oddaniem
- Dodaj wymagane przez uczelnię dane autora i grupy.
- Opublikuj repozytorium i wklej rzeczywisty link.
- Sprawdź APK na urządzeniu, na którym będziesz prezentować.
- Przygotuj przykładowe treningi. Dane demonstracyjne w emulatorze nie są zaszyte w APK.
