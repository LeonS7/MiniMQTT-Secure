package com.mycompany.client.network;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Envelope de criptografia ponta a ponta do payload.
 *
 * O broker recebe e encaminha somente este texto cifrado. Cada destinatario
 * recebe a chave AES do payload cifrada com sua propria chave publica.
 */
final class EndToEndEnvelope {

    private static final String TYPE = "AV3_E2E_V1";
    private static final String RSA_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_CIPHER = "AES/GCM/NoPadding";
    private static final int AES_BITS = 256;
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    private EndToEndEnvelope() {
    }

    /**
     * Criptografa uma mensagem para todos os membros conhecidos do topico.
     */
    static String encrypt(String message, Map<String, String> recipientPublicKeys)
            throws GeneralSecurityException {
        if (recipientPublicKeys == null || recipientPublicKeys.isEmpty()) {
            throw new GeneralSecurityException("Chaves do topico ausentes.");
        }

        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(AES_BITS);
        SecretKey payloadKey = generator.generateKey();

        byte[] iv = new byte[GCM_IV_BYTES];
        new SecureRandom().nextBytes(iv);

        Cipher aes = Cipher.getInstance(AES_CIPHER);
        aes.init(Cipher.ENCRYPT_MODE, payloadKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] ciphertext = aes.doFinal(message.getBytes(StandardCharsets.UTF_8));

        StringBuilder envelope = new StringBuilder();
        envelope.append("type=").append(TYPE).append(System.lineSeparator());
        envelope.append("iv=").append(encodeBytes(iv)).append(System.lineSeparator());
        envelope.append("ciphertext=").append(encodeBytes(ciphertext)).append(System.lineSeparator());

        for (var entry : recipientPublicKeys.entrySet()) {
            PublicKey publicKey = publicKeyFromBase64(entry.getValue());
            Cipher rsa = Cipher.getInstance(RSA_CIPHER);
            rsa.init(Cipher.ENCRYPT_MODE, publicKey);
            envelope.append("key.")
                    .append(encodeText(entry.getKey()))
                    .append("=")
                    .append(encodeBytes(rsa.doFinal(payloadKey.getEncoded())))
                    .append(System.lineSeparator());
        }
        return envelope.toString();
    }

    /**
     * Abre a mensagem destinada ao usuario local.
     */
    static String decrypt(String envelopeText, String username, PrivateKey privateKey)
            throws IOException, GeneralSecurityException {
        Properties envelope = new Properties();
        envelope.load(new StringReader(envelopeText));

        if (!TYPE.equals(envelope.getProperty("type", ""))) {
            return envelopeText;
        }

        String encryptedKey = envelope.getProperty("key." + encodeText(username), "");
        if (encryptedKey.isBlank()) {
            throw new GeneralSecurityException("Mensagem nao destinada a este usuario.");
        }

        Cipher rsa = Cipher.getInstance(RSA_CIPHER);
        rsa.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] payloadKey = rsa.doFinal(decodeBytes(encryptedKey));

        Cipher aes = Cipher.getInstance(AES_CIPHER);
        aes.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(payloadKey, "AES"),
                new GCMParameterSpec(GCM_TAG_BITS, decodeBytes(envelope.getProperty("iv", ""))));
        byte[] plaintext = aes.doFinal(decodeBytes(envelope.getProperty("ciphertext", "")));
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    /**
     * Reconstroi uma chave publica RSA a partir do texto Base64 salvo na conta.
     */
    private static PublicKey publicKeyFromBase64(String value) throws GeneralSecurityException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodeBytes(value));
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    /**
     * Codifica nomes de usuarios para virar chave segura dentro do Properties.
     */
    private static String encodeText(String value) {
        return encodeBytes((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Converte bytes para Base64 URL-safe sem padding.
     */
    private static String encodeBytes(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    /**
     * Converte Base64 URL-safe de volta para bytes.
     */
    private static byte[] decodeBytes(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
