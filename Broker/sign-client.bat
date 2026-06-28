@echo off
setlocal

rem Assina offline um unico cliente com a chave privada do broker.
rem Use quando quiser criar os arquivos de apenas um usuario/VM.
if "%~1"=="" (
    echo Uso: %~nx0 nomeCliente
    echo Exemplo: %~nx0 Cliente1
    exit /b 1
)

set "BASE_DIR=%~dp0"
set "BROKER_JAR=%BASE_DIR%target\Broker.jar"

rem O CertificateTool fica dentro do Broker.jar, por isso o build precisa existir.
if not exist "%BROKER_JAR%" (
    echo Broker.jar nao encontrado em: %BROKER_JAR%
    echo Compile o broker antes com: mvn -q -DskipTests package
    exit /b 1
)

rem Gera .cert e .private.key em Broker\certificados\clientes.
java -cp "%BROKER_JAR%" com.mycompany.broker.CertificateTool sign-client "%~1"
echo.
echo Para a VM do cliente, copie tambem:
echo   certificados\clientes\%~1.cert
echo   certificados\clientes\%~1.private.key
echo   certificados\ca.crt
