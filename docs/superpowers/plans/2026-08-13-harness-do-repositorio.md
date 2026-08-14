# Harness do Repositorio - Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transformar `docs/context/` na fonte unica de "como escrever codigo neste repositorio" - dominio, schema, DDD, testes, MapStruct, Swagger - e fazer o `setup-ai.sh` gerar a rule especifica de Claude ou Cursor a partir dela.

**Architecture:** Conteudo mora **uma vez** em `docs/context/*.md` (commitado). `docs/context/README.md` e o **dicionario de contextos** - a tabela "quando ler o que". As rules de IA (`CLAUDE.md` na raiz para Claude, `.cursor/rules/*.mdc` para Cursor) sao ponteiros finos para esse dicionario, nunca copias do conteudo. `setup-ai.sh` verifica pre-requisitos, instala as skills e gera a rule da IA escolhida; nao gera docs.

**Tech Stack:** Markdown, Bash (macOS hoje, com hook de OS para o futuro), Maven, **Spring Boot 4.0.7**, Flyway, Testcontainers, MapStruct 1.6.3.

**Spec:** Nao houve doc de spec separado - os requisitos vieram do pedido do usuario, transcritos em "Requisitos de origem". As decisoes tomadas em conversa estao em "Global Constraints". O schema SQL fornecido pelo usuario e a fonte de verdade dos nomes.

---

## Estado real do repositorio (verificado antes de escrever)

O `pom.xml` foi reescrito durante a conversa. **Varias remocoes que o plano
previa ja aconteceram.** O que segue e o estado conferido, nao suposicao:

| Item | Estado |
|---|---|
| Spring Boot | **4.0.7** (nao 3.5.16 - o `CLAUDE.md` ja registra isso) |
| Lombok | **ja removido** do `pom.xml` |
| Spring Security + JJWT | **ja removidos** do `pom.xml` |
| Flyway | **presente** (`spring-boot-starter-flyway` + `flyway-database-postgresql`) |
| `src/main/resources/db/migration/` | **nao existe** - nenhuma migration escrita |
| `spring.jpa.hibernate.ddl-auto` | `validate` (`application.yml`) |
| MapStruct | **ausente** - ainda precisa entrar |
| Testcontainers | presente, com nomes novos: `testcontainers-junit-jupiter`, `testcontainers-postgresql`, `spring-boot-testcontainers` |
| Starters de teste | granulares do Boot 4 (`spring-boot-starter-data-jpa-test` etc), **nao** o `spring-boot-starter-test` monolitico |
| REST-assured, Actuator | **ausentes** do `pom.xml` |
| Codigo Java | so `package-info.java` - nenhuma classe real |
| `Makefile` | nao existe no disco |

**Consequencia critica:** Flyway no classpath + zero migrations + `ddl-auto:
validate` = **a aplicacao nao sobe hoje**. Por isso a Task 3 (migration inicial)
existe: sem ela, nenhum passo de verificacao que inicia a app funciona.

## Requisitos de origem (transcricao do pedido)

1. Criar arquivos de referencia/contexto em `docs/`: testes unitarios (bom/ruim); DDD com estrutura de pastas e responsabilidade de cada arquivo (bom/ruim); dominio + dicionario de linguagem ubiqua; MapStruct; Swagger na controller (bom/ruim); arquivo explicando as skills.
2. Remover de `docs/context/` os arquivos que nao fazem mais sentido.
3. Criar rules para Cursor e Claude com um **dicionario dos contextos disponiveis**, citando o arquivo, para redirecionar a leitura.
4. Docs sao commitados - o script **nao** gera docs; gera a rule de `.claude` ou `.cursor`.
5. O script pergunta qual IA a pessoa usa e faz as instalacoes de cada.
6. O script instala as skills - hoje so macOS, com a pergunta de OS ja preparada para o futuro.
7. O script **verifica** os pre-requisitos (`git`, `npm`, `mvn`) e falha com instrucao se faltar. *(Revisado pelo usuario: o pedido inicial dizia "instalar"; ficou so verificar, porque instalar node via brew atropela quem usa `nvm`.)*
8. **Todo o codigo em ingles** - pastas, arquivos, identificadores. Vira rule para Claude e Cursor.

## Global Constraints

### Idioma

- **CODIGO E 100% EM INGLES.** Pasta, arquivo, pacote, classe, metodo, variavel, campo, constante, enum, nome de teste, path de URL e nome de parametro: ingles, sempre. Nunca misturar (`salvarCustomer`, `CustomerRepositorio` sao erros). Comentario dentro do codigo tambem em ingles.
- **Docs e rules em portugues**, sem acentuacao obrigatoria (estilo ja usado no repo: "servico", "decisao", "codigo").
- **Nome de teste em ingles** (`shouldRejectDocumentWithInvalidCheckDigit`). Commits: conventional commits, corpo em portugues.

### Nomes

- **A fonte de verdade dos nomes e o schema SQL**, reproduzido em `docs/context/modelo-de-dados.md` e aplicado pela migration da Task 3. Classe = tabela em PascalCase (`service_order` -> `ServiceOrder`); campo = coluna em camelCase (`stock_quantity` -> `stockQuantity`).
- Nao inventar sinonimo. E `Material` (nao `Part`), `ServiceOrder` (nao `WorkOrder`), `Customer` (nao `Client`).
- **Pacote raiz permanece `com.postech.oficinamecanica`** (decisao do usuario). So os subpacotes viram ingles. Nao renomear `OficinaMecanicaApiApplication`, `pom.xml` nem `Dockerfile`.

### Stack (estado atual, ja no `pom.xml`)

- **Java 21, Spring Boot 4.0.7, PostgreSQL 16.** Nao trocar versao sem perguntar.
- **Spring Boot 4 renomeou starters e moveu pacotes.** `spring-boot-starter-web` esta deprecado em favor de `spring-boot-starter-webmvc` (ja aplicado). As anotacoes de teste mudaram de pacote - ver "Imports do Boot 4" abaixo. **Nao escrever import de memoria da 3.x.**
- **Flyway e quem cria o schema.** `ddl-auto` e `validate`: Hibernate so confere, nunca altera. Toda mudanca de schema e uma migration nova em `src/main/resources/db/migration/`.
- **MapStruct 1.6.3** entra na Task 7 (`defaultComponentModel=spring`, `unmappedTargetPolicy=ERROR`).
- **Sem Lombok, sem Spring Security, sem JWT** - ja removidos. Nao reintroduzir sem aval do usuario; nao escrever codigo ou doc que assuma autenticacao (sem `@SecurityRequirement`, sem `401` documentado, sem `@WithMockUser`).
- **Testcontainers com Postgres real. H2 nao entra.**
- **Sem Makefile** (decisao do usuario): so embrulharia `mvn`. Comando util fica na secao "Comandos" do `CLAUDE.md`.

### Imports do Boot 4 (verificados na doc oficial 4.0.3)

| Anotacao | Pacote no Boot 4 | Pacote antigo (3.x) - **nao usar** |
|---|---|---|
| `@DataJpaTest` | `org.springframework.boot.data.jpa.test.autoconfigure` | `org.springframework.boot.test.autoconfigure.orm.jpa` |
| `@AutoConfigureTestDatabase` | `org.springframework.boot.jdbc.test.autoconfigure` | `org.springframework.boot.test.autoconfigure.jdbc` |

`@ServiceConnection` + `@Container static` continuam sendo o padrao.

### Escopo dos docs

- **`docs/context/` so contem material de "como escrever codigo aqui".** Requisito de entrega, board externo e justificativa de decisao passada nao entram - nao mudam uma linha de codigo e so gastam contexto.
- **Zero duplicacao:** cada regra existe em exatamente um arquivo. `CLAUDE.md` e `.cursor/rules/*.mdc` so apontam.
- **Este plano NAO comita nada** (decisao do usuario). Nenhuma task roda `git commit`, `git add`, `git rm` ou `git mv` - so `mv`/`rm` puros, que mexem no disco e nao no index. Cada task termina em um passo de **verificacao**, nao de commit. Revisar o diff e decidir o que vai para o historico e do usuario, no fim.
- **Concorrencia esta fora de escopo** (decisao do usuario). Nao escrever teste, doc ou codigo sobre lock de linha / `SELECT ... FOR UPDATE`.
- `.cursor/` e `.claude/settings.local.json` sao gerados - entram no `.gitignore`. `CLAUDE.md` da raiz e commitado.
- Nao mexer no Trello nem no Miro. Nao habilitar upload de SARIF.

## Estrutura de arquivos final

| Arquivo | Responsabilidade |
|---|---|
| `docs/context/README.md` | **Dicionario de contextos.** Tabela "situacao -> arquivo a ler". Unico indice. |
| `docs/context/dominio-e-linguagem-ubiqua.md` | O negocio, agregado `ServiceOrder`, fluxo de status, glossario PT->EN, pontos em aberto. |
| `docs/context/modelo-de-dados.md` | Schema, regra tabela->classe, como escrever migration. **Fonte de verdade dos nomes.** |
| `docs/context/arquitetura-ddd.md` | Camadas, direcao de dependencia, responsabilidade por arquivo, bom/ruim por camada. |
| `docs/context/testes.md` | Estrategia de teste, nomenclatura em ingles, bom/ruim, Testcontainers, imports do Boot 4. |
| `docs/context/mapstruct.md` | Onde vive o mapper, o que pode e nao pode fazer, bom/ruim. |
| `docs/context/swagger.md` | Anotacoes obrigatorias na controller, bom/ruim. |
| `docs/context/ferramentas-e-skills.md` | Pre-requisitos, RTK, Caveman, Ponytail, Superpowers, Context7. |
| `src/main/resources/db/migration/V1__initial_schema.sql` | Schema inicial. Sem ela a app nao sobe. |
| `docs/edital-tech-challenge.md` | Movido de `docs/context/`. Assunto humano, fora do caminho do agente. |
| `docs/ai/cursor-rule.mdc.template` | Template consumido pelo script para gerar `.cursor/rules/00-projeto.mdc`. |
| `CLAUDE.md` (raiz) | Rule do Claude. Enxuto: stack, proibicoes, dicionario. |
| `setup-ai.sh` | Verifica pre-requisitos, pergunta a IA, instala skills, gera a rule. |
| ~~`docs/context/01-ddd-decisoes.md`~~ | **Removido** (fundido no doc de dominio). |
| ~~`docs/context/02-trello-board.md`~~ | **Removido** (snapshot datado de board externo). |
| ~~`docs/context/03-decisao-banco-de-dados.md`~~ | **Removido** (justificativa ja esta no `README.md`). |

## Pacotes: de portugues para ingles

| Hoje | Depois |
|---|---|
| `domain/cliente` | `domain/customer` |
| `domain/veiculo` | `domain/vehicle` |
| `domain/peca` | `domain/material` |
| `domain/servico` | `domain/service` |
| `domain/ordemservico` | `domain/serviceorder` |
| - | `domain/employee` (novo - existe no schema) |

`application`, `infrastructure` (`config`, `persistence`, `security`) e `interfaces/rest` ja estao em ingles.

---

### Task 1: Reorganizar `docs/context/` e criar o doc de dominio

**Files:**
- Create: `docs/context/dominio-e-linguagem-ubiqua.md`
- Move: `docs/context/00-edital-tech-challenge.md` -> `docs/edital-tech-challenge.md`
- Delete: `docs/context/01-ddd-decisoes.md`, `docs/context/02-trello-board.md`, `docs/context/03-decisao-banco-de-dados.md`

**Interfaces:**
- Produces: `docs/context/dominio-e-linguagem-ubiqua.md`, referenciado pelo indice (Task 10) e pelas rules (Tasks 11 e 13). Define os nomes de classe em ingles que as Tasks 2-8 usam.

- [ ] **Step 1: Tirar de `docs/context/` o que nao ajuda a escrever codigo**

`02-trello-board.md` e snapshot datado de board externo. `03-decisao-banco-de-dados.md`
e decisao ja fechada, e a justificativa que importa ja esta no `README.md`. O
edital descreve a entrega do trabalho, nao como codar - sai do caminho de leitura
do agente, mas continua no repo para consulta humana.

```bash
rm docs/context/02-trello-board.md docs/context/03-decisao-banco-de-dados.md
mv docs/context/00-edital-tech-challenge.md docs/edital-tech-challenge.md
```

- [ ] **Step 2: Confirmar que a justificativa do banco nao se perdeu**

Run: `grep -q "^## Justificativa do banco de dados" README.md && echo "OK: README cobre a decisao do banco" || echo "FALTA: reponha a secao antes de apagar"`

Esperado: `OK: README cobre a decisao do banco`

- [ ] **Step 3: Criar `docs/context/dominio-e-linguagem-ubiqua.md`**

````markdown
# Dominio: oficina mecanica

Leia antes de criar qualquer entidade, use case ou endpoint. Define **o que as
palavras significam neste repositorio** e qual e o nome delas em ingles.

**Codigo e sempre em ingles.** O portugues fica nestes docs e na conversa com o
time. Ver `modelo-de-dados.md` para o schema, que e a fonte de verdade dos nomes.

## O negocio em um paragrafo

