# MiniMQTT Secure

O **MiniMQTT Secure** é um sistema de mensagens cliente/broker desenvolvido em Java, inspirado no modelo **publish/subscribe** do MQTT. O projeto fornece um cliente com interface Swing, um broker TCP, autenticação de clientes por meio de certificados assinados offline pelo broker, autenticação do broker utilizando um certificado de uma Autoridade Certificadora (CA) externa, bufferização de mensagens, transporte criptografado e criptografia de ponta a ponta das mensagens.

A implementação foi projetada intencionalmente **sem utilizar TLS**. O canal seguro entre clientes e broker é implementado por meio de um envelope digital personalizado baseado em **RSA** e **AES/GCM**.

**Repositório:** https://github.com/LeonS7/MiniMQTT-Secure

## Funcionalidades

* Broker TCP na porta `5000`.
* Descoberta automática do broker via UDP na porta `5001`.
* Interface gráfica desenvolvida em Java Swing.
* Criação de contas e login com validação de certificados.
* Criação, inscrição, cancelamento de inscrição, exclusão e publicação em tópicos.
* Busca de tópicos sem diferenciação entre letras maiúsculas e minúsculas.
* Inscrição automática do criador de um tópico.
* Remoção automática do tópico quando o último inscrito sai.
* Exibição da origem da mensagem com o tópico e o remetente.
* Bufferização de mensagens pendentes no broker para clientes offline.
* Download automático das mensagens pendentes após a reconexão.
* Validação do certificado do broker utilizando `ca.crt`.
* Transporte criptografado personalizado sem utilização de TLS.
* Criptografia de ponta a ponta do conteúdo das mensagens.

## Estrutura do Projeto

