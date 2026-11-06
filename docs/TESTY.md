# Testy

Projekt sprawdzono testami jednostkowymi, testami bazy i interfejsu na emulatorze.

## Ostatnie sprawdzenie

6 listopada 2026 r. kompilacja APK i testy jednostkowe zakończyły się powodzeniem. Na emulatorze przeszedł też test wczytania przykładowych danych z Ustawień i pokazania ich w statystykach.

Wcześniej sprawdzono również zapis i edycję planów oraz treningów, historię, przypomnienia i obsługę zgód urządzenia. Testy uruchamiane na emulatorze czyszczą jego dane.

## Uruchomienie testów

Z folderu projektu w PowerShell:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Do testów interfejsu potrzebny jest uruchomiony emulator. Testy lokalizacji i przypomnień mogą wymagać skonfigurowania uprawnień emulatora.