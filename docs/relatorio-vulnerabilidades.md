# Relatorio de analise de vulnerabilidades

Gerado automaticamente pelo pipeline de CI (GitHub Actions) a cada push na `main`. Nao editar manualmente - o proximo push sobrescreve este arquivo.

- **Gerado em:** 2026-08-31 00:24 UTC
- **Commit analisado:** `4a117c2`
- **Execucao do workflow:** https://github.com/Tech-Challenge-Pos-SOAT/oficina-mecanica-api/actions/runs/33344124293

## 1. Dependencias (OWASP Dependency-Check)

Scan das bibliotecas/dependencias do projeto (Spring Boot, driver do Postgres, JWT, etc.) contra a base de CVEs da NVD.

### Resumo por severidade

| Severidade | Quantidade |
|---|---|
| CRITICAL | 8 |
| HIGH | 7 |
| MEDIUM | 50 |
| **Total** | **65** |

### Vulnerabilidades encontradas

| Severidade | CVE | Dependencia | CVSS |
|---|---|---|---|
| CRITICAL | CVE-2026-65637 | `tomcat-embed-core-10.1.55.jar` | 9.8 |
| CRITICAL | CVE-2026-65905 | `tomcat-embed-core-10.1.55.jar` | 9.8 |
| CRITICAL | CVE-2026-53434 | `tomcat-embed-core-10.1.55.jar` | 9.1 |
| CRITICAL | CVE-2026-55276 | `tomcat-embed-core-10.1.55.jar` | 9.1 |
| CRITICAL | CVE-2026-59083 | `tomcat-embed-core-10.1.55.jar` | 9.1 |
| CRITICAL | CVE-2026-59084 | `tomcat-embed-core-10.1.55.jar` | 9.1 |
| CRITICAL | CVE-2026-65182 | `tomcat-embed-core-10.1.55.jar` | 9.1 |
| CRITICAL | CVE-2026-68525 | `tomcat-embed-core-10.1.55.jar` | 9.1 |
| HIGH | CVE-2026-54291 | `postgresql-42.7.11.jar` | 5.9 |
| HIGH | CVE-2026-65183 | `tomcat-embed-core-10.1.55.jar` | 8.1 |
| HIGH | CVE-2026-66422 | `tomcat-embed-core-10.1.55.jar` | 8.1 |
| HIGH | CVE-2026-68569 | `tomcat-embed-core-10.1.55.jar` | 8.1 |
| HIGH | CVE-2026-65927 | `tomcat-embed-core-10.1.55.jar` | 7.5 |
| HIGH | CVE-2026-68763 | `tomcat-embed-core-10.1.55.jar` | 7.5 |
| HIGH | CVE-2026-53404 | `tomcat-embed-core-10.1.55.jar` | 7.3 |
| MEDIUM | CVE-2025-48924 | `commons-lang3-3.17.0.jar` | 5.3 |
| MEDIUM | CVE-2026-54515 | `jackson-databind-2.21.4.jar` | 5.3 |
| MEDIUM | CVE-2026-34479 | `log4j-api-2.24.3.jar` | 7.5 |
| MEDIUM | CVE-2026-34477 | `log4j-api-2.24.3.jar` | 5.9 |
| MEDIUM | CVE-2026-49844 | `log4j-api-2.24.3.jar` | 5.9 |
| MEDIUM | CVE-2026-49978 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2025-26791 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-49458 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-49459 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-41240 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-0540 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65902 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65914 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2025-15599 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65898 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 7.2 |
| MEDIUM | CVE-2026-65899 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65900 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65901 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65903 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65912 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65913 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-66010 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-41238 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | - |
| MEDIUM | CVE-2026-41239 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | - |
| MEDIUM | CVE-2026-75838 | `swagger-ui-5.18.3.jar: swagger-ui-bundle.js` | - |
| MEDIUM | CVE-2026-49978 | `swagger-ui-5.18.3.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2025-26791 | `swagger-ui-5.18.3.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-49458 | `swagger-ui-5.18.3.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-49459 | `swagger-ui-5.18.3.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-41240 | `swagger-ui-5.18.3.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-0540 | `swagger-ui-5.18.3.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65902 | `swagger-ui-5.18.3.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65914 | `swagger-ui-5.18.3.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2025-15599 | `swagger-ui-5.18.3.jar: swagger-ui-es-bundle.js` | 6.1 |
| MEDIUM | CVE-2026-65898 | `swagger-ui-5.18.3.jar: swagger-ui-es-bundle.js` | 7.2 |

