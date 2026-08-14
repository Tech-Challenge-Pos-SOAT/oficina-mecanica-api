# CLAUDE.md

Regras mínimas. Contexto detalhado em `docs/context/`, indexado por `docs/context/context-index.md`.

## O projeto

MVP de back-end de oficina mecanica: abrir, orcar, acompanhar e concluir Ordens de
Servico, com cadastro de clientes, veiculos, servicos e materiais.

## Código e linguagem

**Código: inglês sempre** (classe, método, variável, teste, URL). Docs/commit/conversa: português.

Exceção: pacote raiz `com.postech.oficinamecanica` fica em português.

Nomes: ver `docs/context/modelo-de-dados.md`.

## Princípios de código

DDD, DRY, KISS, CLEAN code, SOLID. Detalhes: `docs/context/principios-de-codigo.md`.

## Stack (não trocar sem confirmar)

Java 21 - Spring Boot 4.0.7 - PostgreSQL 16 - Flyway - springdoc-openapi 3.0.2 - MapStruct 1.6.3 - JUnit 5, Mockito, Testcontainers - JaCoCo (meta 80%) - Docker/compose.

**Proibido:** Lombok, Spring Security, JWT, H2. Não reintroduzir sem falar com o usuário.

Detalhes: `docs/context/testes.md` (imports Spring 4), `docs/context/modelo-de-dados.md` (migrations).

## Antes de codar

Abra `docs/context/context-index.md` (roteia tarefa → arquivo específico com detalhes).

## Commits e Push

**Nunca commit/push sem permissão do usuário.** Confirmar antes de stage, commit, push, ou operações destrutivas.

Exceção: arquivos de scratch criados nesta sessão podem ser deletados sem avisar.

## Comandos

```bash
./setup-ai.sh                # prepara o ambiente de IA (pergunta a IA e o OS)
mvn spring-boot:run          # sobe a app (Postgres via spring-boot-docker-compose)
docker compose up --build    # app + Postgres
mvn test                     # testes + JaCoCo em target/site/jacoco/index.html
```

## Git / PR

Branch: `feature/nome-da-tarefa`. PR para `main` precisa aprovação de outro. Conventional commits, corpo em português.

Detalhes: `docs/context/git-workflow.md` (branch, commit, PR, CI/CD pipeline).

## Não decida sozinho — pergunte

Mudar versão Java/Spring/banco. Reintroduzir proibidos ou adicionar dependência nova. Editar migration já aplicada. Anotar JPA direto na entidade (só com aval). Pontos abertos em `docs/context/dominio-e-linguagem-ubiqua.md`.
