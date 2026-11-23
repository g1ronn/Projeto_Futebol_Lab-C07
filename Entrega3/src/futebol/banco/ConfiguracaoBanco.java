package futebol.banco;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/** Dados para conectar ao MySQL. Podem vir de um arquivo .properties. */
public record ConfiguracaoBanco(String host, String porta, String banco, String usuario, String senha) {

    public static ConfiguracaoBanco padrao() {
        return new ConfiguracaoBanco("localhost", "3306", "futebol", "root", "");
    }

    /** Lê o arquivo de configuração. Devolve vazio se o arquivo não existir. */
    public static Optional<ConfiguracaoBanco> doArquivo(Path arquivo) throws IOException {
        if (!Files.exists(arquivo)) {
            return Optional.empty();
        }
        Properties propriedades = new Properties();
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            propriedades.load(leitor);
        }
        ConfiguracaoBanco padrao = padrao();
        return Optional.of(new ConfiguracaoBanco(
                propriedades.getProperty("host", padrao.host()),
                propriedades.getProperty("porta", padrao.porta()),
                propriedades.getProperty("banco", padrao.banco()),
                propriedades.getProperty("usuario", padrao.usuario()),
                propriedades.getProperty("senha", padrao.senha())));
    }

    public ConfiguracaoBanco comCredenciais(String novoUsuario, String novaSenha) {
        return new ConfiguracaoBanco(host, porta, banco, novoUsuario, novaSenha);
    }

    public String url() {
        return "jdbc:mysql://" + host + ":" + porta + "/" + banco
                + "?useSSL=false&allowPublicKeyRetrieval=true";
    }
}
