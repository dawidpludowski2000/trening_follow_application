# Plan realizacji i wymagania

Zakres: lokalny dziennik treningów dla jednej osoby. Dane i funkcje aplikacji działają na urządzeniu, bez rejestracji, serwera i synchronizacji internetowej.

## Etapy i warunki przejścia
1. Dane i logika: Room (SQLite), walidacja, obliczenia. Bramka: testy jednostkowe, test CRUD/trwałości bazy na emulatorze, kompilacja.
2. Interfejs MVVM: plany, edytor, zapis treningu, historia, statystyki. Bramka: kompilacja, testy Compose (walidacja i pełny przepływ), zmiana konfiguracji.
3. Urządzenie i przypomnienia: lokalizacja na żądanie, powiadomienia, ustawienia. Bramka: test odmowy uprawnień, harmonogram i powiadomienie testowe.
4. Odbiór: lint, pełny zestaw testów, APK, instrukcja uruchomienia, diagramy, zrzuty i materiały do obrony.

## Macierz wymagań
| Wymaganie | Rozwiązanie | Weryfikacja |
|---|---|---|
| Plany treningowe | CRUD planów z pozycjami ćwiczeń | Test repozytorium i UI |
| Ćwiczenia, serie, powtórzenia, ciężar | Walidowany edytor; pozycja to grupa jednakowych serii | Testy granic i formularza |
| Aktywność i historia | Zapis wykonania, czas, data, notatka; edycja i usuwanie | Test CRUD, trwałości i UI |
| Czujnik lub lokalizacja | Opcjonalny odczyt lokalizacji podczas zapisu treningu | Emulator i odmowa dostępu |
| Statystyki i wykresy | Liczba treningów, czas, objętość; wykres ostatnich treningów | Test obliczeń i UI |
| Przypomnienia | Przybliżona codzienna pora, WorkManager; test powiadomienia | Test harmonogramu i urządzenia |
| Minimum 4 ekrany | Plany, Historia, Postępy, Ustawienia + edytor | Test nawigacji |
| MVVM | Compose → ViewModel → Repository → DAO → Room | Diagram i przegląd |
| Coroutines i Flow | Asynchroniczne DAO, obserwacja list, stan ViewModel | Testy bazy i działania |
| Material Design 3 | Komponenty Material 3, motyw jasny/ciemny | Zrzuty i test UI |
| Stan i konfiguracja | ViewModel + SavedStateHandle dla edytora | Test odtworzenia Activity |
| Błędy i bezpieczeństwo | Walidacja obu warstw, komunikaty, prywatna baza, brak backupu lokalizacji | Testy i lint |
| Dostępność | Etykiety, skalowalny tekst, przewijanie, tekstowa alternatywa wykresu | Kontrola UI |
| Git i dokumentacja | Historia etapów, instrukcje, diagramy, testy i APK | Końcowy odbiór |

## Świadome uproszczenia
- Serie o różnej liczbie powtórzeń lub ciężarze zapisuje się jako osobne pozycje tego samego ćwiczenia.
- Lokalizacja jest punktem zapisanym za zgodą użytkownika, nie trasą ani pomiarem kroków.
- Przypomnienia nie są alarmem o gwarantowanej dokładności; Android może je opóźnić.
- Dane pozostają na urządzeniu; odinstalowanie aplikacji usuwa dane.
- Wykres objętości jest opisowy. Porównywanie różnych zestawów ćwiczeń nie jest miarą kondycji.
