-- Banco de dados de futebol (MySQL 8)
-- Script feito a partir do modelo da Entrega 1 (Projeto_Futebol.mwb).
-- Os nomes de tabelas e colunas seguem o modelo, so que sem acento
-- (Arbitro, Estadio, Posicao...) para nao dar problema de codificacao.
--
-- Ordem do arquivo: tabelas, triggers da heranca, inserts, DML,
-- view/procedures/functions/triggers, consultas com JOIN, usuarios e role.

DROP DATABASE IF EXISTS futebol;
CREATE DATABASE futebol;
USE futebol;


-- ---------------------------------------------------------------
-- TABELAS
-- ---------------------------------------------------------------

CREATE TABLE Pessoa (
    idPessoa INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Nome VARCHAR(45) NOT NULL,
    Nacionalidade VARCHAR(45) NOT NULL,
    Data_Nascimento DATE NOT NULL
);

CREATE TABLE Estadio (
    idEstadio INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Nome VARCHAR(45) NOT NULL,
    Cidade VARCHAR(45) NOT NULL,
    Capacidade INT NOT NULL,
    Data_Inauguracao DATE NOT NULL
);

-- No modelo a relacao Time-Estadio e 1:1, por isso o UNIQUE: dois times
-- nao podem usar o mesmo estadio.
CREATE TABLE Time (
    idTime INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Nome VARCHAR(45) NOT NULL,
    Data_Fundacao DATE NOT NULL,
    Cidade VARCHAR(45) NOT NULL,
    Estadio_idEstadio INT NOT NULL UNIQUE,
    FOREIGN KEY (Estadio_idEstadio) REFERENCES Estadio(idEstadio)
);

-- Heranca: Pessoa e a tabela "pai". Tecnico, Arbitro e Jogador sao os
-- "filhos". Cada filho tem o proprio id e uma FK para Pessoa (Pessoa_idPessoa),
-- que e UNIQUE porque cada linha de filho corresponde a uma unica Pessoa
-- (relacao 1:1). Os dados que todo mundo tem ficam em Pessoa, e cada filho
-- guarda so o que e dele.

-- Tecnico-Time tambem e 1:1 no modelo (um tecnico por time).
CREATE TABLE Tecnico (
    idTecnico INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Licenca INT NOT NULL,
    Time_idTime INT NOT NULL UNIQUE,
    Pessoa_idPessoa INT NOT NULL UNIQUE,
    FOREIGN KEY (Time_idTime) REFERENCES Time(idTime),
    FOREIGN KEY (Pessoa_idPessoa) REFERENCES Pessoa(idPessoa)
);

CREATE TABLE Arbitro (
    idArbitro INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Curso VARCHAR(45) NOT NULL,
    Pessoa_idPessoa INT NOT NULL UNIQUE,
    FOREIGN KEY (Pessoa_idPessoa) REFERENCES Pessoa(idPessoa)
);

-- Jogador_IdJogador e o relacionamento recursivo do modelo: a tabela aponta
-- para ela mesma. Usamos como "o jogador que este substitui" (o reserva
-- aponta para o titular). Ele aceita NULL porque o titular nao substitui
-- ninguem, e e UNIQUE porque o modelo faz essa relacao 1:1.
CREATE TABLE Jogador (
    IdJogador INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    NumeroJogador INT NOT NULL,
    Posicao VARCHAR(45) NOT NULL,
    Time_idTime INT NOT NULL,
    Pessoa_idPessoa INT NOT NULL UNIQUE,
    Jogador_IdJogador INT NULL UNIQUE,
    FOREIGN KEY (Time_idTime) REFERENCES Time(idTime),
    FOREIGN KEY (Pessoa_idPessoa) REFERENCES Pessoa(idPessoa),
    FOREIGN KEY (Jogador_IdJogador) REFERENCES Jogador(IdJogador)
);

CREATE TABLE Campeonato (
    idCampeonato INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Nome VARCHAR(45) NOT NULL,
    Categoria VARCHAR(45) NOT NULL,
    Data_Inicio DATE NOT NULL,
    Data_Final DATE NOT NULL
);

