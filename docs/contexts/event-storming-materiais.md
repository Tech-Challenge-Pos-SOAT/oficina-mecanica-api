# Event Storming — Peças e Insumos (Materiais)

Mapa do subdomínio de estoque como a aplicação o implementa hoje. Notação: **evento** (passado),
**comando** (imperativo), **política** (reação automática), **leitura** (consulta), **hotspot** (em aberto).

## 1. Cadastro no catálogo

| Tipo | Item | Onde |
|---|---|---|
| Ator | Atendente | `Employee` ACTIVE |
| Comando | Cadastrar material | `POST /api/materials` |
| Agregado | `Material` | recusa preço/saldo/mínimo negativos; nome único |
| Evento | **Material cadastrado** | status ACTIVE, saldo inicial e mínimo definidos |
| Comando | Atualizar material | `PUT /api/materials/{id}` |
| Evento | **Material atualizado** | nome, descrição, preço, mínimo |
| 🔴 Hotspot | Saldo inicial não gera movimentação | o extrato não explica o saldo |
| 🔴 Hotspot | O update regrava `stockQuantity` | baixa manual disfarçada de edição de catálogo |

## 2. Reposição de estoque

| Tipo | Item | Onde |
|---|---|---|
| Comando | Registrar entrada | `POST /api/materials/{id}/stock-entry` |
| Agregado | `Material.addStock()` | exige quantidade > 0 |
| Evento | **Estoque reposto** | saldo aumenta |
| Evento | **Movimentação de entrada registrada** | `type = IN`, sem ordem de serviço |
| 🔴 Hotspot | Entrada sem lock pessimista | duas reposições simultâneas perdem uma |

## 3. Peça entra no orçamento

| Tipo | Item | Onde |
|---|---|---|
| Ator | Mecânico | — |
| Comando | Incluir peça na OS | `POST /api/service-orders/{id}/materials` |
| Política | Só material `ACTIVE` entra | `InactiveMaterialException` |
| Agregado | `ServiceOrder` | item com preço unitário congelado |
| Evento | **Peça incluída na ordem** | `stock_debited = false` |
| Evento | **Orçamento recalculado** | Σ serviços + Σ (preço × quantidade) |
| 🔴 Hotspot | Não existe reserva | outra OS pode consumir a peça antes da aprovação |

## 4. Aprovação dispara a baixa (evento pivô)

| Tipo | Item | Onde |
|---|---|---|
| Ator | Cliente | rota pública, sem token |
| Comando | Aprovar orçamento | `POST /public/service-orders/{id}/approval` |
| Evento | **Orçamento aprovado** | `AWAITING_APPROVAL → IN_EXECUTION` |
| Política | Debitar cada peça pendente | só itens com `stock_debited = false` |
| Agregado | `Material.debitStock()` | `SELECT ... FOR UPDATE` |
| Evento | **Estoque debitado** | mesma transação da mudança de status |
| Evento | **Movimentação de saída registrada** | `type = OUT`, com `service_order_id` |
| Evento | **Baixa recusada por estoque insuficiente** | transação revertida; OS segue `AWAITING_APPROVAL` |
| 🔴 Hotspot | Aprovação é tudo ou nada | faltando um item, nenhum é debitado |

## 5. Recusa e devolução

| Tipo | Item | Onde |
|---|---|---|
| Comando | Recusar orçamento | `approved: false` |
| Evento | **Orçamento recusado** | OS vai para `FINISHED`, motivo em `observation` |
| 🔴 Hotspot | Não existe estorno | peça debitada não volta; devolução vira entrada avulsa |
| 🔴 Hotspot | Recusa de adicional encerra a OS | deveria voltar para `IN_EXECUTION` |

## 6. Consulta, alerta e descontinuação

| Tipo | Item | Onde |
|---|---|---|
| Leitura | Catálogo | `GET /api/materials?status=` |
| Leitura | Abaixo do mínimo | `GET /api/materials/low-stock` (saldo **<** mínimo) |
| Leitura | Extrato do material | `GET /api/materials/{id}/transactions` |
| Leitura | Extrato geral | `GET /api/material-transactions?type=IN\|OUT` |
| Comando | Inativar material | `PATCH /api/materials/{id}/status` |
| Evento | **Material inativado** | sai das listagens, não entra em OS, não sofre baixa |
| Política | Nada é deletado | histórico e extrato preservados |
| 🔴 Hotspot | Alerta é consultado, não notificado | ninguém é avisado ao cruzar o mínimo |

