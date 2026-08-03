# Oficina Mecanica API

MVP do back-end de um Sistema Integrado de Atendimento e Execucao de Servicos
para uma oficina mecanica de medio porte, desenvolvido para o **Tech Challenge -
Fase 1** (Pos-Graduacao em Arquitetura de Software, POSTECH/FIAP).

Aplica Domain-Driven Design (DDD) na modelagem, com boas praticas de Qualidade
de Software e Seguranca.

## Stack

- Java 21
- Spring Boot 3.5.x (Web, Data JPA, Security, Validation, Actuator)
- PostgreSQL 16
- JWT (io.jsonwebtoken) para autenticacao das APIs administrativas
- springdoc-openapi (Swagger) para documentacao das APIs
- JUnit 5, Mockito, REST-assured e Testcontainers para testes
- Docker / docker-compose

## Justificativa do banco de dados

**Decisao: PostgreSQL** (relacional), acessado via Spring Data JPA.

Motivos principais:

- Dominio fortemente relacional: Cliente 1-N Veiculo, OS N-N Servico, OS N-N
  Peca (com quantidade/valor no orcamento).
- ACID + locking a nivel de linha (`SELECT ... FOR UPDATE`), necessario para
  reservar/baixar estoque de peca sem condicao de corrida quando mais de uma
  OS concorre pela mesma peca.
- Constraints (`CHECK`/`UNIQUE`) como camada extra de validacao para
  CPF/CNPJ e placa, alem da validacao na aplicacao.
- Ecossistema maduro com Spring Data JPA e com Testcontainers
  (`org.testcontainers:postgresql`) para testes de integracao com banco real,
  evitando divergencias de comportamento do H2 em memoria.
- Gratuito, open-source, sem custo de licenciamento.
- Suporte nativo a `TIMESTAMPTZ`, util para metricas de tempo de execucao da
  OS no relatorio final.

Alternativas consideradas e descartadas: MySQL (Postgres tem locking/tipos
mais robustos para o cenario de concorrencia no estoque), MongoDB (dominio
muito relacional para justificar um banco de documentos), H2 (mantido so
para testes isolados, nunca como banco de desenvolvimento/producao).

Justificativa completa e alternativas detalhadas em
[`docs/context/03-decisao-banco-de-dados.md`](docs/context/03-decisao-banco-de-dados.md).

## Arquitetura

Monolito organizado em camadas, seguindo os principios taticos de DDD:

```
com.postech.oficinamecanica
├── domain            # entidades, agregados, regras de negocio (sem dependencia de framework)
│   ├── cliente
│   ├── veiculo
│   ├── servico
│   ├── peca
│   └── ordemservico   # agregado central (subdominio principal)
├── application        # casos de uso / orquestracao
├── infrastructure      # persistencia JPA, seguranca (JWT), configuracoes
└── interfaces           # controllers REST, DTOs
```

## Como rodar localmente

Pre-requisitos: Docker e Docker Compose.

```bash
docker compose up --build
```

A API sobe em `http://localhost:8080`, o Swagger UI em
`http://localhost:8080/swagger-ui.html`, e o Postgres em `localhost:5432`
(usuario/senha: `oficina`/`oficina`, banco `oficina_mecanica`).

Para rodar sem Docker (usando um Postgres local ou via `spring-boot-docker-compose`,
que ja sobe o banco automaticamente em modo dev):

```bash
mvn spring-boot:run
```

## Testes

```bash
mvn test
```

Cobertura minima exigida pelo edital: 80% nos dominios criticos (relatorio
JaCoCo gerado em `target/site/jacoco/index.html` apos `mvn test`).

## Analise de vulnerabilidades

O CI (`.github/workflows/ci.yml`, job `security-scan`) roda automaticamente
em todo PR/push para `main`:

- **OWASP Dependency-Check** (plugin Maven `org.owasp:dependency-check-maven`)
  - varre as dependencias do projeto (Spring Boot, driver do Postgres, JWT,
    etc.) contra a base de CVEs da NVD.
- **Trivy** - varre a imagem Docker final (SO + dependencias da aplicacao)
  em busca de vulnerabilidades conhecidas.

Como o repositorio e privado, os relatorios nao vao para a aba "Security" do
GitHub (isso exigiria GitHub Advanced Security, que e pago em repo privado) -
eles ficam disponiveis como **artifacts do workflow** (aba Actions > run mais
recente > Artifacts): `dependency-check-report` (HTML/JSON) e `trivy-report`
(tabela/JSON). Esses arquivos sao a base para o "Relatorio de vulnerabilidades"
exigido nos entregaveis da Fase 1.

Para rodar localmente:

```bash
mvn org.owasp:dependency-check-maven:12.2.2:check -DnvdApiKey=SEU_TOKEN
# token gratuito em https://nvd.nist.gov/developers/request-an-api-key
```

## Analise de qualidade de codigo (SonarQube)

O time optou por um **SonarQube Community Edition local**, rodando via
Docker Compose na maquina de quem configurou (mesma abordagem ja usada com
GitLab CI). Job `sonar` no `.github/workflows/ci.yml`:

- Roda em um **runner self-hosted** (registrado na maquina onde o SonarQube
  local esta de pe) - e o que permite o GitHub Actions, que roda na nuvem,
  alcancar um `http://localhost:9000` que so existe naquela maquina. A
  maquina precisa ficar ligada (com o runner e o SonarQube ativos) sempre
  que alguem for abrir/atualizar um PR ou dar merge na main.
- Roda **tanto em push na main quanto em Pull Request**. Duas limitacoes
  reais da Community Edition que isso nao remove:
  1. CE nao suporta `sonar.branch.name` (erro "Developer Edition or above
     is required") - nao existe conceito nativo de branch dentro de um
     projeto.
  2. Por isso, cada PR usa um **projectKey proprio**
     (`oficina-mecanica-api-pr-<numero>`) em vez do projectKey da main -
     senao cada scan de PR sobrescreveria a analise da main. Na pratica,
     cada PR vira um "projeto" separado no dashboard local do SonarQube,
     cada um com sua propria Quality Gate.
- Nao ha comentario/decoracao automatica no PR do GitHub (isso e recurso
  pago, Developer Edition+) - o resultado aparece como sucesso/falha do
  check "Sonar" no PR e no dashboard local (`http://localhost:9000`).
- Ainda falha o job se a Quality Gate nao passar
  (`-Dsonar.qualitygate.wait=true`), mesmo sem comentario inline no PR.

Configuracao necessaria (feita uma vez, fora do codigo):

1. Instalar e registrar um runner self-hosted na maquina com o SonarQube
   local (GitHub > repo > Settings > Actions > Runners > New self-hosted
   runner - o proprio GitHub gera o comando de instalacao e o token de
   registro).
2. No SonarQube local (`http://localhost:9000`), gerar um token em
   *My Account > Security*.
3. Cadastrar em Settings > Secrets and variables > Actions:
   - `SONAR_HOST_URL` (ex.: `http://localhost:9000`)
   - `SONAR_TOKEN` (o token gerado no passo 2 - usar `sonar.token`, nao
     `sonar.login`, que esta descontinuado)

## Documentacao

- Documentacao DDD (Event Storming, Domain Storytelling, Mapa de Contexto,
  Linguagem Ubiqua): ver link do board no Miro no documento de entrega.
- Contexto adicional para desenvolvimento (edital, decisoes de DDD, board do
  Trello): ver `docs/context/`.

## Grupo

- Nome do grupo: _preencher_
- Participantes e usernames no Discord: _preencher_
