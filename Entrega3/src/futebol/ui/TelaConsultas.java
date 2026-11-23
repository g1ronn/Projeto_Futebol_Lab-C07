package futebol.ui;

import futebol.banco.Conexao;
import futebol.consulta.ConsultasJoin;
import futebol.consulta.ObjetosBanco;
import futebol.consulta.ObjetosBanco.NovoJogador;
import futebol.ui.Menu.Opcao;
import java.sql.SQLException;
import java.util.List;

/** Menus das consultas com JOIN e dos objetos do banco (view, procedures e functions). */
public final class TelaConsultas {

    private final Terminal terminal;
    private final ImpressoraTabela impressora;
    private final Conexao conexao;
    private final ObjetosBanco objetos;

    public TelaConsultas(Terminal terminal, ImpressoraTabela impressora, Conexao conexao) {
        this.terminal = terminal;
        this.impressora = impressora;
        this.conexao = conexao;
        this.objetos = new ObjetosBanco(conexao);
    }

    public Menu menuDeJoins() {
        List<Opcao> opcoes = ConsultasJoin.TODAS.stream()
                .map(consulta -> new Opcao(consulta.rotulo(),
                        () -> impressora.imprimir(conexao.consultar(consulta.sql(), List.of()))))
                .toList();
        return new Menu(terminal, "Consultas com JOIN", "Voltar", opcoes);
    }

    public Menu menuDeObjetos() {
        return new Menu(terminal, "View, procedures e functions", "Voltar", List.of(
                new Opcao("View: partidas com campeonato, árbitro e confronto",
                        () -> impressora.imprimir(objetos.viewPartidas())),
                new Opcao("Procedure: partidas de um campeonato", this::partidasDoCampeonato),
                new Opcao("Procedure: cadastrar jogador (Pessoa + Jogador, com transação)", this::cadastrarJogador),
                new Opcao("Function: pessoas com a idade calculada",
                        () -> impressora.imprimir(objetos.pessoasComIdade())),
                new Opcao("Function: quantidade de partidas de cada time",
                        () -> impressora.imprimir(objetos.partidasPorTime()))));
    }

    private void partidasDoCampeonato() throws SQLException {
        int idCampeonato = terminal.inteiro("ID do campeonato: ");
        impressora.imprimir(objetos.partidasDoCampeonato(idCampeonato));
    }

    private void cadastrarJogador() throws SQLException {
        NovoJogador jogador = lerNovoJogador();
        impressora.imprimir(objetos.cadastrarJogador(jogador));
        terminal.mostrar("Jogador cadastrado.");
    }

    private NovoJogador lerNovoJogador() {
        String nome = terminal.texto("Nome: ");
        String nacionalidade = terminal.texto("Nacionalidade: ");
        var nascimento = terminal.data("Data de nascimento");
        int camisa = terminal.inteiro("Número da camisa: ");
        String posicao = terminal.texto("Posição: ");
        int idTime = terminal.inteiro("ID do time: ");
        return new NovoJogador(nome, nacionalidade, nascimento, camisa, posicao, idTime);
    }
}
