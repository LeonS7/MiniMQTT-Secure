#!/usr/bin/env sh
set -eu

if [ "$#" -lt 1 ]; then
    echo "Uso: $0 IP_DO_BROKER"
    echo "Exemplo: $0 192.168.56.10"
    exit 1
fi

BROKER_IP="$1"
case "$BROKER_IP" in
    localhost|LOCALHOST|127.*|::1)
        echo "Informe o IP real da VM/maquina do broker, nao localhost/127.0.0.1/::1."
        exit 1
        ;;
esac

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CERT_DIR="$SCRIPT_DIR/certificados"
mkdir -p "$CERT_DIR"

if [ -f "$CERT_DIR/broker-keystore.p12" ]; then
    echo "Ja existe broker-keystore.p12 em $CERT_DIR"
    echo "Para gerar uma nova CSR com outro IP, renomeie ou remova esse arquivo manualmente."
    exit 1
fi

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
  -ext "SAN=ip:$BROKER_IP" \
  -noprompt

keytool -certreq \
  -alias broker \
  -file "$CERT_DIR/broker.csr" \
  -keystore "$CERT_DIR/broker-keystore.p12" \
  -storepass broker123 \
  -ext "SAN=ip:$BROKER_IP"

echo
echo "CSR criada em: $CERT_DIR/broker.csr"
echo "Envie somente broker.csr para a AC/professor."
echo "Nao envie broker-keystore.p12."
