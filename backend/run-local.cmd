@echo off
REM Arranca el backend en local (perfil dev: crea admin@plataforma.com / Descuentos@04).
REM Requiere PostgreSQL en localhost:5432 con la base "promou" (usuario y contraseña "promou").
cd /d "%~dp0"
if not defined JAVA_HOME for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-21*") do set "JAVA_HOME=%%D"
set "MVN=mvn"
where mvn >nul 2>nul || set "MVN=%USERPROFILE%\tools\apache-maven-3.9.11\bin\mvn.cmd"
call "%MVN%" -B spring-boot:run -Dspring-boot.run.profiles=dev
