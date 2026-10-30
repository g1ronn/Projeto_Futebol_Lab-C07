# Documentação da Entrega 2: banco de dados de futebol

Implementamos em MySQL 8 o modelo da Entrega 1 (`Projeto_Futebol.mwb`). Todo o código está no `futebol.sql`: ele cria o banco do zero, insere os dados e monta a view, as procedures, as functions, os triggers, os usuários e a role.

Os nomes de tabelas e colunas seguem o modelo, só que sem acento (`Arbitro` em vez de `Árbitro`, `Posicao` em vez de `Posição`), para evitar problema de codificação.

## 1. O modelo

O banco guarda dados de campeonatos de futebol: pessoas (técnicos, árbitros e jogadores), times, estádios, campeonatos e partidas. São 10 tabelas.

`Pessoa` guarda o que todo mundo tem (nome, nacionalidade e data de nascimento), e `Tecnico`, `Arbitro` e `Jogador` completam com os dados de cada papel. `Estadio` tem nome, cidade, capacidade e data de inauguração. `Time` tem um estádio. `Campeonato` tem nome, categoria e período, e `Partida` tem data, local e duração, além do árbitro e do campeonato. `Joga` liga jogadores a partidas e `Disputa` liga times a partidas.

Os relacionamentos, como estão no modelo:

- 1:1 entre `Pessoa` e cada tabela filha (a herança), entre `Time` e `Estadio` e entre `Tecnico` e `Time`. No banco isso é feito com `UNIQUE` na chave estrangeira.
- 1:N de time para jogadores, de árbitro para partidas e de campeonato para partidas.
- N:M entre jogadores e partidas (tabela `Joga`) e entre times e partidas (tabela `Disputa`).
- Recursivo na tabela `Jogador` (item 3).

Como no modelo as colunas são `NOT NULL` e os ids são `AUTO_INCREMENT`, mantivemos isso. A única exceção é a coluna recursiva, que aceita `NULL` (explicação no item 3).

## 2. Herança

`Pessoa` é a tabela principal, e `Tecnico`, `Arbitro` e `Jogador` herdam dela. Cada tabela filha tem o próprio id (`idTecnico`, `idArbitro`, `IdJogador`) e uma chave estrangeira `Pessoa_idPessoa` para `Pessoa`, marcada como `UNIQUE` para que cada linha filha corresponda a uma única pessoa. Assim os dados comuns ficam num lugar só, e cada filha guarda apenas o que é dela: licença do técnico, curso do árbitro, camisa e posição do jogador.

Nos dados de exemplo, as pessoas 1 a 5 são técnicos, 6 a 10 são árbitros e 11 a 20 são jogadores. A chave estrangeira sozinha não impede que uma pessoa seja técnico e jogador ao mesmo tempo, então criamos triggers para isso (item 5).

## 3. Relacionamento recursivo

No modelo, `Jogador` tem uma coluna `Jogador_IdJogador` que aponta para a própria tabela. Usamos essa coluna para indicar quem o jogador substitui: o reserva aponta para o titular, e o titular fica com `NULL`. Por isso a coluna aceita `NULL` (o titular não substitui ninguém) e é `UNIQUE` (um titular tem um reserva, como o modelo desenha, 1:1).

Nos dados, cada time tem um titular e um reserva, e a consulta que mostra os dois lado a lado está no item 7.

## 4. Comandos DML

DML são os comandos que mexem nos dados, diferente do `CREATE`, que mexe na estrutura. Usamos os três principais num exemplo com um estádio novo.

O `INSERT` cadastra a "Arena do Vale". Foi ele também que usamos para popular todas as tabelas, com pelo menos 5 registros em cada, começando pelas que não dependem de outras por causa das chaves estrangeiras. Na tabela `Jogador` precisamos inserir primeiro os titulares e depois os reservas, porque o reserva aponta para um titular que já precisa existir.

O `UPDATE` muda a capacidade desse estádio de 20 mil para 25 mil, como se ele tivesse sido ampliado. O `WHERE` é obrigatório na prática: sem ele, todos os estádios ficariam com 25 mil.

O `DELETE` apaga o estádio para a tabela voltar a ter só os 5 originais. Só funcionou porque nenhum time usava o estádio 6. Se algum usasse, a chave estrangeira não deixaria apagar.

## 5. View, procedures, functions e triggers

