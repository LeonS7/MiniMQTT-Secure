package com.mycompany.client.network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implementa o envelopamento digital do transporte cliente-broker sem TLS.
 *
 * O cliente valida o certificado do broker usando a AC local (ca.crt), gera uma
 * chave AES de sessao e envia essa chave cifrada com a chave publica do broker.
 */
final class ClientTransportSecurity {

    private static final String RSA_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_CIPHER = "AES/GCM/NoPadding";
    private static final int AES_BITS = 256;
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    private ClientTransportSecurity() {
    }

    /**
     * Executa o handshake inicial: recebe certificado, valida pela AC e envia
     * chave AES envelopada pela chave publica do broker.
     */
    static SecureSession openSession(BufferedReader reader, PrintWriter writer) throws IOException, GeneralSecurityException {
        String certificateLine = reader.readLine();
        if (certificateLine == null) {
            throw new IOException("Broker encerrou a conexao.");
        }

        String[] parts = certificateLine.split("\\|", -1);
        if (parts.length != 2 || !"BROKER_CERT".equals(parts[0])) {
            throw new IOException("Certificado do broker nao recebido.");
        }

        X509Certificate brokerCertificate = parseCertificate(decodeBytes(parts[1]));
        validateBrokerCertificate(brokerCertificate);

        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(AES_BITS);
        SecretKey sessionKey = generator.generateKey();

        Cipher cipher = Cipher.getInstance(RSA_CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, brokerCertificate.getPublicKey());
        writer.println("SESSION|" + encodeBytes(cipher.doFinal(sessionKey.getEncoded())));

        return new SecureSession(sessionKey.getEncoded());
    }

    /**
     * Valida assinatura e validade temporal do certificado do broker.
     */
    private static void validateBrokerCertificate(X509Certificate brokerCertificate)
            throws IOException, GeneralSecurityException {
        X509Certificate caCertificate = loadCaCertificate();
        brokerCertificate.checkValidity();
        brokerCertificate.verify(caCertificate.getPublicKey());
    }

    /**
     * Carrega o certificado da AC local. Nas VMs, ele deve ficar em
     * Client/certificados/ca.crt.
     */
    private static X509Certificate loadCaCertificate() throws IOException, GeneralSecurityException {
        Path path = ClientCertificateSupport.findCertificateFile("ca.crt");
        if (path == null) {
            throw new IOException("Certificado da AC nao encontrado.");
        }
        return parseCertificate(Files.readAllBytes(path));
    }

    private static X509Certificate parseCertificate(byte[] bytes) throws GeneralSecurityException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));
    }

    private static byte[] decodeBytes(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private static String encodeBytes(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    /**
     * Sessao AES/GCM usada para proteger todas as linhas do protocolo.
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
            byte[] ciphertext = cipher.doFinal(plainLine.getBytes(StandardCharsets.UTF_8));
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
            return new String(plain, StandardCharsets.UTF_8);
        }
    }
}
