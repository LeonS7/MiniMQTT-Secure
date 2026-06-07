package com.mycompany.broker;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

/**
 * Ferramenta de linha de comando para a etapa offline da parcial 2.
 *
 * Comandos aceitos:
 * java -cp Broker.jar com.mycompany.broker.CertificateTool init-server
 * java -cp Broker.jar com.mycompany.broker.CertificateTool sign-client nomeCliente [pastaSaida]
 */
public final class CertificateTool {

    /**
     * Impede instanciacao porque a classe serve apenas como ferramenta CLI.
     */
    private CertificateTool() {
    }

    /**
     * Seleciona a operacao solicitada pelo primeiro argumento.
     */
    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                printUsage();
                return;
            }

            switch (args[0]) {
                case "init-server":
                    initializeServerKeys();
                    break;
                case "sign-client":
                    if (args.length < 2) {
                        throw new IllegalArgumentException("Informe o nome do cliente.");
                    }
                    Path outputDirectory = args.length >= 3
                            ? Path.of(args[2])
                            : BrokerCertificateSupport.defaultClientCertificateDirectory();
                    signClient(args[1], outputDirectory);
                    break;
                default:
                    printUsage();
                    break;
            }
        } catch (IOException | GeneralSecurityException | IllegalArgumentException ex) {
            System.err.println("Erro: " + ex.getMessage());
        }
    }

    /**
     * Gera a chave publica/privada do broker quando ainda nao existem.
     */
    public static void initializeServerKeys() throws IOException, GeneralSecurityException {
        boolean created = BrokerCertificateSupport.ensureServerKeys();
        if (created) {
            System.out.println("Chaves do broker criadas em: " + BrokerCertificateSupport.certificateDirectory());
        } else {
            System.out.println("Chaves do broker ja existem em: " + BrokerCertificateSupport.certificateDirectory());
        }
    }

    /**
     * Cria um par de chaves do cliente e um certificado assinado pelo broker.
     */
    public static Path signClient(String clientName, Path outputDirectory)
            throws IOException, GeneralSecurityException {
        initializeServerKeys();
        Path certificatePath = BrokerCertificateSupport.writeClientCertificate(clientName, outputDirectory);
        String safeName = BrokerCertificateSupport.safeFileName(clientName);

        System.out.println("Certificado criado: " + certificatePath);
        System.out.println("Chave privada do cliente: " + outputDirectory.resolve(safeName + ".private.key"));
        System.out.println("Copie o arquivo .cert para a pasta certificados/clientes ao lado do Client.jar.");
        return certificatePath;
    }

    /**
     * Mostra os comandos aceitos quando o usuario chama a ferramenta sem
     * argumentos ou com uma opcao desconhecida.
     */
    private static void printUsage() {
        System.out.println("Uso:");
        System.out.println("  java -cp Broker.jar com.mycompany.broker.CertificateTool init-server");
        System.out.println("  java -cp Broker.jar com.mycompany.broker.CertificateTool sign-client nomeCliente [pastaSaida]");
    }
}
