package futebol.repositorio;

import futebol.banco.Conexao;
import futebol.banco.ResultadoTabela;
import futebol.modelo.Coluna;
import futebol.modelo.Condicao;
import futebol.modelo.Entidade;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso ao banco para uma Entidade: as operações de CRUD, sem nenhuma
 * interação com o usuário. Quem pergunta e mostra é a camada de telas.
 */
public final class RepositorioEntidade {

    private final Conexao conexao;
    private final Entidade entidade;

    public RepositorioEntidade(Conexao conexao, Entidade entidade) {
        this.conexao = conexao;
        this.entidade = entidade;
    }

    public ResultadoTabela listarTodos() throws SQLException {
        return buscar(List.of());
    }

    public ResultadoTabela buscar(List<Condicao> filtro) throws SQLException {
        return conexao.consultar(ConstrutorSql.selecionar(entidade, filtro), parametros(filtro));
    }

    public long contar(List<Condicao> filtro) throws SQLException {
        return conexao.contar(ConstrutorSql.contar(entidade, filtro), parametros(filtro));
    }

    /** Insere um registro e devolve o id gerado (ou -1 se a tabela não tiver AUTO_INCREMENT). */
    public long inserir(List<Coluna> colunas, List<Object> valores) throws SQLException {
        return conexao.inserir(ConstrutorSql.inserir(entidade, colunas), valores);
    }

    public int atualizar(List<Coluna> colunasAlteradas, List<Object> novosValores, List<Condicao> filtro)
            throws SQLException {
        List<Object> parametros = new ArrayList<>(novosValores);
        parametros.addAll(parametros(filtro));
        return conexao.executar(ConstrutorSql.atualizar(entidade, colunasAlteradas, filtro), parametros);
    }

    public int remover(List<Condicao> filtro) throws SQLException {
        return conexao.executar(ConstrutorSql.remover(entidade, filtro), parametros(filtro));
    }

    private static List<Object> parametros(List<Condicao> filtro) {
        return filtro.stream().map(ConstrutorSql::parametro).toList();
    }
}
