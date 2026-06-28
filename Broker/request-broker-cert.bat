@echo off
setlocal

if "%~1"=="" (
    echo Uso: %~nx0 IP_DO_BROKER
    echo Exemplo: %~nx0 192.168.56.10
    exit /b 1
)

set "BROKER_IP=%~1"
if /i "%BROKER_IP%"=="IP_REAL_DO_BROKER" goto invalid_ip

powershell -NoProfile -Command "$parsed = $null; if (-not [Net.IPAddress]::TryParse($env:BROKER_IP, [ref] $parsed) -or $parsed.AddressFamily -ne [Net.Sockets.AddressFamily]::InterNetwork) { exit 1 }; if ([Net.IPAddress]::IsLoopback($parsed)) { exit 2 }"
if errorlevel 2 goto invalid_loopback
if errorlevel 1 goto invalid_ip

set "BASE_DIR=%~dp0"
set "KEYTOOL=keytool"
where keytool > nul 2> nul
if errorlevel 1 (
    if exist "C:\Program Files\Java\jdk-22\bin\keytool.exe" set "KEYTOOL=C:\Program Files\Java\jdk-22\bin\keytool.exe"
)

if not exist "%BASE_DIR%certificados" mkdir "%BASE_DIR%certificados"

if exist "%BASE_DIR%certificados\broker-keystore.p12" (
    echo Ja existe broker-keystore.p12 em %BASE_DIR%certificados
    echo Para gerar uma nova CSR com outro IP, renomeie ou remova esse arquivo manualmente.
    exit /b 1
)

"%KEYTOOL%" -genkeypair ^
  -alias broker ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity 365 ^
  -keystore "%BASE_DIR%certificados\broker-keystore.p12" ^
  -storetype PKCS12 ^
  -storepass broker123 ^
  -keypass broker123 ^
  -dname "CN=MiniMQTT Broker, OU=Redes, O=MiniMQTT Secure, L=Brusque, ST=SC, C=BR" ^
  -ext "SAN=ip:%BROKER_IP%" ^
  -noprompt

if errorlevel 1 exit /b 1

"%KEYTOOL%" -certreq ^
  -alias broker ^
  -file "%BASE_DIR%certificados\broker.csr" ^
  -keystore "%BASE_DIR%certificados\broker-keystore.p12" ^
  -storepass broker123 ^
  -ext "SAN=ip:%BROKER_IP%"

if errorlevel 1 exit /b 1

echo.
echo CSR criada em: %BASE_DIR%certificados\broker.csr
echo Envie somente broker.csr para a AC/professor.
echo Nao envie broker-keystore.p12.
exit /b 0

:invalid_loopback
echo Informe o IP real da VM/maquina do broker, nao localhost/127.0.0.1/::1.
exit /b 1

:invalid_ip
echo Informe um IPv4 valido da VM/maquina do broker. Exemplo: %~nx0 192.168.56.10
exit /b 1
