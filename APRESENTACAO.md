# Roteiro Da Apresentacao

Este arquivo resume a explicacao do projeto para demonstrar os pontos pedidos
nos enunciados da AV3 e da Parcial 2.

## 1. Ideia Geral

O projeto implementa uma infraestrutura publish/subscribe parecida com MQTT.
Existe um Broker central e varios Clientes. Os clientes criam topicos,
inscrevem-se em topicos, publicam mensagens e recebem mensagens pendentes
quando voltam a ficar online.

Toda comunicacao de comandos usa TCP para garantir entrega e ordenacao. O
projeto nao usa TLS; ele implementa um envelopamento digital proprio no inicio
da conexao.

## 2. Certificado Do Broker

Antes da apresentacao, gere a CSR do broker:

```powershell
cd Broker
.\request-broker-cert.bat
```

O arquivo enviado ao professor/AC e:

```text
Broker/certificados/broker.csr
```

Essa CSR usa a identidade fixa `minimqtt-broker`, nao um IP. Isso evita problema
quando as VMs estao em bridge e a rede da apresentacao entrega outro endereco
IP para a maquina do broker.

Quando o professor devolver o certificado assinado, salve como:

```text
Broker/certificados/broker.crt
```

O `broker-keystore.p12` nunca deve ser enviado, pois contem a chave privada do
broker.

## 3. Certificados Dos Clientes

Na Parcial 2, os clientes precisam de certificados assinados offline pelo
broker. Para gerar Cliente1, Cliente2 e Cliente3:

```powershell
cd Broker
mvn -q -DskipTests package
.\sign-vm-clients.bat
```

Em cada VM, copie apenas os arquivos daquele usuario:

```text
Client/certificados/ca.crt
Client/certificados/clientes/ClienteX.cert
Client/certificados/clientes/ClienteX.private.key
```

O nome usado no login precisa ser igual ao nome do certificado. Exemplo:
`Cliente1.cert` entra como `Cliente1`.

## 4. Fluxo De Conexao

1. O cliente tenta descobrir o broker por UDP na porta 5001.
2. Se a rede bloquear broadcast, o cliente pode ser iniciado com IP e porta:

```powershell
java -jar target\Client.jar IP_DO_BROKER 5000
```

3. O cliente abre TCP com o broker na porta 5000.
4. O broker envia `broker.crt`.
5. O cliente valida `broker.crt` usando `ca.crt` e a identidade
   `minimqtt-broker`.
6. O cliente gera uma chave AES de sessao.
7. O cliente cifra essa chave com a chave publica RSA do broker.
8. Depois disso, os comandos do protocolo trafegam cifrados com AES/GCM.

## 5. Login E Cadastro

No login/cadastro, o cliente envia:

- nome;
- senha;
- certificado `.cert` do usuario.

O broker valida:

- se a conta existe ou pode ser criada;
- se a senha confere;
- se o certificado pertence ao nome informado;
- se a assinatura do certificado foi feita pela chave privada do broker.

Assim, o cliente nao decide sozinho se esta autorizado. Ele apenas envia a
requisicao e aguarda a resposta do broker.

## 6. Topicos E Bufferizacao

Regras principais:

- ao criar um topico, o criador ja fica inscrito nele;
- o cliente so publica se estiver inscrito;
- o botao Entrar na conversa nao inscreve automaticamente;
- topicos sao encontrados sem diferenciar maiusculas/minusculas;
- mensagens indicam topico e remetente;
- mensagens ficam no buffer do broker ate todos os inscritos baixarem;
- quando um cliente reconecta, o broker reenvia mensagens pendentes;
- se o ultimo inscrito cancelar inscricao, o topico e removido automaticamente.

## 7. Criptografia Ponta A Ponta

A criptografia de transporte protege contra terceiros na rede. Alem disso, o
payload da mensagem tambem e cifrado ponta a ponta:

1. O broker envia aos clientes as chaves publicas dos membros do topico.
2. O remetente gera uma chave AES para o payload.
3. O payload e cifrado com AES/GCM.
4. A chave AES do payload e cifrada com RSA para cada destinatario.
5. O broker armazena e encaminha somente o envelope cifrado.

Com isso, o broker consegue ler cabecalho, topico e remetente para rotear, mas
nao consegue decodificar o conteudo da mensagem.

## 8. Checklist Dos Enunciados

- Interface grafica do cliente: implementada em Swing.
- Cliente em multiplos topicos: suportado por inscricoes e topico ativo.
- Mensagens com cliente e topico de origem: exibidas no chat.
- Bufferizacao no broker: mensagens pendentes ficam em memoria ate todos baixarem.
- Download ao reconectar: ocorre apos autenticacao.
- Autenticacao do cliente pelo broker: certificado assinado offline pelo broker.
- Broker autenticado por AC: cliente valida `broker.crt` com `ca.crt`.
- Trafego TCP criptografado sem TLS: RSA/OAEP + AES/GCM.
- Confidencialidade ponta a ponta: payload cifrado por envelope proprio.
