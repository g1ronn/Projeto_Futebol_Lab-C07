package futebol.repositorio;

import futebol.modelo.Coluna;
import futebol.modelo.Condicao;
import futebol.modelo.Entidade;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Monta o texto dos comandos SQL a partir da descrição da Entidade.
 * Só monta texto (não executa nada), e os valores sempre ficam como "?".
 */
final class ConstrutorSql {

    static String selecionar(Entidade entidade, List<Condicao> filtro) {
        return "SELECT * FROM " + tabela(entidade) + onde(filtro) + " ORDER BY 1";
    }

    static String contar(Entidade entidade, List<Condicao> filtro) {
        return "SELECT COUNT(*) FROM " + tabela(entidade) + onde(filtro);
    }

    static String inserir(Entidade entidade, List<Coluna> colunas) {
        String nomes = colunas.stream()
                .map(c -> entreCrases(c.nome()))
                .collect(Collectors.joining(", "));
        String interrogacoes = colunas.stream()
                .map(c -> "?")
                .collect(Collectors.joining(", "));
        return "INSERT INTO " + tabela(entidade) + " (" + nomes + ") VALUES (" + interrogacoes + ")";
    }

    static String atualizar(Entidade entidade, List<Coluna> colunasAlteradas, List<Condicao> filtro) {
        String atribuicoes = colunasAlteradas.stream()
                .map(c -> entreCrases(c.nome()) + " = ?")
                .collect(Collectors.joining(", "));
        return "UPDATE " + tabela(entidade) + " SET " + atribuicoes + onde(filtro);
    }

    static String remover(Entidade entidade, List<Condicao> filtro) {
        return "DELETE FROM " + tabela(entidade) + onde(filtro);
    }

    /** Texto procura quem contém o valor (LIKE); número e data usam igualdade. */
    static Object parametro(Condicao condicao) {
        if (condicao.coluna().tipo() == Coluna.Tipo.TEXTO) {
            return "%" + condicao.valor() + "%";
        }
        return condicao.valor();
    }

    private static String onde(List<Condicao> filtro) {
        if (filtro.isEmpty()) {
            return "";
        }
        return " WHERE " + filtro.stream()
                .map(ConstrutorSql::fragmento)
                .collect(Collectors.joining(" AND "));
    }

    private static String fragmento(Condicao condicao) {
        String coluna = entreCrases(condicao.coluna().nome());
        return condicao.coluna().tipo() == Coluna.Tipo.TEXTO ? coluna + " LIKE ?" : coluna + " = ?";
    }

    private static String tabela(Entidade entidade) {
        return entreCrases(entidade.tabela());
    }

    private static String entreCrases(String nome) {
        return "`" + nome + "`";
    }

    private ConstrutorSql() {
    }
}
