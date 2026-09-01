# Oficina Mecanica API

MVP do back-end de um Sistema Integrado de Atendimento e Execucao de Servicos,
modelado com Domain-Driven Design (DDD) em monolito em camadas.

## Motivacao

Uma oficina mecanica de medio porte, especializada em manutencao de veiculos,
tem enfrentado desafios para expandir seus servicos com qualidade e eficiencia.
Atualmente, o processo de atendimento, diagnostico, execucao de servicos e
entrega dos veiculos e feito de forma desorganizada, utilizando anotacoes manuais
e planilhas, o que gera problemas como:

- Erros na priorizacao dos atendimentos;
- Falhas no controle de pecas e insumos;
- Dificuldade em acompanhar o status dos servicos;
- Perda de historico de clientes e veiculos;
- Ineficiencia no fluxo de orcamentos e autorizacoes.

Diante disso, a oficina decidiu investir no sistema, que permitira aos clientes
acompanhar em tempo real o andamento do servico, autorizar reparos adicionais via
aplicativo e garantir uma gestao interna eficiente e segura.

## Stack

- Java 21, Spring Boot 3.5.x (Web, Data JPA, Security, Validation, Actuator)
- PostgreSQL 16 (schema via Flyway, `ddl-auto=validate`)
- JWT para autenticacao das APIs administrativas
- springdoc-openapi (Swagger), MapStruct (mapeamento entre camadas)
- JUnit 5, Mockito, REST-assured e Testcontainers (Postgres real)
- Docker / docker-compose

## Arquitetura

Fluxo: Controller → UseCase → Domain + Repository (porta) → Impl JPA.

```
com.postech.oficinamecanica
├── domain            # entidades, agregados, regras de negocio (sem Spring/JPA)
├── application       # casos de uso + interfaces de repositorio
├── infrastructure    # persistencia JPA, seguranca (JWT), configuracoes
└── interfaces        # controllers REST e DTOs (record)
```

Entidades de dominio: `Customer`, `Vehicle`, `Material`, `Service`, `Employee` e
o agregado central `ServiceOrder` (com `ServiceOrderHistory`) + `MaterialTransaction`.

## Como rodar

Pre-requisitos: Docker, Docker Compose, **Java 21** e **Maven do sistema**
(o repo NAO tem wrapper `mvnw`).

```bash
docker compose up --build        # sobe app + Postgres
mvn spring-boot:run              # app sem Docker, sobe o banco via spring-boot-docker-compose
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Postgres: `localhost:5432` (usuario/senha `oficina`/`oficina`, banco `oficina_mecanica`)

## Testes

```bash
mvn test
```

- **Docker obrigatorio:** testes JPA/HTTP sobem Postgres 16 real via Testcontainers
  (`postgres:16-alpine`); sem Docker eles falham.
- Cobertura minima exigida: 80% em dominios criticos (JaCoCo em
  `../../target/site/jacoco/index.html`).
- Nome dos testes em ingles, padrao `should<Comportamento>When<Condicao>`.

## Testes locais via collection do Insomnia

Para exercitar a API de ponta a ponta (fluxo completo da Ordem de Servico) sem
escrever codigo, use a collection em [`../../docs/insomnia`](docs/insomnia/):

1. **Suba a API** (como acima): `docker compose up --build` ou `mvn spring-boot:run`.
   O Flyway cria o schema e semeia os dados iniciais.
2. **Importe a collection** no Insomnia: *Insomnia > Import > From File*,
   selecione `../../docs/insomnia/oficina-mecanica-api.insomnia.json`. O ambiente
   `Base Environment` ja aponta para `http://localhost:8080`.
3. **Logue** com o funcionario inicial (criado na migration
   `V6__seed_initial_employee.sql`):
   - `POST /auth/login`
   - Email: `carlos.souza@oficina.com`
   - Senha: `senha123`
   - Copie o `token` da resposta para a variavel de ambiente `token` — todas as
     requisicoes `/api/*` usam Bearer com essa variavel.
