# Dziennik treningowy

Prosta aplikacja na Androida do planowania treningów, zapisywania wykonanych ćwiczeń i przeglądania postępów. Dane są przechowywane lokalnie na urządzeniu.

## Co można zrobić
- Tworzyć, edytować i usuwać plany treningowe.
- Zapisywać wykonane ćwiczenia, serie, powtórzenia, ciężar, czas i notatkę.
- Przeglądać historię oraz statystyki z wykresem.
- Opcjonalnie dodać lokalizację treningu i ustawić przypomnienia.
- W Ustawieniach wczytać przykładowe dane. Ta opcja działa tylko przy pustym dzienniku.

## Uruchomienie
1. Otwórz główny folder projektu w Android Studio.
2. Poczekaj na synchronizację Gradle i zainstaluj wymagany Android SDK.
3. Wybierz emulator lub telefon i uruchom konfigurację `app`.

APK debug po kompilacji: `app/build/outputs/apk/debug/app-debug.apk`.

## Testy
W PowerShell, w folderze projektu:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Testy na emulatorze czyszczą dane aplikacji. Nie uruchamiaj ich na telefonie z własnymi danymi.

## Dokumentacja
- [Wymagania](docs/WYMAGANIA.md)
- [Architektura i baza danych](docs/ARCHITEKTURA.md)
- [Testy](docs/TESTY.md)
- Zrzuty ekranów: [plany](docs/screenshots/01-plany.png), [historia](docs/screenshots/02-historia.png), [postępy](docs/screenshots/03-postepy.png), [ustawienia](docs/screenshots/04-ustawienia.png)

Repozytorium: [github.com/dawidpludowski2000/trening_follow_application](https://github.com/dawidpludowski2000/trening_follow_application).