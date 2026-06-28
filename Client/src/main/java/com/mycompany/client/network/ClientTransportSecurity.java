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
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;

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
    private static final String EXPECTED_BROKER_CN = "MiniMQTT Broker";
    private static final String BROKER_IDENTITY_PROPERTY = "minimqtt.broker.identity";
    private static final String DEFAULT_BROKER_DNS = "minimqtt-broker";

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
        caCertificate.checkValidity();
        brokerCertificate.checkValidity();
        brokerCertificate.verify(caCertificate.getPublicKey());
        validateBrokerIssuer(brokerCertificate, caCertificate);
        validateBrokerIdentity(brokerCertificate);
    }

    /**
     * Confere se o certificado recebido foi emitido pela AC configurada.
     */
    private static void validateBrokerIssuer(X509Certificate brokerCertificate, X509Certificate caCertificate)
            throws GeneralSecurityException {
        if (!brokerCertificate.getIssuerX500Principal().equals(caCertificate.getSubjectX500Principal())) {
            throw new GeneralSecurityException("Certificado do broker nao foi emitido pela AC configurada.");
        }
    }

    /**
     * A identidade do broker e fixa e independe do IP da rede usada no dia.
     * Preferimos o SAN DNS; se a AC nao copiar a extensao da CSR, aceitamos o CN.
     */
    private static void validateBrokerIdentity(X509Certificate brokerCertificate)
            throws GeneralSecurityException {
        if (hasDnsSubjectAlternativeName(brokerCertificate)) {
            if (!hasExpectedDnsSubjectAlternativeName(brokerCertificate)) {
                throw new GeneralSecurityException("Identidade do broker invalida.");
            }
            return;
        }

        String commonName = commonName(brokerCertificate.getSubjectX500Principal());
        if (!EXPECTED_BROKER_CN.equals(commonName)) {
            throw new GeneralSecurityException("Identidade do broker invalida.");
        }
    }

    /**
     * Verifica se o certificado possui algum SAN DNS.
     */
    private static boolean hasDnsSubjectAlternativeName(X509Certificate certificate) throws CertificateParsingException {
        Collection<List<?>> names = certificate.getSubjectAlternativeNames();
        if (names == null) {
            return false;
        }

        for (List<?> name : names) {
            if (isDnsSubjectAlternativeName(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Procura o SAN DNS esperado. O padrao e minimqtt-broker, mas a propriedade
     * minimqtt.broker.identity permite trocar esse nome sem recompilar.
     */
    private static boolean hasExpectedDnsSubjectAlternativeName(X509Certificate certificate) throws CertificateParsingException {
        Collection<List<?>> names = certificate.getSubjectAlternativeNames();
        if (names == null) {
            return false;
        }

        String expectedBrokerDns = expectedBrokerDns();
        for (List<?> name : names) {
            if (isDnsSubjectAlternativeName(name)
                    && expectedBrokerDns.equalsIgnoreCase(String.valueOf(name.get(1)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Le a identidade esperada do broker de uma propriedade opcional da JVM.
     */
    private static String expectedBrokerDns() {
        String configured = System.getProperty(BROKER_IDENTITY_PROPERTY, "").trim();
        return configured.isEmpty() ? DEFAULT_BROKER_DNS : configured;
    }

    /**
     * O tipo 2 de SubjectAlternativeName representa DNSName em certificados X.509.
     */
    private static boolean isDnsSubjectAlternativeName(List<?> name) {
        return name.size() >= 2 && Integer.valueOf(2).equals(name.get(0));
    }

    /**
     * Extrai o CN do Subject usando parser LDAP para evitar string split fragil.
     */
    private static String commonName(X500Principal principal) throws GeneralSecurityException {
        try {
            LdapName ldapName = new LdapName(principal.getName(X500Principal.RFC2253));
            for (Rdn rdn : ldapName.getRdns()) {
                if ("CN".equalsIgnoreCase(rdn.getType())) {
                    return String.valueOf(rdn.getValue());
                }
            }
            return "";
        } catch (InvalidNameException ex) {
            throw new GeneralSecurityException("Identidade do broker invalida.", ex);
        }
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

    /**
     * Converte bytes PEM ou DER para um certificado X.509.
     */
    private static X509Certificate parseCertificate(byte[] bytes) throws GeneralSecurityException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));
    }

    /**
     * Decodifica Base64 URL-safe recebido no protocolo.
     */
    private static byte[] decodeBytes(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    /**
     * Codifica bytes em Base64 URL-safe para trafegar em uma linha de texto.
     */
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
