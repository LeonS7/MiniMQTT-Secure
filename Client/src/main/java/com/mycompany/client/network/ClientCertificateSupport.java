package com.mycompany.client.network;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitario usado pelo cliente para localizar o certificado assinado pelo
 * broker na etapa offline da parcial 2.
 */
final class ClientCertificateSupport {

    /**
     * Impede instanciacao porque a classe e apenas um conjunto de helpers.
     */
    private ClientCertificateSupport() {
    }

    /**
     * Procura e carrega o certificado do cliente pelo nome digitado no login.
     *
     * Exemplo esperado: certificados/clientes/alice.cert.
     */
    static String loadClientCertificate(String clientName) throws IOException {
        String safeName = safeFileName(clientName);
        List<Path> searchDirectories = certificateSearchDirectories();
        for (Path baseDirectory : searchDirectories) {
            Path direct = baseDirectory.resolve(safeName + ".cert");
            Path nested = baseDirectory.resolve("clientes").resolve(safeName + ".cert");
            if (Files.isRegularFile(direct)) {
                return Files.readString(direct, StandardCharsets.UTF_8);
            }
            if (Files.isRegularFile(nested)) {
                return Files.readString(nested, StandardCharsets.UTF_8);
            }
        }

        throw new IOException("Certificado do cliente nao encontrado: " + safeName
                + ".cert. Pastas verificadas: " + searchDirectories);
    }

    /**
     * Monta a lista de pastas onde certificados podem estar, cobrindo execucao
     * pelo JAR, pelo NetBeans/Maven e pela estrutura com broker e cliente em
     * modulos separados.
     */
    private static List<Path> certificateSearchDirectories() {
        List<Path> directories = new ArrayList<>();
        Path appDirectory = applicationDirectory();
        Path userDirectory = Path.of(System.getProperty("user.dir"));

        /*
         * Em execucao pelo JAR, appDirectory normalmente aponta para Client/target.
         * Em execucao pelo NetBeans/Maven, ele pode apontar para
         * Client/target/classes. Por isso tambem verificamos o pai.
         */
        addCertificateDirectory(directories, appDirectory);
        addCertificateDirectory(directories, appDirectory.getParent());
        addCertificateDirectory(directories, userDirectory);
        addCertificateDirectory(directories, userDirectory.resolve("target"));
        addCertificateDirectory(directories, userDirectory.resolve("Broker"));
        addCertificateDirectory(directories, userDirectory.resolve("Broker").resolve("target"));
        addCertificateDirectory(directories, userDirectory.resolve("Broker").resolve("target").resolve("classes"));

        Path parent = userDirectory.getParent();
        if (parent != null) {
            addCertificateDirectory(directories, parent.resolve("Broker"));
            addCertificateDirectory(directories, parent.resolve("Broker").resolve("target"));
            addCertificateDirectory(directories, parent.resolve("Broker").resolve("target").resolve("classes"));
        }
        return directories;
    }

    /**
     * Adiciona uma pasta certificados evitando entradas repetidas.
     */
    private static void addCertificateDirectory(List<Path> directories, Path baseDirectory) {
        if (baseDirectory == null) {
            return;
        }
        Path certificateDirectory = baseDirectory.resolve("certificados");
        if (!directories.contains(certificateDirectory)) {
            directories.add(certificateDirectory);
        }
    }

    /**
     * Cria um nome de arquivo seguro a partir do nome digitado pelo usuario.
     */
    private static String safeFileName(String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) {
            return "cliente";
        }
        return clean.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    /**
     * Descobre a pasta onde o cliente esta rodando para procurar certificados ao
     * lado do JAR/classes.
     */
    private static Path applicationDirectory() {
        try {
            Path location = Path.of(ClientCertificateSupport.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            if (Files.isRegularFile(location)) {
                return location.getParent();
            }
            return location;
        } catch (URISyntaxException | IllegalArgumentException ex) {
            return Path.of(System.getProperty("user.dir"));
        }
    }
}
