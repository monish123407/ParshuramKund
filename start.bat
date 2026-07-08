@echo off
echo ========================================================
echo       Starting Parshuram Kund Mela 2027 Application    
echo ========================================================

REM 1. Free ports 8081 and 4200 (if any processes are running on them)
echo Freeing port 8081...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081') do taskkill /f /pid %%a >nul 2>&1
echo Freeing port 4200...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :4200') do taskkill /f /pid %%a >nul 2>&1

REM 2. Start Backend in a new window
echo Starting Spring Boot Backend in a new terminal window...
start "Spring Boot Backend" cmd /k "cd \"ParshuramKund Backend\" && mvnw.cmd spring-boot:run"

REM 3. Start Frontend in a new window
echo Starting Angular Frontend in a new terminal window...
start "Angular Frontend" cmd /k "cd \"ParshuramKund\" && npm start"

echo --------------------------------------------------------
echo Both servers have been launched!
echo - Backend: http://localhost:8081
echo - Frontend: http://localhost:4200
echo Close the individual command prompt windows to stop them.
echo --------------------------------------------------------
pause
