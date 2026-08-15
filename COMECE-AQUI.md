# Comece Aqui — Superpowers & Fluxo de Trabalho

Configuração única. Setup rápido. Skills para cada situação.

## Setup (primeira vez)

```bash
./setup-ai.sh
```

Instala RTK, Caveman, Ponytail, Context7 CLI, Superpowers. Esperado: "Pronto".

**Troubleshooting:** Node não encontrado? `brew install node` ou `nvm install --lts`.

---

## Superpowers — Skills por Situação

| Situação | Skill | Resultado |
|---|---|---|
| **Novo requisito, preciso de plano** | `/superpowers:writing-plans` | Plano em `docs/superpowers/plans/YYYY-MM-DD-*.md` com tarefas numeradas |
| **Tenho plano, executar tarefa por tarefa** | `/superpowers:executing-plans` | Executa cada tarefa com review/checkpoint |
| **Teste vermelho, bug, comportamento inesperado** | `/superpowers:systematic-debugging` | Identifica raiz causa, propõe fix |
| **Indeciso entre abordagens** | `/superpowers:brainstorming` | Explora opções, recomenda uma |

### Exemplo Completo

#### Passo 1: Criar Plano

Você digita no Claude Code:
```
/superpowers:writing-plans

Precisamos de um endpoint GET /api/customers que:
- Filtra por status (ACTIVE/INACTIVE)
- Retorna id, name, document, phone, email, status, createdAt, updatedAt
- Ordena por ID ascendente
- Autenticado com JWT
- Com testes de integração
```

Superpowers retorna estrutura:
```
# Endpoint Listagem de Clientes — Plano de Implementação

**Objetivo:** Construir endpoint...
**Stack:** Spring Boot 3.5.16, MapStruct, JPA, Testcontainers...

## Tarefa 1: Camada Domínio
- [ ] Criar EntityStatus enum
- [ ] Criar Document value object
- [ ] Criar Customer entidade

## Tarefa 2: Camada Aplicação
- [ ] Criar CustomerRepository (porta)
- [ ] Criar ListCustomersUseCase

...e assim por diante (Infrastructure, Interfaces, Tests)
```

Arquivo salvo: `docs/superpowers/plans/2026-08-15-list-customers-endpoint.md`

#### Passo 2: Executar Plano

Você digita:
```
/superpowers:executing-plans

Arquivo: docs/superpowers/plans/2026-08-15-list-customers-endpoint.md
```

Superpowers:
1. Executa **Tarefa 1** (cria EntityStatus, Document, Customer)
2. Compila (`mvn clean compile`)
3. Review: "Domínio OK"
4. Executa **Tarefa 2** (CustomerRepository, ListCustomersUseCase)
5. Compila e review
6. Continua até fim (Infrastructure → Interfaces → Tests)
7. Tudo passa? Para no fim

Se algo quebrar, skill para e pede ajuda.

#### Passo 3: Pronto

Código completo, testes passando. Você faz commit:
```bash
git add -A
git commit -m "feat: add list customers endpoint"
```
```
---

## Ferramentas — O Que Está Ativo

| Nome | Função | Automático? |
|---|---|---|
| **RTK** | Comprime saída (60-90% tokens economizados) | ✅ Sim |
| **Caveman** | Respostas terse | ✅ Ativa (full) |
| **Ponytail** | YAGNI — mínimo que funciona | ✅ Ativa (full) |
| **Context7** | Doc atualizada (MCP) | ✅ Sim, chame `/context7` |

---

## Dúvidas Técnicas → `/context7`

Spring Boot API mudou, não sabe se sintaxe está certa, precisa de exemplo?

```
/context7

Spring Boot 4: qual import para @Entity, @Repository?
MapStruct: como mapear value object Document → String?
```

Context7 retorna doc oficial + código correto. Use SEMPRE antes de escrever `import` de Spring/MapStruct/JPA.

---

## Documentação de Referência

Regras e contexto em:
- [CLAUDE.md](CLAUDE.md) — Proibições, stack, idioma, padrões globais
- [docs/contexts/context-index.md](docs/contexts/context-index.md) — Qual doc ler para qual situação

Não invente padrões: leia o que existe primeiro.

---

## Checklist Antes de Começar

- [ ] Rodou `./setup-ai.sh`?
- [ ] Entendeu qual skill chamar (escrita acima)?