## Fronteira do subdomínio

`Material` e `MaterialTransaction` são agregados próprios. `ServiceOrder` os referencia por id e
**nunca altera saldo direto** — a única porta de saída do estoque é a política de baixa disparada
pela aprovação do orçamento.

---

## Diagramas

### Ciclo de vida da peça

```mermaid
flowchart TD
    A["Material cadastrado<br/>status ACTIVE"] --> B["Reposição<br/>POST /materials/{id}/stock-entry"]
    B --> C["Movimentação IN<br/>sem ordem de serviço"]
    C --> D["Saldo atualizado"]
    A --> D
    D --> E["Peça incluída no orçamento da OS<br/>POST /service-orders/{id}/materials"]
    E --> F["Preço congelado no item<br/>stock_debited = false"]
    F --> G{"Cliente aprova<br/>o orçamento?"}
    G -- "não" --> H["OS finalizada<br/>estoque intacto"]
    G -- "sim" --> I{"Saldo suficiente<br/>para todos os itens?"}
    I -- "não" --> J["Transação revertida<br/>OS segue AWAITING_APPROVAL"]
    I -- "sim" --> K["Estoque debitado<br/>SELECT FOR UPDATE"]
    K --> L["Movimentação OUT<br/>vinculada à OS"]
    L --> M["stock_debited = true"]
    M --> N["Extrato e saldo consistentes"]
    J --> G
```

### Máquina de estados da OS

```mermaid
stateDiagram-v2
    [*] --> RECEIVED: abrir OS
    RECEIVED --> IN_DIAGNOSIS: iniciar diagnóstico
    IN_DIAGNOSIS --> AWAITING_APPROVAL: enviar orçamento
    IN_DIAGNOSIS --> FINISHED: encerrar sem reparo
    AWAITING_APPROVAL --> IN_EXECUTION: cliente aprova (baixa de estoque)
    AWAITING_APPROVAL --> FINISHED: cliente recusa
    IN_EXECUTION --> AWAITING_APPROVAL: reparo adicional
    IN_EXECUTION --> FINISHED: serviços concluídos
    FINISHED --> DELIVERED: entregar veículo
    DELIVERED --> [*]
```

### Aprovação e baixa atômica

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant API as PublicServiceOrderController
    participant UC as ApproveServiceOrderBudgetUseCase
    participant OS as ServiceOrder
    participant SD as StockDebitUseCase
    participant M as Material
    participant MT as MaterialTransaction

    Cliente->>API: POST /public/service-orders/{id}/approval
    API->>UC: execute(orderId, customerDocument)
    UC->>UC: valida posse pelo CPF/CNPJ
    UC->>OS: approveBudget()
    Note over UC,MT: tudo dentro de uma única transação
    loop cada peça com stock_debited = false
        UC->>SD: execute(materialId, orderId, quantidade)
        SD->>M: findByIdForUpdate (SELECT FOR UPDATE)
        alt saldo suficiente
            SD->>M: debitStock(quantidade)
            SD->>MT: registra OUT vinculada à OS
        else saldo insuficiente
            SD-->>UC: InsufficientStockException
            UC-->>API: rollback total
            API-->>Cliente: 400 BUSINESS_RULE_VIOLATION
        end
    end
    UC->>OS: markStockDebited()
    UC-->>API: OS em IN_EXECUTION
    API-->>Cliente: 200 com orçamento e histórico
```

### Modelo de dados do estoque

```mermaid
erDiagram
    material ||--o{ material_transaction : "movimenta"
    material ||--o{ service_order_material : "é orçado em"
    service_order ||--o{ service_order_material : "contém"
    service_order ||--o{ material_transaction : "origina saída"

    material {
        bigint id PK
        varchar name
        numeric price "preço de catálogo"
        int stock_quantity "saldo atual"
        int stock_minimum "limiar de alerta"
        varchar status "ACTIVE / INACTIVE"
    }
    material_transaction {
        bigint id PK
        bigint material_id FK
        bigint service_order_id FK "nulo na entrada"
        int quantity "sempre positiva"
        varchar type "IN / OUT"
        timestamp created_at
    }
    service_order_material {
        bigint id PK
        bigint service_order_id FK
        bigint material_id FK
        int quantity
        numeric price "congelado na inclusão"
        boolean stock_debited "V7"
    }
```