Oficina mecanica de medio porte. O atendente recebe o cliente e o veiculo e abre
uma **Ordem de Servico** (`ServiceOrder`). O mecanico faz o **diagnostico**,
escolhe **servicos** (`Service`) e **materiais** (`Material`), o que forma o
**orcamento**. O cliente aprova ou recusa. Aprovado, os materiais saem do estoque
e o servico e executado. Se aparecer um **reparo adicional** durante a execucao,
gera-se um novo ciclo de aprovacao sem reiniciar a ordem. No fim o veiculo e
entregue.

## Glossario: portugues -> ingles

Coluna "Classe" e o nome a usar no codigo. Coluna "Tabela" amarra ao schema.

| Termo do time (PT) | Classe (EN) | Tabela | Nao chamar de |
|---|---|---|---|
| Cliente | `Customer` | `customer` | `Client`, `User` |
| Veiculo | `Vehicle` | `vehicle` | `Car`, `Automobile` |
| Peca / material / insumo | `Material` | `material` | **`Part`**, `Product`, `Item` |
| Servico (catalogo) | `Service` | `service` | `Procedure`, `Task` |
| Funcionario | `Employee` | `employee` | `User`, `Staff` |
| Ordem de Servico (OS) | `ServiceOrder` | `service_order` | **`WorkOrder`**, `Order`, `Job` |
| Material da OS | `ServiceOrderMaterial` | `service_order_material` | `OrderItem` |
| Servico da OS | `ServiceOrderService` | `service_order_service` | `OrderItem` |
| Historico de status | `ServiceOrderHistory` | `service_order_history` | `Audit`, `Log` |
| Movimentacao de estoque | `MaterialTransaction` | `material_transaction` | `StockMovement` |
| CPF/CNPJ | `Document` (VO) | `customer.document` | `Cpf`, `TaxId` |
| Placa | `Plate` (VO) | `vehicle.plate` | `LicensePlate` |

Termos que existem no vocabulario do time mas **nao viram classe**:

- **Orcamento**: nao e entidade. E o campo `price` da `ServiceOrder`, derivado da
  soma de `ServiceOrderService` + `ServiceOrderMaterial`. Se precisar de um nome
  no codigo, use `totalPrice` - nao crie uma classe `Estimate`.
- **Diagnostico**: e a etapa em que a `ServiceOrder` esta no status
  `IN_DIAGNOSIS`. Nao ha tabela.
- **Reparo adicional**: nao ha tabela. Modela-se como novo ciclo de status na
  mesma `ServiceOrder`, registrado em `ServiceOrderHistory`.

Ambiguidade conhecida: "peca" para o mecanico e componente fisico; para o
financeiro e item de estoque com custo. No codigo existe **uma** classe,
`Material`, que e a visao de estoque. A visao fisica aparece so como
`ServiceOrderMaterial` dentro da ordem.

## Agregado central: ServiceOrder

`ServiceOrder` e raiz de agregado. `ServiceOrderMaterial`, `ServiceOrderService`
e `ServiceOrderHistory` sao internos a ela - nada de fora os altera direto,
sempre por um metodo da raiz.

`Customer`, `Vehicle`, `Material`, `Service` e `Employee` sao agregados proprios.
A `ServiceOrder` referencia por id, nao por objeto embutido.

### Fluxo de status

```
RECEIVED -> IN_DIAGNOSIS -> AWAITING_APPROVAL -> IN_PROGRESS -> COMPLETED -> DELIVERED
```

`service_order.status` e `VARCHAR(30)` com default `RECEIVED`; o banco nao
restringe os valores, entao **o enum `ServiceOrderStatus` no dominio e quem
manda**. Transicao invalida lanca excecao de dominio - nao retorna `false`, nao e
validada na controller.

Toda mudanca de status grava uma linha em `ServiceOrderHistory` com o `price` do
momento e quem fez (`authorType`: `CUSTOMER`, `EMPLOYEE` ou `SYSTEM`).

## Value Objects obrigatorios

- **`Document`**: CPF ou CNPJ na mesma coluna (`customer.document`, unica).
  Valida formato **e** digito verificador no construtor.
- **`Plate`**: formato antigo (`AAA-1234`) e/ou Mercosul (`AAA1A23`).

Os dois sao imutaveis e validam no construtor. Um `Customer` nunca existe com
documento invalido - nao ha estado intermediario invalido.

## Status de cadastro

`customer`, `vehicle`, `material`, `service` e `employee` tem
`status ACTIVE/INACTIVE` - exclusao e logica, nao `DELETE`. Enum `EntityStatus`.
Listagem padrao filtra `ACTIVE`.

## Pontos em aberto - PARE e pergunte

Nao decida sozinho. Se a tarefa esbarrar em um destes, pergunte ao usuario:

1. **Reserva de material nao existe no schema.** So ha `MaterialTransaction` com
   `type IN/OUT`. Confirmar que a ideia de "reservar antes de dar baixa" foi
   descartada, ou que ela ainda precisa entrar no modelo.
2. **O que fazer quando o mecanico descobre que o material nao existe no catalogo
   durante o diagnostico?** Encerrar a ordem, ou permitir cadastrar e continuar?
3. **Como o estado diferencia "recusa total do orcamento" (encerra a ordem) de
   "recusa parcial de reparo adicional" (volta para `IN_PROGRESS`)?** Nao ha
   status de recusa no fluxo acima.
4. As perguntas marcadas com `?` nos comentarios do schema (ver
   `modelo-de-dados.md`, secao "Perguntas em aberto do schema").
````

- [ ] **Step 4: Verificar que nada aponta para os arquivos removidos**

Run: `grep -rn "01-ddd-decisoes\|02-trello-board\|03-decisao-banco\|context/00-edital" --include=*.md --include=*.sh . | grep -v superpowers/plans || echo "OK: sem referencias orfas"`

Esperado: so ocorrencias em `CLAUDE.md`, corrigidas na Task 13. Anote e siga.

---

### Task 2: Doc do modelo de dados (fonte de verdade dos nomes)

**Files:**
- Create: `docs/context/modelo-de-dados.md`

**Interfaces:**
- Consumes: glossario de `dominio-e-linguagem-ubiqua.md` (Task 1).
- Produces: `docs/context/modelo-de-dados.md`. A Task 3 escreve a migration a partir do SQL daqui; as Tasks 5-8 tiram daqui todo nome de classe, campo e enum.

- [ ] **Step 1: Criar `docs/context/modelo-de-dados.md`**

````markdown
# Modelo de dados

**Fonte de verdade dos nomes.** Antes de criar classe, campo ou endpoint, confira
aqui. Divergiu do schema? O schema esta certo.

## Quem cria o schema

**Flyway.** `spring.jpa.hibernate.ddl-auto` e `validate`: o Hibernate so confere
que as entidades batem com as tabelas, **nunca altera nada**. Consequencias:

- Toda mudanca de schema e um arquivo novo em
  `src/main/resources/db/migration/`, nomeado `V<n>__descricao_em_ingles.sql`.
- **Nunca edite uma migration ja aplicada** - o Flyway guarda o checksum e o
  boot falha com `Migration checksum mismatch`. Corrigiu algo? Nova migration.
- Entidade JPA com campo que nao existe na tabela quebra o boot, nao o teste.
  Isso e proposital: erro cedo e barato.

A migration inicial e `V1__initial_schema.sql`, com exatamente o SQL abaixo.

## Regra de traducao

| Do schema | Para o codigo |
|---|---|
| tabela `service_order` | classe `ServiceOrder` (PascalCase, singular) |
| coluna `stock_quantity` | campo `stockQuantity` (camelCase) |
| coluna `customer_id` (FK) | campo `customerId`, tipo `Long` |
| `NUMERIC(10,2)` | `BigDecimal` - **nunca `double`/`float` para dinheiro** |
| `TIMESTAMP` | `Instant` |
| `VARCHAR` de dominio fechado | `enum` proprio |
| `BIGSERIAL PRIMARY KEY` | `Long id`, gerado pelo banco |

Enums que saem do schema: `ServiceOrderStatus`, `EntityStatus` (ACTIVE/INACTIVE),
`AuthorType` (CUSTOMER/EMPLOYEE/SYSTEM), `TransactionType` (IN/OUT).

## Schema

```sql
-- ========================================================================
-- Schema: Mechanic Shop Management System (MVP)
-- Database: PostgreSQL
-- =============================== CUSTOMER ===============================
CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    document VARCHAR(18) NOT NULL UNIQUE, -- CPF or CNPJ?
    phone VARCHAR(20),
    email VARCHAR(255) UNIQUE,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE?
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== VEHICLE ===============================
CREATE TABLE vehicle (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    plate VARCHAR(10) NOT NULL UNIQUE,
    year INT NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE?
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_vehicle_customer ON vehicle(customer_id);

-- =============================== MATERIAL ===============================
CREATE TABLE material (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    stock_minimum INT NOT NULL DEFAULT 0,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE?
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== SERVICE ===============================
CREATE TABLE service (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE?
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== EMPLOYEE ===============================
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- bcrypt hash?
    role VARCHAR(50) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE?
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== SERVICE ORDER ===============================
CREATE TABLE service_order (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    vehicle_id BIGINT NOT NULL REFERENCES vehicle(id),
    price NUMERIC(10,2),
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_service_order_customer ON service_order(customer_id);
CREATE INDEX idx_service_order_vehicle ON service_order(vehicle_id);
CREATE INDEX idx_service_order_status ON service_order(status);

-- =============================== SERVICE ORDER - MATERIAL ===============================
CREATE TABLE service_order_material (
    id BIGSERIAL PRIMARY KEY,
    service_order_id BIGINT NOT NULL REFERENCES service_order(id),
    material_id BIGINT NOT NULL REFERENCES material(id),
    quantity INT NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_so_material_order ON service_order_material(service_order_id);

-- =============================== SERVICE ORDER - SERVICE ===============================
CREATE TABLE service_order_service (
    id BIGSERIAL PRIMARY KEY,
    service_order_id BIGINT NOT NULL REFERENCES service_order(id),
    service_id BIGINT NOT NULL REFERENCES service(id),
    price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_so_service_order ON service_order_service(service_order_id);

-- =============================== SERVICE ORDER - STATUS HISTORY ===============================
CREATE TABLE service_order_history (
    id BIGSERIAL PRIMARY KEY,
    service_order_id BIGINT NOT NULL REFERENCES service_order(id),
    status VARCHAR(30) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    author_type VARCHAR(20) NOT NULL, -- CUSTOMER / EMPLOYEE / SYSTEM?
    author_id BIGINT, -- null when SYSTEM?
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_so_history_order ON service_order_history(service_order_id);

-- =============================== MATERIAL TRANSACTION ===============================
CREATE TABLE material_transaction (
    id BIGSERIAL PRIMARY KEY,
    material_id BIGINT NOT NULL REFERENCES material(id),
    service_order_id BIGINT REFERENCES service_order(id), -- null on stock entry?
    quantity INT NOT NULL,
    type VARCHAR(10) NOT NULL, -- IN / OUT?
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_mt_material ON material_transaction(material_id);
CREATE INDEX idx_mt_type ON material_transaction(type);
```

## Leitura do modelo

- **`price` aparece em quatro lugares e nao sao a mesma coisa.**
  `material.price` e `service.price` sao o preco de catalogo **hoje**;
  `service_order_material.price` e `service_order_service.price` congelam o preco
  no momento em que o item entrou na ordem. Mudar o catalogo nao pode alterar
  ordem ja existente - por isso o valor e copiado, nao lido por join.
- **`service_order.price` e nulavel** porque a ordem nasce sem orcamento; ele so
  existe depois do diagnostico.
- **`service_order` referencia `customer_id` e `vehicle_id`.** O cliente esta
  denormalizado (daria para chegar nele por `vehicle.customer_id`); isso e
  proposital, porque o veiculo pode trocar de dono. Consequencia no codigo: ao
  abrir a ordem, copie o dono **vigente naquele momento** para
  `service_order.customer_id`. Por isso `ServiceOrder.open(vehicle, ...)` recebe
  so o veiculo e resolve o cliente a partir dele - a ordem antiga continua
  apontando para o dono da epoca mesmo depois de uma venda.
- **`service_order_history.price`** guarda o valor vigente em cada transicao - e o
  que permite reconstruir o historico de orcamento.
- **`material_transaction` e o unico registro de movimentacao de estoque.** Nao
  existe tabela de reserva. `material.stock_quantity` e o saldo; a transacao e o
  extrato.

## Perguntas em aberto do schema

Os comentarios com `?` no SQL sao duvidas do time, nao decisoes. Nao resolva
sozinho:

1. `customer.document` guarda CPF e CNPJ na mesma coluna - o VO `Document` aceita
   os dois e decide o tipo pelo tamanho?
2. `status` e sempre so `ACTIVE`/`INACTIVE`, em todas as cinco tabelas?
3. `employee.password` e hash bcrypt? (As dependencias de seguranca foram
   removidas do `pom.xml` - a tabela existe, a autenticacao nao.)
4. `service_order_history.author_type` tem exatamente os tres valores
   `CUSTOMER`/`EMPLOYEE`/`SYSTEM`?
5. `service_order_history.author_id` fica nulo quando `author_type = SYSTEM`?
6. `material_transaction.type` tem exatamente `IN`/`OUT`?
7. `material_transaction.service_order_id` fica nulo em entrada de estoque
   (compra de fornecedor)?

Os comentarios `?` **ficam tambem na migration**, identicos ao SQL acima
(decisao do usuario). Eles sao o marcador de que a decisao ainda nao foi tomada -
apagar deixaria a duvida invisivel para quem abrir o arquivo depois. Quando a
pergunta for respondida, o comentario sai numa migration nova, junto com a
mudanca que ele descrevia.
````

