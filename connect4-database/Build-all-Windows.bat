@echo off
echo Starte Docker Build für alle Repositories...
echo.

call :build_image "connect4-chat"
call :build_image "connect4-database"
call :build_image "connect4-frontend"
call :build_image "connect4-gaming"
call :build_image "connect4-gateway"
call :build_image "connect4-matchmaking"
call :build_image "connect4-security"
call :build_image "connect4-statistics"
call :build_image "connect4-bot"
call :build_image "connect4-friendlist"

echo.
echo Alle Images wurden erfolgreich gebaut!
echo.
echo ================================
echo Lade RabbitMQ Image herunter...
echo ================================

docker pull rabbitmq
if errorlevel 1 (
    echo Fehler beim Herunterladen des RabbitMQ Images
    exit /b 1
)

echo Erfolgreich: RabbitMQ Image heruntergeladen
echo.
echo Build-Prozess vollstaendig abgeschlossen!
goto :eof

:build_image
echo ================================
echo Baue: %~1
echo ================================
docker build -f "%~1/Dockerfile" -t "%~1" .
if errorlevel 1 (
    echo Fehler beim Bauen von %~1
    exit /b 1
)
echo Erfolgreich: %~1
echo.
goto :eof