```text
.
|-- Broker/
|   |-- src/main/java/
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

Os certificados gerados, chaves privadas, arquivos produzidos pelo Maven e os dados locais do broker são propositalmente ignorados pelo Git.

## Requisitos

* JDK 17 ou superior.
* Maven.

## Compilação

Compile o broker:

```powershell
cd Broker
mvn -q -DskipTests package
```

Compile o cliente:

```powershell
cd Client
mvn -q -DskipTests package
```

Arquivos JAR gerados:

```text
Broker/target/Broker.jar
Client/target/Client.jar
```

## Estrutura dos Certificados

Arquivos do certificado do broker:

```text
Broker/certificados/ca.crt
Broker/certificados/broker-keystore.p12
Broker/certificados/broker.csr
Broker/certificados/broker.crt
```

Arquivos dos certificados dos clientes:

```text
Client/certificados/ca.crt
Client/certificados/clientes/<nome-do-cliente>.cert
Client/certificados/clientes/<nome-do-cliente>.private.key
```

O arquivo `broker-keystore.p12` contém a chave privada do broker e **nunca deve ser enviado ou versionado**. O arquivo `broker.csr` contém apenas a solicitação de assinatura do certificado (CSR) e é o único arquivo que deve ser enviado para a Autoridade Certificadora (CA).

## Solicitação do Certificado do Broker

Gere o CSR a partir do módulo `Broker`:

```powershell
cd Broker
.\request-broker-cert.bat
```

Equivalente para Linux/macOS:

```bash
cd Broker
sh request-broker-cert.sh
```

Por padrão, o CSR utiliza a identidade DNS estável:

```text
minimqtt-broker
```

O CSR solicita `SAN=dns:minimqtt-broker` em vez de vincular o certificado a um endereço IP. Isso evita falhas de validação quando o broker é executado em redes diferentes e recebe um novo endereço IP.

Envie apenas o seguinte arquivo para a CA:

```text
Broker/certificados/broker.csr
```

Após receber o certificado assinado, salve-o como:

```text
Broker/certificados/broker.crt
```

Se o arquivo `broker-keystore.p12` já existir, o script reutilizará a chave privada existente e gerará um novo CSR para a mesma chave.

Para utilizar uma identidade personalizada:

```powershell
cd Broker
.\request-broker-cert.bat nome-personalizado
```

Os clientes deverão ser iniciados utilizando a mesma identidade esperada:

```powershell
java -Dminimqtt.broker.identity=nome-personalizado -jar target\Client.jar
```

## Certificados dos Clientes

Os certificados dos clientes são gerados offline pelo broker. Compile o broker primeiro:

```powershell
cd Broker
mvn -q -DskipTests package
```

Gerar os usuários padrão para máquinas virtuais:

```powershell
.\sign-vm-clients.bat
```

Clientes padrão:

```text
Cliente1
Cliente2
Cliente3
```

Gerar clientes personalizados:

```powershell
.\sign-vm-clients.bat VM1 VM2 VM3
```

Os arquivos gerados são armazenados em:

```text
Broker/certificados/clientes/
```

Quando o módulo `Client` estiver ao lado do módulo `Broker`, os scripts também copiarão automaticamente os arquivos para:

```text
Client/certificados/
Client/certificados/clientes/
```

Para cada máquina virtual, copie apenas o certificado e a chave privada correspondentes ao usuário, juntamente com `ca.crt`:

```text
Client/certificados/ca.crt
Client/certificados/clientes/Cliente1.cert
Client/certificados/clientes/Cliente1.private.key
```

O nome utilizado no login deve ser exatamente igual ao nome do arquivo do certificado.

Exemplo:

```text
Cliente1.cert -> login Cliente1
```

## Modelo de Segurança

### Criptografia do Transporte

O canal TCP entre cliente e broker é protegido por um envelope digital personalizado:

1. O broker envia `broker.crt`.
2. O cliente valida `broker.crt` utilizando `ca.crt`.
3. O cliente verifica a identidade do broker, que por padrão é `minimqtt-broker`.
4. O cliente gera uma chave de sessão AES.
5. A chave AES é criptografada com a chave pública RSA do broker utilizando RSA/OAEP.
6. Todas as mensagens do protocolo passam a ser criptografadas com AES/GCM.

Esse mecanismo protege o canal contra interceptação de tráfego sem utilizar TLS.

### Autenticação do Cliente

O cliente envia:

* Nome de usuário;
* Senha;
* Certificado do cliente assinado pelo broker.

O broker valida:

* o estado da conta;
* o hash da senha;
* o proprietário do certificado;
* o emissor do certificado;
* a assinatura realizada pelo broker sobre os dados do certificado.

O cliente apenas envia requisições. Toda a autorização e validação são realizadas pelo broker.

### Criptografia de Ponta a Ponta

Além da criptografia do transporte, o conteúdo das mensagens também é protegido por criptografia de ponta a ponta:

1. O broker envia as chaves públicas dos participantes do tópico.
2. O remetente gera uma chave AES para a mensagem.
3. O conteúdo é criptografado utilizando AES/GCM.
4. A chave AES é criptografada com RSA/OAEP para cada destinatário.
5. O broker apenas armazena e encaminha o envelope criptografado.

Assim, o broker consegue encaminhar mensagens com base no tópico e no remetente, porém **não consegue descriptografar o conteúdo das mensagens**.

## Execução

Inicie o broker:

```powershell
cd Broker
java -jar target\Broker.jar
```

Inicie um cliente:

```powershell
cd Client
java -jar target\Client.jar
```

Em redes de máquinas virtuais configuradas em modo *bridged*, a descoberta via broadcast UDP pode ser bloqueada. Nesse caso, inicie o cliente informando manualmente o endereço IP do broker:

```powershell
java -jar target\Client.jar <ip-do-broker> 5000
```

Não utilize `127.0.0.1` para acessar o broker a partir de uma máquina virtual, pois esse endereço sempre referencia a própria máquina virtual.

## Validação Funcional

Fluxo recomendado para validação:

1. Compile os módulos `Broker` e `Client`.
2. Gere ou copie o certificado do broker assinado pela CA.
3. Gere os certificados dos clientes utilizando `sign-vm-clients`.
4. Copie `ca.crt`, `.cert` e `.private.key` para cada máquina virtual.
5. Inicie o broker.
6. Inicie três clientes.
7. Crie as contas utilizando os nomes presentes nos certificados.
8. Crie um tópico com um dos clientes.
9. Inscreva os demais clientes no tópico.
10. Entre no tópico pela janela principal.
11. Envie mensagens e verifique a exibição do tópico e do remetente.
12. Desconecte um cliente inscrito, envie mensagens, reconecte-o e confirme o download das mensagens pendentes.
13. Cancele as inscrições até que reste apenas um participante e verifique a remoção automática do tópico.

## Solução de Problemas

### `Certificado nao encontrado.`

* O arquivo `.cert` não está presente em `Client/certificados/clientes`.
* O nome de login não corresponde ao nome do arquivo do certificado.

### `Chave privada nao encontrada.`

* O arquivo `.private.key` não está presente em `Client/certificados/clientes`.

### `Certificado da AC nao encontrado.`

* O arquivo `Client/certificados/ca.crt` está ausente.

### `Assinatura invalida.`

* O certificado do cliente foi gerado por outra chave do broker.
* `broker.crt` não foi assinado pela Autoridade Certificadora esperada.
* `broker.crt` não corresponde ao arquivo `broker-keystore.p12`.

### `Identidade do broker invalida.`

* O certificado assinado do broker não contém a identidade DNS esperada.
* Gere novamente o CSR utilizando `request-broker-cert`.
* Caso utilize uma identidade personalizada, inicie o cliente com:

```text
-Dminimqtt.broker.identity=<nome>
```

### `Broker nao encontrado.`

* A descoberta via UDP pode estar sendo bloqueada pela rede.
* Inicie o cliente manualmente:

```powershell
java -jar target\Client.jar <ip-do-broker> 5000
```

### `Voce nao esta inscrito.`

* O botão **Entrar** da janela principal somente abre uma conversa após a inscrição no tópico.
* Realize a inscrição na janela de configuração antes de tentar entrar.

## Arquivos Ignorados

Os seguintes arquivos e diretórios devem permanecer fora do controle de versão:

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
