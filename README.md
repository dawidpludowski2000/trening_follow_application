# Inteligentny dziennik aktywności fizycznej

Projekt 3 na Androida. Prosta aplikacja do planowania treningów, zapisywania wykonania i analizy historii. Interfejs w języku polskim, dane lokalne, bez konta i serwera.

## Funkcje
- Plany: dodawanie, odczyt, edycja i usuwanie planu oraz pozycji ćwiczeń.
- Wykonanie: kopia ćwiczeń z planu lub trening bez planu; serie, powtórzenia, kg, data, czas i notatka.
- Historia: zapis, edycja, usuwanie i opcjonalna lokalizacja punktowa.
- Postępy: liczba treningów i serii, czas aktywności, objętość oraz wykres ostatnich siedmiu treningów.
- Przypomnienia: codzienna przybliżona pora, włączanie/wyłączanie i powiadomienie testowe.
- Stan formularza zachowany przy odtworzeniu ekranu; walidacja i komunikaty błędów.

## Uruchomienie w Android Studio
1. Otwórz katalog główny projektu (z `settings.gradle.kts`).
2. Pozwól zakończyć synchronizację Gradle. Potrzebny jest internet do pobrania zależności.
3. Użyj JDK dostarczonego z Android Studio jako Gradle JDK. Projekt sprawdzono na Android Studio 2025.3.2, AGP 9.1.1 i Gradle 9.3.1.
4. Zainstaluj platformę SDK 36.1 i narzędzia wskazane przez Android Studio. Minimalna wersja urządzenia to Android 7.0 / API 24.
5. Wybierz emulator lub telefon z debugowaniem USB i uruchom konfigurację `app`.

Na komputerze, na którym przygotowano projekt, Android Studio jest w `D:\AndroidStudio`, projekt w `C:\DziennikTreningowy`, a emulator testowy ma nazwę `DziennikTest` (Android 15 / API 35).

Plik `local.properties` jest lokalny i nie należy do repozytorium. Android Studio utworzy go zgodnie z lokalizacją SDK na danym komputerze.

## Budowanie i testy
Z katalogu projektu, w PowerShell (Java musi być skonfigurowana):

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Jest podpisany kluczem debug i nadaje się do lokalnej prezentacji, nie jest wydaniem do sklepu.

**Testy interfejsu czyszczą dane aplikacji na urządzeniu testowym. Uruchamiaj je na osobnym emulatorze, nie na telefonie z własną historią.**

Testy specjalne GPS i odebranych uprawnień wymagają przygotowania emulatora. Opis i wyniki: [docs/TESTY.md](docs/TESTY.md). Scenariusz zrzutów i przykładowych danych jest opt-in; aplikacja nie wstawia ich użytkownikowi automatycznie.

## Struktura i technologie
- `data/`: encje Room, DAO, baza i repozytorium z transakcjami.
- `domain/`: walidacja, obliczenia i wyznaczanie opóźnienia przypomnienia.
- `ui/`: ekrany Compose, formularz i ViewModel ze stanem zapisanym w SavedStateHandle.
- `device/`: lokalizacja na żądanie i WorkManager/powiadomienia.
- `app/src/test/`: testy jednostkowe.
- `app/src/androidTest/`: testy bazy, interfejsu i funkcji telefonu.
- `app/schemas/`: eksport schematu Room do kontroli wersji.

Kotlin, Compose Material 3, MVVM, Room/SQLite, Coroutines i Flow, WorkManager, LocationManagerCompat, SharedPreferences. Bez Hilt, serwera, REST i zewnętrznej biblioteki wykresów. Wykres rysowany jest na Compose Canvas z tekstowym zestawieniem tych samych danych.

## Ograniczenia i prywatność
- Jedna pozycja opisuje grupę jednakowych serii. Różne ciężary/powtórzenia zapisuj jako osobne pozycje.
- Lokalizacja jest dobrowolnym punktem miejsca treningu, nie trasą ani licznikiem kroków. Odczyt wyłącznie na pierwszym planie po naciśnięciu przycisku, do 15 sekund.
- Przypomnienie WorkManager jest przybliżone. Oszczędzanie baterii, wyłączenie telefonu lub wymuszone zatrzymanie aplikacji może je opóźnić. Zmiana strefy lub czasu letniego może przesunąć porę cyklu 24 h; ponownie ustaw godzinę w Ustawieniach.
- Nie ma konta, chmury, synchronizacji ani eksportu danych użytkownika. Baza jest w prywatnym katalogu aplikacji; kopie systemowe i transfer danych są wyłączone. Odinstalowanie usuwa dane. Baza nie jest dodatkowo szyfrowana przez aplikację; dostęp opiera się na izolacji Androida i blokadzie urządzenia.
- Aplikacja nie wymaga uprawnienia INTERNET. Zapis działa bez sieci; dostawca przybliżonej lokalizacji w systemie może korzystać z usług systemowych.
- Statystyka objętości nie jest medyczną oceną kondycji ani porównaniem jakości różnych treningów.

## Dokumentacja i prezentacja
- [Macierz wymagań](docs/WYMAGANIA.md)
- [Architektura, baza i przypadki użycia](docs/ARCHITEKTURA.md)
- [Testy i wyniki](docs/TESTY.md)
- [Scenariusz obrony](docs/OBRONA.md)
- Zrzuty najważniejszych widoków: `docs/screenshots/`.

## GitHub
Lokalne repozytorium ma historię etapów. Publikacja na GitHubie wymaga repozytorium na koncie autora i autoryzacji tego konta. Nie wpisano fikcyjnego adresu. Po opublikowaniu dodaj rzeczywisty link w dokumentacji przed oddaniem.
