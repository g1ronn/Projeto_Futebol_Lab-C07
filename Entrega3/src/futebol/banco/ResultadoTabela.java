package futebol.banco;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * O resultado de um SELECT já copiado para a memória (nomes das colunas e
 * linhas em texto). Assim o resto do programa não depende do ResultSet do JDBC.
 */
public record ResultadoTabela(List<String> colunas, List<List<String>> linhas) {

    private static final String TEXTO_NULO = "NULL";

    public static ResultadoTabela de(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metadados = resultSet.getMetaData();
        int total = metadados.getColumnCount();

        List<String> colunas = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            colunas.add(metadados.getColumnLabel(i));
        }

        List<List<String>> linhas = new ArrayList<>();
        while (resultSet.next()) {
            List<String> linha = new ArrayList<>();
            for (int i = 1; i <= total; i++) {
                String valor = resultSet.getString(i);
                linha.add(valor == null ? TEXTO_NULO : valor);
            }
            linhas.add(linha);
        }
        return new ResultadoTabela(colunas, linhas);
    }

    public static ResultadoTabela vazio() {
        return new ResultadoTabela(List.of(), List.of());
    }

    public boolean semLinhas() {
        return linhas.isEmpty();
    }
}
