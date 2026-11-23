package futebol.consulta;

import futebol.banco.Conexao;
import futebol.banco.ResultadoTabela;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Usa a view, as procedures e as functions criadas no futebol.sql (Entrega 2). */
public final class ObjetosBanco {

    private static final String VIEW_PARTIDAS = "SELECT * FROM View_Partidas";

    private static final String PESSOAS_COM_IDADE =
            "SELECT idPessoa, Nome, CalcularIdade(Data_Nascimento) AS Idade FROM Pessoa ORDER BY idPessoa";

    private static final String PARTIDAS_POR_TIME =
            "SELECT idTime, Nome, QtdPartidasTime(idTime) AS Partidas FROM Time ORDER BY idTime";

    private static final String CHAMAR_PARTIDAS_DO_CAMPEONATO =
            "{CALL ConsultarPartidasPorCampeonato(?)}";

    private static final String CHAMAR_CADASTRO_DE_JOGADOR =
            "{CALL CadastrarJogador(?, ?, ?, ?, ?, ?)}";

    /** Dados necessários para a procedure CadastrarJogador. */
    public record NovoJogador(String nome, String nacionalidade, LocalDate nascimento,
                              int camisa, String posicao, int idTime) {
    }

    private final Conexao conexao;

    public ObjetosBanco(Conexao conexao) {
        this.conexao = conexao;
    }

    public ResultadoTabela viewPartidas() throws SQLException {
        return conexao.consultar(VIEW_PARTIDAS, List.of());
    }

    public ResultadoTabela pessoasComIdade() throws SQLException {
        return conexao.consultar(PESSOAS_COM_IDADE, List.of());
    }

    public ResultadoTabela partidasPorTime() throws SQLException {
        return conexao.consultar(PARTIDAS_POR_TIME, List.of());
    }

    public ResultadoTabela partidasDoCampeonato(int idCampeonato) throws SQLException {
        return conexao.chamar(CHAMAR_PARTIDAS_DO_CAMPEONATO, List.of(idCampeonato));
    }

    public ResultadoTabela cadastrarJogador(NovoJogador jogador) throws SQLException {
        return conexao.chamar(CHAMAR_CADASTRO_DE_JOGADOR, List.of(
                jogador.nome(), jogador.nacionalidade(), jogador.nascimento(),
                jogador.camisa(), jogador.posicao(), jogador.idTime()));
    }
}
