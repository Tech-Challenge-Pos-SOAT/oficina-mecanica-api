# Indice de decisoes de codigo

Leia so o que sua tarefa pede. Cada linha: "quando preciso fazer X → leia Y".

**Regras globais:**
1. Codigo 100% EN; docs/conversa PT. Nomes saem de `modelo-de-dados.md`.
2. Spring Boot 4 renomeou starters/pacotes — consulte Context7 antes de imports.
3. Flyway cria schema (ddl-auto=validate). Mudou entidade? Nova migration.

## Tabela de redirecionamento

| Vou fazer isso... | Leia primeiro |
|---|---|
| Qualquer coisa neste repo | `CLAUDE.md` (raiz) - stack, proibicoes, comandos |
| Criar branch / commit / abrir PR | `docs/context/git-workflow.md` |
| Descobrir o nome certo de uma classe, campo ou tabela | `docs/context/modelo-de-dados.md` |
| Escrever migration / mudar schema | `docs/context/modelo-de-dados.md` |
| Criar/alterar entidade, agregado, VO, enum de status | `docs/context/dominio-e-linguagem-ubiqua.md` |
| Entender o que um termo do time significa | `docs/context/dominio-e-linguagem-ubiqua.md` (glossario PT->EN) |
| Decidir em que pacote a classe vai | `docs/context/arquitetura-ddd.md` |
| Criar controller, use case, repository, DTO | `docs/context/arquitetura-ddd.md` |
| Escrever ou revisar teste | `docs/context/testes.md` |
| Converter DTO <-> dominio <-> entidade JPA | `docs/context/mapstruct.md` |
| Anotar endpoint para o Swagger | `docs/context/swagger.md` |
| Entender que ferramenta de IA esta ativa | `docs/context/ferramentas-e-skills.md` |
| Mexer no CI, Sonar, Trivy, Dependency-Check | `CLAUDE.md`, secao "Git / PR" |

## Mapa de contextos

| Tarefa | Arquivo |
|---|---|
| Nome de classe/campo/tabela | `modelo-de-dados.md` (fonte de verdade) |
| Migration/schema | `modelo-de-dados.md` |
| Entidade, VO, enum de status | `dominio-e-linguagem-ubiqua.md` + glossario PT→EN |
| Pacote, layer, estrutura | `arquitetura-ddd.md` |
| Controller, use case, DTO, repository | `arquitetura-ddd.md` |
| Teste (VO/regra/use case/JPA/HTTP) | `testes.md` (Boot 4 imports) |
| MapStruct (DTO ↔ domain ↔ JPA) | `mapstruct.md` |
| Swagger/OpenAPI | `swagger.md` |
| Git branch/commit/PR | `git-workflow.md` (CI pipeline) |
| Ferramentas IA (RTK, Caveman, etc) | `ferramentas-e-skills.md` |

## Nao decida sozinho

Pergunte se tarefa esbarrar em:
- **Pontos em aberto** em `dominio-e-linguagem-ubiqua.md` ou `modelo-de-dados.md`
- Versao Java/Spring Boot/banco; reintroduzir Security/JWT/Lombok; adicionar H2; starter novo
- Editar migration ja aplicada; renomear pacote raiz; mudar estrutura de camadas
