
package com.mycompany.interfaces;

import com.mycompany.client.network.BrokerClient;
import com.mycompany.client.network.BrokerDiscovery;
import com.mycompany.ui.DarkWin11Theme;
import java.awt.Cursor;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Tela inicial do cliente.
 *
 * Nesta etapa, o nome informado identifica o cliente no broker.
 */
public class Login_interface extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Login_interface.class.getName());

    /**
     * Inicializa a tela e centraliza a janela.
     */
    public Login_interface() {
        initComponents();
        DarkWin11Theme.apply(this);
        setLocationRelativeTo(null);
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        nome_login = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        senha_login = new javax.swing.JTextField();
        botao_login = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        botao_criarConta = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        nome_criarConta = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        senha_criarConta = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        confirma_senha = new javax.swing.JTextField();
        sair = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Login");

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText("Nome:");

        nome_login.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("Senha:");

        senha_login.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        botao_login.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        botao_login.setText("Login");
        botao_login.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botao_loginMouseClicked(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Ou");

        jLabel6.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Criar Conta");

        botao_criarConta.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        botao_criarConta.setText("Criar Conta");
        botao_criarConta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botao_criarContaMouseClicked(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("Nome:");

        nome_criarConta.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText("Senha:");

        senha_criarConta.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText("Confirmar Senha:");

        confirma_senha.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        sair.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        sair.setText("Sair");
        sair.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sairMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nome_login)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(senha_login)
                    .addComponent(botao_login, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
                    .addComponent(botao_criarConta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nome_criarConta)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(senha_criarConta)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(confirma_senha)
                    .addComponent(sair, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nome_login, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(senha_login, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botao_login)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nome_criarConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(senha_criarConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(confirma_senha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botao_criarConta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sair)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
    /*Botao para fazer login e realizado com sucesso abrir a interface Cliente*/
    private void botao_loginMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botao_loginMouseClicked
        openClient(false);
    }//GEN-LAST:event_botao_loginMouseClicked
    /*Botao para registrar um novo usuario e se a conexão for feita com
    sucesso abir a interface Cliente*/
    private void botao_criarContaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botao_criarContaMouseClicked
        openClient(true);
    }//GEN-LAST:event_botao_criarContaMouseClicked
    /*Botao para fechar a aplicação*/
    private void sairMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sairMouseClicked
        System.exit(0);
    }//GEN-LAST:event_sairMouseClicked

    /**
     * Valida o nome do usuario, descobre o broker via UDP em segundo plano e,
     * quando encontrado, abre a conexao TCP. O parametro createAccount decide
     * se a requisicao enviada ao broker sera LOGIN ou REGISTER.
     */
    private void openClient(boolean createAccount) {
        String username = createAccount ? nome_criarConta.getText().trim() : nome_login.getText().trim();
        String password = createAccount ? senha_criarConta.getText() : senha_login.getText();

        // A interface impede requisicoes claramente incompletas antes de acionar a rede.
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe um nome para conectar.",
                    "Nome obrigatorio",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // No cadastro, a confirmacao evita criar conta com senha digitada errado.
        if (createAccount && !senha_criarConta.getText().equals(confirma_senha.getText())) {
            JOptionPane.showMessageDialog(this,
                    "A confirmacao de senha nao confere.",
                    "Senha invalida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        /*
         * A descoberta UDP e a conexao TCP podem bloquear por alguns segundos.
         * SwingWorker faz isso fora da thread grafica para a janela nao travar.
         */
        setLoginBusy(true);
        new SwingWorker<ConnectionResult, Void>() {
            @Override
            protected ConnectionResult doInBackground() throws Exception {
                // Primeiro localiza o broker pela rede.
                BrokerDiscovery.BrokerAddress target = BrokerDiscovery.discover();
                BrokerClient.configureDefaultConnection(target.getHost(), target.getPort());

                // Depois conecta e so retorna quando o broker aceitar a autenticacao.
                BrokerClient brokerClient = new BrokerClient();
                brokerClient.connectAndAuthenticate(target.getHost(), target.getPort(), username, password, createAccount);
                return new ConnectionResult(brokerClient);
            }

            @Override
            protected void done() {
                setLoginBusy(false);
                try {
                    // Se get() nao lancar excecao, o broker autenticou o cliente.
                    ConnectionResult result = get();
                    Client_interface clientWindow = new Client_interface(username, result.brokerClient);
                    clientWindow.setVisible(true);
                    dispose();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showConnectionError("Busca pelo broker interrompida.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    showConnectionError(cause.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Bloqueia botoes e muda o cursor durante a descoberta/conexao para evitar
     * cliques repetidos enquanto a operacao assincrona esta em andamento.
     */
    private void setLoginBusy(boolean busy) {
        botao_login.setEnabled(!busy);
        botao_criarConta.setEnabled(!busy);
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        setTitle(busy ? "Login - procurando broker via UDP..." : "Login");
    }

    /**
     * Exibe uma mensagem unica para falhas de descoberta UDP ou conexao TCP.
     */
    private void showConnectionError(String detail) {
        JOptionPane.showMessageDialog(this,
                """
                Nao foi possivel descobrir/conectar ao broker via UDP.
                Inicie o broker e libere as portas UDP 5001 e TCP 5000 no firewall.
                Verifique tambem se o certificado do cliente esta na pasta certificados/clientes.
                
                Detalhe: """ + detail,
                "Erro de conexao",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Resultado interno da tarefa assincrona de conexao.
     */
    private static final class ConnectionResult {

        private final BrokerClient brokerClient;

        private ConnectionResult(BrokerClient brokerClient) {
            this.brokerClient = brokerClient;
        }
    }

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botao_criarConta;
    private javax.swing.JButton botao_login;
    private javax.swing.JTextField confirma_senha;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField nome_criarConta;
    private javax.swing.JTextField nome_login;
    private javax.swing.JButton sair;
    private javax.swing.JTextField senha_criarConta;
    private javax.swing.JTextField senha_login;
    // End of variables declaration//GEN-END:variables
}
