# Sistema de Gerenciamento de Futebol
Projeto desenvolvido para a disciplina de Banco de Dados com o objetivo de criar a modelagem de um sistema de gerenciamento de futebol utilizando o MySQL Workbench

## Integrantes

- Gustavo Henrique dos Santos Giron
- Rafael Mello Barbosa da Silva

## Sobre o projeto
O projeto consiste na modelagem de um banco de dados para um sistema de gerenciamento de futebol, o sistema permite representar informações relacionadas a pessoas, times, estádios e competições. A modelagem possui entidades como Pessoa, Jogador, Técnico, Árbitro, Time, Estádio, Campeonato e Partida.
Também foram utilizados diferentes tipos de relacionamentos entre as entidades, incluindo relacionamentos 1:1, 1:N, N:M, herança e relacionamento recursivo


## Entregas

O repositório reúne as três entregas do projeto:

| Entrega | O que é | Onde está |
|---|---|---|
| 1 | Modelagem do banco no MySQL Workbench | `Projeto_Futebol.mwb` |
| 2 | Script SQL (tabelas, dados, DML, herança, usuários e role, JOINs, view, procedures, functions e triggers) e documentação | `futebol.sql` e `DOCUMENTACAO.md` |
| 3 | Aplicação em Java com menu e CRUD ligada ao banco | pasta `Entrega3/` |

O `futebol.sql` da raiz é o script final do banco e é o mesmo usado pela aplicação (há uma cópia dentro da `Entrega3/` para facilitar rodar o projeto).

## Como rodar a aplicação (Entrega 3)

1. Rode o `futebol.sql` no MySQL para criar o banco `futebol`.
2. Entre na pasta `Entrega3`, coloque o driver do MySQL na pasta `lib` (instruções em `lib/LEIAME.txt`) e crie o `config.properties` a partir do `config.example.properties`.
3. Execute o `executar.bat`.

Os detalhes do menu, do código e dos testes estão em `Entrega3/README.md`.
