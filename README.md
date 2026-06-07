# MiniMQTT Secure

Repositorio: https://github.com/LeonS7/MiniMQTT-Secure.git

Projeto Java desenvolvido para a avaliacao AV3 de Redes. A aplicacao implementa
um broker no estilo publish/subscribe e clientes Swing que se conectam ao broker
para criar topicos, assinar topicos, publicar mensagens e baixar mensagens
pendentes.

Esta versao corresponde a parcial 2 do projeto: interface grafica, multiplos
topicos por cliente, identificacao de origem das mensagens, bufferizacao no
broker e autenticacao do cliente por certificado assinado offline pelo broker.

## Funcionalidades

- Broker TCP na porta `5000`.
- Descoberta automatica do broker via UDP na porta `5001`.
- Cliente com interface Swing para login, criacao de conta, topicos e mensagens.
- Cliente pode participar de varios topicos ao mesmo tempo.
- Mensagens exibem cliente remetente e topico de origem.
- Broker guarda mensagens pendentes ate todos os clientes inscritos baixarem.
- Cliente solicita automaticamente mensagens pendentes ao conectar.
- Login e cadastro validados pelo broker.
- Certificado do cliente assinado offline pela chave privada do broker.
- Chaves, certificados e usuarios locais ficam fora do Git.

## Estrutura

```text
.
|-- Broker/             # modulo Maven do broker
|-- Client/             # modulo Maven do cliente Swing
|-- README.md
`-- .gitignore
```

Principais classes:

- `Broker/src/main/java/com/mycompany/avaliacao_3/Broker_main.java`
- `Broker/src/main/java/com/mycompany/broker/BrokerServer.java`
- `Broker/src/main/java/com/mycompany/broker/BrokerVerificationService.java`
- `Broker/src/main/java/com/mycompany/broker/CertificateTool.java`
- `Client/src/main/java/com/mycompany/client/Client_main.java`
- `Client/src/main/java/com/mycompany/client/network/BrokerClient.java`
- `Client/src/main/java/com/mycompany/interfaces/Login_interface.java`
- `Client/src/main/java/com/mycompany/interfaces/Client_interface.java`

## Requisitos

- JDK 17 ou superior.
- Maven.
- Windows, Linux ou macOS com suporte a Java Swing.

## Build

Compile os dois modulos separadamente:

```powershell
cd Broker
mvn -q -DskipTests package

cd ..\Client
mvn -q -DskipTests package
```

Os JARs gerados ficam em:

- `Broker/target/Broker.jar`
- `Client/target/Client.jar`

## Certificados da parcial 2

Na parcial 2, somente o certificado do cliente e usado para autenticacao. O
certificado do broker assinado por uma AC fica para a parte 3.

O broker possui um par de chaves RSA:

- `servidor_privada.key`: chave privada usada para assinar certificados de clientes.
- `servidor_publica.key`: chave publica usada pelo broker para validar assinaturas.

Para criar as chaves do broker e assinar um cliente offline:

```powershell
cd Broker
java -cp target\Broker.jar com.mycompany.broker.CertificateTool init-server
java -cp target\Broker.jar com.mycompany.broker.CertificateTool sign-client alice
```

Esse comando cria arquivos em `Broker/target/certificados`, incluindo:

- `clientes/alice.cert`
- `clientes/alice.private.key`
- `clientes/alice.public.key`

Para executar o cliente pelo JAR, copie o arquivo `.cert` do cliente para a pasta
`certificados/clientes` ao lado do `Client.jar`:

```powershell
mkdir ..\Client\target\certificados\clientes
copy target\certificados\clientes\alice.cert ..\Client\target\certificados\clientes\
```

Nao envie para o GitHub chaves privadas, certificados gerados, `usuarios.properties`
ou a pasta `target`. Esses arquivos ja estao cobertos pelo `.gitignore`.

## Execucao

Inicie primeiro o broker:

```powershell
cd Broker
java -jar target\Broker.jar
```

Depois inicie um ou mais clientes:

```powershell
cd Client
java -jar target\Client.jar
```

No cliente, use o mesmo nome do certificado assinado. Por exemplo, se o arquivo
for `alice.cert`, o usuario do login deve ser `alice`.

## Fluxo de conexao

1. O broker inicia a porta TCP `5000` e a descoberta UDP `5001`.
2. O cliente procura o broker via UDP.
3. O cliente abre conexao TCP com o broker encontrado.
4. O cliente carrega seu certificado local.
5. O cliente envia `LOGIN` ou `REGISTER` com nome, senha e certificado.
6. O broker valida formato, usuario, senha e assinatura do certificado.
7. O broker responde `OK` quando a autenticacao passa.
8. A interface do cliente abre e solicita mensagens pendentes.
9. O broker entrega mensagens ainda nao baixadas por aquele cliente.

## Publicacao no GitHub

Execute os comandos abaixo dentro da pasta deste projeto, nao no diretorio pai
dos projetos NetBeans:

```powershell
git init
git branch -M main
git remote add origin https://github.com/LeonS7/MiniMQTT-Secure.git
git add README.md .gitignore Broker Client
git commit -m "Add MiniMQTT Secure project"
git push -u origin main
```

Antes do commit, confira se nenhum arquivo sensivel entrou na lista:

```powershell
git status --short
```

Nao devem aparecer arquivos de `target/`, `certificados/`, `*.key`, `*.cert` ou
`usuarios.properties`.
