package futebol.consulta;

import java.util.List;

/** As consultas com JOIN do menu, uma de cada tipo de relacionamento (e mais algumas). */
public final class ConsultasJoin {

    public static final List<ConsultaJoin> TODAS = List.of(
        new ConsultaJoin("[1:1] Jogadores com os dados de Pessoa (herança)", """
            SELECT j.IdJogador, pe.Nome, CalcularIdade(pe.Data_Nascimento) AS Idade,
                   j.NumeroJogador AS Camisa, j.Posicao
            FROM Pessoa pe
            INNER JOIN Jogador j ON pe.idPessoa = j.Pessoa_idPessoa
            ORDER BY j.IdJogador"""),

        new ConsultaJoin("[1:1] Técnicos com o time que comandam e o estádio", """
            SELECT pe.Nome AS Tecnico, tc.Licenca, t.Nome AS Time_, e.Nome AS Estadio
            FROM Tecnico tc
            INNER JOIN Pessoa pe ON pe.idPessoa = tc.Pessoa_idPessoa
            INNER JOIN Time t ON t.idTime = tc.Time_idTime
            INNER JOIN Estadio e ON e.idEstadio = t.Estadio_idEstadio
            ORDER BY t.Nome"""),

        new ConsultaJoin("[1:N] Cada time com os seus jogadores", """
            SELECT t.Nome AS Time_, pe.Nome AS Jogador, j.NumeroJogador AS Camisa, j.Posicao
            FROM Time t
            INNER JOIN Jogador j ON j.Time_idTime = t.idTime
            INNER JOIN Pessoa pe ON pe.idPessoa = j.Pessoa_idPessoa
            ORDER BY t.Nome, j.NumeroJogador"""),

        new ConsultaJoin("[1:N] Quantas partidas cada árbitro apitou", """
            SELECT pe.Nome AS Arbitro, a.Curso, COUNT(p.idPartida) AS Partidas_Apitadas
            FROM Arbitro a
            INNER JOIN Pessoa pe ON pe.idPessoa = a.Pessoa_idPessoa
            LEFT JOIN Partida p ON p.Arbitro_idArbitro = a.idArbitro
            GROUP BY a.idArbitro, pe.Nome, a.Curso
            ORDER BY Partidas_Apitadas DESC, pe.Nome"""),

        new ConsultaJoin("[N:M] Confronto de cada partida (times x partidas)", """
            SELECT p.idPartida, p.Data_Partida, p.Local, c.Nome AS Campeonato,
                   t1.Nome AS Time_A, t2.Nome AS Time_B
            FROM Disputa d1
            INNER JOIN Disputa d2 ON d1.Partida_idPartida = d2.Partida_idPartida
                                 AND d1.Time_idTime < d2.Time_idTime
            INNER JOIN Time t1 ON t1.idTime = d1.Time_idTime
            INNER JOIN Time t2 ON t2.idTime = d2.Time_idTime
            INNER JOIN Partida p ON p.idPartida = d1.Partida_idPartida
            INNER JOIN Campeonato c ON c.idCampeonato = p.Campeonato_idCampeonato
            ORDER BY p.Data_Partida"""),

        new ConsultaJoin("[N:M] Quantas partidas cada jogador disputou", """
            SELECT pe.Nome AS Jogador, j.Posicao, COUNT(jg.Partida_idPartida) AS Partidas_Jogadas
            FROM Jogador j
            INNER JOIN Pessoa pe ON pe.idPessoa = j.Pessoa_idPessoa
            LEFT JOIN Joga jg ON jg.Jogador_IdJogador = j.IdJogador
            GROUP BY j.IdJogador, pe.Nome, j.Posicao
            ORDER BY Partidas_Jogadas DESC, pe.Nome"""),

        new ConsultaJoin("[Recursivo] Cada reserva ao lado do titular que ele substitui", """
            SELECT pr.Nome AS Reserva, jr.NumeroJogador AS Camisa_Reserva,
                   pt.Nome AS Titular, jt.NumeroJogador AS Camisa_Titular, t.Nome AS Time_
            FROM Jogador jr
            INNER JOIN Jogador jt ON jt.IdJogador = jr.Jogador_IdJogador
            INNER JOIN Pessoa pr ON pr.idPessoa = jr.Pessoa_idPessoa
            INNER JOIN Pessoa pt ON pt.idPessoa = jt.Pessoa_idPessoa
            INNER JOIN Time t ON t.idTime = jr.Time_idTime
            ORDER BY t.Nome""")
    );

    private ConsultasJoin() {
    }
}
