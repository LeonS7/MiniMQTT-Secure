@echo off
setlocal enabledelayedexpansion

set "BASE_DIR=%~dp0"
set "BROKER_JAR=%BASE_DIR%target\Broker.jar"
set "CLIENT_CERT_DIR=%BASE_DIR%..\Client\certificados\clientes"

if not exist "%BROKER_JAR%" (
    echo Broker.jar nao encontrado em: %BROKER_JAR%
    echo Compile o broker antes com: mvn -q -DskipTests package
    exit /b 1
)

if "%~1"=="" (
    set "CLIENTS=Cliente1 Cliente2 Cliente3"
) else (
    set "CLIENTS=%*"
)

if exist "%BASE_DIR%..\Client" (
    if not exist "%CLIENT_CERT_DIR%" mkdir "%CLIENT_CERT_DIR%"
)

for %%C in (%CLIENTS%) do (
    echo.
    echo Gerando certificado para %%~C...
    java -cp "%BROKER_JAR%" com.mycompany.broker.CertificateTool sign-client "%%~C"
    if errorlevel 1 exit /b 1

    if exist "%CLIENT_CERT_DIR%" (
        copy /Y "%BASE_DIR%certificados\clientes\%%~C.cert" "%CLIENT_CERT_DIR%\" > nul
        echo Certificado copiado para: %CLIENT_CERT_DIR%\%%~C.cert
    )
)

echo.
echo Certificados gerados em: %BASE_DIR%certificados\clientes
echo Copie para cada VM apenas o .cert do usuario daquela VM.