A view `View_Partidas` mostra cada partida com o campeonato, o árbitro e o confronto, por exemplo "Flamengo x Sao Paulo". Para chegar nisso é preciso juntar várias tabelas, e a view evita escrever esses JOINs toda vez que alguém quiser ver as partidas.

A procedure `ConsultarPartidasPorCampeonato` lista as partidas de um campeonato. Como o id do campeonato é um parâmetro, a mesma consulta serve para qualquer campeonato: `CALL ConsultarPartidasPorCampeonato(1);`

A function `CalcularIdade` calcula a idade a partir da data de nascimento. Preferimos guardar a data e não a idade, porque a idade muda e o dado ficaria errado com o tempo. Como a function devolve um valor, dá para usar direto num SELECT:

```sql
SELECT Nome, CalcularIdade(Data_Nascimento) AS Idade FROM Pessoa;
```

Os triggers `ValidarDuracaoPartida_Insert` e `ValidarDuracaoPartida_Update` não deixam salvar uma partida com duração zero ou negativa. A regra fica no banco, então vale para qualquer um que inserir dados. Precisam ser dois porque o MySQL não aceita um trigger só para insert e update. Se tivéssemos feito só o de insert, daria para cadastrar uma partida com 90 minutos e depois trocar para 0.

### Implementações extras

Os triggers `Heranca_Tecnico`, `Heranca_Arbitro` e `Heranca_Jogador` impedem que a mesma pessoa esteja em mais de uma tabela filha. Se alguém tentar cadastrar um técnico também como jogador, o banco dá erro.

A procedure `CadastrarJogador` existe por causa da herança: cadastrar um jogador exige dois inserts, um em `Pessoa` e outro em `Jogador`. Se o segundo falhasse (por exemplo, com um time que não existe), a pessoa ficaria cadastrada sem ser nada. Por isso a procedure faz os dois dentro de uma transação, com `ROLLBACK` se der erro. Os ids novos vêm do `AUTO_INCREMENT`, lidos com `LAST_INSERT_ID()`. Testamos com um time inexistente: o erro aparece e a quantidade de pessoas não muda.

A function `QtdPartidasTime` conta quantas partidas um time disputou, olhando a tabela `Disputa`.

Além dos JOINs pedidos, fizemos consultas com `GROUP BY` e `COUNT`, um self join na tabela `Disputa` (para mostrar os dois times de uma partida na mesma linha) e um self join na tabela `Jogador` (para o relacionamento recursivo).

## 6. Usuários e role

Criamos dois usuários, `usuario1` e `usuario2`, ligados à role `leitura_futebol`. A role é um grupo de permissões. Em vez de dar permissão usuário por usuário, damos para a role e ligamos os usuários a ela, e se o acesso precisar mudar, mexemos só na role.

A `leitura_futebol` pode fazer `SELECT` em todo o banco e executar a procedure `ConsultarPartidasPorCampeonato`. Não pode inserir, alterar nem apagar, e testamos isso: um `DELETE` feito pelo `usuario1` foi negado.

As senhas estão escritas no script só porque é um trabalho de faculdade. Num sistema real elas não ficariam no código.

## 7. Consultas com JOIN

| Relacionamento | O que a consulta mostra |
|---|---|
| 1:1 | Cada jogador com os dados de Pessoa e a idade (`Pessoa` e `Jogador`) |
| 1:1 | Cada técnico com o time que comanda e o estádio do time (`Tecnico`, `Pessoa`, `Time` e `Estadio`) |
| 1:N | Cada time com seus jogadores (`Time`, `Jogador` e `Pessoa`) |
| 1:N | Quantas partidas cada árbitro apitou |
| N:M | O confronto de cada partida, com os dois times na mesma linha (`Disputa` usada duas vezes) |
| N:M | Quantas partidas cada jogador disputou (`Jogador`, `Pessoa` e `Joga`) |
| Recursivo | Cada reserva ao lado do titular que ele substitui (`Jogador` ligada a ela mesma) |

## 8. Como rodar

```bash
mysql -u root -p < futebol.sql
```

O script apaga e recria o banco `futebol` a cada execução. Os testes que dão erro de propósito (triggers e `ROLLBACK`) estão comentados no meio do script, logo depois da criação dos objetos. Para testar, tire o comentário e rode um de cada vez.
