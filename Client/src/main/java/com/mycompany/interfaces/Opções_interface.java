
package com.mycompany.interfaces;

import com.mycompany.client.network.BrokerClient;
import com.mycompany.ui.DarkWin11Theme;
import java.time.LocalTime;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Tela de configuracoes do cliente.
 *
 * Centraliza operacoes de administracao de topicos: criar, inscrever,
 * cancelar inscricao e excluir quando o broker autorizar.
 */
public class Opções_interface extends javax.swing.JFrame implements BrokerClient.Listener {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Opções_interface.class.getName());
    private Client_interface parentWindow;
    private BrokerClient brokerClient;

    /**
     * Construtor padrao usado pelo editor visual. Sem BrokerClient, a tela abre
     * apenas com aviso de cliente nao conectado.
     */
    public Opções_interface() {
        this(null, null);
    }

    /**
     * Recebe a tela principal e a conexao TCP ativa para enviar comandos de
     * topico e receber atualizacoes de status/lista.
     */
    public Opções_interface(Client_interface parentWindow, BrokerClient brokerClient) {
        initComponents();
        DarkWin11Theme.apply(this);
        this.parentWindow = parentWindow;
        this.brokerClient = brokerClient;
        prepareOptionsWindow();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        nome_entrar_topico = new javax.swing.JTextField();
        botao_entrar_topico = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        nome_criar_topico = new javax.swing.JTextField();
        botao_criar_topico = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        nome_cancelar_topico = new javax.swing.JTextField();
        botao_cancelar_topico = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        botao_excluir_topico = new javax.swing.JButton();
        nome_excluir_topico = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tela_status_config = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tela_lista_topicos_config = new javax.swing.JTextArea();
        botao_concluir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Inscreva-se em um tópico");

        jLabel12.setText("Nome do tópico:");

        botao_entrar_topico.setText("Entrar");
        botao_entrar_topico.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botao_entrar_topicoMouseClicked(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Crie um novo tópico");

        jLabel14.setText("Nome:");

        botao_criar_topico.setText("Criar");
        botao_criar_topico.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botao_criar_topicoMouseClicked(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Cancelar inscrição em um tópico");

        jLabel16.setText("Nome:");

        botao_cancelar_topico.setText("Cancelar Inscrição");
        botao_cancelar_topico.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botao_cancelar_topicoMouseClicked(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Excluir tópico");

        jLabel18.setText("Nome:");

        botao_excluir_topico.setText("Excluir");
        botao_excluir_topico.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botao_excluir_topicoMouseClicked(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Obs: Voce só podera excluir tópicos que voce criou");
        jLabel19.setAutoscrolls(true);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nome_cancelar_topico)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nome_entrar_topico)
                    .addComponent(botao_entrar_topico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nome_criar_topico)
                    .addComponent(botao_criar_topico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(botao_cancelar_topico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nome_excluir_topico)
                    .addComponent(botao_excluir_topico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nome_entrar_topico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botao_entrar_topico)
                .addGap(18, 18, 18)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nome_criar_topico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botao_criar_topico)
                .addGap(18, 18, 18)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nome_cancelar_topico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botao_cancelar_topico)
                .addGap(18, 18, 18)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nome_excluir_topico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botao_excluir_topico)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Configurações");

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Status");

        tela_status_config.setColumns(20);
        tela_status_config.setRows(5);
        jScrollPane1.setViewportView(tela_status_config);

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Lista de Tópicos");

        tela_lista_topicos_config.setColumns(20);
        tela_lista_topicos_config.setRows(5);
        jScrollPane2.setViewportView(tela_lista_topicos_config);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2)
                .addContainerGap())
        );

        botao_concluir.setText("Concluir");
        botao_concluir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botao_concluirMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botao_concluir, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botao_concluir)
                .addContainerGap(12, Short.MAX_VALUE))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /*Botao para se inscrever no topico informado*/
    private void botao_entrar_topicoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botao_entrar_topicoMouseClicked
        sendTopicCommand(nome_entrar_topico, "subscribe");
    }//GEN-LAST:event_botao_entrar_topicoMouseClicked
    /*Botao para criar um topico*/
    private void botao_criar_topicoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botao_criar_topicoMouseClicked
        sendTopicCommand(nome_criar_topico, "create");
    }//GEN-LAST:event_botao_criar_topicoMouseClicked
    /*Botao para cancelar a inscrição em um topico*/
    private void botao_cancelar_topicoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botao_cancelar_topicoMouseClicked
        sendTopicCommand(nome_cancelar_topico, "unsubscribe");
    }//GEN-LAST:event_botao_cancelar_topicoMouseClicked
    /*Botao para excluir o topico informado (obs: somente o usuario que criou o 
    topico pode excluir este mesmo topico)*/
    private void botao_excluir_topicoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botao_excluir_topicoMouseClicked
        sendTopicCommand(nome_excluir_topico, "delete");
    }//GEN-LAST:event_botao_excluir_topicoMouseClicked
    /*Botao para fechar a interface Configurações e voltar a interface Cliente*/
    private void botao_concluirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botao_concluirMouseClicked
        closeOptions();
    }//GEN-LAST:event_botao_concluirMouseClicked

    /**
     * Configura campos somente leitura, define fechamento por dispose e registra
     * a janela como ouvinte temporaria dos eventos do broker.
     */
    private void prepareOptionsWindow() {
        tela_status_config.setEditable(false);
        tela_lista_topicos_config.setEditable(false);
        tela_status_config.setLineWrap(true);
        tela_status_config.setWrapStyleWord(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        if (brokerClient != null) {
            brokerClient.addListener(this);
            brokerClient.requestTopics();
            appendStatus("Configuracoes prontas.");
        } else {
            appendStatus("Cliente nao conectado.");
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                if (brokerClient != null) {
                    brokerClient.removeListener(Opções_interface.this);
                }
            }
        });
    }

    /**
     * Normaliza o nome digitado e roteia a acao da interface para o comando
     * correspondente do BrokerClient.
     */
    private void sendTopicCommand(javax.swing.JTextField field, String command) {
        if (!ensureConnected()) {
            return;
        }

        String topic = field.getText().trim();
        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe o nome do topico.",
                    "Topico obrigatorio",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        switch (command) {
            case "create":
                brokerClient.createTopic(topic);
                break;
            case "subscribe":
                brokerClient.subscribe(topic);
                break;
            case "unsubscribe":
                brokerClient.unsubscribe(topic);
                break;
            case "delete":
                brokerClient.deleteTopic(topic);
                break;
            default:
                appendStatus("Acao desconhecida.");
                return;
        }

        field.setText("");
        brokerClient.requestTopics();
    }

    /**
     * Verifica se ainda existe conexao TCP antes de enviar comandos ao broker.
     */
    private boolean ensureConnected() {
        if (brokerClient != null && brokerClient.isConnected()) {
            return true;
        }
        JOptionPane.showMessageDialog(this,
                "Cliente nao conectado ao broker.",
                "Sem conexao",
                JOptionPane.WARNING_MESSAGE);
        return false;
    }

    /**
     * Acrescenta eventos da operacao de topicos na area de status da tela.
     */
    private void appendStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            String time = LocalTime.now().withNano(0).toString();
            tela_status_config.append("[" + time + "] " + message + System.lineSeparator());
            tela_status_config.setCaretPosition(tela_status_config.getDocument().getLength());
        });
    }

    /**
     * Remove o listener desta janela e retorna o foco para a tela principal.
     */
    private void closeOptions() {
        if (brokerClient != null) {
            brokerClient.removeListener(this);
        }
        if (parentWindow != null) {
            parentWindow.toFront();
        }
        dispose();
    }

    @Override
    /**
     * Callback para mensagens de sucesso, erro ou informacao enviadas pelo broker.
     */
    public void onStatus(String message) {
        appendStatus(message);
    }

    @Override
    /**
     * Callback para atualizar a lista de topicos exibida nesta tela.
     */
    public void onTopics(List<String> topics) {
        SwingUtilities.invokeLater(() -> tela_lista_topicos_config.setText(String.join(System.lineSeparator(), topics)));
    }

    @Override
    /**
     * Callback chamado quando a conexao com o broker e encerrada.
     */
    public void onDisconnected() {
        appendStatus("Conexao com o broker encerrada.");
    }

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botao_cancelar_topico;
    private javax.swing.JButton botao_concluir;
    private javax.swing.JButton botao_criar_topico;
    private javax.swing.JButton botao_entrar_topico;
    private javax.swing.JButton botao_excluir_topico;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField nome_cancelar_topico;
    private javax.swing.JTextField nome_criar_topico;
    private javax.swing.JTextField nome_entrar_topico;
    private javax.swing.JTextField nome_excluir_topico;
    private javax.swing.JTextArea tela_lista_topicos_config;
    private javax.swing.JTextArea tela_status_config;
    // End of variables declaration//GEN-END:variables
}
