# MiniMQTT Secure

MiniMQTT Secure is a Java client/broker messaging system inspired by the MQTT
publish/subscribe model. The project provides a Swing client, a TCP broker,
client authentication with offline broker-signed certificates, broker
authentication with an external CA certificate, message buffering, encrypted
transport, and end-to-end encrypted payloads.

The implementation intentionally does not use TLS. The secure channel between
clients and broker is implemented with a custom digital envelope based on RSA
and AES/GCM.

Repository: https://github.com/LeonS7/MiniMQTT-Secure

## Features

- TCP broker on port `5000`.
- UDP broker discovery on port `5001`.
- Java Swing client interface.
- Account creation and login with certificate validation.
- Topic creation, subscription, unsubscription, deletion, and publication.
- Case-insensitive topic lookup.
- Automatic subscription for the creator of a topic.
- Automatic topic removal when the last subscriber leaves.
- Message origin display with topic and sender.
- Broker-side buffering of pending messages for offline subscribers.
- Pending message download after reconnecting.
- Broker certificate validation using `ca.crt`.
- Custom encrypted transport without TLS.
- End-to-end encrypted message payloads.

## Project Structure

```text
.
|-- Broker/
|   |-- src/main/java/com/mycompany/broker/
|   |-- src/main/java/com/mycompany/offlinecert/
|   |-- request-broker-cert.bat
|   |-- request-broker-cert.sh
|   |-- sign-client.bat
|   |-- sign-client.sh
|   |-- sign-vm-clients.bat
|   `-- sign-vm-clients.sh
|-- Client/
|   `-- src/main/java/
|-- README.md
`-- .gitignore
```

Generated certificates, private keys, Maven build outputs, and local broker data
are intentionally ignored by Git.

The `offlinecert` package contains only the local certificate signing tool used
by `sign-client` and `sign-vm-clients` scripts. The running broker does not call
those classes during normal message handling.

## Requirements

- JDK 17 or newer.
- Maven.

## Build

Build the broker:

```powershell
cd Broker
mvn -q -DskipTests package
```

Build the client:

```powershell
cd Client
mvn -q -DskipTests package
```

Generated JARs:

```text
Broker/target/Broker.jar
Client/target/Client.jar
```

## Certificate Layout

Broker certificate files:

```text
Broker/certificados/ca.crt
Broker/certificados/broker-keystore.p12
Broker/certificados/broker.csr
Broker/certificados/broker.crt
```

Client certificate files:

```text
Client/certificados/ca.crt
Client/certificados/clientes/<client-name>.cert
Client/certificados/clientes/<client-name>.private.key
```

The `broker-keystore.p12` file contains the broker private key and must never be
sent or committed. The `broker.csr` file contains only the certificate signing
request and is the file sent to the CA.

## Broker Certificate Request

Generate the broker CSR from the `Broker` module:

```powershell
cd Broker
.\request-broker-cert.bat
```

Linux/macOS equivalent:

```bash
cd Broker
sh request-broker-cert.sh
```

By default, the CSR uses the stable DNS identity:

```text
minimqtt-broker
```

The CSR requests `SAN=dns:minimqtt-broker` instead of binding the broker
certificate to an IP address. This avoids certificate validation failures when
the broker runs in a different bridged network and receives a different IP.

Send only this file to the CA:

```text
Broker/certificados/broker.csr
```

After receiving the signed certificate, save it as:

```text
Broker/certificados/broker.crt
```

If `broker-keystore.p12` already exists, the script reuses the existing private
key and generates a new CSR for the same key.

To use a custom broker identity:

```powershell
cd Broker
.\request-broker-cert.bat custom-broker-name
```

Clients must then be started with the same expected identity:

```powershell
java -Dminimqtt.broker.identity=custom-broker-name -jar target\Client.jar
```

## Client Certificates

Client certificates are generated offline by the broker. Build the broker first:

```powershell
cd Broker
mvn -q -DskipTests package
```

Generate the default VM users:

```powershell
.\sign-vm-clients.bat
```

Default clients:

```text
Cliente1
Cliente2
Cliente3
```

Generate custom clients:

```powershell
.\sign-vm-clients.bat VM1 VM2 VM3
```

Generated files are stored in:

```text
Broker/certificados/clientes/
```

When the `Client` module exists beside `Broker`, the scripts also copy the files
to:

```text
Client/certificados/
Client/certificados/clientes/
```

For each VM, copy only the certificate and private key for that user, plus
`ca.crt`:

```text
Client/certificados/ca.crt
Client/certificados/clientes/Cliente1.cert
Client/certificados/clientes/Cliente1.private.key
```

The login name must match the certificate file name. Example:

```text
Cliente1.cert -> login Cliente1
```

## Security Model

### Transport Encryption

The client/broker TCP channel is protected by a custom digital envelope:

1. The broker sends `broker.crt`.
2. The client validates `broker.crt` using `ca.crt`.
3. The client checks the broker identity, defaulting to `minimqtt-broker`.
4. The client generates an AES session key.
5. The AES key is encrypted with the broker RSA public key using RSA/OAEP.
6. Protocol messages are encrypted with AES/GCM.

This protects the channel against third-party traffic inspection without using
TLS.

### Client Authentication

The client sends its username, password, and broker-signed client certificate.
The broker validates:

- the account state;
- the password hash;
- the certificate owner;
- the certificate issuer;
- the broker signature over the certificate fields.

The client only sends requests. Authorization and validation rules are enforced
by the broker.

### End-To-End Payload Encryption

Transport encryption protects the TCP channel. In addition, message payloads are
encrypted end to end:

1. The broker sends topic member public keys to clients.
2. The sender creates an AES payload key.
3. The payload is encrypted with AES/GCM.
4. The payload key is encrypted with RSA/OAEP for each recipient.
5. The broker stores and forwards only the encrypted envelope.

The broker can route messages by topic and sender, but it cannot decrypt the
payload content.

## Running

Start the broker:

```powershell
cd Broker
java -jar target\Broker.jar
```

Start a client:

```powershell
cd Client
java -jar target\Client.jar
```

In bridged VM networks, UDP broadcast discovery may be blocked. In that case,
start the client with the current broker IP:

```powershell
java -jar target\Client.jar <broker-ip> 5000
```

Do not use `127.0.0.1` from a VM to reach the broker on the host machine. In a
VM, `127.0.0.1` points to the VM itself.

## Functional Validation

Recommended validation flow:

1. Build `Broker` and `Client`.
2. Generate or copy the broker certificate signed by the CA.
3. Generate client certificates with `sign-vm-clients`.
4. Copy `ca.crt`, `.cert`, and `.private.key` to each VM.
5. Start the broker.
6. Start three clients.
7. Create accounts using the names from the certificates.
8. Create a topic with one client.
9. Subscribe the other clients to the topic.
10. Enter the topic from the main client window.
11. Send messages and verify topic/sender display.
12. Disconnect one subscribed client, send messages, reconnect, and verify
    pending message download.
13. Cancel subscriptions until the last subscriber leaves and verify automatic
    topic removal.

## Troubleshooting

`Certificado nao encontrado.`

- The `.cert` file is missing from `Client/certificados/clientes`.
- The login name does not match the certificate file name.

`Chave privada nao encontrada.`

- The `.private.key` file is missing from `Client/certificados/clientes`.

`Certificado da AC nao encontrado.`

- `Client/certificados/ca.crt` is missing.

`Assinatura invalida.`

- The client certificate was generated by another broker key.
- `broker.crt` was not signed by the expected CA.
- `broker.crt` does not match `broker-keystore.p12`.

`Identidade do broker invalida.`

- The signed broker certificate does not contain the expected DNS identity.
- Regenerate the CSR with `request-broker-cert`.
- If a custom identity is used, start the client with
  `-Dminimqtt.broker.identity=<name>`.

`Broker nao encontrado.`

- UDP discovery may be blocked by the network.
- Start the client with `java -jar target\Client.jar <broker-ip> 5000`.

`Voce nao esta inscrito.`

- The main window `Entrar` button opens a conversation only after subscription.
- Subscribe to the topic in the configuration window first.

## Ignored Files

The following files must stay out of version control:

```text
target/
certificados/
*.key
*.cert
*.crt
*.csr
*.p12
usuarios.properties
```