- [ ] **Step 2: Conferir que os blocos fecham**

Run: `awk '/^```/{n++} END{print (n%2==0) ? "OK: blocos balanceados" : "ERRO: bloco aberto"}' docs/context/modelo-de-dados.md`

Esperado: `OK: blocos balanceados`

---

### Task 3: Migration inicial do Flyway

**Sem esta task a aplicacao nao sobe.** Flyway esta no classpath, nao ha nenhuma
migration e `ddl-auto` e `validate` - o boot falha no primeiro `mvn spring-boot:run`.

**Files:**
- Create: `src/main/resources/db/migration/V1__initial_schema.sql`

**Interfaces:**
- Consumes: SQL de `docs/context/modelo-de-dados.md` (Task 2), **sem** os comentarios `?` de duvida.
- Produces: schema aplicado. Todo passo posterior que sobe a app ou usa Testcontainers depende dele.

- [ ] **Step 1: Criar `src/main/resources/db/migration/V1__initial_schema.sql`**

**Conteudo literal, exatamente como o usuario definiu** - incluindo os
comentarios de secao e os comentarios com `?`. Nao reescrever, nao reordenar,
nao "limpar" as duvidas: elas ficam no arquivo como marcador do que ainda vai ser
decidido.

Nome do arquivo: `V1__initial_schema.sql`, com **dois** underscores depois da
versao (`V1` + `__` + descricao). Um underscore so, e o Flyway ignora o arquivo.

```sql
-- ========================================================================
-- Schema: Mechanic Shop Management System (MVP)
-- Database: PostgreSQL
-- =============================== CUSTOMER ===============================
CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    document VARCHAR(18) NOT NULL UNIQUE, -- CPF or CNPJ?
    phone VARCHAR(20),
    email VARCHAR(255) UNIQUE,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE?
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== VEHICLE ===============================
CREATE TABLE vehicle (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    plate VARCHAR(10) NOT NULL UNIQUE,
    year INT NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE?
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_vehicle_customer ON vehicle(customer_id);

-- =============================== MATERIAL ===============================
CREATE TABLE material (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    stock_minimum INT NOT NULL DEFAULT 0,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE?
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== SERVICE ===============================
CREATE TABLE service (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE?
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== EMPLOYEE ===============================
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- bcrypt hash?
    role VARCHAR(50) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE?
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== SERVICE ORDER ===============================
CREATE TABLE service_order (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    vehicle_id BIGINT NOT NULL REFERENCES vehicle(id),
    price NUMERIC(10,2),
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_service_order_customer ON service_order(customer_id);
CREATE INDEX idx_service_order_vehicle ON service_order(vehicle_id);
CREATE INDEX idx_service_order_status ON service_order(status);

-- =============================== SERVICE ORDER - MATERIAL ===============================
CREATE TABLE service_order_material (
    id BIGSERIAL PRIMARY KEY,
    service_order_id BIGINT NOT NULL REFERENCES service_order(id),
    material_id BIGINT NOT NULL REFERENCES material(id),
    quantity INT NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_so_material_order ON service_order_material(service_order_id);

-- =============================== SERVICE ORDER - SERVICE ===============================
CREATE TABLE service_order_service (
    id BIGSERIAL PRIMARY KEY,
    service_order_id BIGINT NOT NULL REFERENCES service_order(id),
    service_id BIGINT NOT NULL REFERENCES service(id),
    price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_so_service_order ON service_order_service(service_order_id);

-- =============================== SERVICE ORDER - STATUS HISTORY ===============================
CREATE TABLE service_order_history (
    id BIGSERIAL PRIMARY KEY,
    service_order_id BIGINT NOT NULL REFERENCES service_order(id),
    status VARCHAR(30) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    author_type VARCHAR(20) NOT NULL, -- CUSTOMER / EMPLOYEE / SYSTEM?
    author_id BIGINT, -- null when SYSTEM?
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_so_history_order ON service_order_history(service_order_id);

-- =============================== MATERIAL TRANSACTION ===============================
CREATE TABLE material_transaction (
    id BIGSERIAL PRIMARY KEY,
    material_id BIGINT NOT NULL REFERENCES material(id),
    service_order_id BIGINT REFERENCES service_order(id), -- null on stock entry?
    quantity INT NOT NULL,
    type VARCHAR(10) NOT NULL, -- IN / OUT?
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_mt_material ON material_transaction(material_id);
CREATE INDEX idx_mt_type ON material_transaction(type);
```

- [ ] **Step 2: Subir a app e ver o Flyway aplicar**

O `application.yml` tem `spring.docker.compose.enabled: true`, entao o
`mvn spring-boot:run` sobe o Postgres sozinho.

Run: `mvn spring-boot:run` (Ctrl-C depois que subir)

Esperado no log: `Successfully applied 1 migration to schema "public"` e a app
subindo sem erro de validate. Se aparecer
`Schema-validation: missing table`, a migration nao rodou - confira o caminho
`src/main/resources/db/migration/` e o nome com **dois** underscores.

- [ ] **Step 3: Conferir as tabelas no banco**

```bash
docker compose exec -T db psql -U oficina -d oficina_mecanica -c "\dt"
```

Esperado: as 10 tabelas mais `flyway_schema_history`.

---

### Task 4: Renomear os subpacotes de dominio para ingles

Hoje so existem `package-info.java` - nenhuma classe real. Renomear agora custa
quase nada e evita refatorar imports depois.

**Files:**
- Move: `src/main/java/com/postech/oficinamecanica/domain/{cliente,veiculo,peca,servico,ordemservico}/`
- Create: `src/main/java/com/postech/oficinamecanica/domain/employee/package-info.java`
- Modify: os `package-info.java` movidos (declaracao `package`)

**Interfaces:**
- Consumes: nomes de `dominio-e-linguagem-ubiqua.md` (Task 1).
- Produces: a arvore de pacotes que `arquitetura-ddd.md` (Task 5) documenta.

- [ ] **Step 1: Ver o que existe hoje**

Run: `find src/main/java/com/postech/oficinamecanica/domain -type f | sort`

Esperado: cinco `package-info.java`, em `cliente`, `ordemservico`, `peca`, `servico`, `veiculo`.

- [ ] **Step 2: Renomear as pastas**

```bash
cd src/main/java/com/postech/oficinamecanica/domain
mv cliente customer
mv veiculo vehicle
mv peca material
mv servico service
mv ordemservico serviceorder
cd -
```

- [ ] **Step 3: Corrigir a declaracao `package` dentro de cada arquivo**

O `mv` move o arquivo mas nao edita o conteudo. Cada `package-info.java` ainda
declara o pacote antigo e o projeto nao compila assim.

```bash
BASE=com.postech.oficinamecanica.domain
D=src/main/java/com/postech/oficinamecanica/domain
sed -i '' "s/^package $BASE\.cliente;/package $BASE.customer;/"          "$D/customer/package-info.java"
sed -i '' "s/^package $BASE\.veiculo;/package $BASE.vehicle;/"           "$D/vehicle/package-info.java"
sed -i '' "s/^package $BASE\.peca;/package $BASE.material;/"             "$D/material/package-info.java"
sed -i '' "s/^package $BASE\.servico;/package $BASE.service;/"           "$D/service/package-info.java"
sed -i '' "s/^package $BASE\.ordemservico;/package $BASE.serviceorder;/" "$D/serviceorder/package-info.java"
```

`sed -i ''` e a sintaxe do BSD sed (macOS). No Linux seria `sed -i`.

- [ ] **Step 4: Criar o pacote `employee`**

Existe no schema e nao tinha pacote. Salve em
`src/main/java/com/postech/oficinamecanica/domain/employee/package-info.java`:

```java
/**
 * Employee aggregate: shop staff that operates service orders.
 */
package com.postech.oficinamecanica.domain.employee;
```

- [ ] **Step 5: Verificar que nao sobrou pacote em portugues e que compila**

```bash
grep -rn "domain\.\(cliente\|veiculo\|peca\|servico\|ordemservico\)" src/ || echo "OK: nenhum pacote em portugues"
mvn -q clean compile && echo "COMPILA OK"
```

Esperado: `OK: nenhum pacote em portugues` e `COMPILA OK`.

> O raiz `oficinamecanica` continua em portugues de proposito - por isso o grep
> busca `domain.<nome>`, nao o nome solto.

---

### Task 5: Doc de arquitetura DDD com responsabilidade por camada

**Files:**
- Create: `docs/context/arquitetura-ddd.md`

**Interfaces:**
- Consumes: pacotes da Task 4, nomes das Tasks 1 e 2.
- Produces: `docs/context/arquitetura-ddd.md` para o indice (Task 10).

- [ ] **Step 1: Criar `docs/context/arquitetura-ddd.md`**

````markdown
# Arquitetura DDD em camadas

Leia antes de criar **qualquer** arquivo `.java`. Responde: "onde essa classe
mora e o que ela pode importar?"

**Todo identificador em ingles.** Nomes vem de `modelo-de-dados.md`.

Os exemplos deste doc seguem **um caso so, do inicio ao fim: cadastrar um
`Customer`**. E o CRUD mais simples do sistema e mesmo assim atravessa todas as
camadas - da para copiar a estrutura para qualquer outra entidade.

## Mapa de pacotes

```
com.postech.oficinamecanica        <- raiz em portugues de proposito, nao renomear
├── domain                          # entidades, agregados, VOs, regras. SEM Spring.
│   ├── customer
│   ├── vehicle
│   ├── material
│   ├── service
│   ├── employee
│   ├── serviceorder                # agregado central
│   └── shared                      # so o que e de todos os contextos (EntityStatus)
├── application                     # use cases, portas (interfaces de repositorio)
├── infrastructure                  # JPA, config (security/ existe mas esta vazio)
└── interfaces.rest                 # controllers, DTOs, mappers
```

`domain/shared` nasce quando aparecer o primeiro tipo genuinamente compartilhado
(hoje, `EntityStatus`). Ele **nao** e deposito de classe sem dono: se um tipo
pertence a um contexto, ele mora nesse contexto.

## Direcao de dependencia (a regra que nao se quebra)

```
interfaces.rest ---> application ---> domain
                          ^              ^
                          |              |
                    infrastructure ------+
```

- `domain` **nao importa nada**: nem Spring, nem JPA, nem DTO, nem outra camada.
- `application` importa `domain`. Nunca `infrastructure` nem `interfaces`.
- `infrastructure` implementa interfaces declaradas em `application`.
- `interfaces.rest` importa `application`. Nunca `infrastructure`.

Teste mental: apagando `infrastructure`, `domain` e `application` ainda compilam.
Se nao compilam, a dependencia esta invertida.

## A fatia vertical do Customer

Uma entidade nao mora numa pasta so - ela aparece uma vez em cada camada. Para
`Customer`, os arquivos sao estes:

```
domain/shared/
└── EntityStatus.java                # enum ACTIVE / INACTIVE - usado por 5 tabelas

domain/customer/
├── Customer.java                    # entidade de dominio, com as regras
├── Document.java                    # Value Object: CPF ou CNPJ
├── InvalidDocumentException.java
├── DuplicateDocumentException.java
└── CustomerAlreadyInactiveException.java

application/customer/
├── CreateCustomerUseCase.java       # orquestra o cadastro
├── CreateCustomerCommand.java       # entrada do use case (nao e o DTO de HTTP)
└── CustomerRepository.java          # PORTA: interface, sem Spring Data

infrastructure/persistence/customer/
├── CustomerJpaEntity.java           # @Entity, mapeia a tabela customer
├── CustomerJpaRepository.java       # extends JpaRepository - so aqui
├── CustomerRepositoryImpl.java      # implementa a porta
└── CustomerPersistenceMapper.java   # dominio <-> entidade JPA

interfaces/rest/customer/
├── CustomerController.java          # POST /api/customers
├── CreateCustomerRequest.java       # DTO de entrada (record + Bean Validation)
├── CustomerResponse.java            # DTO de saida
└── CustomerRestMapper.java          # DTO <-> command/dominio
```

Dois detalhes de onde as coisas moram:

- **`Document` fica em `domain/customer`**, nao num pacote tecnico de "utils". Ele
  e vocabulario do negocio e so o cliente tem documento.
- **`EntityStatus` fica em `domain/shared`** porque as cinco tabelas de cadastro
  (`customer`, `vehicle`, `material`, `service`, `employee`) usam o mesmo enum.
  Deixar em `domain/customer` obrigaria `Vehicle` a importar do pacote de cliente
  - acoplamento sem motivo. `shared/` aqui e para o que e genuinamente de todos os
  contextos, **nao** e deposito de classe sem dono.

## Responsabilidade arquivo por arquivo

