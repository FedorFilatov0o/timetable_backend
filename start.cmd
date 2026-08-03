@echo off
REM ============================================
REM Database Initialization Script
REM Timetable Back
REM ============================================

REM === SETTINGS ===
set DB_HOST=localhost
set DB_USER=postgres
set DB_PASS=8574
set DB_NAME=timetable

echo === Database Initialization ===

REM Execute SQL script for table creation
set PGPASSWORD=%DB_PASS%
psql -h %DB_HOST% -U %DB_USER% -d %DB_NAME% -f src\main\resources\db\init.sql
if errorlevel 1 (
    echo Error creating tables!
    exit /b 1
)

echo Tables created successfully

REM Execute script with initial data
if exist "src\main\resources\db\data.sql" (
    echo Executing data.sql...
    set PGPASSWORD=%DB_PASS%
    psql -h %DB_HOST% -U %DB_USER% -d %DB_NAME% -f src\main\resources\db\data.sql
)

echo.
echo === Database ready ===
