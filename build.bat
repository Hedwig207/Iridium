@echo off
chcp 65001 >nul
setlocal

rem =====================================================
rem  Iridium build script (Windows)
rem  Builds the mod jar into build/libs/iridium-*.jar
rem =====================================================

set "ROOT=%~dp0"
set "G88=%USERPROFILE%\.gradle\wrapper\dists\gradle-8.8-bin\manual\gradle-8.8\bin\gradle.bat"

where gradle >nul 2>nul && set "SYSTEM_GRADLE=1"
powershell -Command "$j='iridium-1.0.0.jar'; if($j -eq ''){$j='iridium.jar'}" >nul 2>nul

echo [Iridium] Build script started.
echo.

rem --- Pick the gradle to use ---
set "GRADLE_BIN="
if exist "%G88%" (
    echo [Iridium] Using local Gradle 8.8.
    set "GRADLE_BIN=%G88%"
) else (
    if defined SYSTEM_GRADLE (
        echo [Iridium] Local Gradle 8.8 not found, falling back to PATH gradle.
        set "GRADLE_BIN=gradle"
    ) else (
        echo [Iridium] ERROR: No suitable Gradle found.
        echo [Iridium] Expected: "%G88%"
        echo [Iridium] Or install gradle and add it to PATH.
        pause
        exit /b 1
    )
)

rem --- Run the build ---
echo [Iridium] Building...
call "%GRADLE_BIN%" %* --no-daemon --console=plain
set "RC=%ERRORLEVEL%"

echo.
if not "%RC%"=="0" (
    echo [Iridium] BUILD FAILED ^(exit code %RC%^)
) else (
    echo [Iridium] BUILD SUCCESSFUL.
    echo [Iridium] Output:
    for %%f in ("%ROOT%build\libs\iridium-1.0.0.jar") do if exist %%f echo   - build\libs\iridium-1.0.0.jar  (%%~zf bytes)
)
echo.
endlocal & exit /b %RC%