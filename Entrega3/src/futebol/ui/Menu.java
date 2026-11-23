package futebol.ui;

import futebol.banco.TradutorErros;
import java.sql.SQLException;
import java.util.List;

/**
 * Um menu numerado genérico. A opção 0 sempre sai do menu. Quando uma ação
 * lança SQLException, o erro aparece traduzido e o menu continua.
 * Todos os menus do programa são montados com esta classe.
 */
public final class Menu {

    /** O que acontece ao escolher uma opção. */
    @FunctionalInterface
    public interface Acao {
        void executar() throws SQLException;
    }

    public record Opcao(String rotulo, Acao acao) {
    }

    private final Terminal terminal;
    private final String titulo;
    private final String rotuloDeSaida;
    private final List<Opcao> opcoes;

    public Menu(Terminal terminal, String titulo, String rotuloDeSaida, List<Opcao> opcoes) {
        this.terminal = terminal;
        this.titulo = titulo;
        this.rotuloDeSaida = rotuloDeSaida;
        this.opcoes = opcoes;
    }

    public void exibir() {
        while (true) {
            mostrarOpcoes();
            int escolha = terminal.opcao("Escolha: ", 0, opcoes.size());
            if (escolha == 0) {
                return;
            }
            executar(opcoes.get(escolha - 1));
        }
    }

    private void mostrarOpcoes() {
        terminal.linhaEmBranco();
        terminal.mostrar("=== " + titulo + " ===");
        for (int i = 0; i < opcoes.size(); i++) {
            terminal.mostrar("  " + (i + 1) + ") " + opcoes.get(i).rotulo());
        }
        terminal.mostrar("  0) " + rotuloDeSaida);
    }

    private void executar(Opcao opcao) {
        terminal.linhaEmBranco();
        try {
            opcao.acao().executar();
        } catch (SQLException erro) {
            terminal.mostrar("Erro: " + TradutorErros.traduzir(erro));
        }
    }
}