CREATE TABLE Partida (
    idPartida INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Data_Partida DATE NOT NULL,
    Local VARCHAR(45) NOT NULL,
    Duracao_Partida INT NOT NULL,
    Arbitro_idArbitro INT NOT NULL,
    Campeonato_idCampeonato INT NOT NULL,
    FOREIGN KEY (Arbitro_idArbitro) REFERENCES Arbitro(idArbitro),
    FOREIGN KEY (Campeonato_idCampeonato) REFERENCES Campeonato(idCampeonato)
);

-- N:M entre Jogador e Partida
CREATE TABLE Joga (
    Jogador_IdJogador INT NOT NULL,
    Partida_idPartida INT NOT NULL,
    PRIMARY KEY (Jogador_IdJogador, Partida_idPartida),
    FOREIGN KEY (Jogador_IdJogador) REFERENCES Jogador(IdJogador),
    FOREIGN KEY (Partida_idPartida) REFERENCES Partida(idPartida)
);

-- N:M entre Time e Partida
CREATE TABLE Disputa (
    Time_idTime INT NOT NULL,
    Partida_idPartida INT NOT NULL,
    PRIMARY KEY (Time_idTime, Partida_idPartida),
    FOREIGN KEY (Time_idTime) REFERENCES Time(idTime),
    FOREIGN KEY (Partida_idPartida) REFERENCES Partida(idPartida)
);


-- ---------------------------------------------------------------
-- TRIGGERS DA HERANCA
-- A FK garante que o tecnico/arbitro/jogador seja uma Pessoa que existe,
-- mas nao impede a mesma pessoa de ser, por exemplo, tecnico e jogador ao
-- mesmo tempo. No nosso modelo uma pessoa so tem um papel, entao esses
-- triggers barram o cadastro da mesma pessoa em duas tabelas filhas.
-- Tem que ficar antes dos inserts para ja valerem nos dados de exemplo.
-- ---------------------------------------------------------------
DELIMITER //

CREATE TRIGGER Heranca_Tecnico
BEFORE INSERT ON Tecnico
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM Arbitro WHERE Pessoa_idPessoa = NEW.Pessoa_idPessoa)
       OR EXISTS (SELECT 1 FROM Jogador WHERE Pessoa_idPessoa = NEW.Pessoa_idPessoa) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Esta pessoa ja e Arbitro ou Jogador e nao pode ser Tecnico';
    END IF;
END //

CREATE TRIGGER Heranca_Arbitro
BEFORE INSERT ON Arbitro
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM Tecnico WHERE Pessoa_idPessoa = NEW.Pessoa_idPessoa)
       OR EXISTS (SELECT 1 FROM Jogador WHERE Pessoa_idPessoa = NEW.Pessoa_idPessoa) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Esta pessoa ja e Tecnico ou Jogador e nao pode ser Arbitro';
    END IF;
END //

CREATE TRIGGER Heranca_Jogador
BEFORE INSERT ON Jogador
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM Tecnico WHERE Pessoa_idPessoa = NEW.Pessoa_idPessoa)
       OR EXISTS (SELECT 1 FROM Arbitro WHERE Pessoa_idPessoa = NEW.Pessoa_idPessoa) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Esta pessoa ja e Tecnico ou Arbitro e nao pode ser Jogador';
    END IF;
END //

DELIMITER ;


-- ---------------------------------------------------------------
-- INSERTS
-- Primeiro as tabelas que nao dependem de ninguem, depois as que tem FK.
-- ---------------------------------------------------------------

