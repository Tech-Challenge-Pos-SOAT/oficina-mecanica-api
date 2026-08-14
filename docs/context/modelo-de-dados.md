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
