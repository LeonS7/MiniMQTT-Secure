@echo off
setlocal

rem Assina offline um unico cliente com a chave privada do broker.
rem Use quando quiser criar os arquivos de apenas um usuario/VM.
if "%~1"=="" (
    echo Uso: %~nx0 nomeCliente
    echo Exemplo: %~nx0 Cliente1
    exit /b 1
)

set "BASE_DIR=%~sdp0"
pushd "%BASE_DIR%"
set "BROKER_JAR=target\Broker.jar"
set "BROKER_CERT_DIR=certificados\clientes"

rem A ferramenta offline fica em com.mycompany.offlinecert dentro do Broker.jar.
rem Ela nao e usada pelo BrokerServer; existe aqui apenas para os scripts gerarem
rem certificados antes da execucao dos clientes/VMs.
if not exist "%BROKER_JAR%" (
    echo Broker.jar nao encontrado em: %BASE_DIR%%BROKER_JAR%
    echo Compile o broker antes com: mvn -q -DskipTests package
    popd
    exit /b 1
)

rem Gera .cert e .private.key em Broker\certificados\clientes.
java -cp "%BROKER_JAR%" com.mycompany.offlinecert.CertificateTool sign-client "%~1" "%BROKER_CERT_DIR%"
if errorlevel 1 (
    popd
    exit /b 1
)
echo.
echo Para a VM do cliente, copie tambem:
echo   certificados\clientes\%~1.cert
echo   certificados\clientes\%~1.private.key
echo   certificados\ca.crt
popd
