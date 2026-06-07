package com.mycompany.broker;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Centraliza as verificacoes executadas pelo broker.
 *
 * O cliente faz apenas as requisicoes; as regras de negocio, login, cadastro,
 * permissao, existencia de topicos, inscricao e certificados ficam aqui.
 */
public final class BrokerVerificationService {

    /*
     * Quantidade de bytes aleatorios usados como salt da senha. O salt impede
     * que duas senhas iguais tenham exatamente o mesmo hash armazenado.
     */
    private static final int SALT_BYTES = 16;

    /*
     * Estruturas de usuario mantidas pelo broker. O mapa fica em memoria para
     * acesso rapido e tambem e persistido em usuarios.properties.
     */
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentMap<String, UserAccount> accounts = new ConcurrentHashMap<>();
    private final Path accountFile = certificateDirectory().resolve("usuarios.properties");

    /**
     * Carrega usuarios ja cadastrados assim que o servico de validacao e criado.
     */
    public BrokerVerificationService() {
        loadAccounts();
    }

    /**
     * Verifica se uma mensagem do protocolo possui a quantidade minima de
     * campos esperada pelo comando recebido.
     */
    public void requireParts(String[] parts, int expected) {
        if (parts == null || parts.length < expected) {
            throw new IllegalArgumentException("Comando incompleto.");
        }
    }