-- Pessoas 1 a 5 = tecnicos, 6 a 10 = arbitros, 11 a 20 = jogadores
INSERT INTO Pessoa (idPessoa, Nome, Nacionalidade, Data_Nascimento)
VALUES
(1,  'Carlos Silva',       'Brasileiro', '1975-05-12'),
(2,  'Joao Santos',        'Brasileiro', '1968-09-20'),
(3,  'Pedro Oliveira',     'Brasileiro', '1972-02-15'),
(4,  'Lucas Costa',        'Brasileiro', '1980-11-03'),
(5,  'Marcos Souza',       'Brasileiro', '1970-07-25'),
(6,  'Rafael Almeida',     'Brasileiro', '1982-03-14'),
(7,  'Anderson Lima',      'Brasileiro', '1979-08-30'),
(8,  'Bruno Pereira',      'Brasileiro', '1985-01-22'),
(9,  'Fernando Rocha',     'Brasileiro', '1977-12-05'),
(10, 'Ricardo Martins',    'Brasileiro', '1983-06-18'),
(11, 'Thiago Mendes',      'Brasileiro', '1995-04-09'),
(12, 'Diego Ferreira',     'Argentino',  '1997-10-27'),
(13, 'Rodrigo Nunes',      'Brasileiro', '1993-01-16'),
(14, 'Matheus Barros',     'Brasileiro', '1999-11-03'),
(15, 'Felipe Araujo',      'Brasileiro', '2001-02-15'),
(16, 'Gustavo Ribeiro',    'Brasileiro', '1996-07-08'),
(17, 'Leonardo Cardoso',   'Uruguaio',   '1998-05-21'),
(18, 'Vinicius Teixeira',  'Brasileiro', '2000-09-12'),
(19, 'Eduardo Moreira',    'Brasileiro', '1994-12-30'),
(20, 'Andre Carvalho',     'Brasileiro', '1991-03-04');

INSERT INTO Estadio (idEstadio, Nome, Cidade, Capacidade, Data_Inauguracao)
VALUES
(1, 'Maracana',         'Rio de Janeiro', 78838, '1950-06-16'),
(2, 'Morumbi',          'Sao Paulo',      66795, '1960-10-02'),
(3, 'Mineirao',         'Belo Horizonte', 61927, '1965-09-05'),
(4, 'Arena do Gremio',  'Porto Alegre',   55662, '2012-12-08'),
(5, 'Arena Fonte Nova', 'Salvador',       50025, '2013-04-07');

INSERT INTO Time (idTime, Nome, Data_Fundacao, Cidade, Estadio_idEstadio)
VALUES
(1, 'Flamengo',  '1895-11-17', 'Rio de Janeiro', 1),
(2, 'Sao Paulo', '1930-01-25', 'Sao Paulo',      2),
(3, 'Cruzeiro',  '1921-01-02', 'Belo Horizonte', 3),
(4, 'Gremio',    '1903-09-15', 'Porto Alegre',   4),
(5, 'Bahia',     '1931-01-01', 'Salvador',       5);

INSERT INTO Tecnico (idTecnico, Licenca, Time_idTime, Pessoa_idPessoa)
VALUES
(1, 101, 1, 1),
(2, 102, 2, 2),
(3, 103, 3, 3),
(4, 104, 4, 4),
(5, 105, 5, 5);

INSERT INTO Arbitro (idArbitro, Curso, Pessoa_idPessoa)
VALUES
(1, 'Arbitragem Nivel 1', 6),
(2, 'Arbitragem Nivel 2', 7),
(3, 'Arbitragem Nivel 1', 8),
(4, 'Arbitragem Nivel 3', 9),
(5, 'Arbitragem Nivel 2', 10);

-- Cada time tem um titular e um reserva. O reserva aponta para o titular
-- na coluna recursiva (Jogador_IdJogador); o titular fica com NULL.
-- Por isso os titulares (ids 1, 3, 5, 7, 9) entram antes dos reservas.
INSERT INTO Jogador (IdJogador, NumeroJogador, Posicao, Time_idTime, Pessoa_idPessoa, Jogador_IdJogador)
VALUES
(1,  10, 'Meio-campo', 1, 11, NULL),
(3,   1, 'Goleiro',    2, 13, NULL),
(5,   9, 'Atacante',   3, 15, NULL),
(7,   4, 'Zagueiro',   4, 17, NULL),
(9,   7, 'Ponta',      5, 19, NULL);

INSERT INTO Jogador (IdJogador, NumeroJogador, Posicao, Time_idTime, Pessoa_idPessoa, Jogador_IdJogador)
VALUES
(2,  18, 'Meio-campo', 1, 12, 1),
(4,  12, 'Goleiro',    2, 14, 3),
(6,  19, 'Atacante',   3, 16, 5),
(8,  14, 'Zagueiro',   4, 18, 7),
(10, 17, 'Ponta',      5, 20, 9);

