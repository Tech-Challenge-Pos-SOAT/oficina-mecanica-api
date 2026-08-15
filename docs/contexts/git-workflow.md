# Git Workflow

Padrão de branch, commit e PR do time.

## Padrão de Branch

**Sempre**: `feature/nome-da-tarefa`

Exemplos:
- `feature/add-customer-validation`
- `feature/fix-serviceorder-status-transition`

Convenção do time. Se abrir PR, deve seguir.

## Conventional Commits

Formato: `type(scope): description en inglês`

**Types:** `feat`, `fix`, `refactor`, `docs`, `test`, `chore`

**Scope:** opcional (customer, serviceorder, docs, ci, etc)

**Descrição:** imperativo, presente, sem ponto

**Corpo:** português. Explica **por que**, não **o que**.

### Exemplo Bom

```
feat(customer): add email uniqueness validation

Customer não pode ter dois registros com mesmo email. Adicionado
constraint UNIQUE no schema e validação no construtor do VO.
```

## Pull Request

**Aprovação obrigatória** de outro integrante. Quem abre não aprova nem mergeia.

**Título:** curto, descritivo, `type: short description`
Exemplo: "Add customer email validation"

**Corpo:** summary (bullets) + test plan (checklist golden path + edge cases)

## CI Pipeline

6 jobs em cadeia (`.github/workflows/ci.yml`): check → build → test → dependency-check → trivy → sonar

Job novo? Adicione `if: ${{ !failure() && !cancelled() }}` para não quebrar cadeia.

Ver `docs/contexts/ci-cd.md` para detalhes completos da pipeline.

## Regras Gerais

Commit atômico (1 feature/fix); corpo explica **por que**; rebase para atualizar com main.