| Camada | Arquivo | Faz | Nao faz |
|---|---|---|---|
| `domain` | `Customer.java` | invariantes do cadastro, ativar/desativar, factory `register(...)` | anotacao JPA/Spring, chamada de repositorio, HTTP |
| `domain` | `Document.java` (VO) | valida formato e digito verificador no construtor, imutavel, `equals` por valor | setter, aceitar estado invalido |
| `domain` | `EntityStatus.java` | enum fechado `ACTIVE`/`INACTIVE` | mapeamento de coluna |
| `application` | `CreateCustomerUseCase.java` | orquestra: checa duplicidade, chama o dominio, persiste, abre transacao | regra de negocio propria, montar resposta HTTP |
| `application` | `CreateCustomerCommand.java` | dado de entrada ja no vocabulario do dominio | Bean Validation de HTTP, anotacao Swagger |
| `application` | `CustomerRepository.java` (porta) | interface com metodos em linguagem de dominio | `extends JpaRepository`, tipos do Spring Data |
| `infrastructure` | `CustomerJpaEntity.java` | `@Entity`, `@Table(name = "customer")`, colunas | regra de negocio, validacao de documento |
| `infrastructure` | `CustomerJpaRepository.java` | `extends JpaRepository`, query derivada | ser exposta para fora da infra |
| `infrastructure` | `CustomerRepositoryImpl.java` | implementa a porta usando o Spring Data + mapper | decidir regra |
| `infrastructure` | `CustomerPersistenceMapper.java` | dominio <-> entidade JPA | calculo, decisao |
| `interfaces.rest` | `CustomerController.java` | recebe HTTP, valida formato, chama use case, devolve status | regra de negocio, repositorio, `@Transactional` |
| `interfaces.rest` | `CreateCustomerRequest.java` | `record` com Bean Validation e `@Schema` | logica |
| `interfaces.rest` | `CustomerRestMapper.java` | Request -> Command, Dominio -> Response | regra, acesso a banco |

## O fluxo completo: cadastrar um Customer

`POST /api/customers` com `{"name": "...", "document": "...", ...}`:

| # | Onde | O que acontece |
|---|---|---|
| 1 | `CustomerController` | Jackson desserializa em `CreateCustomerRequest`; `@Valid` reprova campo faltando (400) |
| 2 | `CustomerRestMapper` | `toCommand(request)` -> `CreateCustomerCommand` |
| 3 | `CreateCustomerUseCase` | abre transacao |
| 4 | `Document` (VO) | construtor valida CPF/CNPJ; invalido lanca `InvalidDocumentException` (400) |
| 5 | `CustomerRepository` (porta) | `existsByDocument` - ja cadastrado lanca `DuplicateDocumentException` (409) |
| 6 | `Customer.register(...)` | cria o cliente ja com `status = ACTIVE` |
| 7 | `CustomerRepositoryImpl` | mapeia para `CustomerJpaEntity` e salva; o banco preenche `id`, `created_at`, `updated_at` |
| 8 | `CustomerRestMapper` | `toResponse(customer)` -> `CustomerResponse` |
| 9 | `CustomerController` | devolve **201 Created** |

A regra "documento e unico" aparece **duas vezes de proposito**: no passo 5, para
dar erro claro ao usuario, e como `UNIQUE` no banco, que e a garantia real sob
concorrencia. Confiar so na checagem da aplicacao deixa passar duplicata; confiar
so no banco devolve erro feio de constraint.

## Controller - bom e ruim

**RUIM** - regra de negocio e repositorio na controller, nomes em portugues:

```java
@RestController
public class ClienteController {                    // nome em portugues

    @Autowired
    private CustomerJpaRepository repository;        // infra na interface!

    @PostMapping("/clientes")                        // path em portugues
    public CustomerJpaEntity criar(@RequestBody CustomerJpaEntity customer) {
        if (customer.getDocument() == null) {        // validacao solta
            throw new RuntimeException("erro");      // excecao generica
        }
        if (repository.existsByDocument(customer.getDocument())) {  // regra na controller
            throw new RuntimeException("ja existe");
        }
        customer.setStatus("ACTIVE");                // regra de dominio aqui
        return repository.save(customer);            // entidade JPA vazando no JSON
    }
}
```

Problemas: nome e path em portugues, entidade JPA exposta no contrato HTTP,
controller conhecendo `infrastructure`, regra de unicidade e de status fora do
dominio, `RuntimeException` sem mapeamento, sempre 200 (nunca 201).

**BOM** - fina, delega, contrato proprio, tudo em ingles:

```java
package com.postech.oficinamecanica.interfaces.rest.customer;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomer;
    private final CustomerRestMapper mapper;

    public CustomerController(CreateCustomerUseCase createCustomer,
                              CustomerRestMapper mapper) {
        this.createCustomer = createCustomer;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@RequestBody @Valid CreateCustomerRequest request) {
        Customer customer = createCustomer.execute(mapper.toCommand(request));
        return mapper.toResponse(customer);
    }
}
```

Nao ha Lombok no projeto: escreva o construtor explicito, nao
`@RequiredArgsConstructor`.

O DTO de entrada e um `record`, e a validacao de **formato** fica nele:

```java
public record CreateCustomerRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank String document,
        @Size(max = 20) String phone,
        @Email @Size(max = 255) String email
) {}
```

Validacao de formato (`@NotBlank`) e da borda HTTP. Validacao de **regra**
(documento valido, cliente nao duplicado) e do dominio. Nao troque as duas de
lugar.

## Use case - bom e ruim

**RUIM** - use case anemico que so repassa, com regra vazando:

```java
@Service
public class CreateCustomerUseCase {

    @Autowired private CustomerJpaRepository repo;   // Spring Data no use case

    public CustomerJpaEntity execute(CreateCustomerRequest request) {  // DTO de HTTP aqui
        var entity = new CustomerJpaEntity();
        entity.setName(request.name());
        entity.setDocument(request.document());       // String crua, sem validar
        entity.setStatus("ACTIVE");                   // regra do dominio no use case
        return repo.save(entity);                     // devolve entidade JPA
    }
}
```

**BOM** - orquestra, o dominio decide:

```java
package com.postech.oficinamecanica.application.customer;

@Service
public class CreateCustomerUseCase {

    private final CustomerRepository customers;   // porta, nao Spring Data

    public CreateCustomerUseCase(CustomerRepository customers) {
        this.customers = customers;
    }

    @Transactional
    public Customer execute(CreateCustomerCommand command) {
        Document document = new Document(command.document());  // valida no construtor

        if (customers.existsByDocument(document)) {
            throw new DuplicateDocumentException(document);
        }

        Customer customer = Customer.register(
                command.name(), document, command.phone(), command.email());

        return customers.save(customer);
    }
}
```

```java
public record CreateCustomerCommand(
        String name,
        String document,
        String phone,
        String email
) {}
```

O `Command` parece igual ao `Request`, e tudo bem. Ele existe para o use case nao
depender de `interfaces.rest` - o dia em que chegar um consumidor que nao e HTTP
(fila, CLI, job), o use case nao muda.

`@Transactional` mora **aqui**, no use case - nao na controller nem no repository.
E o use case que define o limite da unidade de trabalho.

## Repository: porta e implementacao - bom e ruim

**RUIM** - porta acoplada ao Spring Data:

```java
// em application/
public interface CustomerRepository extends JpaRepository<CustomerJpaEntity, Long> {
    Optional<CustomerJpaEntity> findByDocument(String document);
}
```

`application` passou a depender de Spring Data e da entidade JPA. A camada de
caso de uso agora sabe como o dado e gravado - a inversao morreu.

**BOM** - porta no vocabulario do dominio, implementacao na infra:

```java
// application/customer/CustomerRepository.java
public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findByDocument(Document document);
    boolean existsByDocument(Document document);
    List<Customer> findAllActive();
}
```

A porta fala `Customer` e `Document` - tipos de dominio. Nao `String`, nao
`CustomerJpaEntity`.

```java
// infrastructure/persistence/customer/CustomerJpaRepository.java
public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {
    Optional<CustomerJpaEntity> findByDocument(String document);
    boolean existsByDocument(String document);
    List<CustomerJpaEntity> findByStatus(EntityStatus status);
}
```

```java
// infrastructure/persistence/customer/CustomerRepositoryImpl.java
@Repository
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerJpaRepository jpa;          // Spring Data fica aqui
    private final CustomerPersistenceMapper mapper;

    public CustomerRepositoryImpl(CustomerJpaRepository jpa,
                                  CustomerPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Customer save(Customer customer) {
        return mapper.toDomain(jpa.save(mapper.toEntity(customer)));
    }

    @Override
    public boolean existsByDocument(Document document) {
        return jpa.existsByDocument(document.unformatted());
    }

    @Override
    public List<Customer> findAllActive() {
        return jpa.findByStatus(EntityStatus.ACTIVE).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
```

A conversao `Document -> String` acontece **na infra**, na fronteira com o banco.
O resto do sistema nunca ve a String crua.

## Entidade de dominio - bom e ruim

**RUIM** - bag de getters/setters, sem invariante:

```java
public class Customer {
    private String document;
    private String status;
    public void setDocument(String document) { this.document = document; }
    public void setStatus(String status) { this.status = status; }  // qualquer valor entra
}
```

Com isso, `customer.setStatus("BANANA")` compila e o erro so aparece no banco.

**BOM** - factory com invariante, sem setter, comportamento com nome de negocio:

```java
package com.postech.oficinamecanica.domain.customer;

public class Customer {

    private final Long id;
    private String name;
    private final Document document;   // nunca muda: e a identidade do cliente
    private String phone;
    private String email;
    private EntityStatus status;

    private Customer(Long id, String name, Document document,
                     String phone, String email, EntityStatus status) {
        this.id = id;
        this.name = name;
        this.document = document;
        this.phone = phone;
        this.email = email;
        this.status = status;
    }

    /** Registers a new customer, always active. */
    public static Customer register(String name, Document document,
                                    String phone, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("customer name is required");
        }
        return new Customer(null, name, document, phone, email, EntityStatus.ACTIVE);
    }

    public void deactivate() {
        if (status == EntityStatus.INACTIVE) {
            throw new CustomerAlreadyInactiveException(id);
        }
        this.status = EntityStatus.INACTIVE;
    }

    public Long id() { return id; }
    public Document document() { return document; }
    public EntityStatus status() { return status; }
}
```

Tres coisas a notar: `document` e `final` (a identidade nao muda), nao existe
`setStatus` (existe `deactivate()`, que e o que o negocio faz), e o cliente nasce
`ACTIVE` porque quem decide isso e o dominio - nao o `DEFAULT` do banco nem a
controller.

## Value Object - bom e ruim

**RUIM** - VO que aceita qualquer coisa:

```java
public class Document {
    private String value;
    public Document(String value) { this.value = value; }   // sem validacao
}
```

Isso e um `String` com passos extras. Nao protege nada.

**BOM** - valida no construtor, imutavel, compara por valor:

```java
package com.postech.oficinamecanica.domain.customer;

public final class Document {

    private final String value;   // stored unformatted

    public Document(String rawValue) {
        String digits = rawValue == null ? "" : rawValue.replaceAll("\\D", "");
        if (!isValidCpf(digits) && !isValidCnpj(digits)) {
            throw new InvalidDocumentException(rawValue);
        }
        this.value = digits;
    }

    /** Digits only - this is what goes to the database. */
    public String unformatted() { return value; }

    /** Masked for display: 529.982.247-25 or 12.345.678/0001-95. */
    public String formatted() {
        return value.length() == 11
                ? value.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4")
                : value.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Document d && value.equals(d.value);
    }

    @Override
    public int hashCode() { return value.hashCode(); }
}
```

Consequencia: **nao existe `Customer` com documento invalido em lugar nenhum do
sistema**. Nao ha estado intermediario invalido para alguem esquecer de checar.

## Entidade JPA e o schema

```java
package com.postech.oficinamecanica.infrastructure.persistence.customer;

@Entity
@Table(name = "customer")
public class CustomerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 18)
    private String document;

    private String phone;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EntityStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    // construtor sem argumentos exigido pelo JPA, getters/setters de mapeamento
}
```

`created_at` e `updated_at` tem `DEFAULT NOW()` no banco (ver
`modelo-de-dados.md`), entao aqui sao `insertable = false, updatable = false`: o
banco e quem preenche, o Java so le.

`ddl-auto` e `validate`: o Hibernate confere as entidades contra as tabelas no
boot. Campo que nao existe na tabela **derruba a aplicacao ao subir**, nao no
teste. Mudou o modelo? Escreva a migration junto.

## Dinheiro

`price` e `NUMERIC(10,2)` no banco: use **`BigDecimal`**, nunca `double` ou
`float`. Some com `BigDecimal::add`, compare com `compareTo`, nunca `equals`.

## Atalho aceitavel (e como pedir)

Para o MVP, JPA anotado direto na classe de dominio - eliminando
`CustomerJpaEntity` e o mapper de persistencia - e tolerado **se o prazo
apertar**. Mas e atalho, nao padrao: o dominio passa a depender do JPA e a
entidade ganha construtor vazio e setters que as invariantes acima proibiam.
**Pergunte ao usuario antes**, nao assuma.
````

- [ ] **Step 2: Conferir blocos**

Run: `awk '/^```/{n++} END{print (n%2==0) ? "OK" : "ERRO: bloco aberto"}' docs/context/arquitetura-ddd.md`

Esperado: `OK`

---

### Task 6: Doc de testes

**Files:**
- Create: `docs/context/testes.md`

**Interfaces:**
- Consumes: classes e camadas de `arquitetura-ddd.md` (Task 5); migration da Task 3 (o Flyway roda tambem no container de teste).
- Produces: `docs/context/testes.md` para o indice (Task 10).

- [ ] **Step 1: Criar `docs/context/testes.md`**

````markdown
# Testes

Meta: **80% de cobertura** (JaCoCo) nos dominios criticos.
Relatorio: `target/site/jacoco/index.html` apos `mvn test`.

