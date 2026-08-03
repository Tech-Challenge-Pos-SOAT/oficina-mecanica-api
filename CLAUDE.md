# CLAUDE.md

Contexto para o Claude Code trabalhar neste repositorio. Leia tambem os
arquivos em `docs/context/` quando precisar de mais detalhe - este arquivo e
o resumo que fica sempre carregado.

## O que e o projeto

MVP de back-end para uma oficina mecanica (Tech Challenge Fase 1,
POSTECH/FIAP). Objetivo: sistema para abrir, orcar, acompanhar e concluir
Ordens de Servico (OS), com cadastro de clientes/veiculos/servicos/pecas e
autenticacao JWT para as rotas administrativas. Requisitos completos do
edital em `docs/context/00-edital-tech-challenge.md`.

## Stack e versoes (nao trocar sem confirmar com o usuario)

- Java 21
- Spring Boot 3.5.16 (Web, Data JPA, Security, Validation, Actuator)
- PostgreSQL (driver `org.postgresql:postgresql`; justificativa completa em
  `docs/context/03-decisao-banco-de-dados.md`)
- Flyway/Liquibase: ainda NAO configurado - se for adicionar migrations,
  perguntar antes de escolher entre os dois.
- springdoc-openapi 2.8.5 (Swagger UI)
- JWT via `io.jsonwebtoken` (jjwt-api/impl/jackson 0.12.6) - NAO usar
  Spring OAuth2 Resource Server, foi decisao explicita manter simples.
- Testes: JUnit 5, Mockito, REST-assured, Testcontainers (modulo
  `postgresql`) para testes de integracao com banco real (nao usar H2 como
  banco de dev/teste principal).
- JaCoCo 0.8.12 - meta de cobertura: 80% nos dominios criticos.
- Docker / docker-compose para rodar local (`db` + `app`).

## Arquitetura (DDD em camadas - respeitar a direcao de dependencia)

```
com.postech.oficinamecanica
├── domain            # entidades, agregados, VOs, regras de negocio - SEM dependencia de Spring/framework
│   ├── cliente
│   ├── veiculo
│   ├── servico
│   ├── peca
│   └── ordemservico   # agregado central / subdominio Core
├── application        # casos de uso, orquestracao (pode depender de domain)
├── infrastructure      # persistencia JPA, security (JWT), config (depende de domain + application)
└── interfaces.rest      # controllers REST, DTOs (depende de application)
```

Regra de dependencia: `domain` nao importa nada de `infrastructure` nem de
frameworks (sem `@Entity`/`@Component` direto nas classes de dominio "puras"
se der para evitar - preferir separar modelo de dominio de entidade JPA
quando o caso de uso justificar; para o MVP do Tech Challenge e aceitavel
comecar com JPA anotado direto no domain se o tempo apertar, mas perguntar
antes de tomar esse atalho).

## Bounded contexts e subdominios

- **Core (maior esforco de design):** Ordem de Servico - abertura e
  acompanhamento.
- **Suporte:** Cliente, Veiculo, Peca/Estoque, Servico (CRUDs, mais simples).
- **Generico:** autenticacao (JWT).

Contexto de Ordem de Servico e Cliente do contexto de Gestao de Pecas
(padrao Cliente-Fornecedor / Anticorrupcao se necessario).

## Linguagem Ubiqua - usar esses nomes no codigo (classes, metodos, variaveis)

- **OrdemServico (OS)**: agregado central, tem status proprio.
- **Orcamento**: gerado a partir de servicos + pecas selecionados; precisa
  de aprovacao do cliente antes da execucao.
- **Diagnostico**: etapa do mecanico apos receber o veiculo, define o que
  entra no orcamento.
- **ReservaPeca** / **BaixaEstoque**: dois momentos distintos (reservar !=
  debitar definitivamente do estoque).
- **ReparoAdicional**: necessidade identificada durante a execucao, gera
  novo ciclo de orcamento sem reiniciar a OS.

Status da OS (fluxo linear): `RECEBIDA -> EM_DIAGNOSTICO ->
AGUARDANDO_APROVACAO -> EM_EXECUCAO -> FINALIZADA -> ENTREGUE`.

## ATENCAO - 3 pontos ainda em aberto (nao decidir sozinho, perguntar)

Estes pontos ainda nao foram fechados pelo time (ver
`docs/context/01-ddd-decisoes.md`). Se for implementar o agregado
`OrdemServico` e esbarrar em um deles, PARE e pergunte ao usuario em vez de
assumir uma regra:

1. Reserva de peca acontece antes ou depois da aprovacao do orcamento?
   (Recomendacao registrada, mas ainda nao confirmada: so reservar/baixar
   apos aprovacao.)
2. O que fazer quando o mecanico descobre que uma peca nao existe no
   catalogo durante o diagnostico?
