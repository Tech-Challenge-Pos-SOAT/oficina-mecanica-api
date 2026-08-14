# Modelo de dados

**Fonte de verdade dos nomes.** O schema esta certo. Se divergiu, o schema vence.

**Quem cria schema:** Flyway (`ddl-auto=validate`). Toda mudanca = arquivo novo em `src/main/resources/db/migration/V<n>__descricao_en.sql`. **Nunca edite migration ja aplicada** (Flyway falha com checksum mismatch). Criar nova migration sempre.

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

## Entidades — constraints e nuances

### Customer
- `document` VARCHAR(18): guarda CPF ou CNPJ. Tipo decidido pelo tamanho no VO `Document`.
- `email` UNIQUE, opcional.
- `status` ACTIVE/INACTIVE (padrão ACTIVE).

### Vehicle
- `customer_id` FK obrigatória.
- `plate` UNIQUE.
- `status` ACTIVE/INACTIVE (padrão ACTIVE).

### Material
- `price` NUMERIC(10,2): preço de catálogo. Copiado para `service_order_material.price` no momento da inclusão.
- `stock_quantity` INT: saldo atual.
- `stock_minimum` INT: limiar para alerta.
- `status` ACTIVE/INACTIVE (padrão ACTIVE).

### Service
- `price` NUMERIC(10,2): preço de catálogo. Copiado para `service_order_service.price` no momento da inclusão.
- `status` ACTIVE/INACTIVE (padrão ACTIVE).

### Employee
- `name` UNIQUE.
- `password` VARCHAR(255): hash bcrypt.
- `role` VARCHAR(50): função do funcionário.
- `status` ACTIVE/INACTIVE (padrão ACTIVE).

### ServiceOrder (agregado)
- `customer_id` FK obrigatória: cliente denormalizado. Ao abrir OS, copiar o dono vigente naquele momento (veículo pode trocar de dono depois).
- `vehicle_id` FK obrigatória.
- `price` NUMERIC(10,2): nulável até primeira aprovação.
- `status` VARCHAR(30): transições em `regras-de-negocio.md`.

### ServiceOrderHistory
- `status` VARCHAR(30): espelho do status da OS naquele momento.
- `price` NUMERIC(10,2): valor vigente naquela transição. Permite reconstruir histórico de orçamento.
- `author_type` VARCHAR(20): CUSTOMER / EMPLOYEE / SYSTEM.
- `author_id` BIGINT: nulo se author_type = SYSTEM.

### MaterialTransaction
- `type` VARCHAR(10): IN (entrada de estoque) ou OUT (saída por aprovação de OS).
- `service_order_id` BIGINT FK: nulo em entradas (compra de fornecedor); obrigatório em saídas.
- Único registro de movimentação; `material.stock_quantity` é sempre o saldo.
