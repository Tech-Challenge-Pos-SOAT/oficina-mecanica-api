# CLAUDE.md

Rule do Claude Code neste repositorio. Curta de proposito - o contexto detalhado
esta em `docs/context/`, indexado por **`docs/context/context-index.md`**.

## O projeto

MVP de back-end de oficina mecanica: abrir, orcar, acompanhar e concluir Ordens de
Servico, com cadastro de clientes, veiculos, servicos e materiais.

## Codigo e 100% em ingles

Pasta, arquivo, pacote, classe, metodo, variavel, campo, constante, enum, nome de
teste e path de URL: **ingles, sempre**. Nunca misturar (`salvarCustomer`,
`CustomerRepositorio` sao erros). Comentario no codigo tambem em ingles.

Docs, conversa e corpo de commit: portugues.

**Excecao unica:** o pacote raiz `com.postech.oficinamecanica` permanece em
portugues. Nao renomear.

Os nomes saem do schema em `docs/context/modelo-de-dados.md`. E `Material` (nao
`Part`), `ServiceOrder` (nao `WorkOrder`), `Customer` (nao `Client`).

## Stack (nao trocar sem confirmar)

Java 21 - **Spring Boot 4.0.7** (`spring-boot-starter-webmvc`, Data JPA,
Validation) - PostgreSQL 16 - **Flyway** - springdoc-openapi 3.0.2 - MapStruct
1.6.3 - JUnit 5, Mockito, Testcontainers (Postgres real, **nunca H2**) - JaCoCo
0.8.12, meta 80% - Docker/docker-compose.

**Sem Lombok. Sem Spring Security. Sem JWT.** Foram removidos do `pom.xml`. Nao
reintroduza sem falar com o usuario e nao escreva codigo ou doc que assuma
autenticacao existente.

**Spring Boot 4 nao e 3.x.** Starters foram renomeados (`spring-boot-starter-web`
-> `spring-boot-starter-webmvc`) e as anotacoes de teste mudaram de pacote. Os
starters de teste sao granulares (`spring-boot-starter-data-jpa-test` etc), nao o
`spring-boot-starter-test` monolitico. **Consulte a doc atual via Context7 antes
de escrever import de Spring** - nao va de memoria. Os imports corretos estao em
`docs/context/testes.md`.

**Flyway cria o schema**; `ddl-auto` e `validate`. Mudou entidade? Migration nova
em `src/main/resources/db/migration/`. Nunca edite migration ja aplicada.

## Antes de codar: leia o indice

**`docs/context/context-index.md`** roteia tarefa → arquivo. Abra primeiro:

| Tarefa | Arquivo |
|---|---|
| nome de classe, campo, tabela; escrever migration | `docs/context/modelo-de-dados.md` |
| entidade, agregado, VO, glossario, fluxo de status | `docs/context/dominio-e-linguagem-ubiqua.md` |
| em que pacote a classe vai; controller, use case, repository | `docs/context/arquitetura-ddd.md` |
| escrever ou revisar teste | `docs/context/testes.md` |
| converter DTO <-> dominio <-> JPA | `docs/context/mapstruct.md` |
| anotar endpoint | `docs/context/swagger.md` |
| ferramentas de IA ativas | `docs/context/ferramentas-e-skills.md` |

## Nao decida sozinho - pergunte

- Os **pontos em aberto** de `docs/context/dominio-e-linguagem-ubiqua.md` e as
  perguntas do schema em `docs/context/modelo-de-dados.md`.
- Trocar versao de Java, Spring Boot ou banco.
- Reintroduzir Lombok, Spring Security / JWT; adicionar H2; adicionar starter ou
  dependencia nova; renomear o pacote raiz; mudar a estrutura de camadas.
- Editar migration ja aplicada.
- Anotar JPA direto na classe de dominio (atalho tolerado no MVP, mas so com aval
  do usuario).
- Criar ou alterar cartao no Trello / board do Miro - feito fora do codigo.
- Habilitar upload de SARIF para a aba Security do GitHub (repo privado sem GitHub
  Advanced Security) - relatorios ficam como artifact do workflow.

