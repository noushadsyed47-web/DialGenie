@echo off
REM DialGenie - Simple Application Startup Script
REM Starts entire application

setlocal enabledelayedexpansion
set PATH=C:\tools\apache-maven-3.9.6\bin;C:\Program Files\Java\jdk-17\bin;%PATH%

cls
echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║       DialGenie - Local Application Startup            ║
echo ╚════════════════════════════════════════════════════════╝
echo.

echo [1/8] Checking prerequisites...
java -version >nul 2>&1
if errorlevel 1 goto :error_java
call mvn -v >nul 2>&1
if errorlevel 1 goto :error_mvn
node -v >nul 2>&1
if errorlevel 1 goto :error_node
call npm -v >nul 2>&1
if errorlevel 1 goto :error_npm
python --version >nul 2>&1
if errorlevel 1 goto :error_python

echo   [OK] All prerequisites installed
echo.

echo [2/8] Creating logs directory...
if not exist "logs" mkdir logs
echo   [OK] Ready
echo.

echo [3/8] Checking database schema...
if not exist "backend\shared\src\main\resources\schema.sql" goto :error_schema
echo   [OK] Schema file found
echo.

echo [4/8] Building shared module...
cd backend\shared
call mvn clean install -DskipTests -q
if errorlevel 1 goto :error_build
cd ..\..
echo   [OK] Build successful
echo.

echo [5/8] Starting backend services...

start "DialGenie - Auth Service" cmd /k "cd /d C:\DialGenie\backend\auth-service && mvn spring-boot:run"
timeout /t 2 /nobreak >nul

start "DialGenie - Lead Service" cmd /k "cd /d C:\DialGenie\backend\lead-service && mvn spring-boot:run"
timeout /t 2 /nobreak >nul

start "DialGenie - Campaign Service" cmd /k "cd /d C:\DialGenie\backend\campaign-service && mvn spring-boot:run"
timeout /t 2 /nobreak >nul

start "DialGenie - Call Service" cmd /k "cd /d C:\DialGenie\backend\call-service && mvn spring-boot:run"
timeout /t 2 /nobreak >nul

start "DialGenie - API Gateway" cmd /k "cd /d C:\DialGenie\backend\api-gateway && mvn spring-boot:run"
timeout /t 2 /nobreak >nul

echo   [OK] Backend services started
echo.

echo [6/8] Starting AI service...
start "DialGenie - AI Service" cmd /k "cd /d C:\DialGenie\ai-service && pip install -r requirements.txt -q && python -m uvicorn src.main.python.main:app --port 8085"
timeout /t 2 /nobreak >nul
echo   [OK] AI Service started
echo.

echo [7/8] Starting frontend...
start "DialGenie - Frontend" cmd /k "cd /d C:\DialGenie\frontend && npm install -q && npm run dev"
timeout /t 2 /nobreak >nul
echo   [OK] Frontend started
echo.

echo [8/8] All services started!
echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║          [OK] DialGenie is Running!                    ║
echo ╚════════════════════════════════════════════════════════╝
echo.

echo Service URLs:
echo   Frontend:           http://localhost:3000
echo   API Gateway:        http://localhost:8080
echo   Auth Service:       http://localhost:8081
echo   Lead Service:       http://localhost:8082
echo   Campaign Service:   http://localhost:8083
echo   Call Service:       http://localhost:8084
echo   AI Service:         http://localhost:8085
echo.

echo Login Credentials:
echo   Email:    admin@dialgenie.com
echo   Password: admin@123
echo.

echo All services are running in separate windows above.
echo Close this window or press any key to continue...
pause

goto :eof

:error_java
echo   [ERROR] Java not found
exit /b 1

:error_mvn
echo   [ERROR] Maven not found
exit /b 1

:error_node
echo   [ERROR] Node.js not found
exit /b 1

:error_npm
echo   [ERROR] npm not found
exit /b 1

:error_python
echo   [ERROR] Python not found
exit /b 1

:error_schema
echo   [ERROR] Schema file not found
exit /b 1

:error_build
echo   [ERROR] Maven build failed
exit /b 1
