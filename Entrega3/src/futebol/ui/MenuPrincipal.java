package futebol.ui;

import futebol.banco.Conexao;
import futebol.ui.Menu.Opcao;
import java.util.List;

/** Monta o menu principal, que leva às outras telas. */
public final class MenuPrincipal {

    public static Menu criar(Terminal terminal, Conexao conexao) {
        ImpressoraTabela impressora = new ImpressoraTabela(terminal);
        TelaTabelas tabelas = new TelaTabelas(terminal, impressora, conexao);
        TelaConsultas consultas = new TelaConsultas(terminal, impressora, conexao);

        return new Menu(terminal, "Menu principal", "Sair", List.of(
                new Opcao("Gerenciar tabelas (listar, buscar, inserir, atualizar, remover)",
                        () -> tabelas.menuDeTabelas().exibir()),
                new Opcao("Consultas com JOIN",
                        () -> consultas.menuDeJoins().exibir()),
                new Opcao("View, procedures e functions do banco",
                        () -> consultas.menuDeObjetos().exibir())));
    }

    private MenuPrincipal() {
    }
}
