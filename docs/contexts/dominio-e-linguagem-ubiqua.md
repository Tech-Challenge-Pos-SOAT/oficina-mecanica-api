# Domínio e Linguagem Ubíqua — Sistema de Gestão de Oficina Mecânica

**Leia quando:** criar entidade, use case, endpoint ou validação. Define o que as palavras significam, nomes em inglês/português, regras de negócio e agregados.

Código sempre em inglês. Documentação e conversa: português.

---

## Contexto de Negócio

Oficina mecânica de médio porte. Fluxo: atendente abre `ServiceOrder` → mecânico diagnostica e escolhe `Service` e `Material` → orçamento → cliente aprova/recusa → aprovado, materiais saem de estoque, serviço executado → reparo adicional durante execução gera novo ciclo de aprovação (sem reiniciar ordem) → entrega ao cliente.

---

## Dicionário de Linguagem Ubíqua

### Atores

| Termo | Classe | Descrição |
|---|---|---|
| **Atendente** *(Attendant)* | `Employee` (role) | Primeiro contato, cadastro de veículo, abertura da OS e entrega |
| **Mecânico** *(Mechanic)* | `Employee` (role) | Diagnóstico, definição de serviços/materiais, execução de reparos |
| **Funcionário / Employee** | `Employee` | Termo genérico para colaboradores |
| **Cliente / Customer** | `Customer` | Pessoa física/jurídica proprietária do veículo. Identificado por CPF ou CNPJ |

### Entidades do Domínio

| Termo PT | Classe EN | Tabela | Descrição |
|---|---|---|---|
| **Cliente** | `Customer` | `customer` | Proprietário do veículo. Identificado de forma única por CPF ou CNPJ |
| **Veículo** | `Vehicle` | `vehicle` | Bem pertencente a cliente. Identificado pela placa. Marca, modelo, ano |
| **Material** | `Material` | `material` | Peça ou insumo usado em serviço. Gerenciado por estoque |
| **Serviço** | `Service` | `service` | Mão de obra executada no veículo. Possuem catálogo com nome, descrição e valor |
| **Funcionário** | `Employee` | `employee` | Colaborador interno. Nome único, senha bcrypt, status ACTIVE/INACTIVE |
| **Ordem de Serviço** | `ServiceOrder` | `service_order` | Documento central. Registra ciclo completo do atendimento |
| **Estoque** *(não é classe)* | — | `material.stock_quantity` | Quantidade disponível de um material |

### Ordem de Serviço (Termos Específicos)

| Termo PT | Classe EN | Tabela | Descrição |
|---|---|---|---|
| **Abertura** *(Opening)* | — | — | Criar nova OS, vinculando cliente + veículo. Status `RECEIVED` |
| **Diagnóstico** *(Diagnosis)* | — | Status `IN_DIAGNOSIS` | Avaliação do mecânico sobre serviços/materiais necessários |
| **Orçamento** *(Budget)* | `price` em `ServiceOrder` | — | Valor total = soma de serviços + materiais. Congelado no momento |
| **Aprovação** *(Approval)* | — | Status transition → `IN_PROGRESS` | Cliente concorda com orçamento. Dispara baixa atômica de material |
| **Recusa** *(Rejection)* | — | Status transition → `FINISHED` | Cliente rejeita orçamento. OS encerrada sem serviço/consumo |
| **Reparo Adicional** *(Additional Repair)* | — | Status `IN_PROGRESS` → `AWAITING_APPROVAL` | Serviço/material não previsto. Novo orçamento + aprovação |
| **Execução** *(Execution)* | — | Status `IN_EXECUTION` | Serviços aprovados sendo realizados. Ordem FIFO por aprovação |
| **Finalização** *(Finalization)* | — | Status transition → `FINISHED` | Encerramento após conclusão dos serviços |
| **Entrega** *(Delivery)* | — | Status transition → `DELIVERED` | Devolução do veículo ao cliente |
| **Material da OS** | `ServiceOrderMaterial` | `service_order_material` | Peça/insumo incluído na OS, com preço congelado |
| **Serviço da OS** | `ServiceOrderService` | `service_order_service` | Mão de obra incluída na OS, com preço congelado |
| **Histórico** | `ServiceOrderHistory` | `service_order_history` | Registro de cada mudança de status com preço vigente, autor e timestamp |
| **Movimentação** | `MaterialTransaction` | `material_transaction` | Registro de entrada/saída de material. Permite rastrear saldo |

### Status da Ordem de Serviço

