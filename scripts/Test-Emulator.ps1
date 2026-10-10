param([string]$Serial = 'emulator-5554')
$ErrorActionPreference = 'Stop'
# Skrypt przeznaczony WYŁĄCZNIE dla emulatora. Testy czyszczą dane dziennika.
if ($Serial -notmatch '^emulator-\d+$') { throw 'Użyj osobnego emulatora testowego.' }
$projectPath = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $projectPath
if (-not $env:JAVA_HOME -and (Test-Path -LiteralPath 'D:\AndroidStudio\jbr')) { $env:JAVA_HOME = 'D:\AndroidStudio\jbr' }
$androidSdk = $env:ANDROID_HOME
if (-not $androidSdk) { $androidSdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$adbPath = Join-Path $androidSdk 'platform-tools\adb.exe'
$package = 'pl.dawid.dzienniktreningowy'
$reportPath = Join-Path $projectPath 'test-results'
New-Item -ItemType Directory -Path $reportPath -Force | Out-Null
function Adb {
    $result = & $adbPath -s $Serial @args 2>&1
    if ($LASTEXITCODE -ne 0) { throw ($result -join "`n") }
    return $result
}
function Run-Test([string]$Name, [string]$Classes, [string]$Scenario = '') {
    $parameters = @('shell','am','instrument','-w','-r','-e','class',$Classes)
    if ($Scenario) { $parameters += @('-e','deviceScenario',$Scenario) }
    $parameters += "$package.test/androidx.test.runner.AndroidJUnitRunner"
    $result = Adb @parameters
    $result | Set-Content -LiteralPath (Join-Path $reportPath "$Name.txt") -Encoding utf8
    $text = $result -join "`n"
    if ($text -notmatch 'OK \(\d+ tests?\)' -or $text -match 'FAILURES!!!') { throw "Test $Name nie przeszedł. Sprawdź raport." }
    Write-Host "$Name : OK"
}
if ([int]((Adb shell getprop ro.build.version.sdk) -join '') -lt 33) { throw 'Ten zestaw integracyjny wymaga emulatora API 33 lub nowszego.' }
& .\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug --console=plain
if ($LASTEXITCODE -ne 0) { throw 'Kompilacja lub testy jednostkowe nie przeszły.' }
Adb install -r 'app/build/outputs/apk/debug/app-debug.apk'
Adb install -r 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
Run-Test 'main-device-suite' "$package.AppFlowTest,$package.RepositoryTest,$package.DeviceIntegrationTest"
foreach ($permission in @('ACCESS_FINE_LOCATION','ACCESS_COARSE_LOCATION','POST_NOTIFICATIONS')) { Adb shell pm revoke $package "android.permission.$permission" }
Run-Test 'denied' "$package.PermissionDeniedTest" 'denied'
$gpsJob = Start-Job -ArgumentList $adbPath,$Serial -ScriptBlock {
    param($deviceAdb,$deviceSerial)
    while ($true) { & $deviceAdb -s $deviceSerial emu geo fix 21.0122 52.2297 | Out-Null; Start-Sleep -Seconds 1 }
}
try { Run-Test 'gps' "$package.LocationIntegrationTest" 'gps' }
finally { Stop-Job $gpsJob; Remove-Job $gpsJob }
Write-Host "Wyniki zapisano w $reportPath"
