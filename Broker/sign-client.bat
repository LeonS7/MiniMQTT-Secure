@echo off
setlocal

if "%~1"=="" (
    echo Uso: %~nx0 nomeCliente
    echo Exemplo: %~nx0 Cliente1
    exit /b 1
)

set "BASE_DIR=%~dp0"
set "BROKER_JAR=%BASE_DIR%target\Broker.jar"

if not exist "%BROKER_JAR%" (
    echo Broker.jar nao encontrado em: %BROKER_JAR%
    echo Compile o broker antes com: mvn -q -DskipTests package
    exit /b 1
)

java -cp "%BROKER_JAR%" com.mycompany.broker.CertificateTool sign-client "%~1"
