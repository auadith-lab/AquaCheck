@echo off
title Push AquaCheck to GitHub
echo =========================================================
echo   Push AquaCheck to any GitHub Account (Browser Sign-in)
echo =========================================================
echo.
echo Make sure you have created an empty repository on GitHub.
echo Example URL: https://github.com/YOUR_USERNAME/AquaCheck.git
echo.

set /p REPO_URL="Enter your GitHub Repository HTTPS URL: "

if "%REPO_URL%"=="" (
    echo.
    echo [ERROR] No URL provided. Exiting.
    pause
    exit /b 1
)

echo.
echo Configuring remote origin to: %REPO_URL%
git remote set-url origin "%REPO_URL%" 2>nul || git remote add origin "%REPO_URL%"

echo.
echo Pushing to GitHub...
echo (A browser window will open if you need to sign in to your GitHub account)
echo.
git push -u origin main

echo.
if %ERRORLEVEL% EQU 0 (
    echo =========================================================
    echo   SUCCESS! AquaCheck has been uploaded to GitHub!
    echo =========================================================
) else (
    echo [INFO] If push failed, ensure the repository exists on GitHub and you are signed in to that account in your browser.
)

echo.
pause
