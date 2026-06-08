# MiniMQTT Secure

![Java](https://img.shields.io/badge/Java-22-orange)
![Maven](https://img.shields.io/badge/Maven-3.x-blue)
![Status](https://img.shields.io/badge/Status-AV3%20Parcial%202-success)

Implementação de uma infraestrutura de comunicação inspirada no protocolo MQTT (*Message Queuing Telemetry Transport*), desenvolvida em Java para a disciplina de Redes de Computadores (AV3).

O projeto utiliza o modelo **Publish/Subscribe**, permitindo que múltiplos clientes se conectem a um broker central para criar tópicos, assinar tópicos, publicar mensagens e recuperar mensagens pendentes. Além disso, esta versão incorpora mecanismos de **autenticação e segurança baseados em certificados digitais assinados pelo broker**.

**Repositório:**
https://github.com/LeonS7/MiniMQTT-Secure

---

## Objetivos do Projeto

Implementar uma infraestrutura de comunicação semelhante ao MQTT contendo:

* Broker central responsável pelo gerenciamento dos tópicos;
* Clientes capazes de publicar e consumir mensagens;
* Modelo Publish/Subscribe;
* Gerenciamento de mensagens pendentes;
* Autenticação de usuários;
* Validação de certificados digitais assinados pelo broker.

---

## Funcionalidades

### Comunicação

* Broker TCP na porta `5000`;
* Descoberta automática do broker via UDP na porta `5001`;
* Comunicação cliente-servidor baseada em sockets Java;
* Suporte a múltiplos clientes simultaneamente.

### Publish/Subscribe

* Criação dinâmica de tópicos;
* Inscrição em múltiplos tópicos;
* Publicação de mensagens;
* Entrega de mensagens pendentes;
* Identificação do remetente e tópico de origem.

### Segurança

* Login e cadastro controlados pelo broker;
* Certificados digitais de clientes assinados offline;
* Verificação de autenticidade dos certificados durante o login;
* Armazenamento local de chaves e certificados fora do controle de versão.

### Interface

* Interface gráfica desenvolvida com Java Swing;
* Tela de login;
* Tela principal para gerenciamento de tópicos e mensagens.

---

## Estrutura do Projeto

```text
.
├── Broker/
├── Client/
├── README.md
└── .gitignore
```

### Principais Classes

#### Broker

* `Broker_main.java`
* `BrokerServer.java`
* `BrokerVerificationService.java`
* `CertificateTool.java`

#### Cliente

* `Client_main.java`
* `BrokerClient.java`
* `Login_interface.java`
* `Client_interface.java`

---

## Requisitos

* Java JDK 22
* Apache Maven
* Windows, Linux ou macOS

---

## Compilação

Compile os módulos separadamente:

### Broker

```bash
cd Broker
mvn clean package
```

### Cliente

```bash
cd Client
mvn clean package
```

Arquivos gerados:

```text
Broker/target/Broker.jar
Client/target/Client.jar
```

---

## Infraestrutura de Certificados

Nesta etapa do projeto, a autenticação é realizada através de certificados digitais de clientes assinados pela chave privada do broker.

### Gerar chaves do broker

```bash
cd Broker
java -cp target/Broker.jar com.mycompany.broker.CertificateTool init-server
```

Arquivos gerados:

```text
servidor_privada.key
servidor_publica.key
```

### Assinar certificado de um cliente

```bash
java -cp target/Broker.jar com.mycompany.broker.CertificateTool sign-client alice
```

No Windows, voce tambem pode usar o script:

```powershell
.\sign-client.bat NomeCliente
```

No Linux/macOS:

```bash
sh sign-client.sh NomeCliente
```

Para criar os tres certificados usados nas VMs de uma vez:

No Windows:

```powershell
.\sign-vm-clients.bat
```

No Linux/macOS:

```bash
sh sign-vm-clients.sh
```

Sem argumentos, esses scripts criam `Cliente1`, `Cliente2` e `Cliente3`. Para
usar outros nomes:

```powershell
.\sign-vm-clients.bat VM1 VM2 VM3
```

Os certificados ficam em `Broker/certificados/clientes`. Quando a pasta `Client`
existir ao lado do `Broker`, os scripts tambem copiam os `.cert` para
`Client/certificados/clientes`.

Arquivos produzidos:

```text
clientes/alice.cert
clientes/alice.private.key
clientes/alice.public.key
```

### Copiar certificado para o cliente

```bash
mkdir -p ../Client/target/certificados/clientes
cp target/certificados/clientes/alice.cert \
   ../Client/target/certificados/clientes/
```

## Fluxo de Conexão

1. O broker inicia os serviços TCP e UDP.
2. O cliente localiza automaticamente o broker via UDP.
3. Uma conexão TCP é estabelecida.
4. O cliente carrega seu certificado digital.
5. O cliente envia uma requisição de LOGIN ou REGISTER.
6. O broker valida:

   * Usuário;
   * Senha;
   * Estrutura do certificado;
   * Assinatura digital.
7. Em caso de sucesso, o broker responde com `OK`.
8. A interface principal é aberta.
9. As mensagens pendentes são recuperadas automaticamente.

---

## Tecnologias Utilizadas

* Java 22
* Java Swing
* Java Sockets (TCP/UDP)
* Maven
* RSA
* Certificados Digitais

---

## Autor

**Leonardo de Souza da Luz**

Projeto desenvolvido para a disciplina de Redes de Computadores (AV3).
