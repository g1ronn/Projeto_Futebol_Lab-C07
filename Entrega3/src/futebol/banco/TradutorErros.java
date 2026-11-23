package futebol.banco;

import java.sql.SQLException;

/** Transforma os erros do MySQL em mensagens que o usuário entenda. */
public final class TradutorErros {

    /** SQLSTATE usado pelos triggers do banco (SIGNAL) para avisar de uma regra violada. */
    private static final String ESTADO_REGRA_DO_BANCO = "45000";

    private static final int ERRO_REGISTRO_EM_USO = 1451;
    private static final int ERRO_REFERENCIA_INEXISTENTE = 1452;
    private static final int ERRO_VALOR_DUPLICADO = 1062;

    public static String traduzir(SQLException erro) {
        if (ESTADO_REGRA_DO_BANCO.equals(erro.getSQLState())) {
            return erro.getMessage();
        }
        return switch (erro.getErrorCode()) {
            case ERRO_REGISTRO_EM_USO ->
                "Não dá para remover ou alterar: existem registros que dependem deste (chave estrangeira).";
            case ERRO_REFERENCIA_INEXISTENTE ->
                "Referência inválida: o ID informado não existe na tabela relacionada.";
            case ERRO_VALOR_DUPLICADO ->
                "Valor duplicado: já existe um registro com essa chave ou valor único.";
            default -> erro.getMessage();
        };
    }

    private TradutorErros() {
    }
}
