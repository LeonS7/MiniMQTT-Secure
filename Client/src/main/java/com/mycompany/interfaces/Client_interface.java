
package com.mycompany.interfaces;

import com.mycompany.client.network.BrokerClient;
import com.mycompany.ui.DarkWin11Theme;
import java.time.LocalTime;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/**
 * Tela principal do cliente.
 *
 * Exibe status, topicos disponiveis, mensagens recebidas e permite publicar no
 * topico ativo por meio da conexao BrokerClient.
 */
public class Client_interface extends javax.swing.JFrame implements BrokerClient.Listener {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Client_interface.class.getName());
    private BrokerClient brokerClient;
    private String username;
    private String activeTopic = "";
    private String pendingActiveTopic = "";

    /**
     * Construtor padrao usado pelo editor visual. Sem BrokerClient, a tela abre
     * em modo nao conectado.
     */
    public Client_interface() {
        this("Cliente", null);
    }

    /**
     * Construtor usado apos o login. Recebe o usuario e a conexao TCP ja aberta
     * para registrar a tela como ouvinte dos eventos do broker.
     */
    public Client_interface(String username, BrokerClient brokerClient) {
        initComponents();
        DarkWin11Theme.apply(this);
        this.username = username == null || username.trim().isEmpty() ? "Cliente" : username.trim();
        this.brokerClient = brokerClient;
        prepareClientWindow();
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        Tela_msg_servidor = new javax.swing.JTextArea();
        jLabel14 = new javax.swing.JLabel();
        botao_configuracoes = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tela_status_cliente = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        tela_lista_topicos_cliente = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        nome_chat = new javax.swing.JTextField();
        botao_entrar_chat = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tela_chat = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        enviar_msg = new javax.swing.JTextField();
        botao_enviar_msg = new javax.swing.JButton();
        botao_encerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Cliente");

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        Tela_msg_servidor.setEditable(false);
        Tela_msg_servidor.setColumns(20);
        Tela_msg_servidor.setRows(5);
        jScrollPane3.setViewportView(Tela_msg_servidor);

        jLabel14.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("Bem-Vindo!");

        botao_configuracoes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        botao_configuracoes.setText("Configurações");
        botao_configuracoes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botao_configuracoesMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(botao_configuracoes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 487, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botao_configuracoes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Status");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Lista de Tópicos:");

        tela_status_cliente.setColumns(20);
        tela_status_cliente.setRows(5);
        jScrollPane1.setViewportView(tela_status_cliente);

        tela_lista_topicos_cliente.setColumns(20);
        tela_lista_topicos_cliente.setRows(5);
        jScrollPane2.setViewportView(tela_lista_topicos_cliente);

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText("Informe o nome de um tópico para participar da conversa:");

        botao_entrar_chat.setText("Entrar");
        botao_entrar_chat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botao_entrar_chatMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)))
                    .addComponent(nome_chat)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(botao_entrar_chat, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nome_chat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botao_entrar_chat)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Chat");

        tela_chat.setEditable(false);
        tela_chat.setColumns(20);
        tela_chat.setRows(5);
        jScrollPane4.setViewportView(tela_chat);

        jLabel5.setText("Escreva a sua Mensagem:");

        botao_enviar_msg.setText("Enviar");
        botao_enviar_msg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botao_enviar_msgMouseClicked(evt);
            }
        });

        botao_encerrar.setText("Encerrar");
        botao_encerrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botao_encerrarMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane4)
                    .addComponent(enviar_msg)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(botao_enviar_msg, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(botao_encerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(enviar_msg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botao_enviar_msg)
                    .addComponent(botao_encerrar))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /*Botao para abrir a interface Configuraçoes*/
    private void botao_configuracoesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botao_configuracoesMouseClicked
        if (!ensureConnected()) {
            return;
        }
        Opções_interface options = new Opções_interface(this, brokerClient);
        options.setLocationRelativeTo(this);
        options.setVisible(true);
    }//GEN-LAST:event_botao_configuracoesMouseClicked
    /*Botao para entrar no topico informado para começar a mandar mensagens*/
    private void botao_entrar_chatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botao_entrar_chatMouseClicked
        enterTopic();
    }//GEN-LAST:event_botao_entrar_chatMouseClicked
    /*Botao para enviar mensagem*/
    private void botao_enviar_msgMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botao_enviar_msgMouseClicked
        publishMessage();
    }//GEN-LAST:event_botao_enviar_msgMouseClicked
    /*Botao para desconectar usuario do servidor, fechar a interface Cliente e 
    voltar a interface de login*/
    private void botao_encerrarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botao_encerrarMouseClicked
        closeClientAndReturnToLogin();
    }//GEN-LAST:event_botao_encerrarMouseClicked

    /**
     * Configura campos somente leitura, registra listeners e prepara a janela
     * para receber atualizacoes assincronas do broker.
     */
    private void prepareClientWindow() {
        jLabel14.setText("Bem-vindo, " + username + "!");
        tela_status_cliente.setEditable(false);
        tela_lista_topicos_cliente.setEditable(false);
        Tela_msg_servidor.setEditable(false);
        tela_chat.setEditable(false);
        configureTextWrapping();
        Tela_msg_servidor.setText(welcomeMessage());
        Tela_msg_servidor.setCaretPosition(0);
        setLocationRelativeTo(null);

        if (brokerClient != null) {
            brokerClient.addListener(this);
            brokerClient.requestTopics();
            brokerClient.downloadPendingMessages();
            appendStatus("Cliente pronto.");
        } else {
            appendStatus("Cliente nao conectado. Abra pela tela de login.");
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                if (brokerClient != null) {
                    brokerClient.removeListener(Client_interface.this);
                    brokerClient.disconnect();
                }
            }
        });
    }

    /**
     * Habilita quebra automatica de linha nas areas com texto livre para evitar
     * que o usuario precise redimensionar a janela para ler o conteudo.
     */
    private void configureTextWrapping() {
        Tela_msg_servidor.setLineWrap(true);
        Tela_msg_servidor.setWrapStyleWord(true);
        tela_status_cliente.setLineWrap(true);
        tela_status_cliente.setWrapStyleWord(true);
        tela_chat.setLineWrap(true);
        tela_chat.setWrapStyleWord(true);
    }

    /**
     * Texto fixo exibido na area de mensagens do servidor. Esta area nao recebe
     * mensagens de chat nem logs operacionais; ela serve como orientacao inicial.
     */
    private String welcomeMessage() {
        return "Bem-vindo!" + System.lineSeparator()
                + System.lineSeparator()
                + "Para comecar, clique no botao Configuracoes." + System.lineSeparator()
                + System.lineSeparator()
                + "Na tela de Configuracoes voce pode criar topicos, "
                + "inscrever-se em topicos existentes, cancelar inscricoes "
                + "e excluir apenas os topicos que voce criou." + System.lineSeparator()
                + System.lineSeparator()
                + "Depois de se inscrever, volte para esta tela, informe o "
                + "nome do topico no campo de entrada do chat, clique em Entrar "
                + "e envie suas mensagens pelo campo inferior.";
    }

    /**
     * Define o topico ativo do chat e solicita inscricao ao broker.
     */
    private void enterTopic() {
        if (!ensureConnected()) {
            return;
        }

        String topic = nome_chat.getText().trim();
        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe o nome do topico.",
                    "Topico obrigatorio",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        brokerClient.subscribe(topic);
        pendingActiveTopic = topic;
        appendStatus("Solicitando entrada no topico: " + topic);
    }

    /**
     * Publica o texto digitado no topico ativo. O envio so ocorre quando ha
     * conexao e um topico selecionado.
     */
    private void publishMessage() {
        if (!ensureConnected()) {
            return;
        }
        if (activeTopic.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Entre em um topico antes de enviar mensagens.",
                    "Topico nao selecionado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = enviar_msg.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        brokerClient.publish(activeTopic, message);
        enviar_msg.setText("");
    }

    /**
     * Valida se a conexao TCP esta ativa antes de executar operacoes de rede.
     */
    private boolean ensureConnected() {
        if (brokerClient != null && brokerClient.isConnected()) {
            return true;
        }

        JOptionPane.showMessageDialog(this,
                "Cliente nao conectado ao broker. Inicie o broker e entre novamente.",
                "Sem conexao",
                JOptionPane.WARNING_MESSAGE);
        return false;
    }

    /**
     * Remove a tela dos listeners, encerra a conexao e retorna ao login.
     */
    private void closeClientAndReturnToLogin() {
        if (brokerClient != null) {
            brokerClient.removeListener(this);
            brokerClient.disconnect();
        }
        dispose();
        Login_interface login = new Login_interface();
        login.setLocationRelativeTo(null);
        login.setVisible(true);
    }

    /**
     * Escreve eventos de status na area lateral usando a thread grafica do Swing.
     */
    private void appendStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            String time = LocalTime.now().withNano(0).toString();
            tela_status_cliente.append("[" + time + "] " + message + System.lineSeparator());
            tela_status_cliente.setCaretPosition(tela_status_cliente.getDocument().getLength());
        });
    }

    @Override
    /**
     * Callback chamado pelo BrokerClient quando chega OK, INFO ou ERROR.
     */
    public void onStatus(String message) {
        appendStatus(message);
    }

    @Override
    /**
     * Callback chamado quando o broker envia a lista atualizada de topicos.
     */
    public void onTopics(List<String> topics) {
        SwingUtilities.invokeLater(() -> tela_lista_topicos_cliente.setText(String.join(System.lineSeparator(), topics)));
    }

    @Override
    /**
     * Callback chamado quando chega uma publicacao de um topico inscrito.
     */
    public void onMessage(String topic, String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            tela_chat.append("[" + topic + "] " + sender + ": " + message + System.lineSeparator());
            tela_chat.setCaretPosition(tela_chat.getDocument().getLength());
        });
    }

    @Override
    /**
     * Callback chamado quando o servidor confirma uma requisicao enviada pelo
     * cliente. A tela so considera um topico ativo apos o OK do broker.
     */
    public void onOperationConfirmed(String operation, String message, List<String> values) {
        if (!"SUBSCRIBE".equals(operation) || values.isEmpty()) {
            return;
        }

        String confirmedTopic = values.get(0);
        if (!confirmedTopic.equals(pendingActiveTopic)) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            activeTopic = confirmedTopic;
            pendingActiveTopic = "";
            nome_chat.setText("");
            appendStatus("Topico ativo confirmado pelo servidor: " + activeTopic);
        });
    }

    @Override
    /**
     * Callback chamado quando a conexao TCP e finalizada ou perdida.
     */
    public void onDisconnected() {
        activeTopic = "";
        pendingActiveTopic = "";
        appendStatus("Conexao com o broker encerrada.");
    }

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea Tela_msg_servidor;
    private javax.swing.JButton botao_configuracoes;
    private javax.swing.JButton botao_encerrar;
    private javax.swing.JButton botao_entrar_chat;
    private javax.swing.JButton botao_enviar_msg;
    private javax.swing.JTextField enviar_msg;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField nome_chat;
    private javax.swing.JTextArea tela_chat;
    private javax.swing.JTextArea tela_lista_topicos_cliente;
    private javax.swing.JTextArea tela_status_cliente;
    // End of variables declaration//GEN-END:variables
}
