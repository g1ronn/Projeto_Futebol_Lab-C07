package futebol.app;

import futebol.banco.ConfiguracaoBanco;
import futebol.banco.Conexao;
import futebol.ui.MenuPrincipal;
import futebol.ui.Terminal;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

/** Liga as peças: configuração, conexão com o banco e menu principal. */
public final class Aplicacao {

    private static final Path ARQUIVO_DE_CONFIGURACAO = Path.of("config.properties");

    public void executar() {
        Terminal terminal = Terminal.padrao();
        terminal.mostrar("=== Sistema de Futebol ===");

        try (Conexao conexao = abrirConexao(terminal)) {
            MenuPrincipal.criar(terminal, conexao).exibir();
            terminal.mostrar("Até mais!");
        } catch (Terminal.FimDeEntrada e) {
            terminal.mostrar("\nEntrada encerrada.");
        } catch (SQLException | IOException e) {
            terminal.mostrar("Não foi possível conectar ao banco: " + e.getMessage());
            terminal.mostrar("Confira o MySQL, o banco 'futebol' (rode o futebol.sql) e o config.properties.");
        }
    }

    private Conexao abrirConexao(Terminal terminal) throws SQLException, IOException {
        ConfiguracaoBanco configuracao = ConfiguracaoBanco.doArquivo(ARQUIVO_DE_CONFIGURACAO)
                .orElseGet(() -> perguntarCredenciais(terminal));

        Conexao conexao = Conexao.abrir(configuracao);
        terminal.mostrar("Conectado ao banco '" + configuracao.banco() + "' em "
                + configuracao.host() + ":" + configuracao.porta() + ".");
        return conexao;
    }

    private ConfiguracaoBanco perguntarCredenciais(Terminal terminal) {
        terminal.mostrar("config.properties não encontrado, informe os dados da conexão.");
        ConfiguracaoBanco padrao = ConfiguracaoBanco.padrao();
        String usuario = terminal.texto("Usuário do MySQL [" + padrao.usuario() + "]: ");
        String senha = terminal.texto("Senha: ");
        return padrao.comCredenciais(usuario.isEmpty() ? padrao.usuario() : usuario, senha);
    }
}
