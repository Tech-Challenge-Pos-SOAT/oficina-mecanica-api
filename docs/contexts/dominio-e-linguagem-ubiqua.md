# Dominio: oficina mecanica

Define **o que as palavras significam neste repositorio** e o nome delas em ingles.
Codigo sempre em ingles. Docs/conversa: portugues.

<skill>
Antes de criar entidade, use case ou endpoint: leia aqui.
Nomes vem de `modelo-de-dados.md`.
</skill>

<negocio>
Oficina mecanica de medio porte. Atendente abre `ServiceOrder`. Mecanico diagnostica, escolhe `Service` e `Material`, forma orcamento. Cliente aprova/recusa. Aprovado, materiais saem do estoque, servico e executado. Reparo adicional durante execucao = novo ciclo de aprovacao sem reiniciar ordem. Veiculo entregue.
</negocio>

<glossario>
Coluna "Classe" = nome a usar no codigo. "Tabela" amarra ao schema.

| Termo PT | Classe EN | Tabela | NUNCA chamar de |
|---|---|---|---|
| Cliente | `Customer` | `customer` | Client, User |
| Veiculo | `Vehicle` | `vehicle` | Car, Automobile |
| Peca/material/insumo | `Material` | `material` | **Part**, Product, Item |
| Servico (catalogo) | `Service` | `service` | Procedure, Task |
| Funcionario | `Employee` | `employee` | User, Staff |
| Ordem de Servico | `ServiceOrder` | `service_order` | **WorkOrder**, Order, Job |
| Material da OS | `ServiceOrderMaterial` | `service_order_material` | OrderItem |
| Servico da OS | `ServiceOrderService` | `service_order_service` | OrderItem |
| Historico de status | `ServiceOrderHistory` | `service_order_history` | Audit, Log |
| Movimentacao estoque | `MaterialTransaction` | `material_transaction` | StockMovement |
| CPF/CNPJ | `Document` (VO) | `customer.document` | Cpf, TaxId |
| Placa | `Plate` (VO) | `vehicle.plate` | LicensePlate |

**Nao viram classe:**
- **Orcamento**: campo `price` em `ServiceOrder`, derivado de somatorio. Se nome: `totalPrice`.
- **Diagnostico**: status `IN_DIAGNOSIS` de `ServiceOrder`. Sem tabela.
- **Reparo adicional**: novo ciclo de status na mesma `ServiceOrder`, registrado em `ServiceOrderHistory`. Sem tabela.

**Ambiguidade conhecida**: "peca" = componente fisico (mecanico); item com custo (financeiro).
Codigo: **uma classe `Material`** = visao de estoque. Visao fisica = `ServiceOrderMaterial` dentro da ordem.
</glossario>

<agregado>
`ServiceOrder` e raiz. `ServiceOrderMaterial`, `ServiceOrderService`, `ServiceOrderHistory` sao internos — nada de fora altera direto, sempre por metodo da raiz.
`Customer`, `Vehicle`, `Material`, `Service`, `Employee` sao agregados proprios. `ServiceOrder` referencia por id.
</agregado>

<transicoes>
```
RECEIVED → IN_DIAGNOSIS → AWAITING_APPROVAL → IN_PROGRESS → COMPLETED → DELIVERED
```

`status` VARCHAR; banco nao restringe. **Enum `ServiceOrderStatus` em dominio manda.**
Transicao invalida = excecao de dominio (nao `false`, nao validacao na controller).
Toda mudanca registra `ServiceOrderHistory` com `price` vigente e `authorType` (CUSTOMER/EMPLOYEE/SYSTEM).
</transicoes>

<vos>
- **`Document`**: CPF ou CNPJ mesma coluna (unica). Valida formato **e** digito verificador no construtor.
- **`Plate`**: formato antigo (AAA-1234) e/ou Mercosul (AAA1A23).

Ambos imutaveis, validam no construtor. `Customer` nunca existe com documento invalido — sem estado intermediario invalido.
</vos>

<status-cadastro>
`customer`, `vehicle`, `material`, `service`, `employee` tem `status ACTIVE/INACTIVE` (exclusao logica, nao DELETE).
Enum `EntityStatus`. Listagem padrao filtra ACTIVE.
</status-cadastro>

<pontos-em-aberto>
Pergunte ao usuario se a tarefa esbarrar em algum:

1. **Reserva de material nao existe no schema** (so `MaterialTransaction` IN/OUT). Confirmar descarte (MVP) ou se precisa entrar.
2. **Material nao existe no catalogo durante diagnostico?** Regra: encerrar COMPLETED. Confirmar.
3. **Recusa total vs. parcial?** Total encerra, parcial retira itens e volta IN_PROGRESS, registrada em `ServiceOrderHistory`. Confirmar implementacao.
4. Perguntas `?` nos comentarios do schema (ver `modelo-de-dados.md`).
</pontos-em-aberto>
