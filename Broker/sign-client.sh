#!/usr/bin/env sh
set -eu

# Assina offline um unico cliente com a chave privada do broker.
# Use quando quiser criar os arquivos de apenas um usuario/VM.
if [ "$#" -lt 1 ]; then
    echo "Uso: $0 nomeCliente"
    echo "Exemplo: $0 Cliente1"
    exit 1
fi

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BROKER_JAR="$SCRIPT_DIR/target/Broker.jar"

# O CertificateTool fica dentro do Broker.jar, por isso o build precisa existir.
if [ ! -f "$BROKER_JAR" ]; then
    echo "Broker.jar nao encontrado em: $BROKER_JAR"
    echo "Compile o broker antes com: mvn -q -DskipTests package"
    exit 1
fi

# Gera .cert e .private.key em Broker/certificados/clientes.
java -cp "$BROKER_JAR" com.mycompany.broker.CertificateTool sign-client "$1"
echo
echo "Para a VM do cliente, copie tambem:"
echo "  certificados/clientes/$1.cert"
echo "  certificados/clientes/$1.private.key"
echo "  certificados/ca.crt"
