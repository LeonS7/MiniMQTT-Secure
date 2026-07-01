package com.mycompany.broker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

/**
 * Classe utilitaria de certificados usados pelo broker em tempo de execucao.
 *
 * Ela centraliza caminhos, constantes e leitura das chaves que o BrokerServer,
 * BrokerTransportSecurity e BrokerVerificationService precisam enquanto o
 * broker esta rodando. A emissao offline de certificados de clientes fica no
 * pacote com.mycompany.offlinecert para deixar claro que essa etapa e chamada
 * pelos scripts .bat/.sh, nao pelo servidor durante a execucao normal.
 */
public final class BrokerCertificateSupport {

    /*
     * Tipo e emissor esperados em certificados de clientes emitidos pelo broker.
     */
    public static final String CLIENT_CERTIFICATE_TYPE = "AV3_CLIENT_CERTIFICATE_V1";
    public static final String CERTIFICATE_TYPE = CLIENT_CERTIFICATE_TYPE;
    public static final String BROKER_ISSUER = "AV3_BROKER";
    public static final String ISSUER = BROKER_ISSUER;

    /*
     * Algoritmos usados para chaves e assinaturas digitais.
     */
    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /*
     * Arquivos necessarios para a parcial 2. A chave privada nunca deve sair do
     * broker.
     */
    public static final String SERVER_PRIVATE_KEY_FILE = "servidor_privada.key";
    public static final String SERVER_PUBLIC_KEY_FILE = "servidor_publica.key";

    /**
     * Impede instanciacao porque todos os metodos sao estaticos.
     */
    private BrokerCertificateSupport() {
    }

    /**
     * Pasta padrao onde ficam as chaves do broker e certificados de clientes.
     */
    public static Path certificateDirectory() {
        return applicationDirectory().resolve("certificados");
    }

    /**
     * Caminho da chave privada do broker, usada para assinar clientes.
     */
    public static Path serverPrivateKeyPath() {
        return certificateDirectory().resolve(SERVER_PRIVATE_KEY_FILE);
    }

    /**
     * Caminho da chave publica do broker, usada para validar assinaturas.
     */
    public static Path serverPublicKeyPath() {
        return certificateDirectory().resolve(SERVER_PUBLIC_KEY_FILE);
    }

    /**
     * Gera um par de chaves RSA novo.
     */
    public static KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    /**
     * Garante que o broker tenha chave publica e privada.
     *
     * @return true quando criou as chaves agora; false quando elas ja existiam.
     */
    public static boolean ensureServerKeys() throws IOException, GeneralSecurityException {
        Path privateKeyPath = serverPrivateKeyPath();
        Path publicKeyPath = serverPublicKeyPath();

        if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
            return false;
        }

        Path legacyDirectory = findLegacyServerKeyDirectory();
        if (legacyDirectory != null) {
            migrateServerKeys(legacyDirectory, privateKeyPath, publicKeyPath);
            return false;
        }

