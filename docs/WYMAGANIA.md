# Wymagania

Aplikacja służy jednej osobie do planowania i zapisywania treningów. Dane nie wymagają konta ani serwera.

| Wymaganie | Realizacja |
|---|---|
| Plany treningowe | Dodawanie, wyświetlanie, edycja i usuwanie planów. |
| Ćwiczenia i obciążenia | Nazwa ćwiczenia, liczba serii, powtórzeń i ciężar. |
| Historia treningów | Zapis, przeglądanie, edycja i usuwanie wykonanych treningów. |
| Funkcja telefonu | Opcjonalny jednorazowy odczyt lokalizacji. |
| Postępy | Liczba treningów, czas, objętość i wykres. |
| Przypomnienia | Możliwość ustawienia codziennego przypomnienia. |
| Interfejs | Ekrany planów, historii, postępów i ustawień oraz edytor. |
| Trwałość danych | Lokalna baza Room/SQLite. Dane pozostają po ponownym uruchomieniu aplikacji. |
| Jakość i bezpieczeństwo | MVVM, walidacja formularzy, komunikaty błędów i prywatna baza aplikacji. |
| Testy i dokumentacja | Testy jednostkowe i UI, instrukcja uruchomienia, diagramy i zrzuty ekranów. |

## Ograniczenia

- Lokalizacja jest zapisywana tylko po naciśnięciu przycisku i udzieleniu zgody.
- Przypomnienia mogą przyjść z opóźnieniem zależnie od Androida.
- Jedna pozycja ćwiczenia opisuje serie o takich samych powtórzeniach i ciężarze.