| PT | EN | Significado |
|---|---|---|
| Recebida | `RECEIVED` | OS aberta, veículo na oficina |
| Em diagnóstico | `IN_DIAGNOSIS` | Mecânico avaliando o veículo |
| Aguardando aprovação | `AWAITING_APPROVAL` | Orçamento enviado ao cliente para aprovação |
| Em execução | `IN_EXECUTION` | Serviços sendo realizados pelo mecânico |
| Finalizada | `FINISHED` | Atendimento encerrado (conclusão, recusa ou impossibilidade) |
| Entregue | `DELIVERED` | Veículo retirado pelo cliente |
| Cancelada | `CANCELLED` | Encerrada sem execução: peça sem saldo, serviço fora do catálogo ou orçamento vencido sem resposta |

> Status `FINISHED` é genérico: representa conclusão normal ou recusa do primeiro orçamento. O motivo é registrado no campo observação do histórico.
>
> `CANCELLED` é diferente: a oficina **não conseguiu** executar. Só é alcançável de `RECEIVED`, `IN_DIAGNOSIS`, `AWAITING_APPROVAL` (regras automáticas ou cancelamento manual) e de `IN_EXECUTION` **apenas pelo cancelamento manual do funcionário**, que devolve ao estoque as peças já baixadas.

### Regras de encerramento automático

| Gatilho | OS nunca executada | OS já em execução (reparo adicional) |
|---|---|---|
| Peça sem saldo na aprovação | `CANCELLED` | descarta o adicional e volta para `IN_EXECUTION` |
| Serviço inativado no catálogo | `CANCELLED` | descarta o adicional e volta para `IN_EXECUTION` |
| 7 dias sem resposta do cliente | `CANCELLED` (autor `SYSTEM`) | descarta o adicional e volta para `IN_EXECUTION` |
| Recusa do cliente | `FINISHED` | descarta o adicional e volta para `IN_EXECUTION` |

O princípio é único: **trabalho já aprovado pelo cliente nunca é jogado fora**. O prazo é configurável em `serviceorder.budget-approval-deadline-days` (padrão 7) e a varredura roda diariamente às 03:00.

### Estoque e Movimentações

| Termo PT | EN | Descrição |
|---|---|---|
| **Baixa no Estoque** | `Stock debit` | Redução de quantidade por uso em OS. Ocorre apenas em aprovação (atômico) |
| **Entrada de Material** | `Stock entry (IN)` | Aumento por compra/devolução. Não vinculada a OS |
| **Saída de Material** | `Stock output (OUT)` | Redução por consumo em OS. Sempre vinculada à OS |
| **Movimentação** | `MaterialTransaction` | Registro histórico de cada entrada/saída |
| **Estoque Mínimo** | `Minimum quantity` | Quantidade mínima aceitável. Abaixo disso: alerta de reposição |

### Indicadores

| Métrica | Descrição |
|---|---|
| **Tempo Médio de Execução** | Intervalo entre entrada em `IN_EXECUTION` e chegada em `FINISHED`. Do histórico de status |
| **Materiais Abaixo do Mínimo** | `stock_quantity < minimum_quantity` |
| **Fila FIFO** | Ordem de execução das OS = sequência de aprovação dos orçamentos |

---

## Entidades e Agregados

### Regras Gerais

- Nenhuma entidade é deletada, apenas inativada (`EntityStatus = INACTIVE`)
- Toda transição de OS registra histórico com `price` vigente e `authorType`
- `price` é congelado no momento da inclusão; alterações futuras no catálogo não afetam ordens existentes

### Customer

- Identificação única: CPF ou CNPJ (duplicado rejeita)
- Email único, opcional
- Status: `ACTIVE` / `INACTIVE`

### Vehicle

- Referência obrigatória a cliente
- Placa única no sistema
- Status: `ACTIVE` / `INACTIVE`
- **Nota:** cliente pode mudar dono. Ao abrir OS, copie o dono vigente para `ServiceOrder.customerId` (não leia por join de veículo)

### Service (Catálogo)

- Preço de catálogo (`service.price`) copiado para `ServiceOrderService.price` no momento da inclusão
- Futuras alterações do catálogo não afetam OS já existentes
- Status: `ACTIVE` / `INACTIVE`

### Material

- Preço de catálogo (`material.price`) copiado para `ServiceOrderMaterial.price` no momento da inclusão
- Quantidade mínima configurada; alerta disponível na consulta
- Status: `ACTIVE` / `INACTIVE`

### Employee

- Nome único
- Senha hash bcrypt
- Status: `ACTIVE` / `INACTIVE`
- Inativação preserva autoria em histórico

---

## Value Objects

### Document (CPF/CNPJ)

- Valida formato (CPF 11 dígitos, CNPJ 14 dígitos) **e** dígito verificador no construtor
- Imutável
- Campo único em `Customer`
- Nunca existe estado intermediário inválido

### Plate (Placa Veículo)