## Comandos

```bash
./setup-ai.sh                # prepara o ambiente de IA (pergunta a IA e o OS)
mvn spring-boot:run          # sobe a app (Postgres via spring-boot-docker-compose)
docker compose up --build    # app + Postgres
mvn test                     # testes + JaCoCo em target/site/jacoco/index.html
```

## Git / PR (regras do time)

- Branch sempre `feature/nome-da-tarefa`. Nao e verificado pelo CI - so convencao
  do time / Ruleset no GitHub.
- PR para `main` precisa de aprovacao de outro integrante. Quem abre nao aprova
  nem mergeia o proprio PR.
- Conventional commits, corpo em portugues.

### CI (`.github/workflows/ci.yml`) - 6 jobs em cadeia via `needs`

1. **`check`** - "1. Check: branch atualizada com a main". So roda em PR; falha se
   a branch estiver desatualizada em relacao a `main`. Em push direto na main fica
   "skipped". O GitHub Actions propaga "skipped" em **cadeia** por todo o `needs`
   (doc oficial: "a failure or skip applies to all jobs in the dependency chain
   from the point of failure or skip onwards"). Por isso `build`, `test`,
   `dependency-check`, `trivy` e `sonar` tem **todos** individualmente
   `if: ${{ !failure() && !cancelled() }}` (o `sonar` combina isso com a condicao
   de evento que ja tinha). **Job novo nessa cadeia precisa do mesmo guard**,
   senao volta a pular tudo em cascata.
2. **`build`** - so compila/empacota (`mvn clean package -DskipTests`).
3. **`test`** - testes + cobertura JaCoCo, publica artifact `jacoco-report`.
4. **`dependency-check`** - OWASP Dependency-Check contra a NVD. Alem do
   HTML/JSON, converte para o formato de issues externas do SonarQube via
   `.github/scripts/dependency_check_to_sonar.py` e publica o artifact
   `dependency-check-sonar-issues`, consumido pelo job `sonar`. **Nao
   reintroduzir** o `dependency-check-sonar-plugin` da comunidade (sem release
   desde ago/2024, bugs em versoes recentes do SonarQube).
5. **`trivy`** - builda a imagem Docker e escaneia, publica artifact
   `trivy-report`. Repo privado -> **nao** usar `format: sarif` + upload para a
   aba Security (exigiria GitHub Advanced Security pago).
6. **`sonar`** - SonarQube **Community Edition local** (docker-compose na maquina
   de quem configurou), via **runner self-hosted**. Roda em push na main e em PR
   (decisao explicita do usuario). Baixa o artifact
   `dependency-check-sonar-issues` **antes do `mvn clean`**, para
   `external-reports/` (fora de `target/`, que o `clean` apaga), e passa via
   `-Dsonar.externalIssuesReportPaths=...`. Limitacoes da Community Edition:
   - nao suporta `sonar.branch.name` (Developer Edition+) -> **nunca** adicionar
     esse parametro no step do PR;
   - sem branch nativa, cada PR usa projectKey proprio
     (`oficina-mecanica-api-pr-<numero>`, calculado no step "Definir projectKey")
     para nao sobrescrever a analise da main - manter essa logica;
   - sem decoracao automatica no PR (recurso pago) - so o check "SonarQube" e o
     dashboard local;
   - goal chamado de forma totalmente qualificada
     (`org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar`), nao o
     prefixo curto `sonar:sonar`, que so resolve se o `~/.m2/settings.xml` do
     runner tiver o grupo `org.sonarsource.scanner.maven` cadastrado.

   `SONAR_HOST_URL` (`http://sonarqube.local:9000`) e `SONAR_TOKEN` sao secrets do
   repo; usar sempre `sonar.token` (nao `sonar.login`, descontinuado). Se `sonar`
   virar required status check, o time assumiu o risco de PR travar quando essa
   maquina estiver offline.
