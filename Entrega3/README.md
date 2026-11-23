# Entrega 3: aplicação em Java com o banco de futebol

Esta é a última etapa do projeto: um programa em Java, com menu no terminal, que conversa com o banco `futebol` das entregas anteriores usando JDBC. Todas as 10 tabelas podem ser gerenciadas pelo menu.

## Como rodar

Precisa de: JDK 17 ou mais novo, MySQL 8 ou mais novo e o driver do MySQL.

1. Rode o `futebol.sql` no MySQL. Ele recria o banco `futebol` do zero, com os dados de exemplo.
2. Baixe o driver `mysql-connector-j-9.3.0.jar` (Maven Central: `repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.3.0/`) e coloque na pasta `lib`.
3. Copie o `config.example.properties` para `config.properties` e coloque o seu usuário e senha do MySQL. Se esse arquivo não existir, o programa pergunta usuário e senha ao iniciar.
4. Dê dois cliques no `executar.bat` (ou rode `executar.bat` pelo terminal, dentro desta pasta). Ele compila e abre o menu.

Sem o `.bat`, os comandos são:

```bash
javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d out src/futebol/Main.java
java -cp "out;lib/*" futebol.Main
```

No Linux ou Mac, troque o `;` por `:` no classpath.

## O que o menu tem

```
Menu principal
 1) Gerenciar tabelas   -> escolhe uma das 10 tabelas
                           1) Listar todos        (busca geral)
                           2) Buscar com filtro   (busca por atributos)
                           3) Inserir
                           4) Atualizar           (com ou sem filtro)
                           5) Remover             (sempre com filtro)
 2) Consultas com JOIN  -> 7 consultas prontas
 3) View, procedures e functions do banco
 0) Sair
```

## Como cada requisito foi atendido

**Todas as entidades gerenciáveis.** As 10 tabelas (`Pessoa`, `Estadio`, `Time`, `Tecnico`, `Arbitro`, `Jogador`, `Campeonato`, `Partida`, `Joga` e `Disputa`) aparecem no menu "Gerenciar tabelas".

**Busca geral e busca por filtro, em cada entidade.** "Listar todos" mostra todos os registros. "Buscar com filtro" deixa escolher um ou mais campos e o valor de cada um. Campos de texto usam `LIKE` (acha quem contém o texto) e campos de número ou data usam igualdade. Vários filtros se combinam com `AND`.

**Atualização com ou sem filtro.** O usuário escolhe quais campos mudar e depois, se quiser, o filtro. Antes de executar, o programa mostra quantos registros serão alterados e pede confirmação. Sem filtro, o aviso fala em "TODOS os registros".

**Remoção sempre com filtro.** Não existe a opção de apagar sem filtro: se o usuário desistir de escolher um campo, a remoção é cancelada. O programa mostra os registros que serão apagados e pede confirmação.

**Três JOINs, um de cada tipo de relacionamento.** No menu "Consultas com JOIN":

| Tipo | Consulta |
|---|---|
| 1:1 | Jogadores com os dados de `Pessoa` (herança) e técnicos com time e estádio |
| 1:N | Cada time com seus jogadores e árbitros com a quantidade de partidas apitadas |
| N:M | Confronto de cada partida (`Disputa`) e partidas disputadas por jogador (`Joga`) |

Há também uma consulta extra com o relacionamento recursivo de `Jogador` (reserva ao lado do titular).

**Integração com o que foi feito na Entrega 2.** O menu 3 usa a view `View_Partidas`, as procedures `ConsultarPartidasPorCampeonato` e `CadastrarJogador` (via `CallableStatement`) e as functions `CalcularIdade` e `QtdPartidasTime`. As regras dos triggers (duração da partida e herança) também valem pelo programa: se o usuário violar uma, a mensagem do banco aparece na tela.

## Como o código está organizado

O código está dividido em pacotes, cada um com uma responsabilidade:

```
src/futebol/
  Main.java                   só chama a aplicação (14 linhas)
  app/
    Aplicacao.java            liga as peças: configuração, conexão e menu principal
  modelo/                     os dados do programa, sem SQL nem tela
    Entidades.java            descrição das 10 tabelas (colunas, tipos, se aceita nulo)
    Entidade.java  Coluna.java  Condicao.java
  banco/                      tudo que fala com o MySQL
    ConfiguracaoBanco.java    lê o config.properties
    Conexao.java              abre a conexão e executa os comandos (com parâmetros)
    ResultadoTabela.java      o resultado de um SELECT já copiado para a memória
    TradutorErros.java        transforma erros do MySQL em mensagens simples
  repositorio/                o CRUD no banco, sem falar com o usuário
    ConstrutorSql.java        monta o texto dos comandos SQL
    RepositorioEntidade.java  listar, buscar, inserir, atualizar e remover
  consulta/                   consultas prontas
    ConsultasJoin.java        os JOINs      ObjetosBanco.java   view, procedures e functions
  ui/                         tudo que aparece na tela e lê o teclado
    Terminal.java             ler e validar o que o usuário digita, escrever na tela
    ImpressoraTabela.java     desenha a tabela com as colunas alinhadas
    Menu.java                 menu numerado genérico, usado por todos os menus
    MenuPrincipal.java  TelaTabelas.java  TelaCrud.java  TelaConsultas.java
```

A ideia principal é que cada tabela é descrita uma vez em `Entidades.java`, e o `RepositorioEntidade` monta o SQL a partir dessa descrição. Por isso o mesmo código de inserir, atualizar e remover serve para as 10 tabelas. Para o banco ganhar uma tabela nova, basta acrescentar uma descrição lá.

## Boas práticas aplicadas

- **Uma responsabilidade por classe.** Quem lê o teclado (`ui`) não monta SQL, e quem monta SQL (`repositorio`) não escreve na tela. O banco devolve um `ResultadoTabela` e a tela decide como mostrar.
- **Sem código repetido.** Os menus são todos instâncias da mesma classe `Menu`, e o CRUD é um só para as 10 tabelas.
- **Sem estado global.** A conexão é um objeto que é passado para quem precisa dela (injeção de dependência pelo construtor), e não uma variável estática.
- **Métodos curtos, com um nível de detalhe cada um.** Por exemplo, `atualizar()` só conta a história (ler alterações, ler filtro, confirmar, executar) e deixa os detalhes para métodos com nomes claros.
- **Recursos fechados com `try-with-resources`.** Conexão, comandos e resultados sempre são fechados, mesmo quando dá erro.
- **Valores fixos com nome.** Os códigos de erro do MySQL e os textos repetidos viraram constantes.
- **Nomes e convenções Java.** Pacotes em minúsculas, classes em `PascalCase`, métodos e variáveis em `camelCase`, e nomes que dizem o que a coisa é.
- **Sem retornar `null` para "cancelado".** Onde o resultado pode não existir, o código usa `Optional`.

Dois cuidados que valem mencionar na apresentação:

- Todos os comandos usam parâmetros (`?`). O que o usuário digita nunca é colado dentro do SQL, o que evita SQL injection.
- Os erros do MySQL são traduzidos: chave estrangeira (não dá para apagar um estádio que tem times), valor duplicado, id inexistente e mensagens dos triggers aparecem em português simples, sem despejar o erro técnico.

## Testes feitos

Rodamos o programa contra um MySQL com o `futebol.sql` carregado, simulando o uso do menu:

- As 7 consultas com JOIN, a view, as procedures e as functions.
- Inserir, buscar, atualizar e remover registros (com número e data inválidos, que são recusados e perguntados de novo).
- Atualizar sem filtro (pede confirmação, e cancelando nada muda) e remover sem filtro (cancelado).
- Remover um estádio que tem times (erro de chave estrangeira) e inserir com id inexistente.
- Os triggers pelo programa: técnico que já é jogador e partida com duração zero.
- Campo opcional vazio (`Jogador_IdJogador`) virando `NULL` no insert e no update.