- Suporta formato antigo (AAA-1234) e/ou Mercosul (AAA1A23)
- Imutável
- Valida no construtor

---

## Agregado ServiceOrder (Raiz)

`ServiceOrder` é o agregado central. `ServiceOrderMaterial`, `ServiceOrderService` e `ServiceOrderHistory` são internos — nada de fora altera direto, sempre por método da raiz.

`Customer`, `Vehicle`, `Material`, `Service`, `Employee` são agregados próprios; `ServiceOrder` referencia por ID.

### Transições de Status

| FROM | TO | Condição | Notas |
|---|---|---|---|
| `RECEIVED` | `IN_DIAGNOSIS` | — | Ordem recebida |
| `IN_DIAGNOSIS` | `AWAITING_APPROVAL` | Diagnóstico feito | `price` torna-se obrigatório |
| `IN_DIAGNOSIS` | `FINISHED` | — | Encerramento sem reparo |
| `AWAITING_APPROVAL` | `IN_EXECUTION` | Aprovação | **Dispara baixa atômica de material** |
| `AWAITING_APPROVAL` | `FINISHED` | Recusa | Ordem cancelada |
| `IN_EXECUTION` | `AWAITING_APPROVAL` | Reparo adicional | Novo diagnóstico, novo orçamento |
| `IN_EXECUTION` | `FINISHED` | Reparo finalizado | |
| `FINISHED` | `DELIVERED` | Entrega ao cliente | |

Transição inválida = exceção de domínio (não `false`, não validação na controller). Enum `ServiceOrderStatus` em domínio manda.

### Regras de Preço

- `service_order.price` é **nulável** até primeira aprovação (`IN_DIAGNOSIS` → `AWAITING_APPROVAL`)
- Cada histórico registra `price` vigente naquele momento
- Reconstruir orçamento a partir de `ServiceOrderHistory.price`

### Regras de Material e Serviço

- **Durante diagnóstico:** mecânico seleciona apenas `Material` e `Service` com status `ACTIVE`
- Preço de catálogo congelado no momento da inclusão na OS
- **Baixa (saída) ocorre apenas em transição** `AWAITING_APPROVAL` → `IN_EXECUTION`
- Verificação e débito em **uma instrução atômica** (falha = sem transição)
- Se estoque insuficiente, falha da transição sem alterar status
- Cada saída bem-sucedida cria `MaterialTransaction` (type=`OUT`, vinculada à OS)

### Histórico e Rastreabilidade

- Toda mudança de status registra `ServiceOrderHistory`
- Campos: status anterior, novo status, `price` vigente, `authorType` (`CUSTOMER`, `EMPLOYEE`, `SYSTEM`), timestamp
- Campo observação para motivo de encerramento ou informação relevante
- **Snapshot de valor:** serviço e material preservam preço no momento da inclusão

---

## Movimentações de Material

`MaterialTransaction` é o único registro de movimentação. Não existe tabela de reserva.

| Tipo | Vinculação | Notas |
|---|---|---|
| `IN` (entrada) | Sem OS | Compra de fornecedor, devolução, ajuste |
| `OUT` (saída) | OS obrigatória | Gerada apenas por aprovação de OS |

- `material.stock_quantity` é sempre o saldo atual
- `material_transaction` é o extrato histórico

---

## Ambiguidades Resolvidas

**"Peça" (componente físico) vs. "item com custo"**
- Código: uma classe `Material` = visão de estoque
- Visão física: `ServiceOrderMaterial` dentro da ordem

**"Orçamento", "Diagnóstico", "Reparo Adicional"**
- Orçamento = campo `price` em `ServiceOrder`, derivado de somatório
- Diagnóstico = status `IN_DIAGNOSIS` de `ServiceOrder`, sem tabela
- Reparo adicional = novo ciclo de status na mesma `ServiceOrder`, registrado em `ServiceOrderHistory`, sem tabela

**Nomes internacionais (proibidos)**
- Não chamar de: Client, User, Car, Automobile, Part, Product, Item, Procedure, Task, Staff, WorkOrder, Order, Job, Audit, Log, StockMovement, TaxId, LicensePlate

---

## Pontos em Aberto

Pergunte ao usuário se a tarefa esbarrar em algum:

1. **Reserva de material não existe no schema** (só `MaterialTransaction` IN/OUT). Confirmar descarte (MVP) ou se precisa entrar.
2. ~~**Recusa total vs. parcial?**~~ **Resolvido**: recusa do primeiro orçamento encerra (`FINISHED`); recusa de reparo adicional descarta os itens não aprovados e volta para `IN_EXECUTION`.
3. Perguntas `?` nos comentários do schema (ver `modelo-de-dados.md`)
