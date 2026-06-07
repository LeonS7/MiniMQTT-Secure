package com.mycompany.client;

import com.mycompany.client.network.BrokerClient;
import com.mycompany.interfaces.Login_interface;
import javax.swing.SwingUtilities;

/**
 * Ponto de entrada do executavel do cliente.
 */
public class Client_main {

    /**
     * Configura parametros opcionais de conexao e abre a tela de login.
     */
    public static void main(String[] args) {
        configureBrokerFromArgs(args);
        SwingUtilities.invokeLater(() -> {
            Login_interface login = new Login_interface();
            login.setLocationRelativeTo(null);
            login.setVisible(true);
        });
    }

    /**
     * Mantem compatibilidade com execucao por linha de comando. Se forem
     * passados host e porta, esses valores viram fallback para a conexao.
     */
    private static void configureBrokerFromArgs(String[] args) {
        if (args == null || args.length == 0) {
            return;
        }

        String host = args[0];
        int port = BrokerClient.DEFAULT_PORT;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                port = BrokerClient.DEFAULT_PORT;
            }
        }
        BrokerClient.configureDefaultConnection(host, port);
    }
}
