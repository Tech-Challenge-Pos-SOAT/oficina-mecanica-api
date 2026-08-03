# Estado do board Trello (na data desta analise)

Board: "FASE 1 - Tech Challenge - Pos SOAT"
https://trello.com/b/Ne2Bg2Ll/fase-1-tech-challenge-p%C3%B3s-soat

## Cartoes ja existentes (resumo)

- Documentacao: Links uteis, Ferramentas, Padroes de codigo (cobertura minima
  80%, revisao obrigatoria por outra pessoa, conventional commits).
- Backlog: criar repositorio no GitHub, CI no GitHub Actions (workflow
  Check -> Build -> Test -> Sonar -> PR, com regra de branch `feature/` e
  bloqueio de merge sem aprovacao), Docker/docker-compose, relatorio de
  vulnerabilidade, PDF final, sistema de login (JWT), video de ate 15 min.
- A Fazer: os dois cartoes de DDD (Gestao de pecas e insumos; Criacao e
  acompanhamento da OS) - **estavam com vencimento em 01/08, ja passado**.

## Lacuna mais importante identificada

Nao havia, ate a data desta analise, nenhum cartao para a implementacao das
APIs de dominio em si (CRUD de clientes/veiculos/servicos/pecas, fluxo de
criacao/acompanhamento da OS, orcamento, aprovacao). So existiam cartoes de
infraestrutura (login, CI, Docker) e de documentacao/DDD. Vale confirmar com
o time se isso ja foi resolvido antes de comecar a codar, para nao duplicar
planejamento.

## Outras lacunas menores observadas

- Sem cartao para README.md.
- Sem cartao para "escolha e justificativa do banco de dados" (exigida no
  edital).
- "Linguagem Ubiqua aplicada" nao aparecia como item explicito dentro dos
  cartoes de DDD (so Bounded Contexts / Mapa de Contexto).
- Varios cartoes-modelo padrao do Trello (Backlog, A Fazer, Em andamento,
  Teste, Revisao de codigo, Concluido, "[Exemplo de tarefa]") ainda
  misturados com os cartoes reais.

Nenhuma alteracao foi feita no board a partir desta analise - qualquer ajuste
deve ser confirmado com o time antes de ser aplicado.
