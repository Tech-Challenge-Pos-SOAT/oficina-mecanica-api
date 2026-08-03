# Decisao tecnica: banco de dados

Status: **Definido** - PostgreSQL.

## Contexto

O edital (item "Requisitos tecnicos") deixa o banco de dados livre, mas exige
que a escolha seja justificada. O dominio da Oficina Mecanica tem os
seguintes tracos relevantes para essa decisao:

- Relacionamentos fortemente estruturados: Cliente 1-N Veiculo, Ordem de
  Servico N-N Servico, Ordem de Servico N-N Peca (com quantidade e valor no
  momento do orcamento).
- A OS e o agregado central (subdominio Core) e passa por um fluxo de
  status bem definido (Recebida -> Em diagnostico -> Aguardando aprovacao ->
  Em execucao -> Finalizada -> Entregue), com regras de transicao que
  precisam de consistencia forte.
- Reserva/baixa de estoque de pecas e uma operacao sensivel a concorrencia:
  duas OS não podem reservar a mesma peca alem do saldo disponivel.
- Validacoes de CPF/CNPJ e placa de veiculo, que se beneficiam de constraints
  de unicidade e formato no proprio banco, como camada extra alem da
  validacao na aplicacao.
- Metricas de tempo de execucao do servico (para relatorios/entrega) pedem
  timestamps com timezone confiaveis.

## Decisao

Usar **PostgreSQL** como banco de dados relacional principal do projeto,
acessado via Spring Data JPA / Hibernate.

## Justificativa

1. **Aderencia ao dominio relacional**: as relacoes Cliente-Veiculo,
   OS-Servico e OS-Peca sao naturalmente modeladas como tabelas
   relacionadas com chaves estrangeiras; um banco relacional evita
   duplicacao de dados e mantem integridade referencial nativamente.
2. **ACID e concorrencia no controle de estoque**: o PostgreSQL oferece
   transacoes ACID completas e locking a nivel de linha (`SELECT ... FOR
   UPDATE`), essencial para reservar/baixar peca do estoque sem condicoes de
   corrida quando duas OS concorrem pela mesma peca.
3. **Camada extra de validacao via constraints**: `CHECK` e `UNIQUE`
   constraints (CPF/CNPJ, placa) complementam a validacao feita na camada de
   aplicacao/dominio, reduzindo o risco de dado invalido persistido.
4. **Ecossistema Spring Data JPA maduro**: o driver e o dialect do
   PostgreSQL para Hibernate sao amplamente usados e bem documentados,
   reduzindo atrito de configuracao.
5. **Testcontainers**: existe modulo oficial
   (`org.testcontainers:postgresql`) que sobe um Postgres real em container
   para os testes de integracao, evitando divergencias de comportamento que
   um banco em memoria (H2) poderia esconder (tipos, constraints,
   funcoes) - importante para bater a meta de 80% de cobertura do edital
   com testes confiaveis.
6. **Gratuito e open-source**: sem custo de licenciamento, compativel com um
   projeto academico e com qualquer ambiente de deploy (Docker, nuvem
   gratuita, etc.).
7. **Suporte nativo a `TIMESTAMPTZ`**: util para registrar com precisao os
   horarios de cada mudanca de status da OS e calcular metricas de tempo de
   execucao para o relatorio final.

## Alternativas consideradas

- **MySQL/MariaDB**: tambem relacional e valido, mas foi preterido porque o
  PostgreSQL tem locking e suporte a tipos/constraints mais robusto para o
  cenario de concorrencia no estoque, e a equipe tem mais familiaridade com
  Postgres + Testcontainers.
- **MongoDB (NoSQL)**: descartado porque o dominio e fortemente relacional
  (varias entidades ligadas por chave estrangeira e regras de integridade
  entre elas) e o projeto nao tem necessidade de schema flexivel nem de
  escala horizontal massiva - usar um banco de documentos aqui adicionaria
  complexidade sem beneficio real, alem de dificultar `JOIN`s e constraints
  que o edital pratica indiretamente ao pedir validacoes de CPF/CNPJ/placa.
- **H2 (em memoria)**: mantido apenas como opcao para testes rapidos
  isolados, nunca como banco de desenvolvimento/producao - o projeto usa
  Testcontainers com Postgres real para os testes de integracao justamente
  para evitar as diferencas de comportamento do H2 em relacao ao Postgres.

## Consequencias

- `pom.xml` ja inclui o driver `org.postgresql:postgresql` e
  `org.testcontainers:postgresql`.
- `application.yml` e `docker-compose.yml` ja apontam para um servico
  `db` Postgres.
- Versao da imagem em uso no `docker-compose.yml`: `postgres:16-alpine`
  (versao LTS estavel). O PostgreSQL 18 ja e a versao major mais recente
  disponivel (confirmado via postgresql.org/endoflife.date); a atualizacao
  da imagem para `postgres:18-alpine` pode ser feita depois, e nao e
  bloqueante para iniciar o desenvolvimento.
- Fica registrado aqui como referencia para o dia em que a equipe for
  preencher a documentacao de arquitetura formal (ADR) exigida pela
  disciplina de Documentacao de Arquitetura de Solucoes.
