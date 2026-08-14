# Regras de negócio — validações e transições

**Leia quando:** implementar validação, mudar status, criar histórico, movimentar material, consultar indicador.
**Fonte:** decisões de domínio; schema vinculante em `modelo-de-dados.md`.

<regras_gerais>

- Entidades nunca deletadas, só inativadas (`EntityStatus = INACTIVE`).
- Toda transição de OS registra histórico com `price` vigente e `authorType`.
- `price` é congelado no momento da inclusão (`ServiceOrderService`, `ServiceOrderMaterial`); alterações futuras no catálogo não afetam ordens existentes.

</regras_gerais>

<entidades>

## Cliente
- Identificado único: CPF ou CNPJ (documento duplicado rejeita).
- E-mail único, opcional.
- Status: ACTIVE/INACTIVE.

## Veículo
- Referencia obrigatória a cliente.
- Placa única no sistema.
- Status: ACTIVE/INACTIVE.
- **Nota:** cliente pode mudar dono; ao abrir OS, copie o dono vigente para `ServiceOrder.customerId` (não leia por join de veículo).

## Serviço
- Preço de catálogo (`service.price`) copiado para `ServiceOrderService.price` no momento da inclusão.
- Futuras alterações do catálogo não afetam OS já existentes.
- Status: ACTIVE/INACTIVE.

## Material
- Preço de catálogo (`material.price`) copiado para `ServiceOrderMaterial.price` no momento da inclusão.
- Quantidade mínima configurada; alerta disponível na consulta.
- Status: ACTIVE/INACTIVE.

## Funcionário
- Nome único.
- Senha hash bcrypt.
- Status: ACTIVE/INACTIVE. Inativação preserva autoria em histórico.

</entidades>

<agregado_ordem_de_servico>

**ServiceOrder** é o agregado central. Todas as transições registram `ServiceOrderHistory` com `price` e `authorType`.

### Transições de status

| FROM | TO | Condição | Notas |
|---|---|---|---|
| `RECEIVED` | `IN_DIAGNOSIS` | — | Ordem recebida. |
| `IN_DIAGNOSIS` | `AWAITING_APPROVAL` | Diagnóstico feito. | `price` torna-se obrigatório. |
| `IN_DIAGNOSIS` | `COMPLETED` | — | Encerramento sem reparo. |
| `AWAITING_APPROVAL` | `IN_PROGRESS` | Aprovação. | **Dispara baixa atômica de material.** |
| `AWAITING_APPROVAL` | `COMPLETED` | Recusa. | Ordem cancelada. |
| `IN_PROGRESS` | `AWAITING_APPROVAL` | Reparo adicional. | Novo diagnóstico, novo orçamento. |
| `IN_PROGRESS` | `COMPLETED` | Reparo finalizado. | |
| `COMPLETED` | `DELIVERED` | Entrega ao cliente. | |

### Regras de preço
- `service_order.price` é **nulável** até primeira aprovação (diagnóstico → awaiting_approval).
- Cada histórico registra `price` vigente naquele momento.
- Reconstruir orçamento a partir de `ServiceOrderHistory.price`.

### Regras de material (baixa/saída)
- Baixa ocorre **apenas** em transição `AWAITING_APPROVAL → IN_PROGRESS`.
- Verificação e débito em **uma instrução atômica** (falha = sem transição).
- Se estoque insuficiente, falha da transição sem alterar status.
- Cada saída bem-sucedida cria `MaterialTransaction` (type=OUT, vinculada à OS).

</agregado_ordem_de_servico>

<movimentacoes_de_material>

`MaterialTransaction` é o único registro de movimentação. Não existe tabela de reserva.

| Tipo | Vinculação | Notas |
|---|---|---|
| `IN` (entrada) | Sem OS | Compra de fornecedor, devolução, ajuste. |
| `OUT` (saída) | OS obrigatória | Gerada apenas por aprovação de OS. |

- `material.stock_quantity` é sempre o saldo atual.
- `material_transaction` é o extrato.

</movimentacoes_de_material>

<indicadores>

- **Tempo médio de execução:** intervalo entre entrada em `IN_PROGRESS` e chegada em `COMPLETED`, calculado do histórico de status.
- **Materiais abaixo do mínimo:** `stock_quantity < minimum_quantity`.

</indicadores>

<glossario>

| PT | EN | Contexto |
|---|---|---|
| Ordem de Serviço | ServiceOrder | Agregado central. |
| Baixa | Stock debit | Consumo de material em aprovação. |
| Histórico | ServiceOrderHistory | Registro de transição. |
| Movimentação | MaterialTransaction | Registro de entrada/saída. |
| Estoque | Stock quantity | Saldo em `material.stock_quantity`. |
| Autor | Author type | CUSTOMER / EMPLOYEE / SYSTEM. |

</glossario>