**Nome de teste em ingles**, como todo o resto do codigo.

## Atencao: este projeto usa Spring Boot 4

O Boot 4 **moveu os pacotes das anotacoes de teste**. Import de memoria da 3.x
nao compila:

| Anotacao | Import correto (Boot 4) |
|---|---|
| `@DataJpaTest` | `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` |
| `@AutoConfigureTestDatabase` | `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase` |

Os starters de teste tambem sao granulares - `spring-boot-starter-data-jpa-test`,
`spring-boot-starter-webmvc-test`, `spring-boot-starter-validation-test`,
`spring-boot-starter-flyway-test` - e nao o `spring-boot-starter-test`
monolitico. Precisa de um slice que nao esta no `pom.xml`? **Pergunte antes de
adicionar dependencia.**

## Que tipo de teste para que codigo

| O que testar | Como | Spring? | Banco? |
|---|---|---|---|
| Value Object (`Document`, `Plate`) | JUnit puro | nao | nao |
| Invariante do agregado (`ServiceOrder`) | JUnit puro | nao | nao |
| Use case | JUnit + Mockito nas portas | nao | nao |
| Repository impl / mapeamento JPA | `@DataJpaTest` + Testcontainers | sim | Postgres real |
| Controller / contrato HTTP | `@WebMvcTest` | sim | nao |

**A maior parte da suite nao toca banco nenhum.** Regra de dominio, VO e use case
sao JUnit puro, sem Spring e sem container. Banco so aparece nos poucos testes de
mapeamento JPA. E ai que mora a simplicidade - nao em trocar o banco de teste.

**Banco de teste: Postgres real via Testcontainers. Nao usar H2.** Producao e
Postgres; H2, mesmo em modo de compatibilidade, diverge em tipo, funcao nativa e
mensagem de erro de constraint. Alem disso o Testcontainers **ja esta no
`pom.xml`** e o Docker ja e stack do projeto - nao ha nada a instalar.

**`@SpringBootTest` e o ultimo recurso.** Se a regra e pura, teste puro. Subir
contexto para testar `Document` custa segundos e nao acha nada a mais.

## Nomenclatura

`should<ExpectedBehavior>When<Condition>`, no vocabulario do dominio.

```
shouldRejectDocumentWithInvalidCheckDigit
shouldStartDiagnosisWhenOrderIsReceived
shouldNotAllowExecutionWithoutApprovedEstimate
```

## Exemplo RUIM

```java
class ServiceOrderTest {

    @Test
    void test1() throws Exception {                       // nome nao diz nada
        ServiceOrder order = new ServiceOrder();
        order.setStatus("RECEIVED");                       // testa setter, nao comportamento
        assertNotNull(order);                              // assert que nunca falha
        assertTrue(true);
    }

    @Test
    void testeDeStatus() {                                 // nome em portugues
        ServiceOrder order = mock(ServiceOrder.class);     // mock da classe sob teste
        when(order.status()).thenReturn(ServiceOrderStatus.IN_PROGRESS);
        assertEquals(ServiceOrderStatus.IN_PROGRESS, order.status());  // testa o Mockito
    }

    static ServiceOrder shared;                            // estado entre testes

    @Test
    void test3() {
        shared.startDiagnosis();                           // depende da ordem de execucao
    }
}
```

Erros: nome sem informacao, nome em portugues, assert tautologico, mock da classe
sob teste, estado compartilhado, `throws Exception` decorativo, testa acessor em
vez de regra.

## Exemplo BOM - regra de dominio

```java
class ServiceOrderTest {

    @Test
    void shouldStartDiagnosisWhenOrderIsReceived() {
        // Arrange
        ServiceOrder order = ServiceOrder.open(aVehicle(), "noise when braking");

        // Act
        order.startDiagnosis();

        // Assert
        assertThat(order.status()).isEqualTo(ServiceOrderStatus.IN_DIAGNOSIS);
    }

    @Test
    void shouldNotAllowStartingDiagnosisTwice() {
        ServiceOrder order = ServiceOrder.open(aVehicle(), "noise when braking");
        order.startDiagnosis();

        assertThatThrownBy(order::startDiagnosis)
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("IN_DIAGNOSIS");
    }

    private static Vehicle aVehicle() {
        return new Vehicle(new Plate("ABC1D23"), "Volkswagen", "Gol", 2020);
    }
}
```

Um comportamento por teste, AAA visivel, nome que descreve a regra, caminho de
erro testado, helper (`aVehicle`) escondendo ruido sem esconder o que importa.

## Exemplo BOM - Value Object com casos de borda

```java
class DocumentTest {

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "52998224725"})
    void shouldAcceptValidCpfWithOrWithoutFormatting(String value) {
        assertThat(new Document(value).unformatted()).isEqualTo("52998224725");
    }

    @ParameterizedTest
    @ValueSource(strings = {"111.111.111-11", "529.982.247-26", "123", ""})
    void shouldRejectInvalidDocument(String value) {
        assertThatThrownBy(() -> new Document(value))
                .isInstanceOf(InvalidDocumentException.class);
    }
}
```

`111.111.111-11` (todos os digitos iguais) passa no calculo ingenuo de digito
verificador - **esse caso e obrigatorio no teste**.

## Exemplo BOM - use case com mock nas portas

```java
@ExtendWith(MockitoExtension.class)
class OpenServiceOrderUseCaseTest {

    @Mock private ServiceOrderRepository serviceOrders;
    @Mock private VehicleRepository vehicles;
    @InjectMocks private OpenServiceOrderUseCase useCase;

    @Test
    void shouldFailWhenVehicleDoesNotExist() {
        when(vehicles.findByPlate(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                    new OpenServiceOrderCommand("ABC1D23", "noise when braking")))
                .isInstanceOf(VehicleNotFoundException.class);

        verify(serviceOrders, never()).save(any());   // nao pode ter persistido nada
    }
}
```

Mock **so nas portas**. Nunca mock de `ServiceOrder`, `Document` ou objeto de
dominio - eles sao baratos de construir de verdade.

## Exemplo BOM - persistencia com Testcontainers

Um container **estatico**, reusado pela suite inteira. Nao um por classe.

```java
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class CustomerRepositoryImplTest {

    @Container
    @ServiceConnection   // configura url/user/senha sozinho, sem @DynamicPropertySource
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private CustomerJpaRepository repository;

    @Test
    void shouldPersistAndFindCustomerByDocument() {
        repository.save(aCustomerEntity("Maria Souza", "52998224725"));

        assertThat(repository.findByDocument("52998224725")).isPresent();
    }

    // CustomerJpaEntity so tem construtor sem argumentos (exigencia do JPA),
    // entao o helper monta o objeto - nao invente um construtor que nao existe.
    private static CustomerJpaEntity aCustomerEntity(String name, String document) {
        var entity = new CustomerJpaEntity();
        entity.setName(name);
        entity.setDocument(document);
        entity.setStatus(EntityStatus.ACTIVE);
        return entity;
    }
}
```

Tres detalhes que nao sao opcionais:

- **`static`** no campo do container: sem isso o JUnit sobe um Postgres por metodo
  de teste.
- **`@AutoConfigureTestDatabase(replace = Replace.NONE)`**: `@DataJpaTest` usa o
  padrao `Replace.ANY` e trocaria o DataSource do container por um embedded.
- **`postgres:16-alpine`**: a mesma imagem do `docker-compose.yml`. Testar numa
  versao e rodar em outra e o tipo de diferenca que o Testcontainers existe para
  evitar.

O **Flyway roda no container** antes dos testes, aplicando
`V1__initial_schema.sql`. Se a migration estiver quebrada, o teste de persistencia
falha no setup - e o sinal certo.

## Rodar

```bash
mvn test
open target/site/jacoco/index.html
```
````

- [ ] **Step 2: Conferir blocos**

Run: `awk '/^```/{n++} END{print (n%2==0) ? "OK" : "ERRO: bloco aberto"}' docs/context/testes.md`

Esperado: `OK`

---

### Task 7: Adicionar MapStruct e documentar

Lombok, Spring Security e JJWT **ja foram removidos** do `pom.xml` - nao ha nada a
remover aqui. Falta so o MapStruct.

**Files:**
- Modify: `pom.xml` (`<properties>`, `<dependencies>`, `<build><plugins>`)
- Create: `docs/context/mapstruct.md`

**Interfaces:**
- Produces: `${mapstruct.version}`; `@Mapper(componentModel = "spring")` para os mappers de `arquitetura-ddd.md` (Task 5).

- [ ] **Step 1: Confirmar o ponto de partida**

Run: `grep -c "lombok\|jsonwebtoken\|springframework.security\|mapstruct" pom.xml`

Esperado: `0`. Se vier diferente de zero, o `pom.xml` mudou de novo - releia antes
de seguir.

- [ ] **Step 2: Adicionar a versao do MapStruct nas properties**

Dentro de `<properties>`, ao lado de `<java.version>21</java.version>`:

```xml
        <mapstruct.version>1.6.3</mapstruct.version>
```

- [ ] **Step 3: Adicionar a dependencia do MapStruct**

No fim de `<dependencies>`, depois de `testcontainers-postgresql`:

```xml
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>
```

- [ ] **Step 4: Configurar o annotation processor**

Dentro de `<build><plugins>`, antes do `jacoco-maven-plugin`:

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                    </annotationProcessorPaths>
                    <compilerArgs>
                        <!-- injeta os mappers como bean do Spring, sem Mappers.getMapper() -->
                        <arg>-Amapstruct.defaultComponentModel=spring</arg>
                        <!-- campo de destino sem origem = erro de compilacao, nao silencio -->
                        <arg>-Amapstruct.unmappedTargetPolicy=ERROR</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
```

A versao do `maven-compiler-plugin` vem do `spring-boot-starter-parent` - nao fixar.

- [ ] **Step 5: Verificar**

```bash
mvn -q clean compile && mvn -q dependency:tree | grep -E "mapstruct|testcontainers|flyway"
```

Esperado: compilacao sem erro, `org.mapstruct:mapstruct:jar:1.6.3:compile` na
saida, e as linhas de `testcontainers` e `flyway` **ainda presentes** - elas nao
sao para mexer.

> `mvn compile` prova que o processador esta no classpath, nao que a geracao
> funciona (nao ha nenhum `@Mapper` no repo ainda). O primeiro mapper real e quem
> valida a geracao.

- [ ] **Step 6: Criar `docs/context/mapstruct.md`**

````markdown
# MapStruct

Conversao entre camadas e feita por **MapStruct**, nunca a mao e nunca por
reflection (`BeanUtils.copyProperties`, ModelMapper).

**Nao ha Lombok no projeto.** Escreva construtor e getters explicitos, ou use
`record` para DTO. Nao reintroduza Lombok sem perguntar.

## Configuracao ja ativa no `pom.xml`

- `defaultComponentModel=spring` -> todo `@Mapper` vira bean injetavel. Nao use
  `Mappers.getMapper(...)`.
- `unmappedTargetPolicy=ERROR` -> campo do destino sem origem **quebra o build**.
  Proposital: campo esquecido vira `null` silencioso em producao.

## Onde cada mapper mora

| Mapper | Pacote | Converte |
|---|---|---|
| `*RestMapper` | `interfaces.rest.<context>` | Request DTO <-> Command, Dominio -> Response DTO |
| `*PersistenceMapper` | `infrastructure.persistence.<context>` | Dominio <-> Entidade JPA |

Nunca um mapper unico que converte DTO direto para entidade JPA - isso pula o
dominio e quebra a direcao de dependencia de `arquitetura-ddd.md`.

## RUIM

```java
@Mapper
public interface ServiceOrderMapper {

    ServiceOrderMapper INSTANCE = Mappers.getMapper(ServiceOrderMapper.class); // ignora o Spring

    // DTO direto para entidade JPA: pula o dominio
    ServiceOrderJpaEntity toEntity(OpenServiceOrderRequest request);

