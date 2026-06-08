package com.mycompany.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Cliente TCP usado pelas telas Swing para conversar com o broker.
 *
 * A classe encapsula socket, autenticacao, thread de leitura e serializacao do
 * protocolo para que a interface grafica trabalhe apenas com operacoes de alto
 * nivel. Na parcial 2, a autenticacao e feita pelo certificado do cliente
 * assinado offline pelo broker.
 */
public final class BrokerClient {

    /*
     * Endereco padrao usado quando a descoberta UDP ainda nao configurou outro
     * host. A porta precisa bater com BrokerServer.DEFAULT_PORT.
     */
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 5000;

    private static volatile String configuredHost = DEFAULT_HOST;
    private static volatile int configuredPort = DEFAULT_PORT;

    /**
     * Eventos assincronos gerados pela conexao TCP e entregues para as telas.
     */
    public interface Listener {
        /**
         * Recebe mensagens de status, erros e avisos do broker.
         */
        default void onStatus(String message) {
        }

        /**
         * Recebe a lista atualizada de topicos.
         */
        default void onTopics(List<String> topics) {
        }

        /**
         * Recebe uma mensagem publicada em um topico inscrito.
         */
        default void onMessage(String topic, String sender, String message) {
        }

        /**
         * Recebe confirmacoes de comandos como CREATE_TOPIC, SUBSCRIBE e AUTH.
         */
        default void onOperationConfirmed(String operation, String message, List<String> values) {
        }

        /**
         * Recebe erros enviados pelo broker. Por padrao, reutiliza o status.
         */
        default void onError(String message) {
            onStatus(message);
        }

        /**
         * Avisa a interface quando a conexao e encerrada.
         */
        default void onDisconnected() {
        }
    }

    /*
     * Lista thread-safe porque eventos chegam pela thread de rede enquanto as
     * telas Swing podem registrar/remover listeners.
     */
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    /*
     * Estado de rede da sessao atual. Os campos volatile permitem leitura segura
     * entre a thread da interface e a thread listener do socket.
     */
    private volatile Socket socket;
    private volatile BufferedReader reader;
    private volatile PrintWriter writer;
    private volatile Thread listenerThread;
    private volatile boolean connected;
    private String username;

    /**
     * Registra uma tela ou componente interessado em eventos da conexao.
     */
    public void addListener(Listener listener) {
        if (listener != null) {
            listeners.addIfAbsent(listener);
        }
    }

    /**
     * Remove um observador para evitar atualizacoes em janelas ja fechadas.
     */
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Mantido por compatibilidade. Para login real, use connectAndAuthenticate.
     */
    public synchronized void connect(String username) throws IOException {
        connectAndAuthenticate(configuredHost, configuredPort, username, "", false);
    }

    /**
     * Mantido por compatibilidade. Para login real, use connectAndAuthenticate.
     */
    public synchronized void connect(String host, int port, String username) throws IOException {
        connectAndAuthenticate(host, port, username, "", false);
    }

