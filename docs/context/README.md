# Dicionario de contextos

Indice do que ler **antes** de mexer no codigo. Nao leia tudo - leia o que a
tarefa pede. Cada linha e "quando isso -> abra aquilo".

## Regras que valem para tudo

1. **Codigo e 100% em ingles** - pasta, arquivo, classe, metodo, variavel, campo,
   enum, nome de teste, path de URL. Docs e conversa em portugues. Os nomes saem
   de `modelo-de-dados.md`, que espelha o schema.
2. **Spring Boot 4** - starters e pacotes de anotacao mudaram em relacao a 3.x.
   Consulte o Context7 antes de escrever import de Spring; nao va de memoria.
3. **Flyway cria o schema**, `ddl-auto` e `validate`. Mudou entidade? Migration
   nova junto.

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

## Arquivos

| Arquivo | Conteudo |
|---|---|
| `docs/context/git-workflow.md` | Branch (`feature/nome`), conventional commits (tipo+escopo EN, corpo PT), PR, exemplos bom/ruim, CI pipeline. |
| `docs/context/modelo-de-dados.md` | Schema SQL, regra tabela->classe, migration, perguntas em aberto. **Fonte de verdade dos nomes.** |
| `docs/context/dominio-e-linguagem-ubiqua.md` | Glossario PT->EN, agregado `ServiceOrder`, transicoes, VOs, pontos em aberto. |
| `docs/context/arquitetura-ddd.md` | Mapa de pacotes, direcao de dependencia, fatia `Customer` completa. |
| `docs/context/testes.md` | Tipologia (VO/agregado/use case/JPA/controller), **imports Boot 4**, nomenclatura, exemplos bom/ruim. |
| `docs/context/mapstruct.md` | Localizacao mapper, config, VO no MapStruct, exemplos bom/ruim. |
| `docs/context/swagger.md` | Anotacoes obrigatorias, exemplos bom/ruim, regras. |
| `docs/context/ferramentas-e-skills.md` | Pre-requisitos, RTK, Caveman, Ponytail, Superpowers, Context7. |

Contextos focados **apenas em decisoes de codigo**. Requisitos humanos (edital, escopo, etc.) ficam fora.

## Antes de decidir sozinho - PARE

Os **pontos em aberto** estao em `docs/context/dominio-e-linguagem-ubiqua.md` e em
`docs/context/modelo-de-dados.md` (secao "Perguntas em aberto do schema"). Se a
tarefa esbarrar em um deles, **pergunte ao usuario**. Nao assuma a regra.

Tambem nao decida sozinho:

- trocar versao de Java, Spring Boot ou banco;
- **reintroduzir Spring Security / JWT** (removidos de proposito);
- reintroduzir Lombok;
- adicionar H2 (o banco de teste e Postgres via Testcontainers);
- adicionar starter ou dependencia nova (os starters de teste do Boot 4 sao
  granulares - falta um? pergunte);
- editar migration ja aplicada (o Flyway falha por checksum - crie uma nova);
- renomear o pacote raiz `com.postech.oficinamecanica`;
- mudar a estrutura de camadas;
- alterar o Trello ou o Miro.

## Manutencao

Ao adicionar um contexto novo em `docs/context/`, adicione a linha nas duas
tabelas acima. As rules de IA (`CLAUDE.md`, `.cursor/rules/`) apontam para este
indice - elas nao listam arquivo por arquivo, entao so este arquivo precisa mudar.
