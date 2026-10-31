# Relatorio de analise de vulnerabilidades

Gerado automaticamente pelo pipeline de CI (GitHub Actions) a cada push na `main`. Nao editar manualmente - o proximo push sobrescreve este arquivo.

- **Gerado em:** 2026-10-31 00:44 UTC
- **Commit analisado:** `72fad96`
- **Execucao do workflow:** https://github.com/Tech-Challenge-Pos-SOAT/oficina-mecanica-api/actions/runs/41148982885

## 1. Dependencias (OWASP Dependency-Check)

Scan das bibliotecas/dependencias do projeto (Spring Boot, driver do Postgres, JWT, etc.) contra a base de CVEs da NVD.

### Resumo por severidade

_Sem dados de severidade disponiveis._

### Vulnerabilidades encontradas

_Nenhuma vulnerabilidade de dependencia encontrada (ou relatorio indisponivel)._

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