INSERT INTO Campeonato (idCampeonato, Nome, Categoria, Data_Inicio, Data_Final)
VALUES
(1, 'Campeonato Brasileiro', 'Profissional',  '2026-04-01', '2026-12-06'),
(2, 'Copa do Brasil',        'Profissional',  '2026-02-18', '2026-11-22'),
(3, 'Campeonato Paulista',   'Estadual',      '2026-01-10', '2026-03-08'),
(4, 'Campeonato Mineiro',    'Estadual',      '2026-01-10', '2026-03-15'),
(5, 'Copa Libertadores',     'Internacional', '2026-02-03', '2026-11-28');

INSERT INTO Partida
(idPartida, Data_Partida, Local, Duracao_Partida, Arbitro_idArbitro, Campeonato_idCampeonato)
VALUES
(1, '2026-05-10', 'Maracana',         90, 1, 1),
(2, '2026-05-15', 'Mineirao',         90, 2, 2),
(3, '2026-05-20', 'Arena Fonte Nova', 90, 3, 1),
(4, '2026-05-25', 'Morumbi',          90, 4, 3),
(5, '2026-05-30', 'Arena do Gremio',  90, 5, 5);

-- dois times por partida
INSERT INTO Disputa (Time_idTime, Partida_idPartida)
VALUES
(1, 1), (2, 1),
(3, 2), (4, 2),
(5, 3), (1, 3),
(2, 4), (3, 4),
(4, 5), (5, 5);

-- os jogadores escalados sao os dos dois times de cada partida
INSERT INTO Joga (Jogador_IdJogador, Partida_idPartida)
VALUES
(1, 1), (2, 1), (3, 1), (4, 1),
(5, 2), (6, 2), (7, 2), (8, 2),
(9, 3), (10, 3), (1, 3), (2, 3),
(3, 4), (4, 4), (5, 4), (6, 4),
(7, 5), (8, 5), (9, 5), (10, 5);


-- ---------------------------------------------------------------
-- DML
-- Sao os comandos que mexem nos dados (INSERT, UPDATE, DELETE), diferente
-- do CREATE, que mexe na estrutura. Os tres abaixo mostram o "ciclo de vida"
-- de um registro, usando um estadio novo como exemplo.
-- ---------------------------------------------------------------

-- INSERT: cadastra o estadio novo
INSERT INTO Estadio (idEstadio, Nome, Cidade, Capacidade, Data_Inauguracao)
VALUES (6, 'Arena do Vale', 'Pouso Alegre', 20000, '2020-01-15');

-- UPDATE: o estadio foi ampliado, entao muda a capacidade. O WHERE e
-- importante, sem ele todos os estadios ficariam com 25 mil.
UPDATE Estadio SET Capacidade = 25000 WHERE idEstadio = 6;

-- DELETE: apaga o estadio para a tabela voltar a ter so os 5 originais.
-- Funciona porque nenhum time usa o estadio 6; se algum usasse, a FK
-- nao deixaria apagar.
DELETE FROM Estadio WHERE idEstadio = 6;


-- ---------------------------------------------------------------
-- VIEW
-- A View_Partidas junta varias tabelas (partida, campeonato, arbitro,
-- pessoa e os times) para mostrar cada partida de forma legivel, com o
-- confronto tipo "Flamengo x Sao Paulo". Criamos para nao precisar
-- escrever esse monte de JOIN toda vez que quisermos ver as partidas.
-- ---------------------------------------------------------------
CREATE VIEW View_Partidas AS
SELECT
    p.idPartida,
    p.Data_Partida,
    p.Local,
    c.Nome AS Campeonato,
    pe.Nome AS Arbitro,
    GROUP_CONCAT(t.Nome ORDER BY t.Nome SEPARATOR ' x ') AS Confronto
FROM Partida p
INNER JOIN Campeonato c ON c.idCampeonato = p.Campeonato_idCampeonato
INNER JOIN Arbitro a ON a.idArbitro = p.Arbitro_idArbitro
INNER JOIN Pessoa pe ON pe.idPessoa = a.Pessoa_idPessoa
LEFT JOIN Disputa d ON d.Partida_idPartida = p.idPartida
LEFT JOIN Time t ON t.idTime = d.Time_idTime
GROUP BY p.idPartida, p.Data_Partida, p.Local, c.Nome, pe.Nome;

