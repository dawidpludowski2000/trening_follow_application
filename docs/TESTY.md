# Testy i wyniki

Weryfikacja: 10 października 2026 r. Windows, JDK z Android Studio, Gradle 9.3.1. Emulator DziennikTest: Android 15 / API 35, x86_64.

| Sprawdzenie | Wynik |
|---|---|
| assembleDebug | Sukces, powstał instalowalny APK debug |
| assembleDebugAndroidTest | Sukces |
| testDebugUnitTest | 10 testów, 0 błędów, 0 niepowodzeń (w tym 1 przykład szablonu) |
| Główny zestaw na emulatorze | 7 testów, wszystkie przeszły |
| Odmowa uprawnień | 2 testy, oba przeszły |
| Odczyt GPS na pierwszym planie | 1 test, przeszedł dla pozycji 52.2297, 21.0122 |
| Scenariusz demonstracyjny i zrzuty | 1 dodatkowy scenariusz, przeszedł |
| Android Lint | 0 błędów, 19 ostrzeżeń |

## Zakres testów

**TrainingTest (5):** puste nazwy/listy, granice serii i powtórzeń, ujemne i niepoprawne ciężary, NaN/infinity, przecinek dziesiętny, czas i notatka, obliczenie objętości i liczby powtórzeń.

**ReminderTimeTest (4):** termin dzisiaj, termin jutro, wybrana godzina równa bieżącej oraz odrzucenie niepoprawnej godziny.

**RepositoryTest (2):** tworzenie, odczyt, edycja i usuwanie; ponowne otwarcie bazy; trwałość współrzędnych; niezależność historii od planu; usuwanie zależnych wierszy; odrzucenie błędnej edycji bez utraty poprzednich danych.

**AppFlowTest (3):** pełny scenariusz plan → trening → statystyki → edycja → usuwanie; potwierdzenie/anulowanie usunięcia; walidacja i odrzucenie formularza; odtworzenie Activity; serializacja pełnego formularza.

**DeviceIntegrationTest (2):** unikatowy harmonogram, zmiana godziny, anulowanie, trwałość ustawień; worker nie wysyła powiadomień po wyłączeniu i publikuje powiadomienie po włączeniu. Test sprawdza wpis w NotificationManager i obecność akcji otwierającej aplikację.

**PermissionDeniedTest (2):** brak lokalizacji zgłaszany kontrolowanym wyjątkiem; próba powiadomienia bez zgody zwraca odmowę bez awarii.

**LocationIntegrationTest (1):** prawdziwy odczyt przez LocationManagerCompat w aktywnej Activity, przy zgodzie użytkownika, dla pozycji dostarczanej przez emulator.

## Odtwarzanie

Zwykłe testy: polecenia w README. Testy specjalne domyślnie są pomijane, ponieważ potrzebują określonych warunków urządzenia. Kompletny zestaw emulatora API 33+ uruchamia:

```powershell
.\scripts\Test-Emulator.ps1 -Serial emulator-5554
```

Skrypt kompiluje projekt, uruchamia testy JVM/lint, instaluje oba APK, uruchamia zestaw główny, odbiera zgody na czas testów odmowy i przesyła testową lokalizację. GPS działa tylko na pierwszym planie. Nie jest to test odbiornika fizycznego telefonu.

**Testy usuwają dane dziennika na emulatorze.** Skrypt nie przyjmuje identyfikatora fizycznego telefonu. Wyniki nowych uruchomień zapisuje w `test-results/` (ignorowanym przez Git). Wyniki odbioru znajdują się w `docs/test-results/`.

Przykładowe dane i zrzuty można przygotować po instalacji APK testowego:

```powershell
adb -s emulator-5554 shell am instrument -w -e class pl.dawid.dzienniktreningowy.DemoScreenshotsTest -e demo true pl.dawid.dzienniktreningowy.test/androidx.test.runner.AndroidJUnitRunner
```

## Usterka znaleziona i poprawiona

Pierwszy test pełnego przepływu ujawnił, że komunikat po zapisie planu mógł zasłonić przycisk zapisu następnego formularza. Otwarcie edytora czyści teraz poprzedni komunikat, a Snackbar nie jest nakładany na edytor. Po poprawce pełny scenariusz przeszedł.

Test GPS początkowo wykonywał odczyt bez aktywnej Activity. Ponieważ aplikacja celowo nie ma uprawnienia lokalizacji w tle, test poprawiono tak, aby odzwierciedlał faktyczne użycie na pierwszym planie.

## Ostrzeżenia lint i ograniczenia weryfikacji

19 ostrzeżeń dotyczy głównie dostępności nowszych wersji zależności oraz sugestii stylu KTX. Nie aktualizowano całego stosu wyłącznie w celu usunięcia sugestii aktualizacji. Nie wykryto błędów lint blokujących kompilację.

Sprawdzono emulator API 35, odtworzenie Activity oraz widoki na mniejszym ekranie, w poziomie i w motywie ciemnym. Nie przeprowadzono testów na fizycznym telefonie, pełnego audytu dostępności z TalkBack ani wielodniowego badania punktualności przypomnień. Minimalne API 24 wynika z konfiguracji i analizy kompilacji, nie z testu na urządzeniu API 24.
