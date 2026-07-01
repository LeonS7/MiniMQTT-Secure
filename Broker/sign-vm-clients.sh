#!/usr/bin/env sh
set -eu

# Assina offline os clientes usados nas VMs da apresentacao.
# Sem argumentos, gera Cliente1, Cliente2 e Cliente3.
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BROKER_JAR="$SCRIPT_DIR/target/Broker.jar"
BROKER_CERT_DIR="$SCRIPT_DIR/certificados/clientes"
CLIENT_CERT_DIR="$SCRIPT_DIR/../Client/certificados/clientes"

# A ferramenta offline fica em com.mycompany.offlinecert dentro do Broker.jar.
# Ela nao e usada pelo BrokerServer; existe aqui apenas para os scripts gerarem
# certificados antes da execucao dos clientes/VMs.
if [ ! -f "$BROKER_JAR" ]; then
    echo "Broker.jar nao encontrado em: $BROKER_JAR"
    echo "Compile o broker antes com: mvn -q -DskipTests package"
    exit 1
fi

if [ "$#" -eq 0 ]; then
    set -- Cliente1 Cliente2 Cliente3
fi

if [ -d "$SCRIPT_DIR/../Client" ]; then
    # Facilita teste local copiando os certificados tambem para o modulo Client.
    mkdir -p "$CLIENT_CERT_DIR"
    if [ -f "$SCRIPT_DIR/certificados/ca.crt" ]; then
        cp "$SCRIPT_DIR/certificados/ca.crt" "$SCRIPT_DIR/../Client/certificados/"
        echo "Certificado da AC copiado para: $SCRIPT_DIR/../Client/certificados/ca.crt"
    fi
fi

# Cada nome informado gera um par de chaves proprio e um certificado assinado.
for client_name in "$@"; do
    echo
    echo "Gerando certificado para $client_name..."
    java -cp "$BROKER_JAR" com.mycompany.offlinecert.CertificateTool sign-client "$client_name" "$BROKER_CERT_DIR"

    if [ -d "$CLIENT_CERT_DIR" ]; then
        cp "$SCRIPT_DIR/certificados/clientes/$client_name.cert" "$CLIENT_CERT_DIR/"
        cp "$SCRIPT_DIR/certificados/clientes/$client_name.private.key" "$CLIENT_CERT_DIR/"
        echo "Certificado copiado para: $CLIENT_CERT_DIR/$client_name.cert"
        echo "Chave privada copiada para: $CLIENT_CERT_DIR/$client_name.private.key"
    fi
done

echo
echo "Certificados gerados em: $SCRIPT_DIR/certificados/clientes"
echo "Copie para cada VM o .cert e o .private.key do usuario daquela VM, alem do ca.crt."
