package com.mycompany.broker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JOptionPane;

/**
 * Broker publish/subscribe da aplicacao.
 *
 * Mantem um servidor TCP para comandos e mensagens dos clientes e um servico
 * UDP simples para descoberta automatica do endereco do broker na rede local.
 */
public final class BrokerServer {

    /*
     * Porta TCP usada para o protocolo publish/subscribe e porta UDP usada para
     * descoberta automatica do broker na rede.
     */
    public static final int DEFAULT_PORT = 5000;
    public static final int DISCOVERY_PORT = 5001;

    /*
     * Assinaturas textuais simples do protocolo UDP de descoberta.
     */
    private static final String DISCOVERY_REQUEST = "AV3_DISCOVER_BROKER";
    private static final String DISCOVERY_RESPONSE = "AV3_BROKER";

    /**
     * Contrato usado pela interface Swing para receber eventos do broker sem
     * acoplar a regra de rede diretamente aos componentes visuais.
     */
    public interface Listener {
        /**
         * Recebe mensagens de log operacional do broker.
         */
        void onLog(String message);

        /**
         * Recebe status textual como Online/Offline.
         */
        void onStatus(String status);

        /**
         * Recebe uma fotografia dos clientes autenticados e topicos existentes.
         */
        void onSnapshot(List<String> clients, List<String> topics);
    }

    /*
     * Estado principal do broker. topics guarda os topicos por nome e clients
     * guarda conexoes TCP ativas.
     */
    private final int port;
    private final Listener listener;
    private final BrokerVerificationService verifier = new BrokerVerificationService();
    private final ConcurrentMap<String, Topic> topics = new ConcurrentHashMap<>();
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private final AtomicInteger clientCounter = new AtomicInteger(1);
    private final AtomicLong messageCounter = new AtomicLong(1);

    private volatile boolean running;
    private volatile BrokerTransportSecurity transportSecurity;
    private ServerSocket serverSocket;
    private DatagramSocket discoverySocket;
    private ExecutorService executor;

    /**
     * Cria um broker na porta indicada e com listener opcional para interface.
     */
    public BrokerServer(int port, Listener listener) {
        this.port = port;
        this.listener = listener;
    }

    /**
     * Inicializa o socket TCP, o socket UDP de descoberta e as threads de
     * atendimento. O metodo e sincronizado para impedir duas inicializacoes
     * simultaneas do mesmo broker.
     */
    public synchronized void start() throws IOException {
        if (running) {
            return;
        }

        try {
            boolean serverKeysCreated = BrokerCertificateSupport.ensureServerKeys();
            transportSecurity = BrokerTransportSecurity.load();
            serverSocket = new ServerSocket(port);
            discoverySocket = new DatagramSocket(DISCOVERY_PORT);
            executor = Executors.newCachedThreadPool();
            running = true;
            executor.submit(this::acceptLoop);
            executor.submit(this::discoveryLoop);
            notifyStatus("Online na porta " + port);
            log("Broker iniciado na porta TCP " + port + ".");
            log("Descoberta UDP ativa na porta " + DISCOVERY_PORT + ".");
            log(serverKeysCreated
                    ? "Chaves do servidor criadas em: " + BrokerCertificateSupport.certificateDirectory() + "."
                    : "Chaves do servidor carregadas de: " + BrokerCertificateSupport.certificateDirectory() + ".");
            log("Certificado do broker carregado e canal seguro habilitado.");
            log("IPs encontrados nesta maquina: " + localAddressHint() + ".");
            notifySnapshot();
        } catch (IOException ex) {
            closeServerSocket();
            closeDiscoverySocket();
            throw ex;
        } catch (Exception ex) {
            closeServerSocket();
            closeDiscoverySocket();
            throw new IOException("Nao foi possivel preparar as chaves do servidor: " + ex.getMessage(), ex);
        }
    }

