package futebol.modelo;

import java.util.List;

/**
 * Uma tabela do banco: nome da tabela, título para o menu e suas colunas.
 * O CRUD é escrito uma vez só e funciona para qualquer Entidade.
 */
public record Entidade(String tabela, String titulo, List<Coluna> colunas) {

    /** Colunas que o usuário precisa informar ao inserir (todas menos as AUTO_INCREMENT). */
    public List<Coluna> colunasParaInserir() {
        return colunas.stream().filter(c -> !c.automatica()).toList();
    }
}
