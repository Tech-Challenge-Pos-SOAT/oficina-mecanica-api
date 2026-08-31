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
│   ├── auth
│   ├── customer        # equivalente a "cliente"
│   ├── employee         # equivalente a "usuario/funcionario"
│   ├── material          # equivalente a "peca"
│   ├── materialtransaction
│   ├── service             # equivalente a "servico"
│   ├── serviceorder         # equivalente a "ordemservico" - agregado central (subdominio principal)
│   └── vehicle                # equivalente a "veiculo"
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

## Pipeline de CI (`.github/workflows/ci.yml`)

O CI roda em ordem sequencial (cada etapa so comeca depois que a anterior
termina) em todo PR/push para `main`:

1. **Check** - branch atualizada com a main (so em PR).
2. **Build** - compila e empacota (`mvn clean package -DskipTests`).
3. **Test** - roda os testes e gera cobertura JaCoCo.
4. **Dependency-Check** - OWASP Dependency-Check varre as dependencias do
   projeto (Spring Boot, driver do Postgres, JWT, etc.) contra a base de
   CVEs da NVD.
5. **Trivy** - varre a imagem Docker final (SO + dependencias da aplicacao)
   em busca de vulnerabilidades conhecidas.
6. **SonarQube** - analise de qualidade de codigo, com as vulnerabilidades
   da etapa 4 importadas junto (ver abaixo).

Como o repositorio e privado, os relatorios de vulnerabilidade nao vao para
a aba "Security" do GitHub (isso exigiria GitHub Advanced Security, que e
pago em repo privado) - eles ficam disponiveis como **artifacts do
workflow** (aba Actions > run mais recente > Artifacts):
`dependency-check-report` (HTML/JSON) e `trivy-report` (tabela/JSON). Esses
arquivos sao a base para o "Relatorio de vulnerabilidades" exigido nos
entregaveis da Fase 1.

Para rodar o Dependency-Check localmente:

```bash
mvn org.owasp:dependency-check-maven:12.2.2:check -DnvdApiKey=SEU_TOKEN
# token gratuito em https://nvd.nist.gov/developers/request-an-api-key
```

## Analise de qualidade de codigo (SonarQube)

O time optou por um **SonarQube Community Edition local**, rodando via
Docker Compose na maquina de quem configurou (mesma abordagem ja usada com
GitLab CI). Job "6. SonarQube" no `.github/workflows/ci.yml`:

- Roda em um **runner self-hosted** (registrado na maquina onde o SonarQube
  local esta de pe) - e o que permite o GitHub Actions, que roda na nuvem,
  alcancar um `http://sonarqube.local:9000` que so existe naquela maquina. A
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
  check "SonarQube" no PR e no dashboard local (`http://sonarqube.local:9000`).
- Ainda falha o job se a Quality Gate nao passar
  (`-Dsonar.qualitygate.wait=true`), mesmo sem comentario inline no PR.
- **Vulnerabilidades do Dependency-Check aparecem dentro do proprio
  dashboard do SonarQube**, junto com os problemas de qualidade de codigo.
  Isso NAO usa o plugin da comunidade `dependency-check-sonar-plugin` (sem
  release desde ago/2024, com bugs conhecidos em versoes recentes do
  SonarQube) - em vez disso, o job "4. Dependency-Check" converte o
  relatorio pro formato nativo de importacao de issues externas do
  SonarQube (`sonar.externalIssuesReportPaths`,
  `.github/scripts/dependency_check_to_sonar.py`) e o job "6. SonarQube"
  baixa esse arquivo convertido e passa pro scanner.

Configuracao necessaria (feita uma vez, fora do codigo):

1. Instalar e registrar um runner self-hosted na maquina com o SonarQube
   local (GitHub > repo > Settings > Actions > Runners > New self-hosted
   runner - o proprio GitHub gera o comando de instalacao e o token de
   registro).
2. No SonarQube local (`http://sonarqube.local:9000`), gerar um token em
   *My Account > Security*.
3. Cadastrar em Settings > Secrets and variables > Actions:
   - `SONAR_HOST_URL` (ex.: `http://sonarqube.local:9000`)
   - `SONAR_TOKEN` (o token gerado no passo 2 - usar `sonar.token`, nao
     `sonar.login`, que esta descontinuado)

## SonarQube local para cada dev (opcional)

Alem do SonarQube usado pelo CI (secao acima, fixo na maquina do runner),
qualquer integrante do time pode subir o **proprio** SonarQube Community
Edition localmente, via um perfil opcional do `docker-compose.yml`, para ver
relatorio de qualidade e re-analisar quando quiser, sem depender de ninguem:

```bash
./scripts/sonar-local.sh        # macOS/Linux/WSL - sobe, faz bootstrap e ja analisa
.\scripts\sonar-local.ps1       # Windows (PowerShell nativo)
```

Funciona com Docker Desktop, OrbStack ou Podman, em Windows/Linux/macOS.
Guia completo (memoria minima, troubleshooting por SO/runtime, comandos
`up`/`analyze`/`down`/`reset`): [`docs/sonarqube-local.md`](docs/sonarqube-local.md).

## Documentacao

- Documentacao DDD (Event Storming, Domain Storytelling, Mapa de Contexto,
  Linguagem Ubiqua): ver link do board no Miro no documento de entrega.
- Contexto adicional para desenvolvimento (edital, decisoes de DDD, board do
  Trello): ver `docs/context/`.

## Grupo
- Participantes e usernames no Discord: William Bacelar, João Araújo, Alyson Guimarães, Beatriz e Kawan
