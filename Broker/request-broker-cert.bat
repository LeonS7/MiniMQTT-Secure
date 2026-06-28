@echo off
setlocal

rem Gera a requisicao de assinatura do certificado do broker para a AC.
rem O nome padrao minimqtt-broker evita prender o certificado ao IP da rede.
set "BROKER_NAME=%~1"
if "%BROKER_NAME%"=="" set "BROKER_NAME=minimqtt-broker"

rem Recusa IP/localhost porque, em redes bridge, o IP pode mudar na apresentacao.
powershell -NoProfile -Command "$name = $env:BROKER_NAME; $ip = $null; if ([Net.IPAddress]::TryParse($name, [ref] $ip)) { exit 1 }; if ($name.Equals('localhost', [StringComparison]::OrdinalIgnoreCase)) { exit 1 }; if ($name -notmatch '^[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$') { exit 1 }"
if errorlevel 1 goto invalid_name

set "BASE_DIR=%~dp0"
set "KEYTOOL=keytool"
where keytool > nul 2> nul
if errorlevel 1 (
    if exist "C:\Program Files\Java\jdk-22\bin\keytool.exe" set "KEYTOOL=C:\Program Files\Java\jdk-22\bin\keytool.exe"
)

if not exist "%BASE_DIR%certificados" mkdir "%BASE_DIR%certificados"

rem Se a chave ja existir, ela deve ser reaproveitada para manter correspondencia
rem entre broker-keystore.p12, broker.csr e o futuro broker.crt assinado.
if exist "%BASE_DIR%certificados\broker-keystore.p12" (
    echo Reutilizando chave privada existente em %BASE_DIR%certificados\broker-keystore.p12
) else (
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
      -ext "SAN=dns:%BROKER_NAME%" ^
      -noprompt

    if errorlevel 1 exit /b 1
)

rem A CSR contem somente a chave publica e a identidade do broker; ela pode ser
rem enviada ao professor. A chave privada permanece no broker-keystore.p12.
"%KEYTOOL%" -certreq ^
  -alias broker ^
  -file "%BASE_DIR%certificados\broker.csr" ^
  -keystore "%BASE_DIR%certificados\broker-keystore.p12" ^
  -storepass broker123 ^
  -ext "SAN=dns:%BROKER_NAME%"

if errorlevel 1 exit /b 1

echo.
echo CSR criada em: %BASE_DIR%certificados\broker.csr
echo Identidade do broker: %BROKER_NAME%
echo Envie somente broker.csr para a AC/professor.
echo Nao envie broker-keystore.p12.
exit /b 0

:invalid_name
echo Informe um nome DNS estavel para o broker, nao IP nem localhost.
echo Exemplo: %~nx0 minimqtt-broker
exit /b 1
