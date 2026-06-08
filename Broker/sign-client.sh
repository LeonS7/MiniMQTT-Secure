#!/usr/bin/env sh
set -eu

if [ "$#" -lt 1 ]; then
    echo "Uso: $0 nomeCliente"
    echo "Exemplo: $0 Cliente1"
    exit 1
fi

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BROKER_JAR="$SCRIPT_DIR/target/Broker.jar"

if [ ! -f "$BROKER_JAR" ]; then
    echo "Broker.jar nao encontrado em: $BROKER_JAR"
    echo "Compile o broker antes com: mvn -q -DskipTests package"
    exit 1
fi

java -cp "$BROKER_JAR" com.mycompany.broker.CertificateTool sign-client "$1"
