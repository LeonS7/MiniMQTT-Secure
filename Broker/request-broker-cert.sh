#!/usr/bin/env sh
set -eu

if [ "$#" -lt 1 ]; then
    echo "Uso: $0 IP_DO_BROKER"
    echo "Exemplo: $0 192.168.56.10"
    exit 1
fi

BROKER_IP="$1"
case "$BROKER_IP" in
    IP_REAL_DO_BROKER)
        echo "Informe um IPv4 valido da maquina onde o broker roda. Exemplo: $0 192.168.56.10"
        exit 1
        ;;
    localhost|LOCALHOST|127.*|::1)
        echo "Informe o IPv4 real da maquina onde o broker roda, nao localhost/127.0.0.1/::1."
        exit 1
        ;;
esac

if ! printf '%s\n' "$BROKER_IP" | awk -F. 'NF == 4 { for (i = 1; i <= 4; i++) { if ($i !~ /^[0-9]+$/ || $i < 0 || $i > 255) exit 1 } exit 0 } { exit 1 }'; then
    echo "Informe um IPv4 valido da maquina onde o broker roda. Exemplo: $0 192.168.56.10"
    exit 1
fi

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