SELECT * FROM View_Partidas;


DELIMITER //

-- ---------------------------------------------------------------
-- PROCEDURE ConsultarPartidasPorCampeonato
-- Lista as partidas de um campeonato. Recebe o id do campeonato, assim
-- da para reaproveitar a mesma consulta para qualquer campeonato so
-- trocando o numero, em vez de reescrever o SELECT.
-- ---------------------------------------------------------------
CREATE PROCEDURE ConsultarPartidasPorCampeonato(IN p_idCampeonato INT)
BEGIN
    SELECT
        p.idPartida,
        p.Data_Partida,
        p.Local,
        p.Duracao_Partida,
        GROUP_CONCAT(t.Nome ORDER BY t.Nome SEPARATOR ' x ') AS Confronto
    FROM Partida p
    LEFT JOIN Disputa d ON d.Partida_idPartida = p.idPartida
    LEFT JOIN Time t ON t.idTime = d.Time_idTime
    WHERE p.Campeonato_idCampeonato = p_idCampeonato
    GROUP BY p.idPartida, p.Data_Partida, p.Local, p.Duracao_Partida;
END //

-- ---------------------------------------------------------------
-- PROCEDURE CadastrarJogador (extra)
-- Por causa da heranca, cadastrar um jogador precisa de dois inserts: um
-- em Pessoa e outro em Jogador. Se o segundo desse erro (por exemplo, um
-- time que nao existe), o primeiro ficaria salvo sozinho. Por isso usamos
-- transacao: se algo falhar, o ROLLBACK desfaz tudo. Os ids novos vem do
-- AUTO_INCREMENT, lidos com LAST_INSERT_ID().
-- ---------------------------------------------------------------
CREATE PROCEDURE CadastrarJogador(
    IN p_nome VARCHAR(45),
    IN p_nacionalidade VARCHAR(45),
    IN p_nascimento DATE,
    IN p_camisa INT,
    IN p_posicao VARCHAR(45),
    IN p_idTime INT
)
BEGIN
    DECLARE v_idPessoa INT;
    DECLARE v_idJogador INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    INSERT INTO Pessoa (Nome, Nacionalidade, Data_Nascimento)
    VALUES (p_nome, p_nacionalidade, p_nascimento);
    SET v_idPessoa = LAST_INSERT_ID();

    INSERT INTO Jogador (NumeroJogador, Posicao, Time_idTime, Pessoa_idPessoa, Jogador_IdJogador)
    VALUES (p_camisa, p_posicao, p_idTime, v_idPessoa, NULL);
    SET v_idJogador = LAST_INSERT_ID();
    COMMIT;

    SELECT v_idPessoa AS idPessoa_criado, v_idJogador AS IdJogador_criado;
END //

-- ---------------------------------------------------------------
-- FUNCTION CalcularIdade
-- No banco guardamos a data de nascimento, e nao a idade, porque a idade
-- muda com o tempo e ficaria desatualizada. A function calcula a idade na
-- hora e, como devolve um valor, da para usar direto dentro de um SELECT.
-- ---------------------------------------------------------------
CREATE FUNCTION CalcularIdade(p_nascimento DATE)
RETURNS INT
NO SQL
BEGIN
    RETURN TIMESTAMPDIFF(YEAR, p_nascimento, CURDATE());
END //

-- FUNCTION QtdPartidasTime (extra): conta quantas partidas um time disputou
-- olhando a tabela Disputa.
CREATE FUNCTION QtdPartidasTime(p_idTime INT)
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_total INT;
    SELECT COUNT(*) INTO v_total FROM Disputa WHERE Time_idTime = p_idTime;
    RETURN v_total;
END //

-- ---------------------------------------------------------------
-- TRIGGERS ValidarDuracaoPartida
-- Nao deixam salvar uma partida com duracao zero ou negativa. Como a regra
-- fica no banco, vale para qualquer um que inserir dados. Sao dois triggers
-- (insert e update) porque o MySQL nao deixa fazer os dois em um so; sem o
-- de update, daria para inserir 90 e depois trocar para 0.
-- ---------------------------------------------------------------
CREATE TRIGGER ValidarDuracaoPartida_Insert
BEFORE INSERT ON Partida
FOR EACH ROW
BEGIN
    IF NEW.Duracao_Partida <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'A duracao da partida deve ser maior que zero';
    END IF;