    // regra de negocio dentro do mapper
    default BigDecimal calculateTotal(ServiceOrder order) {
        return order.services().stream()
                .map(ServiceOrderService::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

Problemas: `Mappers.getMapper` em vez de injecao, salta a camada de dominio, e o
mapper calcula o total - isso e regra, mora em `ServiceOrder`.

## BOM

```java
package com.postech.oficinamecanica.interfaces.rest.serviceorder;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ServiceOrderRestMapper {

    OpenServiceOrderCommand toCommand(OpenServiceOrderRequest request);

    @Mapping(target = "plate", source = "vehicle.plate.value")
    @Mapping(target = "totalPrice", source = "price")
    ServiceOrderResponse toResponse(ServiceOrder serviceOrder);
}
```

```java
package com.postech.oficinamecanica.infrastructure.persistence.serviceorder;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ServiceOrderPersistenceMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    ServiceOrderJpaEntity toEntity(ServiceOrder serviceOrder);

    ServiceOrder toDomain(ServiceOrderJpaEntity entity);
}
```

Injecao normal:

```java
public ServiceOrderController(OpenServiceOrderUseCase useCase, ServiceOrderRestMapper mapper) {
    this.useCase = useCase;
    this.mapper = mapper;
}
```

## Value Object no MapStruct

VO com construtor validante nao e mapeado sozinho. Declare metodos `default` no
proprio mapper:

```java
@Mapper
public interface CustomerRestMapper {

    CustomerResponse toResponse(Customer customer);

    default String map(Document document) {
        return document == null ? null : document.formatted();
    }

    default Document map(String document) {
        return document == null ? null : new Document(document);  // validacao no construtor
    }
}
```

## Regras

1. Mapper **so move dado**. Calculo, decisao e validacao ficam no dominio.
2. Um mapper por contexto e por fronteira. Nao um `GeneralMapper`.
3. Nao escrever a implementacao a mao - `target/generated-sources/annotations` e
   gerado no build. Nao commitar codigo gerado.
4. Build falhou com `Unmapped target property`? **Mapeie o campo** ou marque
   `@Mapping(target = "x", ignore = true)` com o motivo. Nao afrouxe a politica
   global.
````

- [ ] **Step 7: Conferir blocos**

Run: `awk '/^```/{n++} END{print (n%2==0) ? "OK" : "ERRO: bloco aberto"}' docs/context/mapstruct.md`

Esperado: `OK`

---

### Task 8: Doc de documentacao Swagger

**Files:**
- Create: `docs/context/swagger.md`

**Interfaces:**
- Consumes: `ServiceOrderController` de `arquitetura-ddd.md` (Task 5), agora anotado.
- Produces: `docs/context/swagger.md` para o indice (Task 10).

- [ ] **Step 1: Criar `docs/context/swagger.md`**

````markdown
# Documentacao Swagger (springdoc-openapi)

Toda controller e todo endpoint sao documentados. Endpoint sem `@Operation` nao
passa em review.

UI: `http://localhost:8080/swagger-ui.html`
JSON: `http://localhost:8080/v3/api-docs`

(Os dois caminhos estao fixados em `application.yml`, secao `springdoc`.)

**Texto das anotacoes:** o publico do Swagger e humano, entao `summary` e
`description` podem ser em portugues. **Identificador continua em ingles** - nome
de classe, metodo, campo e path.

## Anotacoes obrigatorias

| Nivel | Anotacao | Obrigatorio |
|---|---|---|
| Classe | `@Tag(name, description)` | sim |
| Metodo | `@Operation(summary, description)` | sim |
| Metodo | `@ApiResponses` com **todos** os status que o endpoint retorna | sim |
| Path/query param | `@Parameter(description, example)` | sim |
| DTO | `@Schema(description, example)` nos campos | sim |

Pacote correto: `io.swagger.v3.oas.annotations.*` (OpenAPI 3).
`io.swagger.annotations.*` e Springfox/Swagger 2 - **nao existe neste projeto**.

> **Autenticacao esta fora do escopo atual.** Nao ha Spring Security nem JWT no
> `pom.xml`. Nao documente `@SecurityRequirement`, esquema `bearer-jwt` nem
> resposta `401` - seria documentar comportamento que a API nao tem.

## RUIM

```java
@RestController
@RequestMapping("/api/ordens-servico")             // path em portugues
public class ServiceOrderController {

    @Operation(summary = "Abrir")                  // resume nada
    @PostMapping
    public ServiceOrderResponse open(@RequestBody OpenServiceOrderRequest request) {
        ...
    }

    @ApiOperation("Busca OS")                      // Swagger 2, pacote errado
    @GetMapping("/{id}")
    public ServiceOrderResponse byId(@PathVariable Long id) {   // param sem descricao
        ...
    }
}
```

Faltando: `@Tag` na classe, nenhum `@ApiResponses` (o Swagger anuncia so 200,
mentindo sobre 201/404), `summary` que repete o nome do metodo, anotacao de
Swagger 2, path em portugues.

## BOM

```java
@RestController
@RequestMapping("/api/service-orders")
@Tag(name = "Service Orders",
     description = "Abertura e acompanhamento do ciclo de vida da ordem de servico")
public class ServiceOrderController {

    private final OpenServiceOrderUseCase openServiceOrder;
    private final ServiceOrderRestMapper mapper;

    public ServiceOrderController(OpenServiceOrderUseCase openServiceOrder,
                                  ServiceOrderRestMapper mapper) {
        this.openServiceOrder = openServiceOrder;
        this.mapper = mapper;
    }

    @Operation(
        summary = "Abre uma ordem de servico",
        description = """
            Registra a entrada do veiculo na oficina com status RECEIVED.
            O veiculo precisa estar previamente cadastrado - a ordem nao cria veiculo.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ordem aberta",
            content = @Content(schema = @Schema(implementation = ServiceOrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Payload invalido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veiculo nao encontrado para a placa",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOrderResponse open(@RequestBody @Valid OpenServiceOrderRequest request) {
        return mapper.toResponse(openServiceOrder.execute(mapper.toCommand(request)));
    }

    @Operation(summary = "Consulta uma ordem de servico pelo identificador")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ordem encontrada"),
        @ApiResponse(responseCode = "404", description = "Ordem inexistente",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ServiceOrderResponse byId(
            @Parameter(description = "Identificador da ordem de servico", example = "42", required = true)
            @PathVariable Long id) {
        ...
    }
}
```

## DTO documentado

```java
@Schema(description = "Dados para abertura de uma ordem de servico")
public record OpenServiceOrderRequest(

        @Schema(description = "Placa do veiculo, formato antigo ou Mercosul",
                example = "ABC1D23", requiredMode = RequiredMode.REQUIRED)
        @NotBlank String plate,

        @Schema(description = "Problema relatado pelo cliente na recepcao",
                example = "Barulho no freio dianteiro ao frear")
        @NotBlank @Size(max = 500) String reportedIssue
) {}
```

## Regras

1. `description` explica **regra de negocio**, nao repete o `summary`.
2. Liste todos os status que o endpoint realmente retorna hoje. Nao antecipe
   `401`/`403` enquanto nao houver autenticacao no projeto.
3. `example` em todo `@Schema` de campo: e o que popula o "Try it out".
4. Anotacao Swagger **nunca** entra em classe de `domain`. Elas moram nos DTO de
   `interfaces.rest`.
5. Path de URL em ingles e kebab-case: `/api/service-orders`, nunca
   `/api/ordens-servico` nem `/api/serviceOrders`.
6. Config global (bean `OpenAPI` com titulo, versao e descricao) fica em
   `infrastructure.config`, uma vez so - nao repetir por controller.
````

- [ ] **Step 2: Conferir blocos**

Run: `awk '/^```/{n++} END{print (n%2==0) ? "OK" : "ERRO: bloco aberto"}' docs/context/swagger.md`

Esperado: `OK`

---

### Task 9: Doc de ferramentas e skills

Substitui o `.claude/SKILLS.md` que o `setup-ai.sh` gera hoje - e gera quebrado,
por causa de um `EOF` duplicado no heredoc.

**Files:**
- Create: `docs/context/ferramentas-e-skills.md`

**Interfaces:**
- Produces: `docs/context/ferramentas-e-skills.md`, referenciado pelo indice (Task 10) e pelas rules (Tasks 11 e 13). Substitui a geracao de `.claude/SKILLS.md` removida na Task 12.

- [ ] **Step 1: Criar `docs/context/ferramentas-e-skills.md`**

````markdown
# Ferramentas e skills de IA do projeto

Instaladas por `./setup-ai.sh`. Este arquivo diz **o que cada uma faz e quando
vale a pena** - a instalacao mora no script, nao aqui.

## Pre-requisitos

**O `setup-ai.sh` verifica, mas nao instala.** Ele falha com instrucao se algo
faltar. O motivo: instalar node via `brew` atropelaria quem gerencia versao com
`nvm`, e o script nao deve mexer no ambiente de quem o roda.

| Ferramenta | Como instalar | Quem depende |
|---|---|---|
| `git` | `brew install git` | tudo (o repo em si) |
| `npm` | `nvm install --lts` ou `brew install node` | Context7 (`npx ctx7 setup`) |
| `mvn` | `brew install maven` | build e testes do projeto |

Nao sao verificados pelo script, mas voce vai precisar: **JDK 21** (o `pom.xml`
exige), **Docker** (Testcontainers e `docker compose`), o CLI do **Claude Code** e
`curl` (para o RTK).

## Resumo

| Ferramenta | O que faz | Como ativa | So no Claude Code? |
|---|---|---|---|
| **RTK** | Comprime saida de comandos de terminal | hook automatico | sim |
| **Caveman** | Respostas terse, sem prosa | sempre ativo | sim (plugin) |
| **Ponytail** | YAGNI: escreve o minimo que funciona | sempre ativo | sim (plugin) |
| **Superpowers** | Skills de processo (plano, TDD, debug, review) | `/superpowers:<skill>` | sim (plugin) |
| **Context7** | Busca doc atualizada de lib/framework | MCP, automatico | Claude e Cursor |

## RTK

Proxy de CLI que corta 60-90% dos tokens de saida de comando. Ja reescreve
`git status`, `ls`, `grep` via hook - transparente.

```bash
rtk --version         # confirma instalacao
rtk gain              # economia acumulada
rtk gain --history    # por comando
rtk proxy <cmd>       # roda sem filtro, para debug
```

Colisao de nome: existe outro binario `rtk` (Rust Type Kit). Se `rtk gain`
falhar, e o binario errado no PATH.

## Caveman

Reduz a prosa da resposta. Codigo, commits e avisos de seguranca ficam normais.
`/caveman lite|full|ultra`. Desliga com "stop caveman".

## Ponytail

Forca a solucao mais simples que funciona: pergunta se a coisa precisa existir,
reusa o que ja tem no repo, stdlib antes de dependencia nova.
`/ponytail lite|full|ultra`.

Combina com este projeto porque o escopo e MVP - nao arquitetura especulativa.
Nunca simplifica validacao de entrada, seguranca ou erro que causa perda de dado.

## Superpowers

Skills de processo. As uteis aqui:

| Skill | Quando |
|---|---|
| `/superpowers:brainstorming` | Antes de projetar feature nova |
| `/superpowers:writing-plans` | Requisito pronto, virar plano de tasks |
| `/superpowers:test-driven-development` | Implementar regra de dominio |
| `/superpowers:systematic-debugging` | Bug ou teste vermelho |
| `/superpowers:requesting-code-review` | Antes de abrir PR |
| `/superpowers:finishing-a-development-branch` | Feature pronta, integrar |

## Context7 (MCP)

Busca doc **atual** de biblioteca em vez de confiar no knowledge cutoff.

**Neste projeto isso nao e opcional.** O Spring Boot 4 renomeou starters e moveu
pacotes de anotacao de teste; responder de memoria da 3.x gera codigo que nao
compila. Sempre que a pergunta envolver Spring Boot, Spring Data JPA, Flyway,
MapStruct, springdoc-openapi ou Testcontainers, consulte antes.

Cite a versao na pergunta: "como usar @DataJpaTest com Testcontainers no Spring
Boot 4" traz resultado melhor que "como testar repository".

Nao use para refatorar, debugar regra de negocio ou escrever script - so para doc
de biblioteca.

## O que nao esta instalado (e nao deve ser adicionado sem perguntar)

- Nada que envie codigo deste repo para servico externo alem dos ja listados.
- Nenhum plugin que escreva no Trello ou no Miro - o board e atualizado a mao.
````

- [ ] **Step 2: Conferir blocos**

Run: `awk '/^```/{n++} END{print (n%2==0) ? "OK" : "ERRO: bloco aberto"}' docs/context/ferramentas-e-skills.md`

Esperado: `OK`

---

### Task 10: Dicionario de contextos (`docs/context/README.md`)

O indice que as rules das duas IAs apontam. E o arquivo mais importante do harness.

**Files:**
- Create: `docs/context/README.md`

**Interfaces:**
- Consumes: todos os arquivos das Tasks 1-9, pelos caminhos exatos.
- Produces: `docs/context/README.md` - referenciado por `CLAUDE.md` (Task 13) e por `.cursor/rules/00-projeto.mdc` (Task 11).

- [ ] **Step 1: Criar `docs/context/README.md`**

````markdown
# Dicionario de contextos

Indice do que ler **antes** de mexer no codigo. Nao leia tudo - leia o que a
tarefa pede. Cada linha e "quando isso -> abra aquilo".

## Regras que valem para tudo

1. **Codigo e 100% em ingles** - pasta, arquivo, classe, metodo, variavel, campo,
   enum, nome de teste, path de URL. Docs e conversa em portugues. Os nomes saem
   de `modelo-de-dados.md`, que espelha o schema.
2. **Spring Boot 4** - starters e pacotes de anotacao mudaram em relacao a 3.x.
   Consulte o Context7 antes de escrever import de Spring; nao va de memoria.
3. **Flyway cria o schema**, `ddl-auto` e `validate`. Mudou entidade? Migration
   nova junto.

## Tabela de redirecionamento

| Vou fazer isso... | Leia primeiro |
|---|---|
| Qualquer coisa neste repo | `CLAUDE.md` (raiz) - stack, proibicoes, comandos |
| Descobrir o nome certo de uma classe, campo ou tabela | `docs/context/modelo-de-dados.md` |
| Escrever migration / mudar schema | `docs/context/modelo-de-dados.md` |
| Criar/alterar entidade, agregado, VO, enum de status | `docs/context/dominio-e-linguagem-ubiqua.md` |
| Entender o que um termo do time significa | `docs/context/dominio-e-linguagem-ubiqua.md` (glossario PT->EN) |
| Decidir em que pacote a classe vai | `docs/context/arquitetura-ddd.md` |
| Criar controller, use case, repository, DTO | `docs/context/arquitetura-ddd.md` |
| Escrever ou revisar teste | `docs/context/testes.md` |
| Converter DTO <-> dominio <-> entidade JPA | `docs/context/mapstruct.md` |
| Anotar endpoint para o Swagger | `docs/context/swagger.md` |
| Entender que ferramenta de IA esta ativa | `docs/context/ferramentas-e-skills.md` |
| Mexer no CI, Sonar, Trivy, Dependency-Check | `CLAUDE.md`, secao "Git / PR" |

## Arquivos

| Arquivo | Conteudo |
|---|---|
| `docs/context/modelo-de-dados.md` | Schema SQL, regra tabela->classe, como escrever migration, perguntas em aberto. **Fonte de verdade dos nomes.** |
| `docs/context/dominio-e-linguagem-ubiqua.md` | O negocio, glossario PT->EN, agregado `ServiceOrder`, fluxo de status, VOs obrigatorios, pontos em aberto. |
| `docs/context/arquitetura-ddd.md` | Mapa de pacotes, direcao de dependencia, responsabilidade por arquivo, bom/ruim de controller, use case, repository e entidade. |
| `docs/context/testes.md` | Que teste para que codigo, **imports do Boot 4**, nomenclatura em ingles, bom/ruim, Testcontainers (e por que nao H2). |
| `docs/context/mapstruct.md` | Onde o mapper mora, config do annotation processor, VO no MapStruct, bom/ruim. |
| `docs/context/swagger.md` | Anotacoes obrigatorias por nivel, bom/ruim de controller e DTO. |
| `docs/context/ferramentas-e-skills.md` | Pre-requisitos, RTK, Caveman, Ponytail, Superpowers, Context7. |

Fora daqui, de proposito: `docs/edital-tech-challenge.md` (requisito de entrega,
assunto humano) e a justificativa do PostgreSQL (no `README.md`). Nenhum dos dois
muda uma linha de codigo - nao traga de volta para `docs/context/`.

## Antes de decidir sozinho - PARE

Os **pontos em aberto** estao em `docs/context/dominio-e-linguagem-ubiqua.md` e em
`docs/context/modelo-de-dados.md` (secao "Perguntas em aberto do schema"). Se a
tarefa esbarrar em um deles, **pergunte ao usuario**. Nao assuma a regra.

Tambem nao decida sozinho:

- trocar versao de Java, Spring Boot ou banco;
- **reintroduzir Spring Security / JWT** (removidos de proposito);
- reintroduzir Lombok;
- adicionar H2 (o banco de teste e Postgres via Testcontainers);
- adicionar starter ou dependencia nova (os starters de teste do Boot 4 sao
  granulares - falta um? pergunte);
- editar migration ja aplicada (o Flyway falha por checksum - crie uma nova);
- renomear o pacote raiz `com.postech.oficinamecanica`;
- mudar a estrutura de camadas;
- alterar o Trello ou o Miro.

## Manutencao

Ao adicionar um contexto novo em `docs/context/`, adicione a linha nas duas
tabelas acima. As rules de IA (`CLAUDE.md`, `.cursor/rules/`) apontam para este
indice - elas nao listam arquivo por arquivo, entao so este arquivo precisa mudar.
````

- [ ] **Step 2: Verificar que todo caminho citado existe**

```bash
grep -o '`docs/[^`]*\.md`\|`CLAUDE\.md`' docs/context/README.md | tr -d '`' | sort -u \
  | while read f; do [ -f "$f" ] || echo "QUEBRADO: $f"; done; echo fim
```

Esperado: so `fim`.

---

### Task 11: Template da rule do Cursor

O conteudo nao e duplicado: a rule so aponta para `docs/context/README.md`.

**Files:**
- Create: `docs/ai/cursor-rule.mdc.template`
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `docs/context/README.md` (Task 10).
- Produces: `docs/ai/cursor-rule.mdc.template`, lido pelo `setup-ai.sh` (Task 12) e copiado para `.cursor/rules/00-projeto.mdc`.

- [ ] **Step 1: Criar `docs/ai/cursor-rule.mdc.template`**

```markdown
---
description: Regras do projeto Oficina Mecanica API - dicionario de contextos
globs:
alwaysApply: true
---

# Oficina Mecanica API

Back-end DDD de oficina mecanica. Java 21, **Spring Boot 4.0.7**, PostgreSQL 16,
Flyway, MapStruct 1.6.3, springdoc-openapi, Testcontainers.
**Sem Lombok. Sem Spring Security / JWT.**

## Codigo e 100% em ingles

Pasta, arquivo, pacote, classe, metodo, variavel, campo, constante, enum, nome de
teste e path de URL: ingles, sempre. Nunca misturar (`salvarCustomer`,
`CustomerRepositorio` sao erros). Comentario no codigo tambem em ingles.

Docs, conversa e corpo de commit: portugues.

Excecao unica: o pacote raiz `com.postech.oficinamecanica` permanece em
portugues. Nao renomear.

Os nomes saem do schema, em `docs/context/modelo-de-dados.md`. E `Material` (nao
`Part`), `ServiceOrder` (nao `WorkOrder`), `Customer` (nao `Client`).

## Spring Boot 4, nao 3.x

Starters e pacotes de anotacao mudaram. `spring-boot-starter-web` virou
`spring-boot-starter-webmvc`; `@DataJpaTest` e `@AutoConfigureTestDatabase`
mudaram de pacote. **Consulte a doc atual antes de escrever import de Spring** -
nao va de memoria. Os imports corretos estao em `docs/context/testes.md`.

## Flyway cria o schema

`ddl-auto` e `validate`: o Hibernate so confere. Mudou entidade? Migration nova em
`src/main/resources/db/migration/`. **Nunca edite migration ja aplicada** - o
Flyway falha por checksum.

## Leia o contexto antes de codar

O indice esta em **`docs/context/README.md`**, com a tabela "vou fazer isso ->
leia aquilo". Abra o indice primeiro e siga so o arquivo que a tarefa pede:

- nome de classe, campo, tabela ou migration -> `docs/context/modelo-de-dados.md`
- dominio, glossario, fluxo de status -> `docs/context/dominio-e-linguagem-ubiqua.md`
- em que pacote a classe vai, controller/use case/repository -> `docs/context/arquitetura-ddd.md`
- testes -> `docs/context/testes.md`
- conversao entre camadas -> `docs/context/mapstruct.md`
- anotacao de endpoint -> `docs/context/swagger.md`
- ferramentas de IA ativas -> `docs/context/ferramentas-e-skills.md`

## Nao decida sozinho - pergunte

- Os **pontos em aberto** em `dominio-e-linguagem-ubiqua.md` e as perguntas do
  schema em `modelo-de-dados.md`.
- Trocar versao de Java, Spring Boot ou banco.
- Reintroduzir Lombok, Spring Security / JWT, ou adicionar H2.
- Adicionar starter/dependencia nova.
- Editar migration ja aplicada.
- Renomear o pacote raiz ou mudar a estrutura de camadas.
- Alterar cartao no Trello ou board do Miro - isso e feito fora do codigo.
- Habilitar upload de SARIF para a aba Security do GitHub (repo privado, sem
  Advanced Security) - relatorios ficam como artifact.

## Sempre

- Branch `feature/<nome-da-tarefa>`. PR para `main` precisa de aprovacao de outra
  pessoa.
- Conventional commits, corpo em portugues.
- Cobertura minima 80% (JaCoCo).
- `BigDecimal` para dinheiro, nunca `double`.
```

- [ ] **Step 2: Ignorar os artefatos gerados**

Acrescente ao fim do `.gitignore`:

```gitignore

# gerados por ./setup-ai.sh (a fonte commitada e docs/)
.cursor/
.claude/settings.local.json
```

- [ ] **Step 3: Confirmar que o `.gitignore` pegou**

Run: `git check-ignore -v .cursor/rules/00-projeto.mdc .claude/settings.local.json`

Esperado: duas linhas apontando para as regras recem-adicionadas.

> Se `.claude/settings.local.json` ja estiver rastreado pelo git, ele continua
> aparecendo no `status` mesmo com a regra nova. Quem for commitar decide se roda
> `git rm --cached .claude/settings.local.json` - este plano nao mexe no index.

---

### Task 12: Reescrever `setup-ai.sh`

Hoje o script e macOS-only sem perguntar, nao verifica pre-requisitos, nao pergunta
a IA, gera docs (que agora sao commitados), gera um `Makefile` e tem um **bug
real**: o heredoc de `.claude/SKILLS.md` tem um `EOF` extra, que fecha o heredoc
cedo e faz o shell tentar executar `EOF` como comando.

**Files:**
- Modify: `setup-ai.sh` (reescrita completa)
- Delete: `Makefile`, se existir (residuo de execucao anterior do script antigo)

**Interfaces:**
- Consumes: `docs/ai/cursor-rule.mdc.template` (Task 11).
- Produces: `.cursor/rules/00-projeto.mdc` e/ou plugins do Claude instalados. Aceita `--ai=`, `--os=`, `--yes` para rodar sem interacao (usado no teste do Step 4).

- [ ] **Step 1: Substituir `setup-ai.sh` inteiro**

```bash
#!/usr/bin/env bash
set -euo pipefail

# setup-ai.sh - prepara o ambiente de IA deste repositorio.
#
# NAO gera documentacao: os docs sao commitados em docs/.
# NAO instala pre-requisito: so verifica e avisa (quem usa nvm nao quer um
# "brew install node" por baixo).
#
# Uso:
#   ./setup-ai.sh                      # interativo
#   ./setup-ai.sh --ai=cursor --yes    # sem perguntar
#   ./setup-ai.sh --ai=claude --os=macos

GREEN='\033[0;32m'; BLUE='\033[0;34m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
log_section() { printf "\n${BLUE}== %s ==${NC}\n\n" "$1"; }
log_ok()      { printf "${GREEN}OK   %s${NC}\n" "$1"; }
log_warn()    { printf "${YELLOW}...  %s${NC}\n" "$1"; }
log_err()     { printf "${RED}ERRO %s${NC}\n" "$1" >&2; }

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_ROOT"

AI=""; OS_ALVO=""; ASSUME_YES="no"
for arg in "$@"; do
  case "$arg" in
    --ai=*)  AI="${arg#*=}" ;;
    --os=*)  OS_ALVO="${arg#*=}" ;;
    --yes|-y) ASSUME_YES="yes" ;;
    -h|--help) sed -n '3,14p' "$0"; exit 0 ;;
    *) log_err "flag desconhecida: $arg"; exit 1 ;;
  esac
done

# ------------------------------------------------------------------
# 1. Sistema operacional
# ------------------------------------------------------------------
log_section "1. Sistema operacional"

if [ -z "$OS_ALVO" ]; then
  case "$OSTYPE" in
    darwin*) OS_ALVO="macos" ;;
    linux*)  OS_ALVO="linux" ;;
    *)       OS_ALVO="desconhecido" ;;
  esac
fi

case "$OS_ALVO" in
  macos)
    log_ok "macOS detectado"
    ;;
  linux|windows|desconhecido)
    log_err "OS '$OS_ALVO' ainda nao e suportado por este script."
    echo "Instale a mao e rode de novo com --os=macos para so gerar as rules:"
    echo "  - git, npm, mvn, JDK 21, Docker"
    echo "  - Claude Code:  https://claude.com/claude-code"
    echo "  - plugins:      caveman, ponytail, superpowers"
    exit 1
    ;;
  *)
    log_err "valor invalido para --os: $OS_ALVO (use macos)"; exit 1 ;;
esac

# ------------------------------------------------------------------
# 2. Pre-requisitos (verifica, NAO instala)
# ------------------------------------------------------------------
log_section "2. Pre-requisitos"

FALTANDO=()
verificar() {  # $1 = comando, $2 = como instalar
  if command -v "$1" >/dev/null 2>&1; then
    log_ok "$1 ($("$1" --version 2>&1 | head -1))"
  else
    log_err "$1 nao encontrado - $2"
    FALTANDO+=("$1")
  fi
}

verificar git "brew install git"
verificar npm "instale o Node (nvm install --lts, ou brew install node)"
verificar mvn "brew install maven"

if [ ${#FALTANDO[@]} -gt 0 ]; then
  log_err "faltam ${#FALTANDO[@]} pre-requisito(s): ${FALTANDO[*]}"
  echo "Instale e rode de novo."
  exit 1
fi

# ------------------------------------------------------------------
# 3. Qual IA
# ------------------------------------------------------------------
log_section "3. Assistente de IA"

if [ -z "$AI" ]; then
  if [ "$ASSUME_YES" = "yes" ]; then
    AI="claude"
  else
    echo "Qual assistente voce usa neste repositorio?"
    echo "  1) Claude Code"
    echo "  2) Cursor"
    echo "  3) Ambos"
    read -r -p "Escolha [1-3]: " escolha
    case "$escolha" in
      1) AI="claude" ;;
      2) AI="cursor" ;;
      3) AI="ambos" ;;
      *) log_err "escolha invalida"; exit 1 ;;
    esac
  fi
fi

case "$AI" in
  claude|cursor|ambos) log_ok "IA escolhida: $AI" ;;
  *) log_err "valor invalido para --ai: $AI (use claude, cursor ou ambos)"; exit 1 ;;
esac

# ------------------------------------------------------------------
# 4. Claude Code: skills e plugins
# ------------------------------------------------------------------
instalar_claude() {
  log_section "4. Claude Code: skills e plugins"

  if ! command -v claude >/dev/null 2>&1; then
    log_err "Claude Code nao encontrado. Instale em https://claude.com/claude-code e rode de novo."
    return 1
  fi
  log_ok "Claude Code disponivel"

  if command -v rtk >/dev/null 2>&1; then
    log_ok "RTK ja instalado ($(rtk --version 2>&1 | head -1))"
  else
    log_warn "instalando RTK..."
    curl -fsSL https://raw.githubusercontent.com/rtk-ai/rtk/refs/heads/master/install.sh | sh
    case ":$PATH:" in
      *":$HOME/.local/bin:"*) ;;
      *) echo 'export PATH="$HOME/.local/bin:$PATH"' >> "$HOME/.zshrc"
         export PATH="$HOME/.local/bin:$PATH" ;;
    esac
    log_ok "RTK instalado"
  fi
  rtk init -g >/dev/null 2>&1 || log_warn "rtk init falhou (siga sem ele)"

  log_warn "instalando plugins (caveman, ponytail, superpowers)..."
  claude plugin marketplace add JuliusBrussee/caveman   >/dev/null 2>&1 || true
  claude plugin install caveman@caveman                 >/dev/null 2>&1 || true
  claude plugin marketplace add DietrichGebert/ponytail >/dev/null 2>&1 || true
  claude plugin install ponytail@ponytail               >/dev/null 2>&1 || true
  claude plugin install superpowers@claude-plugins-official >/dev/null 2>&1 || true
  log_ok "plugins instalados"

  if command -v npx >/dev/null 2>&1; then
    npx ctx7 setup --claude >/dev/null 2>&1 && log_ok "Context7 configurado" \
      || log_warn "Context7 nao configurado (rode 'npx ctx7 setup --claude' a mao)"
  fi

  log_ok "rule do Claude: CLAUDE.md ja esta versionado na raiz, nada a gerar"
}

# ------------------------------------------------------------------
# 5. Cursor: rule gerada a partir do template commitado
# ------------------------------------------------------------------
instalar_cursor() {
  log_section "5. Cursor: rules"

  local template="docs/ai/cursor-rule.mdc.template"
  local destino=".cursor/rules/00-projeto.mdc"

  if [ ! -f "$template" ]; then
    log_err "template nao encontrado: $template"
    return 1
  fi

  mkdir -p "$(dirname "$destino")"
  cp "$template" "$destino"
  log_ok "gerado $destino"
  log_warn "Cursor nao instala plugin por CLI - RTK/Caveman/Ponytail sao so do Claude Code."
  log_warn "Para Context7 no Cursor, adicione o MCP em Settings > MCP."
}

case "$AI" in
  claude) instalar_claude ;;
  cursor) instalar_cursor ;;
  ambos)  instalar_claude || true; instalar_cursor ;;
esac

# ------------------------------------------------------------------
# 6. Fim
# ------------------------------------------------------------------
log_section "Setup finalizado"
echo "Contexto do projeto (commitado, nao gerado):"
echo "  docs/context/README.md   <- dicionario: comece por aqui"
echo "  CLAUDE.md                <- rule do Claude Code"
echo "  docs/ai/                 <- templates de rule"
echo
echo "Proximos passos:"
echo "  1. Reinicie o assistente para carregar as rules/plugins."
echo "  2. mvn test   # testes + cobertura JaCoCo"
```

- [ ] **Step 2: Apagar o Makefile, se existir**

O script antigo criava um. Quem ja rodou tem o arquivo na raiz; quem clonou agora,
nao. Por isso o comando tolera a ausencia.

```bash
rm -f Makefile
```

- [ ] **Step 3: Conferir sintaxe**

Run: `bash -n setup-ai.sh && chmod +x setup-ai.sh && echo "sintaxe OK"`

Esperado: `sintaxe OK`

- [ ] **Step 4: Testar o caminho do Cursor de ponta a ponta**

```bash
rm -rf .cursor
./setup-ai.sh --ai=cursor --os=macos --yes
test -f .cursor/rules/00-projeto.mdc \
  && grep -q "docs/context/README.md" .cursor/rules/00-projeto.mdc \
  && grep -q "100% em ingles" .cursor/rules/00-projeto.mdc \
  && echo "PASS: rule gerada, aponta para o dicionario e carrega a regra de idioma" \
  || echo "FAIL"
```

Esperado: `PASS: rule gerada, aponta para o dicionario e carrega a regra de idioma`

- [ ] **Step 5: Testar a rejeicao de OS nao suportado**

Run: `./setup-ai.sh --ai=cursor --os=linux --yes; echo "exit=$?"`

Esperado: `ERRO OS 'linux' ainda nao e suportado` e `exit=1`.

- [ ] **Step 6: Confirmar pre-requisitos e ausencia de Makefile**

```bash
for c in git npm mvn; do command -v $c >/dev/null && echo "ok   $c" || echo "FALTA $c"; done
test ! -f Makefile && echo "OK: sem Makefile"
git status --short .cursor || true
```

Esperado: `ok` para os tres, `OK: sem Makefile`, nenhuma linha de `git status`.

---

### Task 13: Enxugar `CLAUDE.md` para virar rule + ponteiro

**O `CLAUDE.md` ja foi atualizado quanto a stack** - ele ja documenta Boot 4.0.7,
Flyway, ausencia de Lombok e ausencia de security. Nao ha nada a corrigir ali.

O problema e outro: ele **cresceu para ~12K** e carrega em toda conversa, com
secoes que agora vivem em `docs/context/` (linguagem ubiqua, mapa de camadas,
estrategia de teste). O que falta:

1. cortar o que virou doc, deixando o ponteiro no lugar;
2. adicionar a **regra de idioma** (codigo em ingles);
3. tirar as referencias ao edital, que saiu de `docs/context/` na Task 1.

Antes de reescrever, **leia o `CLAUDE.md` atual** - ele mudou depois que este
plano foi escrito e pode ter mudado de novo.

**Files:**
- Modify: `CLAUDE.md` (reescrita)
- Modify: `README.md` (secao apontando o harness)
- Delete: `tasks.MD` (arquivo vazio, 0 bytes)

**Interfaces:**
- Consumes: `docs/context/README.md` (Task 10) e todos os docs das Tasks 1-9.
- Produces: `CLAUDE.md` como rule final do Claude Code.

- [ ] **Step 1: Reescrever `CLAUDE.md`**

Mantenha **na integra** a secao de CI - o detalhe de skip em cascata, projectKey
por PR e limitacoes da Community Edition e conhecimento operacional que nao existe
em nenhum outro lugar. Corte o que virou doc.

````markdown
# CLAUDE.md

Rule do Claude Code neste repositorio. Curta de proposito - o contexto detalhado
esta em `docs/context/`, indexado por **`docs/context/README.md`** (o dicionario
de contextos).

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

## Antes de codar: leia o dicionario

**`docs/context/README.md`** tem a tabela "vou fazer isso -> leia aquilo". Abra
ele primeiro e siga so o que a tarefa pede:

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
````

- [ ] **Step 2: Apagar o `tasks.MD` vazio**

```bash
rm -f tasks.MD
```

- [ ] **Step 3: Adicionar a secao de contexto no `README.md`**

Insira logo apos a secao "## Stack" do `README.md`:

```markdown
## Contexto para desenvolvimento (e para assistentes de IA)

A documentacao de arquitetura, dominio e convencoes vive em `docs/context/`.
Comece por **[`docs/context/README.md`](docs/context/README.md)** - o dicionario
que diz qual arquivo ler para cada tipo de tarefa.

**Codigo neste repositorio e escrito em ingles**; a documentacao, em portugues.

Para preparar o ambiente de IA (Claude Code ou Cursor):

```bash
./setup-ai.sh
```

O script verifica os pre-requisitos, instala as skills e gera a rule do assistente
escolhido. Os docs sao versionados - o script nao os gera.
```

- [ ] **Step 4: Conferir se a secao "Stack" do README tambem envelheceu**

Run: `grep -nE "Spring Boot|JWT|Lombok|Actuator" README.md`

Esperado: se aparecer "Spring Boot 3.5.x", "JWT" ou "Actuator", atualize para
Spring Boot 4.0.7 e remova as mencoes a JWT - o `pom.xml` nao tem mais nada disso.

- [ ] **Step 5: Verificar que nao restou link quebrado**

```bash
grep -o '`docs/[^`]*\.md`' CLAUDE.md README.md | sed 's/.*`\(docs[^`]*\)`/\1/' | sort -u \
  | while read f; do [ -f "$f" ] || echo "QUEBRADO: $f"; done; echo fim
```

Esperado: so `fim`.

- [ ] **Step 6: Conferir a reducao**

Run: `wc -c CLAUDE.md`

Esperado: bem abaixo dos ~12K atuais (alvo ~6K). Se ficou maior, sobrou
conteudo que devia ter ido para `docs/context/`.

---

## Verificacao final

- [ ] **Todos os arquivos existem**

```bash
for f in docs/context/README.md docs/context/dominio-e-linguagem-ubiqua.md \
         docs/context/modelo-de-dados.md docs/context/arquitetura-ddd.md \
         docs/context/testes.md docs/context/mapstruct.md \
         docs/context/swagger.md docs/context/ferramentas-e-skills.md \
         docs/edital-tech-challenge.md docs/ai/cursor-rule.mdc.template \
         src/main/resources/db/migration/V1__initial_schema.sql; do
  [ -f "$f" ] && echo "ok   $f" || echo "FALTA $f"
done
```

- [ ] **Nenhum link quebrado em nenhum md**

```bash
grep -rho '`docs/[^`]*\.md`' --include=*.md . | tr -d '`' | sort -u \
  | while read f; do [ -f "$f" ] || echo "QUEBRADO: $f"; done; echo fim
```

- [ ] **Arquivos antigos sumiram de `docs/context/`**

```bash
ls docs/context/01-ddd-decisoes.md docs/context/02-trello-board.md \
   docs/context/03-decisao-banco-de-dados.md docs/context/00-edital-tech-challenge.md 2>&1 \
   | grep -c "No such file"
```
Esperado: `4`

- [ ] **Nenhum pacote de dominio em portugues**

```bash
find src -type d \( -name cliente -o -name veiculo -o -name peca -o -name servico -o -name ordemservico \) \
  | grep . && echo "FALHOU: pacote em portugues" || echo "OK: pacotes em ingles"
```

- [ ] **Build de pe, e a app sobe com o schema aplicado**

```bash
mvn -q clean compile && echo "BUILD OK"
mvn spring-boot:run     # Ctrl-C apos ver "Successfully applied 1 migration"
```

- [ ] **Script funciona**

```bash
bash -n setup-ai.sh && ./setup-ai.sh --ai=cursor --os=macos --yes \
  && test -f .cursor/rules/00-projeto.mdc && echo "SCRIPT OK"
```

- [ ] **As regras chegaram nas duas rules**

```bash
for f in CLAUDE.md docs/ai/cursor-rule.mdc.template; do
  grep -q "ingles" "$f" && grep -q "Boot 4" "$f" \
    && echo "ok   $f" || echo "FALTA regra em $f"
done
```

---

## Decisoes registradas nesta sessao

Ficam aqui para quem executar o plano nao reabrir a discussao:

| Decisao | Motivo |
|---|---|
| Codigo em ingles, docs em portugues | Pedido do usuario. Nomes saem do schema SQL. |
| Pacote raiz continua `com.postech.oficinamecanica` | Diff menor; nao mexe em `pom.xml`, main class nem `Dockerfile`. |
| `Material`, nao `Part`; `ServiceOrder`, nao `WorkOrder` | O schema SQL manda. |
| Lombok removido | Nao era usado; MapStruct cobre o que interessa. |
| Testcontainers mantido, H2 recusado | Ja esta no `pom.xml`; a maior parte da suite nao toca banco, entao o custo e baixo. |
| Spring Security e JJWT removidos | Decisao do usuario. Tabela `employee` ja existe no schema - modelo pronto, implementacao adiada. |
| Trello, ADR do banco e edital fora de `docs/context/` | Nao mudam uma linha de codigo; so gastam contexto do agente. |
| Concorrencia/lock fora de escopo | Nao ha reserva no schema, so `material_transaction`. |
| Makefile removido | So embrulhava `mvn` e `rtk gain`. |
| Script verifica pre-requisitos, nao instala | O usuario usa `nvm`; `brew install node` sobrescreveria a versao gerenciada. |
| Migration V1 entrou no plano | Nao e escopo extra: sem ela, Flyway + `ddl-auto: validate` impedem a app de subir. |

## Fora de escopo (proposital)

- **Implementar o dominio.** Este plano cria o harness; o codigo de `ServiceOrder`,
  `Customer` etc e trabalho de feature, com plano proprio. Os exemplos nos docs sao
  ilustrativos e nao compilam contra o repo atual.
- **Seed de dados.** A migration cria as tabelas, nao popula nada. Se precisar de
  dados de exemplo, e uma migration `V2__seed_*.sql` ou um `data.sql` - decisao do
  usuario.
- **Suporte a Linux/Windows no script.** A pergunta de OS existe e a ramificacao
  esta pronta; so o caminho macOS esta implementado, como pedido.
- **Instalar plugins no Cursor.** Cursor nao tem CLI de plugin. O script gera a
  rule e avisa o que fazer a mao (MCP do Context7).
- **CI validando os docs.** Se um link quebrado escapar mais de uma vez, ai sim
  vale um step de link check. Antes disso, nao.