3. Como diferenciar, no codigo/estado da OS, "recusa total do orcamento
   inicial" (encerra a OS) de "recusa parcial de reparo adicional" (volta
   para em execucao)?

## Validacoes obrigatorias (edital)

- CPF/CNPJ do cliente (formato + digito verificador).
- Placa de veiculo (formato Mercosul e/ou antigo).
- Tratar como Value Objects no domain sempre que fizer sentido, com
  validacao no construtor.

## Seguranca

JWT obrigatorio para as APIs administrativas (CRUDs). Endpoints de consulta
publica de status da OS (se existirem) devem ser definidos explicitamente -
perguntar se nao estiver claro no card do Trello/edital.

## Testes

- Cobertura minima: 80% (JaCoCo, `target/site/jacoco/index.html` apos
  `mvn test`).
- Regras de dominio (VOs, invariantes da OS) -> testes unitarios puros, sem
  Spring context.
- Persistencia/integracao -> Testcontainers com Postgres real, nao H2.
- Seguranca (JWT) -> `spring-security-test`.

## Comandos uteis

```bash
mvn spring-boot:run          # roda a app (sobe Postgres via spring-boot-docker-compose se disponivel)
docker compose up --build     # roda app + Postgres via Docker
mvn test                      # roda testes + gera relatorio JaCoCo
```

## Git / PR (regras do time)

- Branch sempre `feature/nome-da-tarefa`. Isso NAO e verificado pelo
  workflow do CI (so por convencao do time / eventual Ruleset no GitHub).
- PR para `main` precisa de aprovacao de outro integrante do time.
- Quem abre o PR nao pode aprovar/mergear o proprio PR (o GitHub ja bloqueia
  isso por padrao).
- CI (`.github/workflows/ci.yml`) tem 4 jobs:
  1. `check-branch-atualizada` - so roda em PR, falha se a branch estiver
     desatualizada em relacao a `main` (equivalente ao "Require branches to
     be up to date before merging" da branch protection do GitHub).
  2. `build` - Check -> Build -> Test (roda em runner do GitHub, sempre
     disponivel).
  3. `security-scan` - roda OWASP Dependency-Check (dependencias) e Trivy
     (imagem Docker), publica os relatorios como artifacts do workflow
     (repo e privado, entao nao usamos a aba Security do GitHub - isso
     exigiria GitHub Advanced Security pago). Esses artifacts sao a base do
     "Relatorio de vulnerabilidades" exigido nos entregaveis da Fase 1.
  4. `sonar` - SonarQube **Community Edition local** (docker-compose na
     maquina de quem configurou), acessado via **runner self-hosted**. Por
     decisao explicita do usuario, roda tanto em push na main quanto em
     Pull Request (a maquina do Sonar fica sempre ligada). Duas limitacoes
     reais da CE que continuam valendo:
     - Nao suporta `sonar.branch.name` (Developer Edition+ apenas) -> por
       isso NUNCA adicionar esse parametro no step do PR.
     - Sem branch nativa, cada PR usa projectKey proprio
       (`oficina-mecanica-api-pr-<numero>`, calculado no step
       "Definir projectKey") para nao sobrescrever a analise da main -
       manter essa logica se for mexer nesse job.
     - Sem comentario/decoracao automatica no PR (recurso pago) - so o
       status do check "Sonar" e o dashboard local mostram o resultado.
     `SONAR_HOST_URL` e `SONAR_TOKEN` sao secrets do repo; usar sempre
     `sonar.token` (nao `sonar.login`, descontinuado). Se quiser marcar
     `sonar` como required status check na branch protection, o time
     assumiu o risco de PR travar quando essa maquina especifica estiver
     offline - avisar se isso mudar.

## O que NAO fazer sem perguntar

- Nao trocar versao de Java, Spring Boot ou banco de dados.
- Nao decidir sozinho os 3 pontos em aberto do fluxo de OS acima.
- Nao adicionar Flyway/Liquibase, trocar estrategia de JWT, ou mudar a
  estrutura de pacotes sem avisar antes.
- Nao criar/alterar cartoes no Trello ou no board do Miro - isso e feito
  fora do codigo, pelo usuario.
- Nao habilitar upload de SARIF para a aba Security do GitHub (repo privado
  sem GitHub Advanced Security) - manter os relatorios de seguranca como
  artifacts do workflow.

## Documentos de referencia completos

- `docs/context/00-edital-tech-challenge.md` - requisitos completos do
  Tech Challenge.
- `docs/context/01-ddd-decisoes.md` - decisoes de DDD do board do Miro,
  incluindo os 3 pontos em aberto.
- `docs/context/02-trello-board.md` - estado do board do Trello (snapshot).
- `docs/context/03-decisao-banco-de-dados.md` - justificativa completa da
  escolha do PostgreSQL (ADR).
