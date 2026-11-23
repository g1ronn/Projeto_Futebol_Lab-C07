package futebol.modelo;

import java.util.List;

/** As 10 tabelas do banco, descritas como no futebol.sql. */
public final class Entidades {

    public static final List<Entidade> TODAS = List.of(
        new Entidade("Pessoa", "Pessoa", List.of(
            Coluna.id("idPessoa", "ID da pessoa"),
            Coluna.texto("Nome", "Nome"),
            Coluna.texto("Nacionalidade", "Nacionalidade"),
            Coluna.data("Data_Nascimento", "Data de nascimento")
        )),
        new Entidade("Estadio", "Estádio", List.of(
            Coluna.id("idEstadio", "ID do estádio"),
            Coluna.texto("Nome", "Nome"),
            Coluna.texto("Cidade", "Cidade"),
            Coluna.numero("Capacidade", "Capacidade"),
            Coluna.data("Data_Inauguracao", "Data de inauguração")
        )),
        new Entidade("Time", "Time", List.of(
            Coluna.id("idTime", "ID do time"),
            Coluna.texto("Nome", "Nome"),
            Coluna.data("Data_Fundacao", "Data de fundação"),
            Coluna.texto("Cidade", "Cidade"),
            Coluna.numero("Estadio_idEstadio", "ID do estádio")
        )),
        new Entidade("Tecnico", "Técnico", List.of(
            Coluna.id("idTecnico", "ID do técnico"),
            Coluna.numero("Licenca", "Licença"),
            Coluna.numero("Time_idTime", "ID do time"),
            Coluna.numero("Pessoa_idPessoa", "ID da pessoa")
        )),
        new Entidade("Arbitro", "Árbitro", List.of(
            Coluna.id("idArbitro", "ID do árbitro"),
            Coluna.texto("Curso", "Curso"),
            Coluna.numero("Pessoa_idPessoa", "ID da pessoa")
        )),
        new Entidade("Jogador", "Jogador", List.of(
            Coluna.id("IdJogador", "ID do jogador"),
            Coluna.numero("NumeroJogador", "Número da camisa"),
            Coluna.texto("Posicao", "Posição"),
            Coluna.numero("Time_idTime", "ID do time"),
            Coluna.numero("Pessoa_idPessoa", "ID da pessoa"),
            Coluna.numeroOpcional("Jogador_IdJogador", "ID do jogador que ele substitui")
        )),
        new Entidade("Campeonato", "Campeonato", List.of(
            Coluna.id("idCampeonato", "ID do campeonato"),
            Coluna.texto("Nome", "Nome"),
            Coluna.texto("Categoria", "Categoria"),
            Coluna.data("Data_Inicio", "Data de início"),
            Coluna.data("Data_Final", "Data final")
        )),
        new Entidade("Partida", "Partida", List.of(
            Coluna.id("idPartida", "ID da partida"),
            Coluna.data("Data_Partida", "Data da partida"),
            Coluna.texto("Local", "Local"),
            Coluna.numero("Duracao_Partida", "Duração (minutos)"),
            Coluna.numero("Arbitro_idArbitro", "ID do árbitro"),
            Coluna.numero("Campeonato_idCampeonato", "ID do campeonato")
        )),
        new Entidade("Joga", "Joga (jogador x partida)", List.of(
            Coluna.numero("Jogador_IdJogador", "ID do jogador"),
            Coluna.numero("Partida_idPartida", "ID da partida")
        )),
        new Entidade("Disputa", "Disputa (time x partida)", List.of(
            Coluna.numero("Time_idTime", "ID do time"),
            Coluna.numero("Partida_idPartida", "ID da partida")
        ))
    );

    private Entidades() {
    }
}
