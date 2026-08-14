# Ferramentas e skills

Instaladas por `./setup-ai.sh`. Cada uma tem proposito - leia quando aplicavel.

<skill>
Nao decida sozinho qual tool usar — vire em perguntas de contexto:
- **Criando feature nova?** `/superpowers:brainstorming` + planejamento.
- **Bug ou teste vermelho?** `/superpowers:systematic-debugging`.
- **Antes de abrir PR?** `/superpowers:requesting-code-review`.
</skill>

<tools>
| Ferramenta | O que faz | Como | Quando usar |
|---|---|---|---|
| **RTK** | Comprime saida de terminal (60-90% economia) | Hook automatico | Sempre (transparente em `git`, `ls`, `grep`) |
| **Caveman** | Respostas terse, sem prosa | `/caveman lite\|full\|ultra` | MVP: economiza prosa |
| **Ponytail** | YAGNI: escreve minimo que funciona | `/ponytail lite\|full\|ultra` | MVP: nao especula |
| **Superpowers** | Skills de processo (plano, TDD, debug) | `/superpowers:<skill>` | Antes de grande decisao |
| **Context7** | Doc atualizada de lib/framework (MCP) | Automatico em perguntas | Spring Boot, MapStruct, Testcontainers, etc. |
</tools>

<config>
Pre-requisitos verificados pelo `setup-ai.sh`:
- `git` (o repo)
- `npm` (Context7: `npx ctx7 setup`)
- `mvn` (build/testes)
- JDK 21 (pom.xml), Docker (Testcontainers), Claude Code CLI, `curl` (RTK)
</config>

<context7-obrigatorio>
**Nao vale pular**: Spring Boot 4 renomeou starters e moveu pacotes de teste. Memory da 3.x = codigo que nao compila.
Consulte Context7 (MCP) antes de escrever import de Spring, sempre.
</context7-obrigatorio>

<nota>
Nada envia codigo para servico externo alem dos ya listados.
Nenhum plugin escreve no Trello ou Miro — board atualizado a mao.
</nota>