END //

CREATE TRIGGER ValidarDuracaoPartida_Update
BEFORE UPDATE ON Partida
FOR EACH ROW
BEGIN
    IF NEW.Duracao_Partida <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'A duracao da partida deve ser maior que zero';
    END IF;
END //

DELIMITER ;

-- testando a procedure e as functions
CALL ConsultarPartidasPorCampeonato(1);
CALL CadastrarJogador('Henrique Duarte', 'Brasileiro', '2003-03-21', 11, 'Atacante', 1);
SELECT Nome, CalcularIdade(Data_Nascimento) AS Idade FROM Pessoa WHERE idPessoa <= 3;
SELECT Nome, QtdPartidasTime(idTime) AS Partidas_Disputadas FROM Time;

/* Testes que dao erro de proposito, por isso estao comentados (se rodassem
   junto com o script, ele pararia aqui). Tire o comentario e rode um por vez
   para ver a mensagem de erro.

   -- duracao zero no insert
   INSERT INTO Partida (Data_Partida, Local, Duracao_Partida,
                        Arbitro_idArbitro, Campeonato_idCampeonato)
   VALUES ('2026-06-10', 'Teste', 0, 1, 1);

   -- duracao zero no update
   UPDATE Partida SET Duracao_Partida = 0 WHERE idPartida = 1;

   -- pessoa 1 ja e tecnico, nao pode virar jogador
   INSERT INTO Jogador (NumeroJogador, Posicao, Time_idTime, Pessoa_idPessoa)
   VALUES (99, 'Teste', 1, 1);

   -- time 99 nao existe: a procedure da erro e o ROLLBACK desfaz o insert
   -- em Pessoa (confira que a contagem de Pessoa nao muda)
   CALL CadastrarJogador('Teste Rollback', 'Brasileiro', '2000-01-01', 5, 'Zagueiro', 99);
   SELECT COUNT(*) FROM Pessoa;
*/


-- ---------------------------------------------------------------
-- CONSULTAS COM JOIN
-- ---------------------------------------------------------------

-- 1:1 (heranca): dados da Pessoa junto com os de Jogador
SELECT
    pe.idPessoa,
    pe.Nome,
    CalcularIdade(pe.Data_Nascimento) AS Idade,
    j.NumeroJogador,
    j.Posicao
FROM Pessoa pe
INNER JOIN Jogador j ON pe.idPessoa = j.Pessoa_idPessoa
ORDER BY j.NumeroJogador, pe.Nome;

-- 1:1: tecnicos com o time que comandam e o estadio do time
SELECT
    pe.Nome AS Tecnico,
    tc.Licenca,
    t.Nome AS Nome_Time,
    e.Nome AS Estadio
FROM Tecnico tc
INNER JOIN Pessoa pe ON pe.idPessoa = tc.Pessoa_idPessoa
INNER JOIN Time t ON t.idTime = tc.Time_idTime
INNER JOIN Estadio e ON e.idEstadio = t.Estadio_idEstadio;

-- 1:N: um time tem varios jogadores; lista cada jogador com o seu time
SELECT
    t.Nome AS Nome_Time,
    pe.Nome AS Jogador,
    j.NumeroJogador,
    j.Posicao
FROM Time t
INNER JOIN Jogador j ON j.Time_idTime = t.idTime
INNER JOIN Pessoa pe ON pe.idPessoa = j.Pessoa_idPessoa
ORDER BY t.Nome, j.NumeroJogador;

-- 1:N: um arbitro apita varias partidas; conta quantas cada um apitou
SELECT
    pe.Nome AS Arbitro,
    a.Curso,
    COUNT(p.idPartida) AS Partidas_Apitadas
FROM Arbitro a
INNER JOIN Pessoa pe ON pe.idPessoa = a.Pessoa_idPessoa
LEFT JOIN Partida p ON p.Arbitro_idArbitro = a.idArbitro
GROUP BY a.idArbitro, pe.Nome, a.Curso
ORDER BY Partidas_Apitadas DESC, pe.Nome;

