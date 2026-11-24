@echo off
setlocal
REM Crea en el PostgreSQL local el usuario y la base "promou" que usa el backend (perfil dev).
REM Pide la contrasena del usuario "postgres" que se definio al instalar PostgreSQL.
cd /d "%~dp0"

set "PSQL="
for /d %%D in ("%ProgramFiles%\PostgreSQL\*") do if exist "%%D\bin\psql.exe" set "PSQL=%%D\bin\psql.exe"
if not defined PSQL (
    where psql >nul 2>nul && set "PSQL=psql"
)
if not defined PSQL (
    echo No se encontro psql.exe. Revisa la instalacion de PostgreSQL.
    pause
    exit /b 1
)

echo Usando "%PSQL%"
echo.
echo Escribe la contrasena del usuario postgres, la que pusiste al instalar PostgreSQL:
"%PSQL%" -h localhost -p 5432 -U postgres -d postgres -v ON_ERROR_STOP=1 -f "%~dp0crear-bd-local.sql"
if errorlevel 1 (
    echo.
    echo Hubo un error. Revisa el mensaje de arriba.
    pause
    exit /b 1
)

echo.
echo Listo: base "promou" con usuario "promou" y contrasena "promou".
echo Las tablas las crea Flyway la primera vez que ejecutes run-local.cmd.
pause
