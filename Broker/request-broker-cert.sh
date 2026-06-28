#!/usr/bin/env sh
set -eu

# Gera a requisicao de assinatura do certificado do broker para a AC.
# O nome padrao minimqtt-broker evita prender o certificado ao IP da rede.
BROKER_NAME="${1:-minimqtt-broker}"

# Recusa IP/localhost porque, em redes bridge, o IP pode mudar na apresentacao.
if ! printf '%s\n' "$BROKER_NAME" | awk '
{
    if ($0 == "" || tolower($0) == "localhost") exit 1
    if ($0 ~ /^[0-9]+(\.[0-9]+){3}$/) exit 1
    labelCount = split($0, labels, ".")
    for (i = 1; i <= labelCount; i++) {
        if (labels[i] !~ /^[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?$/) exit 1
    }
    exit 0
}'; then
    echo "Informe um nome DNS estavel para o broker, nao IP nem localhost."
    echo "Exemplo: $0 minimqtt-broker"
    exit 1
fi

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CERT_DIR="$SCRIPT_DIR/certificados"
mkdir -p "$CERT_DIR"

# Se a chave ja existir, ela deve ser reaproveitada para manter correspondencia
# entre broker-keystore.p12, broker.csr e o futuro broker.crt assinado.
if [ -f "$CERT_DIR/broker-keystore.p12" ]; then
    echo "Reutilizando chave privada existente em $CERT_DIR/broker-keystore.p12"
else
    keytool -genkeypair \
      -alias broker \
      -keyalg RSA \
      -keysize 2048 \
      -validity 365 \
      -keystore "$CERT_DIR/broker-keystore.p12" \
      -storetype PKCS12 \
      -storepass broker123 \
      -keypass broker123 \
      -dname "CN=MiniMQTT Broker, OU=Redes, O=MiniMQTT Secure, L=Brusque, ST=SC, C=BR" \
      -ext "SAN=dns:$BROKER_NAME" \
      -noprompt
fi

# A CSR contem somente a chave publica e a identidade do broker; ela pode ser
# enviada ao professor. A chave privada permanece no broker-keystore.p12.
keytool -certreq \
  -alias broker \
  -file "$CERT_DIR/broker.csr" \
  -keystore "$CERT_DIR/broker-keystore.p12" \
  -storepass broker123 \
  -ext "SAN=dns:$BROKER_NAME"

echo
echo "CSR criada em: $CERT_DIR/broker.csr"
echo "Identidade do broker: $BROKER_NAME"
echo "Envie somente broker.csr para a AC/professor."
echo "Nao envie broker-keystore.p12."
