# MiniMQTT Secure

Projeto Java da AV3 de Redes de Computadores II. A aplicacao implementa uma
infraestrutura publish/subscribe inspirada no MQTT, com broker TCP, clientes
Swing, autenticacao por certificados, bufferizacao de mensagens e criptografia.

Repositorio: https://github.com/LeonS7/MiniMQTT-Secure

## Funcionalidades

- Broker TCP na porta `5000`.
- Descoberta automatica do broker via UDP na porta `5001`.
- Cliente Swing com login, criacao de conta, topicos e chat.
- Cliente pode participar de varios topicos simultaneamente.
- Mensagens mostram cliente e topico de origem.
- Broker mantem mensagens pendentes ate todos os inscritos baixarem.
- Cliente baixa mensagens pendentes ao reconectar.
- Cliente e autenticado por certificado assinado offline pelo broker.
- Broker e autenticado por certificado assinado pela AC do professor.
- Trafego TCP usa envelopamento digital proprio, sem TLS.
- Payload das mensagens usa criptografia ponta a ponta.

## Estrutura

```text
.
|-- Broker/
|-- Client/
|-- README.md
`-- .gitignore
```

## Requisitos

- JDK 17 ou superior.
- Maven.

## Build

```powershell
cd Broker
mvn -q -DskipTests package

cd ..\Client
mvn -q -DskipTests package
```

JARs gerados:

```text
Broker/target/Broker.jar
Client/target/Client.jar
```

## Certificados

Na versao final existem dois conjuntos de certificados.

Certificado do broker:

- `Broker/certificados/ca.crt`: certificado da AC do professor.
- `Broker/certificados/broker-keystore.p12`: keystore local com a chave privada do broker.
- `Broker/certificados/broker.csr`: requisicao enviada ao professor/AC.
- `Broker/certificados/broker.crt`: certificado assinado que o professor devolve.

Para gerar uma CSR sem `localhost` ou `127.0.0.1`, use o IP real da maquina do
broker. Cada VM deve ter seu proprio IP na rede e o broker deve usar o IP dele:

```powershell
cd Broker
.\request-broker-cert.bat 192.168.56.10
```

No Linux/macOS:

```bash
cd Broker
sh request-broker-cert.sh 192.168.56.10
```

Troque `192.168.56.10` pelo IP real do broker. O projeto nao usa loopback como
endereco padrao, a descoberta UDP ignora interfaces de loopback e os scripts
recusam `localhost`, `127.*` e `::1`.

Envie ao professor somente:

```text
Broker/certificados/broker.csr
```

Depois que ele devolver o certificado assinado, salve como:

```text
Broker/certificados/broker.crt
```

Nao envie nem apague o `broker-keystore.p12`, porque ele contem a chave privada
que corresponde a CSR enviada.

Certificados dos clientes:

- cada cliente tem um `.cert` assinado offline pelo broker;
- cada cliente tambem precisa do seu `.private.key` para abrir mensagens ponta a ponta;
- cada VM precisa de `Client/certificados/ca.crt` para validar o broker.

## Gerar Clientes Das VMs

Na maquina do broker:

```powershell
cd Broker
mvn -q -DskipTests package
.\sign-vm-clients.bat
```

Sem argumentos, o script cria:

```text
Cliente1
Cliente2
Cliente3
```

Para nomes customizados:

```powershell
.\sign-vm-clients.bat VM1 VM2 VM3
```

O script gera os arquivos em:

```text
Broker/certificados/clientes/
```

e, se a pasta `Client` estiver ao lado do `Broker`, tambem copia para:

```text
Client/certificados/clientes/
Client/certificados/ca.crt
```

Em cada VM, copie:

```text
Client/certificados/ca.crt
Client/certificados/clientes/NomeDoUsuario.cert
Client/certificados/clientes/NomeDoUsuario.private.key
```

O nome digitado no login precisa ser igual ao nome do certificado. Exemplo:

```text
Cliente1.cert -> login Cliente1
```

## Criptografia

O projeto nao usa TLS. O canal TCP usa envelopamento digital proprio:

1. o broker envia `broker.crt`;
2. o cliente valida `broker.crt` com `ca.crt`;
3. o cliente gera uma chave AES de sessao;
4. essa chave AES e cifrada com RSA/OAEP usando a chave publica do broker;
5. o protocolo passa a trafegar cifrado com AES/GCM.

O payload das publicacoes tambem e criptografado ponta a ponta:

1. o broker envia aos clientes as chaves publicas dos inscritos no topico;
2. o cliente cifra a mensagem com AES/GCM;
3. a chave AES do payload e cifrada para cada destinatario com RSA/OAEP;
4. o broker armazena e encaminha somente o envelope cifrado;
5. apenas os clientes destinatarios conseguem abrir o conteudo.

Assim, o broker consegue ler cabecalho/topico/remetente para rotear, mas nao
consegue decodificar o payload da mensagem.

## Execucao

Inicie primeiro o broker:

```powershell
cd Broker
java -jar target\Broker.jar
```

Depois inicie os clientes:

```powershell
cd Client
java -jar target\Client.jar
```

## Fluxo De Teste

1. Gere `Cliente1`, `Cliente2` e `Cliente3` com `sign-vm-clients`.
2. Copie `ca.crt`, `.cert` e `.private.key` para cada VM.
3. Inicie o broker com `broker.crt` ja assinado pela AC.
4. Crie conta em cada VM usando o nome do certificado.
5. Crie um topico em `Cliente1`.
6. Inscreva `Cliente2` e `Cliente3` nesse topico.
7. Abra o topico na tela principal antes de enviar mensagens.
8. Deixe um cliente offline, envie mensagens e reconecte-o para testar pendencias.

## Erros Comuns

`Certificado nao encontrado.`

- Falta o `.cert` em `Client/certificados/clientes`.
- O nome do login nao bate com o nome do arquivo.

`Chave privada nao encontrada.`

- Falta o `.private.key` do usuario em `Client/certificados/clientes`.

`Certificado da AC nao encontrado.`

- Falta `Client/certificados/ca.crt`.

`Assinatura invalida.`

- O certificado do cliente foi gerado por outro broker.
- Ou `broker.crt` nao foi assinado pela AC correta.
- Ou `broker.crt` nao corresponde ao `broker-keystore.p12` usado na CSR.

`Voce nao esta inscrito.`

- O botao `Entrar` da tela principal nao inscreve automaticamente.
- Inscreva-se antes pela tela de configuracoes.

## Arquivos Ignorados

Nao envie ao GitHub:

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