4. **Siga o roteiro** da collection: pre-requisitos (veiculo, servico) → fluxo da OS
   (abrir, diagnostico, itens, orcamento, aprovacao, conclusao, entrega) →
   acompanhamento do cliente (sem token) → cenarios de erro. Ajuste as variaveis
   `orderId`, `serviceId` e `materialId` conforme avancar.

Detalhes do roteiro, efeitos colaterais por etapa e codigos de erro em
[`../../docs/insomnia/README.md`](docs/insomnia/README.md).

## Endpoints principais

| Recurso | Base | Obs |
|---|---|---|
| Autenticacao | `POST /auth/login` | publica, gera JWT |
| Clientes | `/api/customers` | CRUD + `PATCH /{id}/status`, `GET /document` |
| Veiculos | `/api/vehicles` | CRUD + `PATCH /{id}/status`, `PATCH /{id}/owner` |
| Materiais | `/api/materials` | CRUD + `GET /low-stock`, `PATCH /{id}/status`, `POST /{id}/stock-entry` |
| Movimentacao de estoque | `/api/material-transactions`, `/api/materials/{id}/transactions` | entradas/saidas de estoque |
| Servicos | `/api/services` | CRUD + `PATCH /{id}/status` |
| Ordem de servico | `/api/service-orders` | diagnostico, servicos, materiais, orcamento, conclusao, cancelamento, entrega |
| Funcionarios | `/api/employees` | CRUD + `PATCH /{id}/status` |

Colecao completa de requests (importavel no Insomnia): [`../../docs/insomnia`](docs/insomnia/).
Endpoints estruturados no Swagger ao subir a aplicacao.

## Pipeline de CI (`../../.github/workflows/ci.yml`)

Sequencial em todo PR/push para `main`: **check** (branch atualizada) → **build**
→ **test** (+ JaCoCo) → **dependency-check** (OWASP/NVD) → **trivy** (imagem) →
**sonar**. Relatorios de vulnerabilidade ficam como artifacts do workflow.

Dependency-Check roda fora do ciclo Maven, de proposito; local:

```bash
mvn org.owasp:dependency-check-maven:12.2.2:check -DnvdApiKey=SEU_TOKEN
```

SonarQube: Community Edition local, job roda em runner self-hosted
(`http://sonarqube.local:9000`). Mais detalhes em `../../docs/sonarqube-local.md`.

## Arquitetura DDD e convencoes

Codigo, identificadores e commits em ingles; docs e conversa em portugues.
Nomes de dominio saem de `../../docs/contexts/modelo-de-dados.md`.

- Banco: schema so via Flyway; **mudou entidade JPA → nova migration `V<N>__desc.sql`**,
  nunca editar migration ja aplicada.
- Mapeamento entre camadas via MapStruct (`@Mapper(componentModel="spring",
  unmappedTargetPolicy=ReportingPolicy.ERROR)`); DTOs sao `record`.
- Dominio sem dependencia de framework; mocks so em portas (repository), nunca em domain.

## Documentacao

- Indice "tarefa → doc": [`../../docs/contexts/context-index.md`](docs/contexts/context-index.md)
- Modelo de dados: [`../../docs/contexts/modelo-de-dados.md`](docs/contexts/modelo-de-dados.md)
- Testes automatizados: [`../../docs/contexts/testes-automatizados.md`](docs/contexts/testes-automatizados.md)
- SonarQube local: [`../../docs/sonarqube-local.md`](docs/sonarqube-local.md)
- Requests (Insomnia): [`../../docs/insomnia`](docs/insomnia/README.md)

## Grupo

- Willian (Bacelar): rm375919
- Alyson (Guimaraes): rm375812
- Joao (Araujo): rm376123
- Beatriz: rm376242
- Kawan: rm376144
