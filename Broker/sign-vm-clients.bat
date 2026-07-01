@echo off
setlocal enabledelayedexpansion

rem Assina offline os clientes usados nas VMs da apresentacao.
rem Sem argumentos, gera Cliente1, Cliente2 e Cliente3.
set "BASE_DIR=%~sdp0"
pushd "%BASE_DIR%"
set "BROKER_JAR=target\Broker.jar"
set "BROKER_CERT_DIR=certificados\clientes"
set "CLIENT_CERT_DIR=%BASE_DIR%..\Client\certificados\clientes"

rem A ferramenta offline fica em com.mycompany.offlinecert dentro do Broker.jar.
rem Ela nao e usada pelo BrokerServer; existe aqui apenas para os scripts gerarem
rem certificados antes da execucao dos clientes/VMs.
if not exist "%BROKER_JAR%" (
    echo Broker.jar nao encontrado em: %BASE_DIR%%BROKER_JAR%
    echo Compile o broker antes com: mvn -q -DskipTests package
    popd
    exit /b 1
)

if "%~1"=="" (
    set "CLIENTS=Cliente1 Cliente2 Cliente3"
) else (
    set "CLIENTS=%*"
)

if exist "%BASE_DIR%..\Client" (
    rem Facilita teste local copiando os certificados tambem para o modulo Client.
    if not exist "%CLIENT_CERT_DIR%" mkdir "%CLIENT_CERT_DIR%"
    if exist "%BASE_DIR%certificados\ca.crt" (
        copy /Y "%BASE_DIR%certificados\ca.crt" "%BASE_DIR%..\Client\certificados\" > nul
        echo Certificado da AC copiado para: %BASE_DIR%..\Client\certificados\ca.crt
    )
)

rem Cada nome informado gera um par de chaves proprio e um certificado assinado.
for %%C in (%CLIENTS%) do (
    echo.
    echo Gerando certificado para %%~C...
    java -cp "%BROKER_JAR%" com.mycompany.offlinecert.CertificateTool sign-client "%%~C" "%BROKER_CERT_DIR%"
    if errorlevel 1 (
        popd
        exit /b 1
    )

    if exist "%CLIENT_CERT_DIR%" (
        copy /Y "%BASE_DIR%certificados\clientes\%%~C.cert" "%CLIENT_CERT_DIR%\" > nul
        copy /Y "%BASE_DIR%certificados\clientes\%%~C.private.key" "%CLIENT_CERT_DIR%\" > nul
        echo Certificado copiado para: %CLIENT_CERT_DIR%\%%~C.cert
        echo Chave privada copiada para: %CLIENT_CERT_DIR%\%%~C.private.key
    )
)

echo.
echo Certificados gerados em: %BASE_DIR%certificados\clientes
echo Copie para cada VM o .cert e o .private.key do usuario daquela VM, alem do ca.crt.
popd