    /**
     * Encerra o broker de forma controlada: fecha sockets, desconecta clientes,
     * limpa topicos em memoria e interrompe o pool de threads.
     */
    public synchronized void stop() {
        if (!running) {
            return;
        }

        running = false;
        closeServerSocket();
        closeDiscoverySocket();

        for (ClientHandler client : new ArrayList<>(clients)) {
            client.close();
        }
        clients.clear();
        topics.clear();

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        notifyStatus("Offline");
        log("Broker encerrado.");
        notifySnapshot();
    }

    /**
     * Indica se o broker esta aceitando conexoes e respondendo descoberta UDP.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Loop bloqueante do servidor TCP. Cada conexao aceita cria um
     * ClientHandler independente, executado pelo pool de threads.
     */
    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                executor.submit(handler);
            } catch (IOException ex) {
                if (running) {
                    log("Falha ao aceitar cliente: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Loop UDP de descoberta. Ao receber o pacote padrao de busca, responde ao
     * remetente com a porta TCP usada pelo broker.
     */
    private void discoveryLoop() {
        byte[] buffer = new byte[256];
        while (running) {
            try {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                discoverySocket.receive(request);

                String message = new String(
                        request.getData(),
                        request.getOffset(),
                        request.getLength(),
                        StandardCharsets.UTF_8).trim();

                if (!DISCOVERY_REQUEST.equals(message)) {
                    continue;
                }

                byte[] responseData = (DISCOVERY_RESPONSE + "|" + port).getBytes(StandardCharsets.UTF_8);
                DatagramPacket response = new DatagramPacket(
                        responseData,
                        responseData.length,
                        request.getAddress(),
                        request.getPort());
                discoverySocket.send(response);
                log("Descoberta UDP respondida para " + request.getAddress().getHostAddress() + ".");
            } catch (IOException ex) {
                if (running) {
                    log("Falha na descoberta UDP: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Interpreta uma linha do protocolo textual.
     *
     * Formato geral: COMANDO|campo1|campo2. Os campos variaveis sao codificados
     * em Base64 URL-safe para evitar conflito com o separador '|'.
     */
    private void handleCommand(ClientHandler client, String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length == 0) {
            return;
        }

        try {
            switch (parts[0]) {
                case "LOGIN":
                    verifier.requireParts(parts, 4);
                    authenticateClient(client,
                            verifier.verifyLogin(
                                    verifier.decodeProtocolField(parts[1]),
                                    verifier.decodeProtocolField(parts[2]),
                                    verifier.decodeProtocolField(parts[3])),
                            "Cliente autenticado.");
                    break;
                case "REGISTER":
                    verifier.requireParts(parts, 4);
                    authenticateClient(client,
                            verifier.verifyAccountCreation(
                                    verifier.decodeProtocolField(parts[1]),
                                    verifier.decodeProtocolField(parts[2]),
                                    verifier.decodeProtocolField(parts[3])),
                            "Conta criada e cliente autenticado.");
                    break;
                case "CREATE_TOPIC":
                    requireAuthenticated(client);
                    verifier.requireParts(parts, 2);
                    createTopic(client, verifier.verifyTopicName(verifier.decodeProtocolField(parts[1])));
                    break;
                case "SUBSCRIBE":
                    requireAuthenticated(client);
                    verifier.requireParts(parts, 2);
                    subscribe(client, verifier.verifyTopicName(verifier.decodeProtocolField(parts[1])));
                    break;
                case "ENTER_TOPIC":
                    requireAuthenticated(client);
                    verifier.requireParts(parts, 2);
                    enterTopic(client, verifier.verifyTopicName(verifier.decodeProtocolField(parts[1])));
                    break;
                case "UNSUBSCRIBE":
                    requireAuthenticated(client);
                    verifier.requireParts(parts, 2);
                    unsubscribe(client, verifier.verifyTopicName(verifier.decodeProtocolField(parts[1])));
                    break;
                case "DELETE_TOPIC":
                    requireAuthenticated(client);
                    verifier.requireParts(parts, 2);
                    deleteTopic(client, verifier.verifyTopicName(verifier.decodeProtocolField(parts[1])));
                    break;
                case "PUBLISH":
                    requireAuthenticated(client);
                    verifier.requireParts(parts, 3);
                    publish(client,
                            verifier.verifyTopicName(verifier.decodeProtocolField(parts[1])),
                            verifier.decodeProtocolField(parts[2]));
                    break;
                case "LIST_TOPICS":
                    requireAuthenticated(client);
                    sendTopics(client);
                    break;
                case "DOWNLOAD_PENDING":
                    requireAuthenticated(client);
                    downloadPendingMessages(client);
                    client.sendOk("DOWNLOAD_PENDING", "Pendencias do topico ativo baixadas.");
                    break;
                case "DISCONNECT":
                    client.close();
                    break;
                default:
                    client.sendError("Comando desconhecido: " + parts[0]);
                    break;
            }
        } catch (IllegalArgumentException ex) {
            client.sendError(ex.getMessage());
        }
    }

    /**
     * Conclui o login ou cadastro validado pelo BrokerVerificationService.
     */
    private void authenticateClient(ClientHandler client, String verifiedName, String message) {
        client.setName(verifiedName);
        client.setAuthenticated(true);
        restoreClientSubscriptions(client);
        client.sendOk("AUTH", message, verifiedName);
        sendTopics(client);
        sendRestoredTopicKeys(client);
        log("Cliente autenticado e conectado: " + client.getName() + ".");
        notifySnapshot();
    }

    /**
     * Cria um topico se ele ainda nao existir e registra o cliente criador como
     * proprietario e primeiro inscrito. A conversa so fica ativa quando o
     * cliente usa ENTER_TOPIC pela tela principal.
     */
    private synchronized void createTopic(ClientHandler client, String topicName) {
        Topic existing = findTopic(topicName);
        verifier.verifyTopicCanBeCreated(topicName, existing != null);

        Topic topic = new Topic(topicName, client.getName());
        topics.put(topic.name, topic);

        topic.members.add(client.getName());
        client.subscriptions.add(topic.name);
        client.sendOk("CREATE_TOPIC", "Topico criado e inscricao realizada: " + topicName, topicName);
        sendTopicKeys(topic, client);
        log(client.getName() + " criou o topico " + topicName + ".");
        broadcastTopics();
        notifySnapshot();
    }

    /**
     * Inscreve o cliente em um topico existente. A inscricao autoriza receber
     * mensagens, mas elas so sao entregues quando o topico estiver ativo.
     */
    private void subscribe(ClientHandler client, String topicName) {
        Topic topic = findTopic(topicName);
        verifier.verifyTopicExists(topicName, topic != null);

        topic.members.add(client.getName());
        client.subscriptions.add(topic.name);
        client.sendOk("SUBSCRIBE", "Inscrito no topico: " + topic.name, topic.name);
        broadcastTopicKeys(topic);
        log(client.getName() + " se inscreveu no topico " + topic.name + ".");
        notifySnapshot();
    }

    /**
     * Abre um topico para conversa sem criar inscricao nova.
     */
    private void enterTopic(ClientHandler client, String topicName) {
        Topic topic = findTopic(topicName);
        verifier.verifyTopicExists(topicName, topic != null);
        verifier.verifyClientSubscribed(topic.name, topic.members.contains(client.getName()));

        activateTopic(client, topic);
        sendTopicKeys(topic, client);
        downloadPendingMessages(client, topic.name);
        client.sendOk("ENTER_TOPIC", "Topico aberto.", topic.name);
    }

    /**
     * Define qual topico esta aberto na tela de chat do cliente.
     *
     * O cliente pode estar inscrito em varios topicos, mas apenas o topico
     * ativo recebe mensagens imediatamente. As mensagens dos outros topicos
     * continuam no buffer ate o cliente entrar neles.
     */
    private void activateTopic(ClientHandler client, Topic topic) {
        String previousTopicName = client.activeTopic;
        if (!previousTopicName.isBlank() && !previousTopicName.equals(topic.name)) {
            Topic previousTopic = findTopic(previousTopicName);
            if (previousTopic != null) {
                previousTopic.activeSubscribers.remove(client);
            }
        }

        client.subscriptions.add(topic.name);
        client.activeTopic = topic.name;
        topic.activeSubscribers.add(client);
    }

    /**
     * Remove a inscricao do cliente no topico informado, mantendo o topico
     * disponivel para outros clientes.
     */
    private void unsubscribe(ClientHandler client, String topicName) {
        Topic topic = findTopic(topicName);
        verifier.verifyTopicExists(topicName, topic != null);
        verifier.verifyClientSubscribed(topicName, topic.members.contains(client.getName()));

        topic.members.remove(client.getName());
        topic.activeSubscribers.remove(client);
        client.subscriptions.remove(topic.name);
        if (topic.name.equals(client.activeTopic)) {
            client.activeTopic = "";
        }
        removePendingRecipient(topic, client.getName());

        if (topic.members.isEmpty() && topics.remove(topic.name, topic)) {
            topic.buffer.clear();
            client.sendOk("UNSUBSCRIBE", "Inscricao cancelada. Topico excluido.", topic.name);
            log(client.getName() + " saiu do topico " + topic.name + ". Topico excluido automaticamente.");
            broadcastTopics();
            notifySnapshot();
            return;
        }

        client.sendOk("UNSUBSCRIBE", "Inscricao cancelada.", topic.name);
        broadcastTopicKeys(topic);
        log(client.getName() + " saiu do topico " + topic.name + ".");
        broadcastTopics();
        notifySnapshot();
    }

    /**
     * Exclui um topico somente quando o solicitante e o proprietario original.
     * Todos os inscritos sao notificados e suas inscricoes locais sao removidas.
     */
    private void deleteTopic(ClientHandler client, String topicName) {
        Topic topic = findTopic(topicName);
        verifier.verifyTopicExists(topicName, topic != null);
        verifier.verifyTopicDeletion(client.getName(), topic.owner);

        if (topics.remove(topic.name, topic)) {
            for (ClientHandler connectedClient : new ArrayList<>(clients)) {
                if (connectedClient.subscriptions.remove(topic.name) || topic.members.contains(connectedClient.getName())) {
                    if (topic.name.equals(connectedClient.activeTopic)) {
                        connectedClient.activeTopic = "";
                    }
                    connectedClient.sendInfo("Topico excluido: " + topic.name);
                }
            }
            client.sendOk("DELETE_TOPIC", "Topico excluido: " + topic.name, topic.name);
            log(client.getName() + " excluiu o topico " + topic.name + ".");
            broadcastTopics();
            notifySnapshot();
        }
    }

    /**
     * Publica uma mensagem no topico informado.
     *
     * A regra atual exige que o remetente esteja inscrito no topico antes de
     * publicar. A mensagem e encaminhada para todos os inscritos, incluindo o
     * proprio remetente.
     */
    private void publish(ClientHandler client, String topicName, String message) {
        Topic topic = findTopic(topicName);
        boolean topicExists = topic != null;
        boolean clientSubscribed = topicExists && topic.members.contains(client.getName());
        String cleanMessage = verifier.verifyPublication(
                topicName,
                topicExists,
                clientSubscribed,
                message);

        StoredMessage storedMessage = new StoredMessage(
                messageCounter.getAndIncrement(),
                topic.name,
                client.getName(),
                cleanMessage,
                new HashSet<>(topic.members));

        if (!storedMessage.pendingRecipients.isEmpty()) {
            topic.buffer.add(storedMessage);
            deliverPendingMessages(topic);
        }
        client.sendOk("PUBLISH", "Mensagem enviada.", topic.name);
        logEncryptedPayload(client.getName(), topic.name, cleanMessage);
        log(client.getName() + " publicou mensagem criptografada em " + topic.name + ".");
    }

    /**
     * Remove um cliente desconectado de todas as estruturas compartilhadas para
     * evitar envio posterior para sockets fechados.
     */
    private void removeClient(ClientHandler client) {
        if (!clients.remove(client)) {
            return;
        }

        for (String topicName : new ArrayList<>(client.subscriptions)) {
            Topic topic = topics.get(topicName);
            if (topic != null) {
                topic.activeSubscribers.remove(client);
            }
        }
        client.subscriptions.clear();
        client.activeTopic = "";
        log("Cliente desconectado: " + client.getName() + ".");
        notifySnapshot();
    }

    /**
     * Restaura, apos autenticação, as inscricoes persistentes do cliente nos
     * topicos que o broker ainda mantem em memoria.
     */
    private void restoreClientSubscriptions(ClientHandler client) {
        client.subscriptions.clear();
        client.activeTopic = "";
        for (Topic topic : topics.values()) {
            if (topic.members.contains(client.getName())) {
                client.subscriptions.add(topic.name);
            }
        }
    }

    /**
     * Apos login, reenvia chaves dos topicos persistentes do cliente.
     */
    private void sendRestoredTopicKeys(ClientHandler client) {
        for (String topicName : new ArrayList<>(client.subscriptions)) {
            Topic topic = findTopic(topicName);
            if (topic != null) {
                sendTopicKeys(topic, client);
            }
        }
    }

    /**
     * Faz download das mensagens pendentes somente do topico aberto no chat.
     */
    private void downloadPendingMessages(ClientHandler client) {
        String activeTopic = client.activeTopic;
        if (!activeTopic.isBlank()) {
            downloadPendingMessages(client, activeTopic);
        }
    }

    /**
     * Faz download das mensagens pendentes de um unico topico para o cliente.
     */
    private void downloadPendingMessages(ClientHandler client, String topicName) {
        Topic topic = findTopic(topicName);
        if (topic == null) {
            return;
        }
        deliverPendingMessages(topic, client);
        purgeDeliveredMessages(topic);
    }

    /**
     * Tenta entregar mensagens pendentes para os membros conectados que estao
     * com este topico aberto no chat.
     */
    private void deliverPendingMessages(Topic topic) {
        for (ClientHandler subscriber : new ArrayList<>(topic.activeSubscribers)) {
            deliverPendingMessages(topic, subscriber);
        }
        purgeDeliveredMessages(topic);
    }

    /**
     * Entrega ao cliente apenas as mensagens que ainda possuem seu nome na lista
     * de destinatarios pendentes.
     */
    private void deliverPendingMessages(Topic topic, ClientHandler client) {
        if (!topic.members.contains(client.getName()) || !topic.name.equals(client.activeTopic)) {
            return;
        }

        for (StoredMessage message : new ArrayList<>(topic.buffer)) {
            if (message.pendingRecipients.remove(client.getName())) {
                client.sendMessage(message.topic, message.sender, message.payload);
            }
        }
    }

    /**
     * Remove mensagens do buffer quando todos os membros do topico ja receberam
     * o conteudo.
     */
    private void purgeDeliveredMessages(Topic topic) {
        topic.buffer.removeIf(message -> message.pendingRecipients.isEmpty());
    }

    /**
     * Remove um cliente dos destinatarios pendentes quando ele cancela inscricao.
     */
    private void removePendingRecipient(Topic topic, String clientName) {
        for (StoredMessage message : topic.buffer) {
            message.pendingRecipients.remove(clientName);
        }
        purgeDeliveredMessages(topic);
    }

    /**
     * Envia ao cliente as chaves publicas dos membros do topico para que ele
     * consiga cifrar o payload ponta a ponta para todos os destinatarios.
     */
    private void sendTopicKeys(Topic topic, ClientHandler client) {
        List<String> fields = new ArrayList<>();
        fields.add("TOPIC_KEYS");
        fields.add(encode(topic.name));

        List<String> members = new ArrayList<>(topic.members);
        Collections.sort(members);
        for (String member : members) {
            String publicKey = verifier.publicKeyFor(member);
            if (publicKey == null || publicKey.isBlank()) {
                continue;
            }
            fields.add(encode(member));
            fields.add(encode(publicKey));
        }
        client.sendRaw(String.join("|", fields));
    }

    /**
     * Atualiza os clientes online de um topico quando a lista de membros muda.
     */
    private void broadcastTopicKeys(Topic topic) {
        for (ClientHandler subscriber : new ArrayList<>(topic.activeSubscribers)) {
            sendTopicKeys(topic, subscriber);
        }
    }

    /**
     * Impede comandos de clientes que ainda nao passaram pela autenticacao por
     * certificado.
     */
    private void requireAuthenticated(ClientHandler client) {
        if (!client.isAuthenticated()) {
            throw new IllegalArgumentException("Cliente nao autenticado.");
        }
    }

    /**
     * Envia a lista atual de topicos para todos os clientes conectados.
     */
    private void broadcastTopics() {
        for (ClientHandler client : clients) {
            sendTopics(client);
        }
    }

    /**
     * Envia a lista ordenada de topicos para um cliente especifico usando o
     * comando TOPICS do protocolo.
     */
    private void sendTopics(ClientHandler client) {
        List<String> names = sortedTopicNames();
        List<String> fields = new ArrayList<>();
        fields.add("TOPICS");
        for (String topic : names) {
            fields.add(encode(topic));
        }
        client.sendRaw(String.join("|", fields));
    }

    /**
     * Retorna os nomes dos topicos em ordem alfabetica para manter a exibicao
     * previsivel nas interfaces.
     */
    private List<String> sortedTopicNames() {
        List<String> names = new ArrayList<>();
        for (Topic topic : topics.values()) {
            names.add(topic.name);
        }
        Collections.sort(names);
        return names;
    }

    /**
     * Localiza topicos sem diferenciar maiusculas e minusculas.
     */
    private Topic findTopic(String topicName) {
        String cleanName = topicName == null ? "" : topicName.trim();
        if (cleanName.isEmpty()) {
            return null;
        }

        Topic exact = topics.get(cleanName);
        if (exact != null) {
            return exact;
        }

        for (Topic topic : topics.values()) {
            if (topic.name.equalsIgnoreCase(cleanName)) {
                return topic;
            }
        }
        return null;
    }

    /**
     * Retorna os nomes dos clientes conectados em ordem alfabetica para o painel
     * administrativo do broker.
     */
    private List<String> sortedClientNames() {
        List<String> names = new ArrayList<>();
        for (ClientHandler client : clients) {
            if (client.isAuthenticated()) {
                names.add(client.getName());
            }
        }
        Collections.sort(names);
        return names;
    }

    /**
     * Publica para a interface uma fotografia consistente do estado atual:
     * clientes conectados e topicos existentes.
     */
    private void notifySnapshot() {
        if (listener != null) {
            listener.onSnapshot(sortedClientNames(), sortedTopicNames());
        }
    }

    /**
     * Atualiza o status textual exibido pela interface do broker.
     */
    private void notifyStatus(String status) {
        if (listener != null) {
            listener.onStatus(status);
        }
    }

    /**
     * Encaminha mensagens operacionais para a area de log da interface.
     */
    private void log(String message) {
        if (listener != null) {
            listener.onLog(message);
        }
    }

    /**
     * Mostra no log do broker o envelope ponta a ponta recebido.
     *
     * O broker registra o texto cifrado para demonstrar que o payload chega
     * criptografado e que a descriptografia acontece apenas no cliente.
     */
    private void logEncryptedPayload(String sender, String topicName, String encryptedPayload) {
        log("Payload criptografado recebido de " + sender + " em " + topicName + ": "
                + compactEncryptedPayload(encryptedPayload));
    }

    /**
     * Compacta o envelope criptografado para caber em uma linha de log.
     */
    private static String compactEncryptedPayload(String encryptedPayload) {
        String compact = (encryptedPayload == null ? "" : encryptedPayload)
                .replace("\r", "")
                .replace("\n", " | ");
        int maxLength = 900;
        if (compact.length() <= maxLength) {
            return compact;
        }
        return compact.substring(0, maxLength) + "...";
    }

    /**
     * Fecha o socket TCP principal. Fechar o socket desbloqueia accept() e
     * permite que a thread de aceite termine.
     */
    private void closeServerSocket() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
            serverSocket = null;
        }
    }

    /**
     * Fecha o socket UDP de descoberta. Fechar o socket desbloqueia receive().
     */
    private void closeDiscoverySocket() {
        if (discoverySocket != null) {
            discoverySocket.close();
            discoverySocket = null;
        }
    }

    /**
     * Coleta enderecos IPv4 nao-loopback para auxiliar diagnostico de rede no
     * log do broker.
     */
    private static String localAddressHint() {
        List<String> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        addresses.add(address.getHostAddress());
                    }
                }
            }
        } catch (IOException ex) {
            return "nao foi possivel listar automaticamente";
        }

        if (addresses.isEmpty()) {
            return "verifique o IP com ipconfig";
        }
        Collections.sort(addresses);
        return String.join(", ", addresses);
    }

    /**
     * Codifica campos textuais para trafegar no protocolo de linha sem conflito
     * com separadores ou quebras de linha.
     */
    private static String encode(String value) {
        String safeValue = value == null ? "" : value;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(safeValue.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Codifica bytes em Base64 URL-safe para o handshake seguro.
     */
    private static String encodeBytes(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    /**
     * Representa uma conexao TCP de cliente.
     *
     * Cada instancia possui seu socket, escritor e conjunto de inscricoes. O
     * broker mantem estruturas concorrentes para permitir varios clientes ao
     * mesmo tempo.
     */
    private final class ClientHandler implements Runnable {

        private final Socket socket;
        private final Set<String> subscriptions = ConcurrentHashMap.newKeySet();
        private volatile String name;
        private volatile String activeTopic = "";
        private volatile PrintWriter writer;
        private volatile boolean connected = true;
        private volatile boolean authenticated;
        private volatile BrokerTransportSecurity.SecureSession secureSession;

        /**
         * Recebe o socket aceito pelo ServerSocket e cria um nome temporario
         * ate o cliente fazer login com certificado.
         */
        ClientHandler(Socket socket) {
            this.socket = socket;
            this.name = "cliente-" + clientCounter.getAndIncrement();
        }

        /**
         * Le comandos do socket ate desconexao, fechamento do broker ou erro de
         * rede. O finally garante a remocao do cliente das estruturas globais.
         */
        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    PrintWriter socketWriter = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {

                writer = socketWriter;
                performSecureHandshake(reader);
                sendInfo("Aguardando autenticacao do cliente.");
                notifySnapshot();

                String line;
                while (connected && running && (line = reader.readLine()) != null) {
                    handleCommand(this, decryptLine(line));
                }
            } catch (IOException ex) {
                if (running && connected) {
                    log("Conexao perdida com " + name + ": " + ex.getMessage());
                }
            } catch (GeneralSecurityException ex) {
                if (running && connected) {
                    log("Falha de seguranca com " + name + ": " + ex.getMessage());
                }
            } finally {
                close();
                removeClient(this);
            }
        }

        /**
         * Envia o certificado do broker e recebe a chave de sessao AES cifrada.
         */
        private void performSecureHandshake(BufferedReader reader) throws IOException, GeneralSecurityException {
            BrokerTransportSecurity currentSecurity = transportSecurity;
            if (currentSecurity == null) {
                throw new IOException("Canal seguro indisponivel.");
            }

            sendPlain("BROKER_CERT|" + encodeBytes(currentSecurity.encodedCertificate()));
            String sessionLine = reader.readLine();
            if (sessionLine == null) {
                throw new IOException("Cliente encerrou antes do canal seguro.");
            }

            String[] parts = sessionLine.split("\\|", -1);
            if (parts.length != 2 || !"SESSION".equals(parts[0])) {
                throw new IOException("Sessao segura invalida.");
            }
            secureSession = currentSecurity.openSession(parts[1]);
        }

        /**
         * Decifra uma linha protegida por AES/GCM.
         */
        private String decryptLine(String line) throws GeneralSecurityException {
            BrokerTransportSecurity.SecureSession session = secureSession;
            if (session == null) {
                throw new GeneralSecurityException("Sessao segura nao iniciada.");
            }
            return session.decryptLine(line);
        }

        /**
         * Nome atual do cliente. Antes da autenticacao e um nome temporario.
         */
        String getName() {
            return name;
        }

        /**
         * Define o nome validado pelo login/certificado.
         */
        void setName(String name) {
            this.name = name;
        }

        /**
         * Indica se o cliente ja passou por LOGIN ou REGISTER com sucesso.
         */
        boolean isAuthenticated() {
            return authenticated;
        }

        /**
         * Marca a conexao como autenticada apos a validacao do broker.
         */
        void setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
        }

        /**
         * Envia uma confirmacao padronizada para uma operacao aceita.
         */
        void sendOk(String operation, String message, String... values) {
            List<String> fields = new ArrayList<>();
            fields.add("OK");
            fields.add(operation);
            fields.add(encode(message));
            for (String value : values) {
                fields.add(encode(value));
            }
            sendRaw(String.join("|", fields));
        }

        /**
         * Envia erro padronizado ao cliente.
         */
        void sendError(String message) {
            sendRaw("ERROR|" + encode(message));
        }

        /**
         * Envia mensagem informativa que nao representa erro nem confirmacao.
         */
        void sendInfo(String message) {
            sendRaw("INFO|" + encode(message));
        }

        /**
         * Envia publicacao recebida de um topico, preservando topico e remetente
         * para a interface mostrar a origem.
         */
        void sendMessage(String topic, String sender, String message) {
            sendRaw("MESSAGE|" + encode(topic) + "|" + encode(sender) + "|" + encode(message));
        }

        /**
         * Envia uma linha ja formatada do protocolo. A sincronizacao evita que
         * duas threads misturem escritas no mesmo socket.
         */
        synchronized void sendRaw(String line) {
            PrintWriter currentWriter = writer;
            if (currentWriter != null && connected) {
                try {
                    BrokerTransportSecurity.SecureSession session = secureSession;
                    currentWriter.println(session == null ? line : session.encryptLine(line));
                } catch (GeneralSecurityException ex) {
                    close();
                    log("Falha ao cifrar resposta para " + name + ": " + ex.getMessage());
                }
            }
        }

        /**
         * Envia linhas publicas usadas apenas antes da sessao segura existir.
         */
        private synchronized void sendPlain(String line) {
            PrintWriter currentWriter = writer;
            if (currentWriter != null && connected) {
                currentWriter.println(line);
            }
        }

        /**
         * Marca a conexao como encerrada e fecha o socket do cliente.
         */
        void close() {
            connected = false;
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Modelo em memoria de um topico: nome, proprietario e clientes inscritos.
     */
    private static final class Topic {

        private final String name;
        private final String owner;

        /*
         * members guarda todos os clientes inscritos, inclusive os offline. Ja
         * activeSubscribers guarda somente conexoes com este topico aberto no chat.
         */
        private final Set<String> members = ConcurrentHashMap.newKeySet();
        private final Set<ClientHandler> activeSubscribers = ConcurrentHashMap.newKeySet();

        /*
         * Buffer de mensagens que ainda nao foram baixadas por todos os members.
         */
        private final List<StoredMessage> buffer = Collections.synchronizedList(new ArrayList<>());

        /**
         * Cria um topico com nome e dono original.
         */
        Topic(String name, String owner) {
            this.name = name;
            this.owner = owner;
        }

        /**
         * Representacao simples para logs/debug.
         */
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Mensagem mantida no broker ate todos os membros do topico baixarem.
     */
    private static final class StoredMessage {

        /*
         * id ajuda debug/log, topic e sender preservam a origem, payload guarda
         * a mensagem e pendingRecipients controla quem ainda precisa baixar.
         */
        private final long id;
        private final String topic;
        private final String sender;
        private final String payload;
        private final Set<String> pendingRecipients;

        /**
         * Cria uma mensagem pendente para todos os membros do topico no momento
         * da publicacao.
         */
        StoredMessage(long id, String topic, String sender, String payload, Set<String> pendingRecipients) {
            this.id = id;
            this.topic = topic;
            this.sender = sender;
            this.payload = payload;
            this.pendingRecipients = ConcurrentHashMap.newKeySet();
            this.pendingRecipients.addAll(pendingRecipients);
        }

        /**
         * Representacao curta usada apenas para diagnostico.
         */
        @Override
        public String toString() {
            return "#" + id + " " + topic + " <- " + sender;
        }
    }

}
