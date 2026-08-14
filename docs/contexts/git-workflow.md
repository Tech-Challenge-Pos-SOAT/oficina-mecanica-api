# Git workflow

Padrão de branch, commit e PR do time.

<skill>
Sempre `feature/nome-da-tarefa`. Conventional commits em ingles (tipo+escopo). Corpo em portugues.
PR para `main` precisa aprovacao de outro integrante — quem abre nao aprova nem mergeia.
</skill>

<branch>
**Padrao**: `feature/nome-descritivo-tarefa`

Exemplos: `feature/add-customer-validation`, `feature/fix-serviceorder-status-transition`

Convencao do time. Se abrir PR, deve seguir.
</branch>

<conventional-commits>
Formato: `type(scope): description en ingles`

**Types:** `feat`, `fix`, `refactor`, `docs`, `test`, `chore`
**Scope:** opcional (customer, serviceorder, docs, ci, etc)
**Descricao:** imperativo, presente, sem ponto
**Corpo:** portugues. Explica **por que**, nao **o que**.

**Exemplo bom:**
```
feat(customer): add email uniqueness validation

Customer nao pode ter dois registros com mesmo email. Adicionado
constraint UNIQUE no schema e validacao no construtor do VO.
```
</conventional-commits>

<pull-request>
**Aprovacao obrigatoria** de outro integrante. Quem abre nao aprova.

**Titulo:** curto, descritivo, `type: short description` (ex: "Add customer email validation")

**Corpo:** summary (bullets) + test plan (checklist golden path + edge cases)
</pull-request>

<ci-pipeline>
6 jobs em cadeia (`.github/workflows/ci.yml`): check → build → test → dependency-check → trivy → sonar

Job novo? Adicione `if: ${{ !failure() && !cancelled() }}` pra nao quebrar cadeia.
</ci-pipeline>

**Regras:** Commit atomico (1 feature/fix); corpo explica **por que**; rebase pra atualizar com main.

Ver `CLAUDE.md` "Git / PR" pra detalhes de CI/Sonar.