-- N:M (Time e Partida): usa a tabela Disputa duas vezes (d1 e d2) para
-- colocar os dois times da partida na mesma linha. O d1.Time < d2.Time
-- evita que cada jogo apareca duas vezes (A x B e B x A).
SELECT
    p.idPartida,
    p.Data_Partida,
    p.Local,
    c.Nome AS Campeonato,
    t1.Nome AS Time_A,
    t2.Nome AS Time_B
FROM Disputa d1
INNER JOIN Disputa d2 ON d1.Partida_idPartida = d2.Partida_idPartida
                     AND d1.Time_idTime < d2.Time_idTime
INNER JOIN Time t1 ON t1.idTime = d1.Time_idTime
INNER JOIN Time t2 ON t2.idTime = d2.Time_idTime
INNER JOIN Partida p ON p.idPartida = d1.Partida_idPartida
INNER JOIN Campeonato c ON c.idCampeonato = p.Campeonato_idCampeonato
ORDER BY p.Data_Partida;

-- N:M (Jogador e Partida): quantas partidas cada jogador disputou. O LEFT
-- JOIN faz aparecer tambem quem ainda nao jogou nenhuma (com 0).
SELECT
    pe.Nome AS Jogador,
    j.Posicao,
    COUNT(jg.Partida_idPartida) AS Partidas_Jogadas
FROM Jogador j
INNER JOIN Pessoa pe ON pe.idPessoa = j.Pessoa_idPessoa
LEFT JOIN Joga jg ON jg.Jogador_IdJogador = j.IdJogador
GROUP BY j.IdJogador, pe.Nome, j.Posicao
ORDER BY Partidas_Jogadas DESC, pe.Nome;

-- Relacionamento recursivo (extra): a tabela Jogador ligada a ela mesma
-- para mostrar cada reserva ao lado do titular que ele substitui.
SELECT
    pr.Nome AS Reserva,
    jr.NumeroJogador AS Camisa_Reserva,
    pt.Nome AS Titular,
    jt.NumeroJogador AS Camisa_Titular,
    t.Nome AS Nome_Time
FROM Jogador jr
INNER JOIN Jogador jt ON jt.IdJogador = jr.Jogador_IdJogador
INNER JOIN Pessoa pr ON pr.idPessoa = jr.Pessoa_idPessoa
INNER JOIN Pessoa pt ON pt.idPessoa = jt.Pessoa_idPessoa
INNER JOIN Time t ON t.idTime = jr.Time_idTime
ORDER BY t.Nome;


-- ---------------------------------------------------------------
-- USUARIOS E ROLE
-- A role e um grupo de permissoes com nome. Em vez de dar as permissoes
-- para cada usuario, damos para a role e depois ligamos os usuarios a ela.
-- Se precisar mudar o acesso de todo mundo, mexe so na role.
-- A leitura_futebol so pode consultar (SELECT) e executar a procedure de
-- consulta; nao pode inserir, alterar nem apagar.
-- ---------------------------------------------------------------
CREATE ROLE IF NOT EXISTS 'leitura_futebol';
GRANT SELECT ON futebol.* TO 'leitura_futebol';
GRANT EXECUTE ON PROCEDURE futebol.ConsultarPartidasPorCampeonato TO 'leitura_futebol';

CREATE USER IF NOT EXISTS 'usuario1'@'localhost' IDENTIFIED BY 'Futebol123!';
CREATE USER IF NOT EXISTS 'usuario2'@'localhost' IDENTIFIED BY 'Futebol456!';

GRANT 'leitura_futebol' TO 'usuario1'@'localhost';
GRANT 'leitura_futebol' TO 'usuario2'@'localhost';

-- deixa a role ligada automaticamente quando o usuario conecta
SET DEFAULT ROLE 'leitura_futebol' TO 'usuario1'@'localhost';
SET DEFAULT ROLE 'leitura_futebol' TO 'usuario2'@'localhost';

-- mostra o que cada usuario consegue fazer pela role
SHOW GRANTS FOR 'usuario1'@'localhost' USING 'leitura_futebol';
SHOW GRANTS FOR 'usuario2'@'localhost' USING 'leitura_futebol';