_... e mais 15 vulnerabilidade(s). Relatorio completo: artifact `dependency-check-report` (HTML/JSON) do workflow._

## 2. Imagem Docker (Trivy)

Scan da imagem Docker final (sistema operacional base + dependencias empacotadas na imagem de producao).

### Resumo por severidade

| Severidade | Quantidade |
|---|---|
| HIGH | 4 |
| MEDIUM | 14 |
| **Total** | **18** |

### Vulnerabilidades encontradas

| Severidade | ID | Pacote | Instalada | Corrigida em |
|---|---|---|---|---|
| HIGH | CVE-2026-14456 | `libcrypto3` | 3.5.7-r0 | 3.5.8-r0 |
| HIGH | CVE-2026-14456 | `libssl3` | 3.5.7-r0 | 3.5.8-r0 |
| HIGH | CVE-2026-14456 | `openssl` | 3.5.7-r0 | 3.5.8-r0 |
| HIGH | CVE-2026-54291 | `org.postgresql:postgresql` | 42.7.11 | 42.7.12 |
| MEDIUM | CVE-2026-54515 | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 3.1.4, 2.18.9, 2.21.5, 2.22.1 |
| MEDIUM | CVE-2026-59889 | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 2.21.5, 2.18.9, 2.22.1 |
| MEDIUM | GHSA-mhm7-754m-9p8w | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | 2.18.9, 2.21.5 |
| MEDIUM | CVE-2026-18798 | `libcrypto3` | 3.5.7-r0 | 3.5.8-r0 |
| MEDIUM | CVE-2026-63072 | `libcrypto3` | 3.5.7-r0 | 3.5.8-r0 |
| MEDIUM | CVE-2026-63076 | `libcrypto3` | 3.5.7-r0 | 3.5.8-r0 |
| MEDIUM | CVE-2026-18798 | `libssl3` | 3.5.7-r0 | 3.5.8-r0 |
| MEDIUM | CVE-2026-63072 | `libssl3` | 3.5.7-r0 | 3.5.8-r0 |
| MEDIUM | CVE-2026-63076 | `libssl3` | 3.5.7-r0 | 3.5.8-r0 |
| MEDIUM | CVE-2026-18798 | `openssl` | 3.5.7-r0 | 3.5.8-r0 |
| MEDIUM | CVE-2026-63072 | `openssl` | 3.5.7-r0 | 3.5.8-r0 |
| MEDIUM | CVE-2026-63076 | `openssl` | 3.5.7-r0 | 3.5.8-r0 |
| MEDIUM | CVE-2025-48924 | `org.apache.commons:commons-lang3` | 3.17.0 | 3.18.0 |
| MEDIUM | CVE-2026-49844 | `org.apache.logging.log4j:log4j-api` | 2.24.3 | 2.25.5, 2.26.1 |

## 3. Qualidade de codigo e security hotspots (SonarQube)

Analise complementar de qualidade de codigo, security hotspots e issues (incluindo as vulnerabilidades da secao 1, importadas via `sonar.externalIssuesReportPaths`) roda na etapa "6. SonarQube" do pipeline, numa instancia local (self-hosted). Como essa instancia roda na maquina do time (nao publicamente acessivel), o dashboard completo fica disponivel para consulta local em `http://localhost:9000` - nao e possivel embutir os dados aqui automaticamente. Para o documento final de entrega, tirar um print/export do dashboard (Overview + aba Issues filtrada por "External") e anexar junto com este relatorio.

## 4. Como interpretar

- **CRITICAL/HIGH**: prioridade de correcao antes da entrega, quando possivel (atualizar a dependencia para a versao corrigida).
- **MEDIUM/LOW**: registradas para transparencia; correcao pode ficar para uma iteracao futura sem bloquear a entrega da Fase 1.
- Vulnerabilidades sem correcao disponivel (`Corrigida em: -`) sao aceitas como risco conhecido e documentado.

