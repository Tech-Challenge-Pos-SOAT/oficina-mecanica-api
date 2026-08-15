# Ferramentas e Skills

Instaladas por `./setup-ai.sh`. Cada uma tem propósito - leia quando aplicável.

## Decisão de Ferramentas

Não decida sozinho qual tool usar — vire em perguntas de contexto:
- **Criando feature nova?** `/superpowers:brainstorming` + planejamento.
- **Bug ou teste vermelho?** `/superpowers:systematic-debugging`.
- **Antes de abrir PR?** `/superpowers:requesting-code-review`.

## Ferramentas Disponíveis

| Ferramenta | O que faz | Como | Quando usar |
|---|---|---|---|
| **RTK** | Comprime saída de terminal (60-90% economia) | Hook automático | Sempre (transparente em `git`, `ls`, `grep`) |
| **Caveman** | Respostas terse, sem prosa | `/caveman lite\|full\|ultra` | MVP: economiza prosa |
| **Ponytail** | YAGNI: escreve mínimo que funciona | `/ponytail lite\|full\|ultra` | MVP: não especula |
| **Superpowers** | Skills de processo (plano, TDD, debug) | `/superpowers:<skill>` | Antes de grande decisão |
| **Context7** | Doc atualizada de lib/framework (MCP) | Automático em perguntas | Spring Boot, MapStruct, Testcontainers, etc. |

## Pré-requisitos

Verificados pelo `setup-ai.sh`:
- `git` (o repo)
- `npm` (Context7: `npx ctx7 setup`)
- `mvn` (build/testes)
- JDK 21 (pom.xml), Docker (Testcontainers), Claude Code CLI, `curl` (RTK)

## Context7 — Obrigatório

**Não vale pular**: Spring Boot 4 renomeou starters e moveu pacotes de teste. Memory da 3.x = código que não compila.
Consulte Context7 (MCP) antes de escrever import de Spring, sempre.

## Observações de Segurança

Nada envia código para serviço externo além dos já listados.
Nenhum plugin escreve no Trello ou Miro — board atualizado a mão.
