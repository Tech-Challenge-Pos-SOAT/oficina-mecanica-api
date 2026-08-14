# Regras de negócio — validações e transições

## Cliente

- Identificado de forma única por CPF ou CNPJ (documento duplicado rejeita).
- E-mail único, opcional.
- Inativado, nunca deletado.

## Veículo

- Vinculado a cliente obrigatoriamente.
- Placa única no sistema.
- Inativado, nunca deletado.

## Serviço

- Preço copiado na `ServiceOrderService` no momento da inclusão (alterações futuras não afetam OS existentes).
- Inativado, nunca deletado.

## Material

- Preço copiado na `ServiceOrderMaterial` no momento da inclusão.
- Quantidade mínima gera alerta (consulta possível).
- Inativado, nunca deletado.

## Movimentação de material

- Tipo `IN` (entrada): não vinculada a OS.
- Tipo `OUT` (saída): vinculada a OS obrigatoriamente.

## Ordem de serviço

Agregado central. Transições e atomicidade:

| Status | Transições permitidas |
|---|---|
| `RECEIVED` | → `IN_DIAGNOSIS` |
| `IN_DIAGNOSIS` | → `AWAITING_APPROVAL` ou → `COMPLETED` (encerramento) |
| `AWAITING_APPROVAL` | → `IN_PROGRESS` (aprovação) ou → `COMPLETED` (recusa) |
| `IN_PROGRESS` | → `AWAITING_APPROVAL` (reparo adicional) ou → `COMPLETED` |
| `COMPLETED` | → `DELIVERED` |

Regras:
- Toda transição registra histórico com `price` vigente e `authorType` (CUSTOMER/EMPLOYEE/SYSTEM).
- `price` é nulável até primeira aprovação (depois do diagnóstico).
- Baixa de material é **atômica** (verificação + débito em única instrução).
- Baixa ocorre apenas em `AWAITING_APPROVAL` → `IN_PROGRESS`.
- Se estoque insuficiente na aprovação, falha e não transiciona.
- Cada saída bem-sucedida gera `MaterialTransaction` vinculada na mesma transação.

## Funcionário

- E-mail único.
- Inativado, nunca deletado (preserva autoria em histórico).
- Senha armazenada como hash (bcrypt).

## Indicadores

- **Tempo médio de execução**: calculado a partir do histórico de status, medindo o intervalo entre a entrada em Em execução e a chegada em Finalizada.
- **Materiais abaixo do estoque mínimo**: quantidade atual < quantidade mínima configurada.
