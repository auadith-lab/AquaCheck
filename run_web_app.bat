@echo off
title AquaCheck - Water Potability Predictor
echo ===================================================
echo   AquaCheck - Water Potability Predictor (ML)
echo ===================================================
echo.

set "PORT=8086"
set "PYTHON_EXE="

if exist "C:\Users\ACER\AppData\Roaming\uv\python\cpython-3.11-windows-x86_64-none\python.exe" (
    set "PYTHON_EXE=C:\Users\ACER\AppData\Roaming\uv\python\cpython-3.11-windows-x86_64-none\python.exe"
) else (
    where python >nul 2>nul && set "PYTHON_EXE=python"
)

if defined PYTHON_EXE (
    echo Starting local web server on port %PORT%...
    cd /d "%~dp0web"
    start "" "%PYTHON_EXE%" -m http.server %PORT%
    timeout /t 2 /nobreak >nul
    echo Opening AquaCheck in your browser...
    start http://localhost:%PORT%
    echo.
    echo App running at: http://localhost:%PORT%
    echo Keep this window open while using the web app.
) else (
    echo Opening AquaCheck directly in default browser...
    start "" "%~dp0web\index.html"
)

echo.
pause
