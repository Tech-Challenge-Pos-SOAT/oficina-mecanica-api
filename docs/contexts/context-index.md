# Context Index

Leia só o que sua tarefa pede. Cada linha: "quando preciso fazer X → leia Y".

## Regras Globais

1. **Código 100% EN**; docs/conversa PT. Nomes saem de `modelo-de-dados.md`.
2. **Spring Boot 4**: renomeou starters/pacotes — consulte Context7 antes de imports.
3. **Flyway** cria schema (ddl-auto=validate). Mudou entidade? Nova migration.

## Tabela de Redirecionamento

| Tarefa | Arquivo |
|---|---|
| Qualquer coisa neste repo (stack, proibicoes, comandos, CI) | `CLAUDE.md` |
| Nome de classe, campo, tabela, migration ou schema | `modelo-de-dados.md` |
| Entidade, agregado, VO, enum de status, glossario PT→EN | `dominio-e-linguagem-ubiqua.md` |
| Pacote, layer, estrutura, controller, use case, DTO, repository | `arquitetura-ddd.md` |
| Exceção de domínio, handler global, mapeamento para HTTP | `api-exception-handler.md` |
| Teste (VO/regra/use case/JPA/HTTP), imports Spring Boot 4 | `testes-automatizados.md` |
| Converter DTO ↔ domain ↔ JPA entity | `mapstruct.md` |
| Anotar endpoint com OpenAPI/Swagger, @Tag/@Operation/@Schema | `openapi-annotations.md` |
| Ferramentas IA ativas (RTK, Caveman, Ponytail, etc) | `ferramentas-e-skills.md` |
| Branch, commit, PR, CI pipeline | `git-workflow.md` |

## Não Decida Sozinho

Pergunte se tarefa esbarrar em:

- **Pontos em aberto** em `dominio-e-linguagem-ubiqua.md` ou `modelo-de-dados.md`
- Versão Java/Spring Boot/banco
- Editar migration já aplicada
- Renomear pacote raiz; mudar estrutura de camadas