    /**
     * Abre a conexao TCP e envia login/cadastro com o certificado do cliente.
     * A autenticacao do certificado do broker pela AC fica reservada para a
     * proxima etapa do projeto.
     */
    public synchronized void connectAndAuthenticate(String host, int port, String username, String password, boolean createAccount)
            throws IOException {
        if (connected) {
            disconnect();
        }

        this.username = clean(username, "");
        Socket newSocket = new Socket();
        try {
            newSocket.connect(new InetSocketAddress(host, port), 3000);
            socket = newSocket;
            reader = new BufferedReader(new InputStreamReader(newSocket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(newSocket.getOutputStream(), true, StandardCharsets.UTF_8);

            authenticate(password, createAccount);

            connected = true;
            listenerThread = new Thread(this::listenLoop, "broker-client-listener");
            listenerThread.setDaemon(true);
            listenerThread.start();

            fireStatus("Conectado ao broker em " + host + ":" + port + ".");
        } catch (IOException | RuntimeException ex) {
            cleanup();
            if (ex instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException(ex.getMessage(), ex);
        }
    }

    /**
     * Encerra a sessao TCP de forma cooperativa, enviando DISCONNECT antes de
     * fechar o socket local.
     */
    public synchronized void disconnect() {
        if (!connected) {
            cleanup();
            return;
        }
        send("DISCONNECT");
        cleanup();
        fireStatus("Desconectado do broker.");
        fireDisconnected();
    }

    /**
     * Informa se existe socket autenticado ativo para envio de comandos.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Retorna o nome de usuario autenticado pelo broker.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Define o endereco padrao que sera usado por novas conexoes sem parametros.
     * A descoberta UDP chama este metodo apos localizar o broker.
     */
    public static void configureDefaultConnection(String host, int port) {
        configuredHost = clean(host, DEFAULT_HOST);
        configuredPort = port > 0 ? port : DEFAULT_PORT;
    }

    /**
     * Retorna o host configurado pela descoberta UDP ou o fallback local.
     */
    public static String getConfiguredHost() {
        return configuredHost;
    }

    /**
     * Retorna a porta TCP configurada pela descoberta UDP ou a porta padrao.
     */
    public static int getConfiguredPort() {
        return configuredPort;
    }

    /**
     * Solicita ao broker a criacao de um topico e inscricao automatica nele.
     */
    public void createTopic(String topic) {
        send("CREATE_TOPIC", topic);
    }

    /**
     * Solicita inscricao em um topico existente.
     */
    public void subscribe(String topic) {
        send("SUBSCRIBE", topic);
    }

    /**
     * Solicita entrada em um topico ja assinado, sem criar inscricao nova.
     */
    public void enterTopic(String topic) {
        send("ENTER_TOPIC", topic);
    }

    /**
     * Solicita cancelamento de inscricao em um topico.
     */
    public void unsubscribe(String topic) {
        send("UNSUBSCRIBE", topic);
    }

    /**
     * Solicita exclusao de um topico. A autorizacao e validada pelo broker.
     */
    public void deleteTopic(String topic) {
        send("DELETE_TOPIC", topic);
    }

    /**
     * Publica uma mensagem textual no topico ativo do cliente.
     */
    public void publish(String topic, String message) {
        send("PUBLISH", topic, message);
    }

    /**
     * Pede ao broker a lista atual de topicos.
     */
    public void requestTopics() {
        send("LIST_TOPICS");
    }

    /**
     * Pede ao broker o download de mensagens pendentes dos topicos inscritos.
     */
    public void downloadPendingMessages() {
        send("DOWNLOAD_PENDING");
    }

    /**
     * Thread de leitura da conexao TCP. Mantem a interface responsiva recebendo
     * mensagens do broker em paralelo ao uso normal da tela.
     */
    private void listenLoop() {
        try {
            BufferedReader currentReader = reader;
            String line;
            while (connected && currentReader != null && (line = currentReader.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException ex) {
            if (connected) {
                fireStatus("Conexao perdida: " + ex.getMessage());
            }
        } finally {
            if (connected) {
                cleanup();
                fireDisconnected();
            }
        }
    }

    /**
     * Envia LOGIN ou REGISTER para o broker junto com nome, senha e certificado
     * do cliente. A tela so abre quando o broker responde OK AUTH.
     */
    private void authenticate(String password, boolean createAccount) throws IOException {
        String clientCertificate = ClientCertificateSupport.loadClientCertificate(username);
        sendProtocolLine(createAccount ? "REGISTER" : "LOGIN", username, password, clientCertificate);

        while (true) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Broker encerrou a conexao durante a autenticacao.");
            }

            // Respostas INFO iniciais sao ignoradas ate chegar OK AUTH ou ERROR.
            String[] parts = line.split("\\|", -1);
            if (parts.length == 0) {
                continue;
            }

            if ("ERROR".equals(parts[0])) {
                requireParts(parts, 2);
                throw new IOException(decode(parts[1]));
            }

            if ("OK".equals(parts[0]) && parts.length >= 3 && "AUTH".equals(parts[1])) {
                // O broker devolve o nome confirmado para sincronizar o estado local.
                List<String> values = new ArrayList<>();
                for (int i = 3; i < parts.length; i++) {
                    values.add(decode(parts[i]));
                }
                if (!values.isEmpty()) {
                    username = values.get(0);
                }
                return;
            }
        }
    }

    /**
     * Decodifica uma resposta do protocolo e dispara o evento correspondente:
     * status, lista de topicos ou mensagem publicada.
     */
    private void processLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length == 0) {
            return;
        }

        try {
            switch (parts[0]) {
                case "OK":
                    processOk(parts);
                    break;
                case "ERROR":
                    requireParts(parts, 2);
                    fireError(decode(parts[1]));
                    break;
                case "INFO":
                    requireParts(parts, 2);
                    fireStatus(decode(parts[1]));
                    break;
                case "TOPICS":
                    List<String> topics = new ArrayList<>();
                    for (int i = 1; i < parts.length; i++) {
                        topics.add(decode(parts[i]));
                    }
                    fireTopics(topics);
                    break;
                case "MESSAGE":
                    requireParts(parts, 4);
                    fireMessage(decode(parts[1]), decode(parts[2]), decode(parts[3]));
                    break;
                default:
                    fireStatus("Resposta desconhecida do broker: " + parts[0]);
                    break;
            }
        } catch (IllegalArgumentException ex) {
            fireStatus("Resposta invalida do broker.");
        }
    }

    /**
     * Processa confirmacoes de operacao enviadas pelo broker.
     *
     * Formato atual: OK|OPERACAO|mensagem|valor1|valor2. O formato antigo
     * OK|mensagem ainda e aceito para compatibilidade.
     */
    private void processOk(String[] parts) {
        requireParts(parts, 2);
        if (parts.length == 2) {
            fireStatus(decode(parts[1]));
            return;
        }

        requireParts(parts, 3);
        String operation = parts[1];
        String message = decode(parts[2]);
        List<String> values = new ArrayList<>();
        for (int i = 3; i < parts.length; i++) {
            values.add(decode(parts[i]));
        }
        fireStatus(message);
        fireOperationConfirmed(operation, message, values);
    }

    /**
     * Monta e envia uma linha do protocolo. Os argumentos sao codificados em
     * Base64 URL-safe para preservar caracteres especiais digitados pelo usuario.
     */
    private synchronized void send(String command, String... values) {
        if (!connected || writer == null) {
            fireStatus("Cliente nao conectado ao broker.");
            return;
        }
        sendProtocolLine(command, values);
    }

    /**
     * Envia uma linha de protocolo ja montada. E usado tanto no login inicial
     * quanto em comandos normais depois da autenticacao.
     */
    private synchronized void sendProtocolLine(String command, String... values) {
        PrintWriter currentWriter = writer;
        if (currentWriter == null) {
            return;
        }

        List<String> fields = new ArrayList<>();
        fields.add(command);
        for (String value : values) {
            fields.add(encode(value));
        }

        currentWriter.println(String.join("|", fields));
    }

    /**
     * Limpa referencias de rede e fecha o socket local apos desconexao ou erro.
     */
    private synchronized void cleanup() {
        connected = false;
        writer = null;
        reader = null;
        Socket currentSocket = socket;
        socket = null;
        if (currentSocket != null) {
            try {
                currentSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Notifica todos os ouvintes sobre mensagens operacionais ou erros.
     */
    private void fireStatus(String message) {
        for (Listener listener : listeners) {
            listener.onStatus(message);
        }
    }

    /**
     * Notifica os ouvintes com a lista atualizada de topicos.
     */
    private void fireTopics(List<String> topics) {
        for (Listener listener : listeners) {
            listener.onTopics(topics);
        }
    }

    /**
     * Notifica os ouvintes sobre uma publicacao recebida do broker.
     */
    private void fireMessage(String topic, String sender, String message) {
        for (Listener listener : listeners) {
            listener.onMessage(topic, sender, message);
        }
    }

    /**
     * Notifica os ouvintes quando o broker aceita uma operacao solicitada.
     */
    private void fireOperationConfirmed(String operation, String message, List<String> values) {
        for (Listener listener : listeners) {
            listener.onOperationConfirmed(operation, message, values);
        }
    }

    /**
     * Notifica os ouvintes sobre erros curtos enviados pelo broker.
     */
    private void fireError(String message) {
        for (Listener listener : listeners) {
            listener.onError(message);
        }
    }

    /**
     * Notifica os ouvintes quando a conexao TCP deixa de estar ativa.
     */
    private void fireDisconnected() {
        for (Listener listener : listeners) {
            listener.onDisconnected();
        }
    }

    /**
     * Valida a quantidade minima de campos de uma resposta recebida.
     */
    private static void requireParts(String[] parts, int expected) {
        if (parts.length < expected) {
            throw new IllegalArgumentException("Mensagem incompleta.");
        }
    }

    /**
     * Normaliza textos informados pelo usuario e aplica fallback quando vazios.
     */
    private static String clean(String value, String fallback) {
        String clean = value == null ? "" : value.trim();
        return clean.isEmpty() ? fallback : clean;
    }

    /**
     * Codifica campos textuais antes do envio pelo protocolo de linha.
     */
    private static String encode(String value) {
        String safeValue = value == null ? "" : value.trim();
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(safeValue.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodifica campos textuais recebidos do broker.
     */
    private static String decode(String value) {
        byte[] bytes = Base64.getUrlDecoder().decode(value);
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
