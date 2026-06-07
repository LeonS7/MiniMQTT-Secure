package com.mycompany.avaliacao_3;

import com.mycompany.Interfaces.Broker_interface;
import javax.swing.SwingUtilities;

/**
 * Ponto de entrada do executavel do broker.
 */
public class Broker_main {

    /**
     * Abre a interface Swing do broker na Event Dispatch Thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Broker_interface broker = new Broker_interface();
            broker.setLocationRelativeTo(null);
            broker.setVisible(true);
        });
    }
}