    /**
     * Decodifica um campo Base64 URL-safe do protocolo textual.
     */
    public String decodeProtocolField(String value) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(value);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Mensagem invalida.");
        }
    }

    /**
     * Normaliza o nome do cliente e usa fallback quando o cliente nao informa
     * um identificador valido.
     */
    public String verifyClientName(String value, String fallback) {
        String clean = value == null ? "" : value.trim();
        return clean.isEmpty() ? fallback : clean;
    }

    /**
     * Valida usuario, senha e certificado para login.
     */
    public String verifyLogin(String clientName, String password, String certificateText) {
        String cleanName = verifyRequiredClientName(clientName);
        verifyPasswordPresent(password);
        verifyClientCertificate(cleanName, certificateText);

        UserAccount account = accounts.get(cleanName);
        if (account == null || !account.matches(password)) {
            throw new IllegalArgumentException("Usuario ou senha invalidos.");
        }
        return cleanName;
    }

    /**
     * Valida usuario, senha e certificado e cria a conta quando ela ainda nao
     * existe.
     */
    public synchronized String verifyAccountCreation(String clientName, String password, String certificateText) {
        String cleanName = verifyRequiredClientName(clientName);
        verifyPasswordPresent(password);
        verifyClientCertificate(cleanName, certificateText);

        if (accounts.containsKey(cleanName)) {
            throw new IllegalArgumentException("Usuario ja cadastrado.");
        }

        accounts.put(cleanName, UserAccount.create(password, secureRandom));
        saveAccounts();
        return cleanName;
    }

    /**
     * Verifica se o certificado do cliente foi assinado pela chave privada do
     * broker e se o certificado pertence ao nome de cliente informado.
     */
    public void verifyClientCertificate(String clientName, String certificateText) {
        String cleanClientName = verifyRequiredClientName(clientName);
        String cleanCertificate = certificateText == null ? "" : certificateText.trim();
        if (cleanCertificate.isEmpty()) {
            throw new IllegalArgumentException("Certificado do cliente nao informado.");
        }

        try {
            Properties certificate = loadCertificateProperties(cleanCertificate);
            String type = certificate.getProperty("type", "");
            String subject = certificate.getProperty("subject", "");
            String publicKey = certificate.getProperty("publicKey", "");
            String issuer = certificate.getProperty("issuer", "");
            String issuedAt = certificate.getProperty("issuedAt", "");
            String signature = certificate.getProperty("signature", "");

            // Confere se o arquivo realmente segue o tipo de certificado esperado para cliente.
            if (!BrokerCertificateSupport.CLIENT_CERTIFICATE_TYPE.equals(type)) {
                throw new IllegalArgumentException("Tipo de certificado invalido.");
            }
            // Na parcial 2, quem deve ter emitido o certificado do cliente e o broker.
            if (!BrokerCertificateSupport.BROKER_ISSUER.equals(issuer)) {
                throw new IllegalArgumentException("Emissor do certificado invalido.");
            }

            // O subject vem codificado no certificado e deve ser igual ao usuario do login.
            String certificateOwner = BrokerCertificateSupport.decodeText(subject);
            if (!cleanClientName.equals(certificateOwner)) {
                throw new IllegalArgumentException("Certificado nao pertence ao cliente informado.");
            }

            // Depois dos campos basicos, valida a assinatura digital do certificado.
            verifyServerSignature(type, subject, publicKey, issuer, issuedAt, signature);
        } catch (IOException | GeneralSecurityException ex) {
            throw new IllegalArgumentException("Certificado do cliente nao pode ser validado: " + ex.getMessage(), ex);
        }
    }

    /**
     * Normaliza e valida o nome do topico recebido pelo broker.
     */
    public String verifyTopicName(String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do topico.");
        }
        return clean;
    }

    /**
     * Valida criacao de topico, recusando duplicidade.
     */
    public void verifyTopicCanBeCreated(String topicName, boolean topicExists) {
        verifyTopicName(topicName);
        if (topicExists) {
            throw new IllegalArgumentException("Topico ja existe: " + topicName);
        }
    }

    /**
     * Valida operacoes que exigem um topico previamente criado.
     */
    public void verifyTopicExists(String topicName, boolean topicExists) {
        verifyTopicName(topicName);
        if (!topicExists) {
            throw new IllegalArgumentException("Topico nao encontrado: " + topicName);
        }
    }

    /**
     * Valida se o cliente esta inscrito antes de executar uma operacao que
     * depende da inscricao.
     */
    public void verifyClientSubscribed(String topicName, boolean subscribed) {
        verifyTopicName(topicName);
        if (!subscribed) {
            throw new IllegalArgumentException("Cliente nao esta inscrito no topico: " + topicName);
        }
    }

    /**
     * Valida exclusao de topico. Apenas o criador original pode excluir.
     */
    public void verifyTopicDeletion(String requester, String owner) {
        String cleanRequester = requester == null ? "" : requester.trim();
        String cleanOwner = owner == null ? "" : owner.trim();
        if (cleanOwner.isEmpty() || !cleanOwner.equals(cleanRequester)) {
            throw new IllegalArgumentException("Apenas o criador pode excluir o topico.");
        }
    }

    /**
     * Valida e normaliza o payload de uma publicacao.
     */
    public String verifyMessage(String message) {
        String clean = message == null ? "" : message.trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Mensagem vazia.");
        }
        return clean;
    }

    /**
     * Valida todos os requisitos para publicar em um topico.
     */
    public String verifyPublication(String topicName, boolean topicExists, boolean subscribed, String message) {
        verifyTopicExists(topicName, topicExists);
        if (!subscribed) {
            throw new IllegalArgumentException("Inscreva-se no topico antes de publicar.");
        }
        return verifyMessage(message);
    }

    /**
     * Carrega um arquivo do disco e retorna seu conteudo bruto.
     */
    public byte[] loadFile(String filePath) throws IOException {
        String cleanPath = filePath == null ? "" : filePath.trim();
        if (cleanPath.isEmpty()) {
            throw new IllegalArgumentException("Caminho do arquivo nao informado.");
        }
        return loadFile(Path.of(cleanPath));
    }

    /**
     * Carrega um arquivo a partir de um Path ja resolvido pelo chamador.
     */
    public byte[] loadFile(Path filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("Caminho do arquivo nao informado.");
        }
        if (!Files.exists(filePath)) {
            throw new IOException("Arquivo nao encontrado: " + filePath);
        }
        if (!Files.isRegularFile(filePath)) {
            throw new IOException("O caminho informado nao e um arquivo: " + filePath);
        }
        if (!Files.isReadable(filePath)) {
            throw new IOException("Arquivo sem permissao de leitura: " + filePath);
        }
        return Files.readAllBytes(filePath);
    }

    /**
     * Retorna o diretorio padrao onde o broker procura chaves/certificados.
     */
    public Path certificateDirectory() {
        return BrokerCertificateSupport.certificateDirectory();
    }

    /**
     * Exige que o nome do cliente esteja presente em login/cadastro.
     */
    private String verifyRequiredClientName(String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Nome do cliente nao informado.");
        }
        return clean;
    }

    /**
     * Exige que a senha seja informada antes de validar ou criar usuario.
     */
    private void verifyPasswordPresent(String password) {
        String cleanPassword = password == null ? "" : password;
        if (cleanPassword.isBlank()) {
            throw new IllegalArgumentException("Senha nao informada.");
        }
    }

    /**
     * Converte o texto do certificado, em formato chave=valor, para Properties.
     */
    private Properties loadCertificateProperties(String certificateText) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(certificateText));
        return properties;
    }

    /**
     * Valida a assinatura do certificado de cliente usando a chave publica do
     * broker. Se a assinatura confere, o certificado foi gerado pela chave
     * privada correspondente.
     */
    private void verifyServerSignature(String type, String subject, String publicKey, String issuer, String issuedAt, String signature)
            throws IOException, GeneralSecurityException {
        if (subject.isBlank() || publicKey.isBlank() || issuedAt.isBlank() || signature.isBlank()) {
            throw new IllegalArgumentException("Certificado do cliente esta incompleto.");
        }

        Path serverPublicKeyPath = BrokerCertificateSupport.serverPublicKeyPath();
        if (!Files.exists(serverPublicKeyPath)) {
            throw new IOException("Chave publica do broker nao encontrada em " + serverPublicKeyPath);
        }

        PublicKey serverPublicKey = BrokerCertificateSupport.readPublicKey(serverPublicKeyPath);
        Signature verifier = Signature.getInstance(BrokerCertificateSupport.SIGNATURE_ALGORITHM);
        verifier.initVerify(serverPublicKey);
        verifier.update(BrokerCertificateSupport.signaturePayload(type, subject, publicKey, issuer, issuedAt)
                .getBytes(StandardCharsets.UTF_8));

        boolean valid = verifier.verify(BrokerCertificateSupport.decodeBytes(signature));
        if (!valid) {
            throw new IllegalArgumentException("Assinatura do certificado invalida.");
        }
    }

    /**
     * Le usuarios salvos no arquivo usuarios.properties. Se o arquivo estiver
     * ausente, o broker comeca sem usuarios cadastrados.
     */
    private void loadAccounts() {
        if (!Files.exists(accountFile)) {
            return;
        }

        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(accountFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
            for (String key : properties.stringPropertyNames()) {
                // Cada usuario e salvo com a chave account.<nome-em-base64>.
                if (!key.startsWith("account.")) {
                    continue;
                }
                String encodedName = key.substring("account.".length());
                String userName = BrokerCertificateSupport.decodeText(encodedName);
                UserAccount account = UserAccount.fromStored(properties.getProperty(key));
                if (account != null) {
                    accounts.put(userName, account);
                }
            }
        } catch (IOException | IllegalArgumentException ex) {
            // Em caso de arquivo corrompido, evita aceitar dados inconsistentes.
            accounts.clear();
        }
    }

    /**
     * Persiste todos os usuarios em disco apos cadastro novo.
     */
    private synchronized void saveAccounts() {
        Properties properties = new Properties();
        for (var entry : accounts.entrySet()) {
            properties.setProperty("account." + BrokerCertificateSupport.encodeText(entry.getKey()), entry.getValue().toStored());
        }

        try {
            Files.createDirectories(accountFile.getParent());
            try (var writer = Files.newBufferedWriter(accountFile, StandardCharsets.UTF_8)) {
                properties.store(writer, "Usuarios do broker AV3");
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Nao foi possivel salvar usuarios: " + ex.getMessage(), ex);
        }
    }

    /**
     * Representa uma conta local do broker. Ela guarda apenas salt e hash da
     * senha, nunca a senha em texto puro.
     */
    private static final class UserAccount {

        private final String salt;
        private final String passwordHash;

        /**
         * Cria a conta a partir de salt e hash ja calculados.
         */
        private UserAccount(String salt, String passwordHash) {
            this.salt = salt;
            this.passwordHash = passwordHash;
        }

        /**
         * Cria uma conta nova gerando salt aleatorio e hash SHA-256 da senha.
         */
        static UserAccount create(String password, SecureRandom secureRandom) {
            byte[] saltBytes = new byte[SALT_BYTES];
            secureRandom.nextBytes(saltBytes);
            String salt = Base64.getUrlEncoder().withoutPadding().encodeToString(saltBytes);
            return new UserAccount(salt, hash(password, salt));
        }

        /**
         * Reconstroi uma conta a partir do formato salvo em disco: salt:hash.
         */
        static UserAccount fromStored(String stored) {
            if (stored == null) {
                return null;
            }
            String[] parts = stored.split(":", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                return null;
            }
            return new UserAccount(parts[0], parts[1]);
        }

        /**
         * Compara a senha informada com o hash armazenado usando comparacao em
         * tempo constante para reduzir vazamento por timing.
         */
        boolean matches(String password) {
            return MessageDigest.isEqual(
                    passwordHash.getBytes(StandardCharsets.UTF_8),
                    hash(password, salt).getBytes(StandardCharsets.UTF_8));
        }

        /**
         * Formato usado no arquivo usuarios.properties.
         */
        String toStored() {
            return salt + ":" + passwordHash;
        }

        /**
         * Calcula SHA-256(salt + senha) e retorna em Base64 URL-safe.
         */
        private static String hash(String password, String salt) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(salt.getBytes(StandardCharsets.UTF_8));
                digest.update((password == null ? "" : password).getBytes(StandardCharsets.UTF_8));
                return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest());
            } catch (GeneralSecurityException ex) {
                throw new IllegalArgumentException("Nao foi possivel validar senha.", ex);
            }
        }
    }
}
