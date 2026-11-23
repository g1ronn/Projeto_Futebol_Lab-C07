package futebol.ui;

import futebol.banco.ResultadoTabela;
import futebol.modelo.Coluna;
import futebol.modelo.Condicao;
import futebol.modelo.Entidade;
import futebol.repositorio.RepositorioEntidade;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * As cinco operações do CRUD de uma Entidade, do ponto de vista do usuário:
 * pergunta o que precisa, chama o repositório e mostra o resultado.
 */
public final class TelaCrud {

    private static final String SEM_FILTRO = "Sem filtro";
    private static final String CANCELAR = "Cancelar";

    private record Alteracoes(List<Coluna> colunas, List<Object> valores) {
        boolean vazia() {
            return colunas.isEmpty();
        }
    }

    private final Terminal terminal;
    private final ImpressoraTabela impressora;
    private final Entidade entidade;
    private final RepositorioEntidade repositorio;

    public TelaCrud(Terminal terminal, ImpressoraTabela impressora,
                    Entidade entidade, RepositorioEntidade repositorio) {
        this.terminal = terminal;
        this.impressora = impressora;
        this.entidade = entidade;
        this.repositorio = repositorio;
    }

    // ---------------------------------------------------------------
    // READ
    // ---------------------------------------------------------------
    public void listarTodos() throws SQLException {
        impressora.imprimir(repositorio.listarTodos());
    }

    public void buscarComFiltro() throws SQLException {
        Optional<List<Condicao>> filtro = lerFiltroObrigatorio("Buscar por quais campos?");
        if (filtro.isEmpty()) {
            terminal.mostrar("Busca cancelada.");
            return;
        }
        impressora.imprimir(repositorio.buscar(filtro.get()));
    }

    // ---------------------------------------------------------------
    // CREATE: as colunas AUTO_INCREMENT não são perguntadas, o banco gera o id
    // ---------------------------------------------------------------
    public void inserir() throws SQLException {
        List<Coluna> colunas = entidade.colunasParaInserir();
        List<Object> valores = new ArrayList<>();

        terminal.mostrar("Novo registro em " + entidade.titulo() + ":");
        for (Coluna coluna : colunas) {
            valores.add(terminal.valor(coluna, true));
        }

        long id = repositorio.inserir(colunas, valores);
        terminal.mostrar(id > 0 ? "Registro inserido. ID gerado: " + id : "Registro inserido.");
    }

    // ---------------------------------------------------------------
    // UPDATE: o filtro é opcional. Sem filtro, todos os registros mudam,
    // por isso o programa mostra quantos são e pede confirmação.
    // ---------------------------------------------------------------
    public void atualizar() throws SQLException {
        Alteracoes alteracoes = lerAlteracoes();
        if (alteracoes.vazia()) {
            terminal.mostrar("Atualização cancelada.");
            return;
        }

        terminal.mostrar("Agora o filtro: quais registros serão alterados? (escolha 0 para não filtrar)");
        List<Condicao> filtro = lerCondicoes("Filtrar por quais campos?", SEM_FILTRO);

        long afetados = repositorio.contar(filtro);
        if (afetados == 0) {
            terminal.mostrar("Nenhum registro corresponde ao filtro. Nada foi alterado.");
            return;
        }
        if (!confirmarAtualizacao(afetados, filtro.isEmpty())) {
            terminal.mostrar("Atualização cancelada.");
            return;
        }

        int alterados = repositorio.atualizar(alteracoes.colunas(), alteracoes.valores(), filtro);
        terminal.mostrar(alterados + " registro(s) atualizado(s).");
    }

