@echo off
title Install AquaCheck APK
echo ===================================================
echo   Installing AquaCheck APK to connected device
echo ===================================================
echo.

set "ADB=D:\AndroidToolchain\android-sdk\platform-tools\adb.exe"
if not exist "%ADB%" (
    where adb >nul 2>nul && set "ADB=adb"
)

"%ADB%" devices
echo.

if not exist "%~dp0AquaCheck.apk" (
    if exist "%~dp0app\build\outputs\apk\debug\app-debug.apk" (
        copy /y "%~dp0app\build\outputs\apk\debug\app-debug.apk" "%~dp0AquaCheck.apk" >nul
    )
)

if exist "%~dp0AquaCheck.apk" (
    echo Installing AquaCheck.apk...
    "%ADB%" install -r "%~dp0AquaCheck.apk"
) else (
    echo [ERROR] AquaCheck.apk not found. Please build the APK first with gradlew assembleDebug.
)

echo.
pause