        KeyPair keyPair = generateKeyPair();
        writePrivateKey(privateKeyPath, keyPair.getPrivate());
        writePublicKey(publicKeyPath, keyPair.getPublic());
        return true;
    }

    /**
     * Le uma chave publica salva em Base64 MIME.
     */
    public static PublicKey readPublicKey(Path path) throws IOException, GeneralSecurityException {
        byte[] encoded = Base64.getMimeDecoder().decode(Files.readString(path, StandardCharsets.UTF_8));
        return publicKeyFromBytes(encoded);
    }

    /**
     * Le uma chave privada salva em Base64 MIME.
     */
    public static PrivateKey readPrivateKey(Path path) throws IOException, GeneralSecurityException {
        byte[] encoded = Base64.getMimeDecoder().decode(Files.readString(path, StandardCharsets.UTF_8));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(keySpec);
    }

    /**
     * Converte bytes X.509 em uma chave publica RSA.
     */
    public static PublicKey publicKeyFromBytes(byte[] encoded) throws GeneralSecurityException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(keySpec);
    }

    /**
     * Grava uma chave publica em arquivo texto Base64.
     */
    public static void writePublicKey(Path path, PublicKey publicKey) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, Base64.getMimeEncoder(64, System.lineSeparator().getBytes(StandardCharsets.UTF_8))
                .encodeToString(publicKey.getEncoded()), StandardCharsets.UTF_8);
    }

    /**
     * Grava uma chave privada em arquivo texto Base64.
     */
    public static void writePrivateKey(Path path, PrivateKey privateKey) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, Base64.getMimeEncoder(64, System.lineSeparator().getBytes(StandardCharsets.UTF_8))
                .encodeToString(privateKey.getEncoded()), StandardCharsets.UTF_8);
    }

    /**
     * Monta o conteudo exato que e assinado no certificado.
     */
    public static String signaturePayload(String type, String subject, String publicKey, String issuer, String issuedAt) {
        return type + "\n"
                + subject + "\n"
                + publicKey + "\n"
                + issuer + "\n"
                + issuedAt;
    }

    /**
     * Codifica texto em Base64 URL-safe.
     */
    public static String encodeText(String value) {
        String safe = value == null ? "" : value;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(safe.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodifica texto em Base64 URL-safe.
     */
    public static String decodeText(String value) {
        byte[] decoded = Base64.getUrlDecoder().decode(value);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    /**
     * Codifica bytes em Base64 URL-safe.
     */
    public static String encodeBytes(byte[] value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value);
    }

    /**
     * Decodifica bytes em Base64 URL-safe.
     */
    public static byte[] decodeBytes(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    /**
     * Transforma nomes de clientes em nomes de arquivo seguros.
     */
    public static String safeFileName(String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) {
            return "cliente";
        }
        return clean.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    /**
     * Procura chaves antigas geradas antes da pasta de certificados ficar
     * centralizada em Broker/certificados.
     */
    private static Path findLegacyServerKeyDirectory() {
        Path baseDirectory = applicationDirectory();
        List<Path> candidates = List.of(
                baseDirectory.resolve("target").resolve("certificados"),
                baseDirectory.resolve("target").resolve("classes").resolve("certificados"));

        for (Path candidate : candidates) {
            if (Files.exists(candidate.resolve(SERVER_PRIVATE_KEY_FILE))
                    && Files.exists(candidate.resolve(SERVER_PUBLIC_KEY_FILE))) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Copia chaves antigas para a pasta estavel usada pelo broker e pela
     * ferramenta offline.
     */
    private static void migrateServerKeys(Path legacyDirectory, Path privateKeyPath, Path publicKeyPath) throws IOException {
        Files.createDirectories(privateKeyPath.getParent());
        Files.copy(legacyDirectory.resolve(SERVER_PRIVATE_KEY_FILE),
                privateKeyPath,
                StandardCopyOption.REPLACE_EXISTING);
        Files.copy(legacyDirectory.resolve(SERVER_PUBLIC_KEY_FILE),
                publicKeyPath,
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Descobre a pasta base da aplicacao. Quando o codigo roda por
     * target/classes ou target/Broker.jar, volta para a pasta Broker para evitar
     * pares de chaves diferentes entre NetBeans, Maven e JAR.
     */
    private static Path applicationDirectory() {
        try {
            Path location = Path.of(BrokerCertificateSupport.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            if (Files.isRegularFile(location)) {
                return normalizeApplicationDirectory(location.getParent());
            }
            return normalizeApplicationDirectory(location);
        } catch (URISyntaxException | IllegalArgumentException ex) {
            return Path.of(System.getProperty("user.dir"));
        }
    }

    /**
     * Normaliza target/classes e target para a pasta do modulo Broker.
     */
    private static Path normalizeApplicationDirectory(Path directory) {
        if (directory == null) {
            return Path.of(System.getProperty("user.dir"));
        }

        Path name = directory.getFileName();
        Path parent = directory.getParent();
        if (name != null && "classes".equals(name.toString())
                && parent != null
                && parent.getFileName() != null
                && "target".equals(parent.getFileName().toString())
                && parent.getParent() != null) {
            return parent.getParent();
        }
        if (name != null && "target".equals(name.toString()) && parent != null) {
            return parent;
        }
        return directory;
    }
}
