
package com.mycompany.Interfaces;

import com.mycompany.broker.BrokerServer;
import com.mycompany.ui.DarkWin11Theme;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/**
 * Janela administrativa do broker.
 *
 * A tela controla o ciclo de vida do servidor e exibe eventos recebidos pela
 * implementacao BrokerServer.
 */
public class Broker_interface extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Broker_interface.class.getName());
    private final BrokerServer brokerServer;

    /**
     * Inicializa componentes Swing e injeta um listener no broker para atualizar
     * status, log, clientes conectados e topicos em tempo real.
     */
    public Broker_interface() {
        initComponents();
        DarkWin11Theme.apply(this);
        brokerServer = new BrokerServer(BrokerServer.DEFAULT_PORT, new BrokerServer.Listener() {
            @Override
            public void onLog(String message) {
                appendLog(message);
            }

            @Override
            public void onStatus(String status) {
                SwingUtilities.invokeLater(() -> tela_status.setText(status));
            }

            @Override
            public void onSnapshot(List<String> clients, List<String> topics) {
                updateSnapshot(clients, topics);
            }
        });
        tela_status.setText("Offline");
        Botao_encerrar.setEnabled(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                brokerServer.stop();
            }
        });
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Tela_log = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        tela_status = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        Tela_clients_conectados = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tela_lista_topicos_servidor = new javax.swing.JTextArea();
        Botao_iniciar = new javax.swing.JButton();
        Botao_encerrar = new javax.swing.JButton();
        sair = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Servidor");

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Log");

        Tela_log.setEditable(false);
        Tela_log.setColumns(20);
        Tela_log.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        Tela_log.setRows(5);
        jScrollPane1.setViewportView(Tela_log);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("Status:");

        tela_status.setEditable(false);
        tela_status.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("Clients conectados:");

        Tela_clients_conectados.setEditable(false);
        Tela_clients_conectados.setColumns(20);
        Tela_clients_conectados.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        Tela_clients_conectados.setRows(5);
        jScrollPane2.setViewportView(Tela_clients_conectados);

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText("Topicos Criados:");

        tela_lista_topicos_servidor.setEditable(false);
        tela_lista_topicos_servidor.setColumns(20);
        tela_lista_topicos_servidor.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tela_lista_topicos_servidor.setRows(5);
        jScrollPane3.setViewportView(tela_lista_topicos_servidor);

        Botao_iniciar.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        Botao_iniciar.setText("Iniciar Servidor");
        Botao_iniciar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Botao_iniciarMouseClicked(evt);
            }
        });

        Botao_encerrar.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        Botao_encerrar.setText("Encerrar Servidor");
        Botao_encerrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Botao_encerrarMouseClicked(evt);
            }
        });

        sair.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        sair.setText("Sair");
        sair.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sairMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tela_status)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3)
                    .addComponent(Botao_iniciar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Botao_encerrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sair, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tela_status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Botao_iniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Botao_encerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sair, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /*Botao para iniciar o servidor e coloca-lo online*/
    private void Botao_iniciarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Botao_iniciarMouseClicked
        try {
            brokerServer.start();
            Botao_iniciar.setEnabled(false);
            Botao_encerrar.setEnabled(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Nao foi possivel iniciar o broker na porta " + BrokerServer.DEFAULT_PORT + ".\n" + ex.getMessage(),
                    "Erro ao iniciar broker",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_Botao_iniciarMouseClicked
    /*Botao para encerrar o servidor e coloca-lo offline*/
    private void Botao_encerrarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Botao_encerrarMouseClicked
        brokerServer.stop();
        Botao_iniciar.setEnabled(true);
        Botao_encerrar.setEnabled(false);
    }//GEN-LAST:event_Botao_encerrarMouseClicked
    /*Botao para fechar a aplicação*/
    private void sairMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sairMouseClicked
        System.exit(0);
    }//GEN-LAST:event_sairMouseClicked

    /**
     * Adiciona uma linha ao log com horario local. A atualizacao usa
     * SwingUtilities para executar na Event Dispatch Thread do Swing.
     */
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String time = LocalTime.now().withNano(0).toString();
            Tela_log.append("[" + time + "] " + message + System.lineSeparator());
            Tela_log.setCaretPosition(Tela_log.getDocument().getLength());
        });
    }

    /**
     * Atualiza as listas visuais com a fotografia atual do broker.
     */
    private void updateSnapshot(List<String> clients, List<String> topics) {
        SwingUtilities.invokeLater(() -> {
            Tela_clients_conectados.setText(String.join(System.lineSeparator(), clients));
            tela_lista_topicos_servidor.setText(String.join(System.lineSeparator(), topics));
        });
    }

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Botao_encerrar;
    private javax.swing.JButton Botao_iniciar;
    private javax.swing.JTextArea Tela_clients_conectados;
    private javax.swing.JTextArea Tela_log;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton sair;
    private javax.swing.JTextArea tela_lista_topicos_servidor;
    private javax.swing.JTextField tela_status;
    // End of variables declaration//GEN-END:variables
}
