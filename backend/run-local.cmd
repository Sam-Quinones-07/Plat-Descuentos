@echo off
setlocal
REM Arranca el backend en local (perfil dev: crea admin@plataforma.com / Descuentos@04).
REM Requiere PostgreSQL en localhost:5432 con la base "promou": ejecuta antes crear-bd-local.cmd.
cd /d "%~dp0"

REM 1. Java: prefiere un JDK 21; si no hay, usa el JDK que este instalado.
if not defined JAVA_HOME for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-21*" "%ProgramFiles%\Java\jdk-21*") do set "JAVA_HOME=%%D"
if not defined JAVA_HOME for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-*" "%ProgramFiles%\Java\jdk-*") do set "JAVA_HOME=%%D"
if not defined JAVA_HOME (
    echo No se encontro un JDK. Instala Java 21 con: winget install EclipseAdoptium.Temurin.21.JDK
    pause
    exit /b 1
)
echo Usando JAVA_HOME=%JAVA_HOME%
REM Con un JDK distinto de 21, Hibernate necesita este ajuste de Byte Buddy para funcionar.
set "JVM_ARGS="
echo %JAVA_HOME% | find "jdk-21" >nul || set "JVM_ARGS=-Dspring-boot.run.jvmArguments=-Dnet.bytebuddy.experimental=true"

REM 2. Maven: usa el instalado o lo descarga una sola vez en %USERPROFILE%\tools.
set "MVN=mvn"
where mvn >nul 2>nul && goto :arrancar
set "MVN=%USERPROFILE%\tools\apache-maven-3.9.11\bin\mvn.cmd"
if exist "%MVN%" goto :arrancar
echo Descargando Maven 3.9.11, solo la primera vez...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $t=Join-Path $env:USERPROFILE 'tools'; New-Item -ItemType Directory -Force -Path $t | Out-Null; $z=Join-Path $env:TEMP 'maven-3.9.11.zip'; Invoke-WebRequest -UseBasicParsing -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip' -OutFile $z; Expand-Archive -Path $z -DestinationPath $t -Force; Remove-Item $z"
if not exist "%MVN%" (
    echo No se pudo descargar Maven. Revisa la conexion a internet.
    pause
    exit /b 1
)

:arrancar
call "%MVN%" -B spring-boot:run -Dspring-boot.run.profiles=dev %JVM_ARGS%
pause
