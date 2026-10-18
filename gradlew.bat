@echo off
setlocal
set APP_HOME=%~dp0
set GRADLE_VERSION=8.13
set BOOT=%USERPROFILE%\.gradle\dispensa-bootstrap
set GRADLE_EXE=%BOOT%\gradle-%GRADLE_VERSION%\bin\gradle.bat
if exist "%GRADLE_EXE%" goto runGradle
where gradle >NUL 2>NUL
if %ERRORLEVEL% EQU 0 (
  gradle -p "%APP_HOME:~0,-1%" %*
  exit /b %ERRORLEVEL%
)
if not exist "%BOOT%" mkdir "%BOOT%"
echo Gradle %GRADLE_VERSION% non presente: download della distribuzione ufficiale...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%BOOT%\gradle.zip'; Expand-Archive -LiteralPath '%BOOT%\gradle.zip' -DestinationPath '%BOOT%' -Force; Remove-Item '%BOOT%\gradle.zip' -Force"
if errorlevel 1 exit /b 1
:runGradle
call "%GRADLE_EXE%" -p "%APP_HOME:~0,-1%" %*
exit /b %ERRORLEVEL%

