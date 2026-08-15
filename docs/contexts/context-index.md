# Context Index

Leia so o que sua tarefa pede. Cada linha: "quando preciso fazer X → leia Y".

<regras_globais>

1. **Codigo 100% EN**; docs/conversa PT. Nomes saem de `modelo-de-dados.md`.
2. **Spring Boot 4**: renomeou starters/pacotes — consulte Context7 antes de imports.
3. **Flyway** cria schema (ddl-auto=validate). Mudou entidade? Nova migration.

</regras_globais>

<tabela_redirecionamento>

| Tarefa | Arquivo |
|---|---|
| Qualquer coisa neste repo (stack, proibicoes, comandos, CI) | `CLAUDE.md` |
| Nome de classe, campo, tabela, migration ou schema | `modelo-de-dados.md` |
| Entidade, agregado, VO, enum de status, glossario PT→EN | `dominio-e-linguagem-ubiqua.md` |
| Pacote, layer, estrutura, controller, use case, DTO, repository | `arquitetura-ddd.md` |
| Teste (VO/regra/use case/JPA/HTTP), imports Spring Boot 4 | `testes-automatizados.md` |
| Converter DTO ↔ domain ↔ JPA entity | `mapstruct.md` |
| Anotar endpoint com OpenAPI/Swagger, @Tag/@Operation/@Schema | `openapi-annotations.md` |
| Ferramentas IA ativas (RTK, Caveman, Ponytail, etc) | `ferramentas-e-skills.md` |
| Branch, commit, PR, CI pipeline | `git-workflow.md` |

</tabela_redirecionamento>

<nao_decida_sozinho>

Pergunte se tarefa esbarrar em:

- **Pontos em aberto** em `dominio-e-linguagem-ubiqua.md` ou `modelo-de-dados.md`
- Versao Java/Spring Boot/banco
- Reintroduzir Security/JWT/Lombok; adicionar H2; starter novo
- Editar migration ja aplicada
- Renomear pacote raiz; mudar estrutura de camadas

</nao_decida_sozinho>
