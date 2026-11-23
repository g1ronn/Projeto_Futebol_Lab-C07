package futebol.modelo;

/**
 * Uma coluna de uma tabela: nome no banco, texto exibido no menu, tipo (usado
 * para validar o que o usuário digita) e se aceita NULL.
 * As colunas "automáticas" são AUTO_INCREMENT: o banco preenche sozinho.
 */
public record Coluna(String nome, String rotulo, Tipo tipo, boolean anulavel, boolean automatica) {

    public enum Tipo { TEXTO, NUMERO, DATA }

    public static Coluna id(String nome, String rotulo) {
        return new Coluna(nome, rotulo, Tipo.NUMERO, false, true);
    }

    public static Coluna texto(String nome, String rotulo) {
        return new Coluna(nome, rotulo, Tipo.TEXTO, false, false);
    }

    public static Coluna numero(String nome, String rotulo) {
        return new Coluna(nome, rotulo, Tipo.NUMERO, false, false);
    }

    public static Coluna numeroOpcional(String nome, String rotulo) {
        return new Coluna(nome, rotulo, Tipo.NUMERO, true, false);
    }

    public static Coluna data(String nome, String rotulo) {
        return new Coluna(nome, rotulo, Tipo.DATA, false, false);
    }
}
