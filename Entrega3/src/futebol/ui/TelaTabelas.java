package futebol.ui;

import futebol.banco.Conexao;
import futebol.modelo.Entidade;
import futebol.modelo.Entidades;
import futebol.repositorio.RepositorioEntidade;
import futebol.ui.Menu.Opcao;
import java.util.List;

/** Menus de gerenciamento das tabelas: a lista de tabelas e o CRUD de cada uma. */
public final class TelaTabelas {

    private final Terminal terminal;
    private final ImpressoraTabela impressora;
    private final Conexao conexao;

    public TelaTabelas(Terminal terminal, ImpressoraTabela impressora, Conexao conexao) {
        this.terminal = terminal;
        this.impressora = impressora;
        this.conexao = conexao;
    }

    public Menu menuDeTabelas() {
        List<Opcao> opcoes = Entidades.TODAS.stream()
                .map(entidade -> new Opcao(entidade.titulo(), () -> menuDaEntidade(entidade).exibir()))
                .toList();
        return new Menu(terminal, "Tabelas", "Voltar", opcoes);
    }

    private Menu menuDaEntidade(Entidade entidade) {
        TelaCrud crud = new TelaCrud(terminal, impressora, entidade,
                new RepositorioEntidade(conexao, entidade));
        return new Menu(terminal, entidade.titulo(), "Voltar", List.of(
                new Opcao("Listar todos", crud::listarTodos),
                new Opcao("Buscar com filtro", crud::buscarComFiltro),
                new Opcao("Inserir", crud::inserir),
                new Opcao("Atualizar", crud::atualizar),
                new Opcao("Remover (com filtro)", crud::remover)));
    }
}
