package com.mycompany.offlinecert;

import com.mycompany.broker.BrokerCertificateSupport;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Properties;

/**
 * Rotinas usadas exclusivamente pela assinatura offline de certificados.
 *
 * Este arquivo fica separado do pacote do broker para deixar claro que ele nao
 * e chamado pelo BrokerServer quando o sistema esta rodando. Ele existe porque
 * os scripts sign-client.bat, sign-vm-clients.bat e equivalentes .sh precisam
 * de uma rotina local para criar as chaves dos clientes e assinar o certificado
 * antes de copiar os arquivos para as VMs.
 */
final class OfflineCertificateSupport {

    /**
     * Impede instanciacao porque todos os metodos sao utilitarios.
     */
    private OfflineCertificateSupport() {
    }

    /**
     * Pasta padrao onde a ferramenta offline grava certificados de clientes.
     */
    static Path defaultClientCertificateDirectory() {
        return BrokerCertificateSupport.certificateDirectory().resolve("clientes");
    }

    /**
     * Cria um certificado de cliente assinado pela chave privada do broker.
     */
    static Path writeClientCertificate(String clientName, Path outputDirectory)
            throws IOException, GeneralSecurityException {
        BrokerCertificateSupport.ensureServerKeys();

        String cleanName = clientName == null ? "" : clientName.trim();
        if (cleanName.isEmpty()) {
            throw new IllegalArgumentException("Nome do cliente nao informado.");
        }

        Path targetDirectory = outputDirectory == null
                ? defaultClientCertificateDirectory()
                : outputDirectory;
        Files.createDirectories(targetDirectory);

        String safeName = BrokerCertificateSupport.safeFileName(cleanName);
        KeyPair clientKeyPair = BrokerCertificateSupport.generateKeyPair();
        Path privateKeyPath = targetDirectory.resolve(safeName + ".private.key");
        Path publicKeyPath = targetDirectory.resolve(safeName + ".public.key");
        Path certificatePath = targetDirectory.resolve(safeName + ".cert");

        BrokerCertificateSupport.writePrivateKey(privateKeyPath, clientKeyPair.getPrivate());
        BrokerCertificateSupport.writePublicKey(publicKeyPath, clientKeyPair.getPublic());

        String subject = BrokerCertificateSupport.encodeText(cleanName);
        String publicKey = BrokerCertificateSupport.encodeBytes(clientKeyPair.getPublic().getEncoded());
        String issuedAt = Instant.now().toString();
        String signature = signPayload(
                BrokerCertificateSupport.CLIENT_CERTIFICATE_TYPE,
                subject,
                publicKey,
                BrokerCertificateSupport.BROKER_ISSUER,
                issuedAt,
                BrokerCertificateSupport.readPrivateKey(BrokerCertificateSupport.serverPrivateKeyPath()));

        writeCertificate(certificatePath,
                BrokerCertificateSupport.CLIENT_CERTIFICATE_TYPE,
                subject,
                publicKey,
                BrokerCertificateSupport.BROKER_ISSUER,
                issuedAt,
                signature);

        return certificatePath;
    }

    /**
     * Assina o conteudo do certificado com a chave privada do broker.
     */
    private static String signPayload(String type, String subject, String publicKey, String issuer,
            String issuedAt, PrivateKey privateKey) throws GeneralSecurityException {
        Signature signer = Signature.getInstance(BrokerCertificateSupport.SIGNATURE_ALGORITHM);
        signer.initSign(privateKey);
        signer.update(BrokerCertificateSupport.signaturePayload(type, subject, publicKey, issuer, issuedAt)
                .getBytes(StandardCharsets.UTF_8));
        return BrokerCertificateSupport.encodeBytes(signer.sign());
    }

    /**
     * Grava o certificado em formato simples de propriedades chave=valor.
     */
    private static void writeCertificate(Path path, String type, String subject, String publicKey, String issuer,
            String issuedAt, String signature) throws IOException {
        Files.createDirectories(path.getParent());
        Properties certificate = new Properties();
        certificate.setProperty("type", type);
        certificate.setProperty("subject", subject);
        certificate.setProperty("publicKey", publicKey);
        certificate.setProperty("issuer", issuer);
        certificate.setProperty("issuedAt", issuedAt);
        certificate.setProperty("signature", signature);

        StringBuilder content = new StringBuilder();
        content.append("type=").append(certificate.getProperty("type")).append(System.lineSeparator());
        content.append("subject=").append(certificate.getProperty("subject")).append(System.lineSeparator());
        content.append("publicKey=").append(certificate.getProperty("publicKey")).append(System.lineSeparator());
        content.append("issuer=").append(certificate.getProperty("issuer")).append(System.lineSeparator());
        content.append("issuedAt=").append(certificate.getProperty("issuedAt")).append(System.lineSeparator());
        content.append("signature=").append(certificate.getProperty("signature")).append(System.lineSeparator());
        Files.writeString(path, content.toString(), StandardCharsets.UTF_8);
    }
}
