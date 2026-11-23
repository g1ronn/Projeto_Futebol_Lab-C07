package futebol.banco;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Conexão com o MySQL e os comandos básicos que o resto do programa usa.
 * Todos os comandos usam parâmetros (?) e nunca juntam o que o usuário
 * digitou direto no SQL, o que evita SQL injection.
 */
public final class Conexao implements AutoCloseable {

    private final Connection conexao;

    private Conexao(Connection conexao) {
        this.conexao = conexao;
    }

    public static Conexao abrir(ConfiguracaoBanco configuracao) throws SQLException {
        return new Conexao(DriverManager.getConnection(
                configuracao.url(), configuracao.usuario(), configuracao.senha()));
    }

    /** SELECT: devolve o resultado completo. */
    public ResultadoTabela consultar(String sql, List<Object> parametros) throws SQLException {
        try (PreparedStatement comando = preparar(sql, parametros, Statement.NO_GENERATED_KEYS);
             ResultSet resultado = comando.executeQuery()) {
            return ResultadoTabela.de(resultado);
        }
    }

    /** SELECT COUNT(*): devolve o número da primeira coluna da primeira linha. */
    public long contar(String sql, List<Object> parametros) throws SQLException {
        try (PreparedStatement comando = preparar(sql, parametros, Statement.NO_GENERATED_KEYS);
             ResultSet resultado = comando.executeQuery()) {
            resultado.next();
            return resultado.getLong(1);
        }
    }

    /** UPDATE ou DELETE: devolve quantas linhas foram afetadas. */
    public int executar(String sql, List<Object> parametros) throws SQLException {
        try (PreparedStatement comando = preparar(sql, parametros, Statement.NO_GENERATED_KEYS)) {
            return comando.executeUpdate();
        }
    }

    /** INSERT: devolve o id gerado pelo AUTO_INCREMENT, ou -1 se a tabela não tiver. */
    public long inserir(String sql, List<Object> parametros) throws SQLException {
        try (PreparedStatement comando = preparar(sql, parametros, Statement.RETURN_GENERATED_KEYS)) {
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                return chaves.next() ? chaves.getLong(1) : -1;
            }
        }
    }

    /** CALL de procedure. Se ela devolver um resultado, ele vem na tabela; senão, vem vazia. */
    public ResultadoTabela chamar(String chamada, List<Object> parametros) throws SQLException {
        try (CallableStatement comando = conexao.prepareCall(chamada)) {
            definirParametros(comando, parametros);
            if (!comando.execute()) {
                return ResultadoTabela.vazio();
            }
            try (ResultSet resultado = comando.getResultSet()) {
                return ResultadoTabela.de(resultado);
            }
        }
    }

    @Override
    public void close() throws SQLException {
        conexao.close();
    }

    private PreparedStatement preparar(String sql, List<Object> parametros, int opcaoChaves)
            throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(sql, opcaoChaves);
        definirParametros(comando, parametros);
        return comando;
    }

    private static void definirParametros(PreparedStatement comando, List<Object> parametros)
            throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            comando.setObject(i + 1, parametros.get(i));
        }
    }
}
