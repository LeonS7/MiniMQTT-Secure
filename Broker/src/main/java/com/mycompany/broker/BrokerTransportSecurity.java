package com.mycompany.broker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Suporte ao envelopamento digital do canal cliente-broker.
 *
 * Esta classe nao usa TLS. Ela carrega o certificado do broker assinado pela AC
 * e a chave privada local correspondente, recebe uma chave AES enviada pelo
 * cliente cifrada com RSA e cria uma sessao simetrica AES/GCM para o protocolo.
 */
final class BrokerTransportSecurity {

    private static final String KEYSTORE_FILE = "broker-keystore.p12";
    private static final String KEYSTORE_PASSWORD = "broker123";
    private static final String BROKER_ALIAS = "broker";
    private static final String RSA_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    private final X509Certificate brokerCertificate;
    private final PrivateKey brokerPrivateKey;
    private final byte[] encodedCertificate;

    private BrokerTransportSecurity(X509Certificate brokerCertificate, PrivateKey brokerPrivateKey, byte[] encodedCertificate) {
        this.brokerCertificate = brokerCertificate;
        this.brokerPrivateKey = brokerPrivateKey;
        this.encodedCertificate = encodedCertificate;
    }

    /**
     * Carrega os arquivos finais da parte 3: broker-keystore.p12 e broker.crt.
     */
    static BrokerTransportSecurity load() throws IOException, GeneralSecurityException {
        Path certificateDirectory = BrokerCertificateSupport.certificateDirectory();
        Path keystorePath = certificateDirectory.resolve(KEYSTORE_FILE);
        Path certificatePath = findBrokerCertificate(certificateDirectory);

        if (!Files.isRegularFile(keystorePath)) {
            throw new IOException("Keystore do broker nao encontrado: " + keystorePath);
        }
        if (certificatePath == null) {
            throw new IOException("Certificado assinado do broker nao encontrado. Use broker.crt em " + certificateDirectory);
        }

        byte[] certificateBytes = Files.readAllBytes(certificatePath);
        X509Certificate certificate = parseCertificate(certificateBytes);
        PrivateKey privateKey = readPrivateKey(keystorePath);
        validateKeyPair(certificate, privateKey);
        return new BrokerTransportSecurity(certificate, privateKey, certificateBytes);
    }

    /**
     * Certificado enviado ao cliente para autenticacao do broker.
     */
    byte[] encodedCertificate() {
        return encodedCertificate.clone();
    }

    /**
     * Decifra a chave AES de sessao enviada pelo cliente.
     */
    SecureSession openSession(String encryptedSessionKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(RSA_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, brokerPrivateKey);
        byte[] sessionKey = cipher.doFinal(decodeBytes(encryptedSessionKey));
        return new SecureSession(sessionKey);
    }

    /**
     * Localiza o certificado final do broker com nomes comuns.
     */
    private static Path findBrokerCertificate(Path certificateDirectory) {
        List<String> names = List.of(
                "broker.crt",
                "broker.cer",
                "broker.cert",
                "broker-assinado.crt",
                "broker_assinado.crt");
        for (String name : names) {
            Path path = certificateDirectory.resolve(name);
            if (Files.isRegularFile(path)) {
                return path;
            }
        }
        return null;
    }

    /**
     * Le a chave privada do keystore criado junto com a CSR enviada para a AC.
     */
    private static PrivateKey readPrivateKey(Path keystorePath) throws IOException, GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        char[] password = KEYSTORE_PASSWORD.toCharArray();
        try (var input = Files.newInputStream(keystorePath)) {
            keyStore.load(input, password);
        }
        return (PrivateKey) keyStore.getKey(BROKER_ALIAS, password);
    }

    /**
     * Aceita certificado em PEM ou DER.
     */
    private static X509Certificate parseCertificate(byte[] bytes) throws GeneralSecurityException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));
    }

    /**
     * Garante que o certificado assinado pertence a chave privada local.
     */
    private static void validateKeyPair(X509Certificate certificate, PrivateKey privateKey) throws GeneralSecurityException {
        byte[] challenge = new byte[32];
        new SecureRandom().nextBytes(challenge);

        Cipher encrypt = Cipher.getInstance(RSA_CIPHER);
        encrypt.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());
        byte[] encrypted = encrypt.doFinal(challenge);

        Cipher decrypt = Cipher.getInstance(RSA_CIPHER);
        decrypt.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = decrypt.doFinal(encrypted);

        if (!java.security.MessageDigest.isEqual(challenge, decrypted)) {
            throw new GeneralSecurityException("Certificado do broker nao corresponde ao keystore local.");
        }
    }

    private static byte[] decodeBytes(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private static String encodeBytes(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    /**
     * Sessao AES/GCM usada para proteger as linhas do protocolo TCP.
     */
    static final class SecureSession {

        private final SecretKeySpec key;
        private final SecureRandom random = new SecureRandom();

        SecureSession(byte[] rawKey) {
            this.key = new SecretKeySpec(rawKey, "AES");
        }

        String encryptLine(String plainLine) throws GeneralSecurityException {
            byte[] iv = new byte[GCM_IV_BYTES];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plainLine.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return "SECURE|" + encodeBytes(iv) + "|" + encodeBytes(ciphertext);
        }

        String decryptLine(String secureLine) throws GeneralSecurityException {
            String[] parts = secureLine.split("\\|", -1);
            if (parts.length != 3 || !"SECURE".equals(parts[0])) {
                throw new GeneralSecurityException("Mensagem segura invalida.");
            }

            Cipher cipher = Cipher.getInstance(AES_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, decodeBytes(parts[1])));
            byte[] plain = cipher.doFinal(decodeBytes(parts[2]));
            return new String(plain, java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
