# CI/CD Pipeline

Esteira de testes, build e análise automática. Arquivo config: `.github/workflows/ci.yml`

## Arquitetura: 6 jobs em cadeia via `needs`

```
check → build → test → dependency-check → trivy → sonar
```

**Importante:** GitHub Actions propaga "skipped" em **cadeia** — se um job pula, todos os downstream pulam também (doc oficial: "a failure or skip applies to all jobs in the dependency chain from the point of failure or skip onwards").

Por isso **todo job precisa do guard**:
```yaml
if: ${{ !failure() && !cancelled() }}
```

(O `sonar` combina isso com condicoes de evento que ja tinha.) **Job novo nessa cadeia precisa desse guard, senao quebra a cadeia inteira.**

## Cada job

### 1. `check` — Branch up-to-date com main

- Só roda em **PR**.
- Falha se branch estiver desatualizada em relação a `main`.
- Em push direto na `main` fica "skipped".

### 2. `build` — Compila e empacota

```bash
mvn clean package -DskipTests
```

- Sem testes (rodam depois).
- Produz artefato (JAR + Docker image).

### 3. `test` — Testes + cobertura JaCoCo

- Roda suite JUnit 5 via Testcontainers (Postgres real, **nunca H2**).
- Gera relatório JaCoCo (meta: 80%).
- Publica artifact `jacoco-report` (lê em `target/site/jacoco/index.html`).

### 4. `dependency-check` — OWASP Dependency-Check

- Escaneia `pom.xml` contra NVD (National Vulnerability Database).
- Produz relatórios HTML/JSON.
- **Converte resultado para formato externo do SonarQube** via `.github/scripts/dependency_check_to_sonar.py`.
- Publica artifact `dependency-check-sonar-issues` (consumido pelo job `sonar`).

**Nota:** Não reintroduzir o `dependency-check-sonar-plugin` da comunidade — sem release desde ago/2024, bugs em versões recentes do SonarQube.

### 5. `trivy` — Escaneia imagem Docker

- Builda imagem Docker.
- Escaneia com Trivy (vulnerabilidades em camadas, dependências).
- Publica artifact `trivy-report`.

**Repo privado:** `format: sarif` + upload para aba Security exigiria **GitHub Advanced Security (pago)**. Não usar; relatorios ficam como artifact do workflow.

### 6. `sonar` — SonarQube Community Edition local

- **Runner self-hosted** (Docker Compose na máquina de quem configurou).
- Roda em push na `main` e em **PR** (decisão explícita do usuário).
- Antes de rodar `mvn clean`: baixa artifact `dependency-check-sonar-issues` para `external-reports/` (fora de `target/`, que o clean apaga), passa via `-Dsonar.externalIssuesReportPaths=...`.

**Limitações da Community Edition:**

- ❌ Sem `sonar.branch.name` (Developer Edition+) → **nunca** adicionar esse param no step do PR.
- ❌ Sem branch nativa → cada PR usa projectKey próprio (`oficina-mecanica-api-pr-<numero>`, calculado no step "Definir projectKey") pra não sobrescrever análise da main. **Manter essa lógica.**
- ❌ Sem decoração automática no PR (recurso pago) → só o check "SonarQube" + dashboard local.
- ⚠️ Goal qualificado (Community não resolve prefixo curto):
  ```
  org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar
  ```
  (Não use `sonar:sonar`; só resolve se `~/.m2/settings.xml` do runner tiver o grupo `org.sonarsource.scanner.maven` cadastrado.)

**Secrets:**
- `SONAR_HOST_URL`: `http://sonarqube.local:9000`
- `SONAR_TOKEN`: usar `sonar.token` (não `sonar.login`, descontinuado)

**Risco:** Se `sonar` virar required status check, PR travará quando essa máquina estiver offline. Time assumiu o risco.

## Regra de ouro

**Job novo nessa cadeia?** Adicione:
```yaml
if: ${{ !failure() && !cancelled() }}
```

Caso contrário, quebra a cadeia inteira em cascata.

---

Ver `CLAUDE.md` seção "Git / PR" para regras de branch, commit e approval.