    // ---------------------------------------------------------------
    // DELETE: o filtro é obrigatório, não existe a opção de apagar tudo
    // ---------------------------------------------------------------
    public void remover() throws SQLException {
        terminal.mostrar("Para remover é obrigatório informar um filtro (por exemplo, o ID).");
        Optional<List<Condicao>> resposta = lerFiltroObrigatorio("Remover pelos registros onde...");
        if (resposta.isEmpty()) {
            terminal.mostrar("Remoção cancelada.");
            return;
        }
        List<Condicao> filtro = resposta.get();

        long afetados = repositorio.contar(filtro);
        if (afetados == 0) {
            terminal.mostrar("Nenhum registro corresponde ao filtro. Nada foi removido.");
            return;
        }

        terminal.mostrar("Registros que serão removidos:");
        ResultadoTabela registros = repositorio.buscar(filtro);
        impressora.imprimir(registros);
        if (!terminal.confirmar("Remover " + afetados + " registro(s)")) {
            terminal.mostrar("Remoção cancelada.");
            return;
        }

        int removidos = repositorio.remover(filtro);
        terminal.mostrar(removidos + " registro(s) removido(s).");
    }

    // ---------------------------------------------------------------
    // Leitura dos dados digitados
    // ---------------------------------------------------------------
    private boolean confirmarAtualizacao(long afetados, boolean semFiltro) {
        String aviso = semFiltro
                ? "SEM FILTRO: isso vai alterar TODOS os " + afetados + " registro(s)."
                : "Isso vai alterar " + afetados + " registro(s).";
        terminal.mostrar(aviso);
        return terminal.confirmar("Confirmar");
    }

    private Alteracoes lerAlteracoes() {
        List<Coluna> colunas = new ArrayList<>();
        List<Object> valores = new ArrayList<>();
        while (true) {
            terminal.mostrar("Qual campo alterar?");
            mostrarColunas();
            terminal.mostrar("  0) " + (colunas.isEmpty() ? CANCELAR : "Pronto"));

            int escolha = terminal.opcao("Escolha: ", 0, entidade.colunas().size());
            if (escolha == 0) {
                return new Alteracoes(colunas, valores);
            }
            Coluna coluna = entidade.colunas().get(escolha - 1);
            if (colunas.contains(coluna)) {
                terminal.mostrar("Esse campo já foi escolhido.");
                continue;
            }
            terminal.mostrar("Novo valor para " + coluna.rotulo() + ":");
            colunas.add(coluna);
            valores.add(terminal.valor(coluna, true));
        }
    }

    /** Filtro com pelo menos uma condição. Vazio se o usuário desistir. */
    private Optional<List<Condicao>> lerFiltroObrigatorio(String titulo) {
        List<Condicao> filtro = lerCondicoes(titulo, CANCELAR);
        return filtro.isEmpty() ? Optional.empty() : Optional.of(filtro);
    }

    /**
     * Pergunta campo e valor até o usuário escolher "0". Várias condições são
     * combinadas com AND. O rótulo do 0 muda conforme ainda não há nenhuma
     * condição ("Sem filtro" ou "Cancelar") ou já há ("Pronto").
     */
    private List<Condicao> lerCondicoes(String titulo, String rotuloSemCondicao) {
        List<Condicao> condicoes = new ArrayList<>();
        while (true) {
            terminal.mostrar(titulo);
            mostrarColunas();
            terminal.mostrar("  0) " + (condicoes.isEmpty() ? rotuloSemCondicao : "Pronto"));

            int escolha = terminal.opcao("Escolha: ", 0, entidade.colunas().size());
            if (escolha == 0) {
                return condicoes;
            }
            Coluna coluna = entidade.colunas().get(escolha - 1);
            condicoes.add(new Condicao(coluna, lerValorDoFiltro(coluna)));
            terminal.mostrar("Condição adicionada. Mais de uma condição são combinadas com E (AND).");
        }
    }

    private Object lerValorDoFiltro(Coluna coluna) {
        String dica = coluna.tipo() == Coluna.Tipo.TEXTO ? " (procura quem contém o texto)" : "";
        terminal.mostrar("Valor para " + coluna.rotulo() + dica + ":");
        return terminal.valor(coluna, false);
    }

    private void mostrarColunas() {
        List<Coluna> colunas = entidade.colunas();
        for (int i = 0; i < colunas.size(); i++) {
            terminal.mostrar("  " + (i + 1) + ") " + colunas.get(i).rotulo());
        }
    }
}
