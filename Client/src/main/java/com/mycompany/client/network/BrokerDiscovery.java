package com.mycompany.client.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Descoberta UDP do broker na rede local.
 *
 * O cliente envia um broadcast com uma assinatura fixa e aguarda a resposta do
 * broker contendo a porta TCP que deve ser usada para a conexao real.
 */
public final class BrokerDiscovery {

    public static final int DISCOVERY_PORT = 5001;
    public static final int DEFAULT_TIMEOUT_MS = 4000;
    private static final String DISCOVERY_REQUEST = "AV3_DISCOVER_BROKER";
    private static final String DISCOVERY_RESPONSE = "AV3_BROKER";

    /**
     * Impede instanciacao porque a descoberta e executada por metodos estaticos.
     */
    private BrokerDiscovery() {
    }

    /**
     * Executa a descoberta usando o tempo limite padrao.
     */
    public static BrokerAddress discover() throws IOException {
        return discover(DEFAULT_TIMEOUT_MS);
    }

    /**
     * Envia pacotes UDP de descoberta e retorna o primeiro broker valido que
     * responder dentro do tempo limite informado.
     */
    public static BrokerAddress discover(int timeoutMs) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(timeoutMs);
            sendDiscoveryRequests(socket);

            byte[] buffer = new byte[256];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            String message = new String(
                    response.getData(),
                    response.getOffset(),
                    response.getLength(),
                    StandardCharsets.UTF_8).trim();

            String[] parts = message.split("\\|", -1);
            if (parts.length != 2 || !DISCOVERY_RESPONSE.equals(parts[0])) {
                throw new IOException("Resposta UDP invalida recebida do broker.");
            }

            int tcpPort;
            try {
                tcpPort = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                throw new IOException("Porta TCP invalida recebida do broker.", ex);
            }

            return new BrokerAddress(response.getAddress().getHostAddress(), tcpPort);
        } catch (SocketTimeoutException ex) {
            throw new IOException("Broker nao encontrado via UDP. Verifique se o broker esta iniciado, se a VM esta na mesma rede e se o firewall liberou UDP "
                    + DISCOVERY_PORT + ".", ex);
        }
    }

    /**
     * Envia a requisicao UDP para todos os enderecos de broadcast conhecidos.
     * Isso melhora a chance de funcionar em redes com multiplas interfaces.
     */
    private static void sendDiscoveryRequests(DatagramSocket socket) throws IOException {
        byte[] requestData = DISCOVERY_REQUEST.getBytes(StandardCharsets.UTF_8);
        for (InetAddress address : broadcastAddresses()) {
            DatagramPacket packet = new DatagramPacket(requestData, requestData.length, address, DISCOVERY_PORT);
            socket.send(packet);
        }
    }

    /**
     * Monta a lista de destinos UDP: broadcast global, localhost e broadcasts
     * especificos de cada interface de rede ativa.
     */
    private static Set<InetAddress> broadcastAddresses() throws IOException {
        Set<InetAddress> addresses = new LinkedHashSet<>();
        addresses.add(InetAddress.getByName("255.255.255.255"));
        addresses.add(InetAddress.getByName("127.0.0.1"));

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (!networkInterface.isUp()) {
                continue;
            }

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast != null) {
                    addresses.add(broadcast);
                }
            }
        }

        return addresses;
    }

    /**
     * Resultado da descoberta: endereco IP do broker e porta TCP anunciada.
     */
    public static final class BrokerAddress {

        private final String host;
        private final int port;

        /**
         * Guarda endereco IP e porta TCP informados pelo broker via UDP.
         */
        private BrokerAddress(String host, int port) {
            this.host = host;
            this.port = port;
        }

        /**
         * IP do broker que respondeu a descoberta.
         */
        public String getHost() {
            return host;
        }

        /**
         * Porta TCP anunciada pelo broker.
         */
        public int getPort() {
            return port;
        }

        /**
         * Representacao host:porta usada em logs e mensagens.
         */
        @Override
        public String toString() {
            return host + ":" + port;
        }
    }
}
