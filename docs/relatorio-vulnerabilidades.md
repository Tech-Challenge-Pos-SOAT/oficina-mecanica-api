# Relatorio de analise de vulnerabilidades

Gerado automaticamente pelo pipeline de CI (GitHub Actions) a cada push na `main`. Nao editar manualmente - o proximo push sobrescreve este arquivo.

- **Gerado em:** 2026-10-31 00:41 UTC
- **Commit analisado:** `4ee1ef0`
- **Execucao do workflow:** https://github.com/Tech-Challenge-Pos-SOAT/oficina-mecanica-api/actions/runs/41150710249

## 1. Dependencias (OWASP Dependency-Check)

Scan das bibliotecas/dependencias do projeto (Spring Boot, driver do Postgres, JWT, etc.) contra a base de CVEs da NVD.

### Resumo por severidade

| Severidade | Quantidade |
|---|---|
| CRITICAL | 7 |
| HIGH | 11 |
| MEDIUM | 40 |
| LOW | 5 |
| **Total** | **63** |

### Vulnerabilidades encontradas

| Severidade | CVE | Dependencia | CVSS |
|---|---|---|---|
| CRITICAL | CVE-2026-47884 | `spring-core-6.2.19.jar` | 9.8 |
| CRITICAL | CVE-2026-47890 | `spring-core-6.2.19.jar` | 9.8 |
| CRITICAL | CVE-2026-47891 | `spring-core-6.2.19.jar` | 9.8 |
| CRITICAL | CVE-2026-47892 | `spring-core-6.2.19.jar` | 9.8 |
| CRITICAL | CVE-2026-59313 | `spring-core-6.2.19.jar` | 9.8 |
| CRITICAL | CVE-2026-59283 | `spring-core-6.2.19.jar` | 9.1 |
| CRITICAL | CVE-2026-59270 | `spring-security-crypto-6.5.11.jar` | 9.1 |
| HIGH | CVE-2026-54291 | `postgresql-42.7.11.jar` | 5.9 |
| HIGH | CVE-2026-47885 | `spring-core-6.2.19.jar` | 7.5 |
| HIGH | CVE-2026-47886 | `spring-core-6.2.19.jar` | 7.5 |
| HIGH | CVE-2026-47888 | `spring-core-6.2.19.jar` | 7.5 |
| HIGH | CVE-2026-47889 | `spring-core-6.2.19.jar` | 7.5 |
| HIGH | CVE-2026-47893 | `spring-core-6.2.19.jar` | 7.5 |
| HIGH | CVE-2026-59282 | `spring-core-6.2.19.jar` | 7.5 |
| HIGH | CVE-2026-41707 | `spring-security-crypto-6.5.11.jar` | 7.4 |
| HIGH | CVE-2026-47841 | `spring-security-crypto-6.5.11.jar` | 7.4 |
| HIGH | CVE-2026-116803 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | - |
| HIGH | CVE-2026-116803 | `swagger-ui-5.32.2.jar: swagger-ui-es-bundle.js` | - |
| MEDIUM | CVE-2025-48924 | `commons-lang3-3.17.0.jar` | 5.3 |
| MEDIUM | CVE-2026-54515 | `jackson-databind-2.21.4.jar` | 5.3 |
| MEDIUM | CVE-2026-34479 | `log4j-api-2.24.3.jar` | 7.5 |
| MEDIUM | CVE-2026-34477 | `log4j-api-2.24.3.jar` | 5.9 |
| MEDIUM | CVE-2026-49844 | `log4j-api-2.24.3.jar` | 5.9 |
| MEDIUM | CVE-2026-47883 | `spring-core-6.2.19.jar` | 6.1 |
| MEDIUM | CVE-2026-47887 | `spring-core-6.2.19.jar` | 6.1 |
| MEDIUM | CVE-2026-59281 | `spring-core-6.2.19.jar` | 6.1 |
| MEDIUM | CVE-2026-59280 | `spring-core-6.2.19.jar` | 4.3 |
| MEDIUM | CVE-2026-47834 | `spring-data-jpa-3.5.13.jar` | 6.5 |
| MEDIUM | CVE-2026-47842 | `spring-security-crypto-6.5.11.jar` | 6.5 |
| MEDIUM | CVE-2026-59276 | `spring-security-crypto-6.5.11.jar` | 5.9 |
| MEDIUM | CVE-2026-49978 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-49458 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-49459 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-41240 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65902 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65898 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | 7.2 |
| MEDIUM | CVE-2026-65899 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65900 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65901 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65903 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-66010 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-41238 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | - |
| MEDIUM | CVE-2026-41239 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | - |
| MEDIUM | CVE-2026-75838 | `swagger-ui-5.32.2.jar: swagger-ui-bundle.js` | - |
| MEDIUM | CVE-2026-49978 | `swagger-ui-5.32.2.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-49458 | `swagger-ui-5.32.2.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-49459 | `swagger-ui-5.32.2.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-41240 | `swagger-ui-5.32.2.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65902 | `swagger-ui-5.32.2.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65898 | `swagger-ui-5.32.2.jar: swagger-ui-es-bundle.js` | 7.2 |

_... e mais 13 vulnerabilidade(s). Relatorio completo: artifact `dependency-check-report` (HTML/JSON) do workflow._

## 2. Imagem Docker (Trivy)

Scan da imagem Docker final (sistema operacional base + dependencias empacotadas na imagem de producao).

### Resumo por severidade

| Severidade | Quantidade |
|---|---|
| CRITICAL | 2 |
| HIGH | 6 |
| MEDIUM | 8 |
| **Total** | **16** |

### Vulnerabilidades encontradas

| Severidade | ID | Pacote | Instalada | Corrigida em |
|---|---|---|---|---|
| CRITICAL | CVE-2026-47884 | `org.springframework:spring-webmvc` | 6.2.19 | 7.0.9 |
| CRITICAL | CVE-2026-47890 | `org.springframework:spring-webmvc` | 6.2.19 | 7.0.9 |
| HIGH | CVE-2026-89407 | `com.fasterxml.jackson.core:jackson-core` | 2.21.4 | 2.18.11, 2.21.7, 2.22.3 |
| HIGH | CVE-2026-89425 | `com.fasterxml.jackson.core:jackson-core` | 2.21.4 | 2.21.7, 2.22.3, 2.18.11 |
| HIGH | CVE-2026-68497 | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 2.18.10, 2.21.6, 2.22.2 |
| HIGH | CVE-2026-91776 | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 2.18.11, 2.21.7, 2.22.3 |
| HIGH | CVE-2026-91777 | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 2.21.7, 2.18.11, 2.22.3 |
| HIGH | CVE-2026-54291 | `org.postgresql:postgresql` | 42.7.11 | 42.7.12 |
| MEDIUM | CVE-2026-19032 | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 2.18.10, 2.21.6, 2.22.2 |
| MEDIUM | CVE-2026-54515 | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 3.1.4, 2.18.9, 2.21.5, 2.22.1 |
| MEDIUM | CVE-2026-59889 | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 2.21.5, 2.18.9, 2.22.1 |
| MEDIUM | CVE-2026-77310 | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 2.18.9, 2.21.5, 2.22.1 |
| MEDIUM | CVE-2026-83557 | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 2.18.10, 2.21.6, 2.22.2 |
| MEDIUM | GHSA-mhm7-754m-9p8w | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 2.18.9, 2.21.5 |
| MEDIUM | CVE-2025-48924 | `org.apache.commons:commons-lang3` | 3.17.0 | 3.18.0 |
| MEDIUM | CVE-2026-49844 | `org.apache.logging.log4j:log4j-api` | 2.24.3 | 2.25.5, 2.26.1 |

## 3. Qualidade de codigo e security hotspots (SonarQube)

Analise complementar de qualidade de codigo, security hotspots e issues (incluindo as vulnerabilidades da secao 1, importadas via `sonar.externalIssuesReportPaths`) roda na etapa "6. SonarQube" do pipeline, numa instancia local (self-hosted). Como essa instancia roda na maquina do time (nao publicamente acessivel), o dashboard completo fica disponivel para consulta local em `http://localhost:9000` - nao e possivel embutir os dados aqui automaticamente. Para o documento final de entrega, tirar um print/export do dashboard (Overview + aba Issues filtrada por "External") e anexar junto com este relatorio.

## 4. Como interpretar

- **CRITICAL/HIGH**: prioridade de correcao antes da entrega, quando possivel (atualizar a dependencia para a versao corrigida).
- **MEDIUM/LOW**: registradas para transparencia; correcao pode ficar para uma iteracao futura sem bloquear a entrega da Fase 1.
- Vulnerabilidades sem correcao disponivel (`Corrigida em: -`) sao aceitas como risco conhecido e documentado.

