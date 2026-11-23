package futebol.modelo;

/** Uma condição de filtro: "coluna" com o "valor" informado pelo usuário. */
public record Condicao(Coluna coluna, Object valor) {
}
