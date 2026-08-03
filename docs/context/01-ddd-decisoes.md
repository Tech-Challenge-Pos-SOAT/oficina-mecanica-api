# Decisoes de DDD ja tomadas (board do Miro)

Board: "Oficina Mecanica - DDD (Event Storming + Storytelling)".
Este arquivo resume o que ja foi modelado, para servir de ponto de partida
para a implementacao - nao substitui o board, que e a fonte oficial para a
entrega.

## Subdominios identificados

- **Core (subdominio principal):** Ordem de Servico - abertura e
  acompanhamento. E o que diferencia a oficina da concorrencia, deve receber
  o maior esforco de design.
- **Suporte:** cadastro de clientes e veiculos, gestao de pecas/insumos.
- **Generico:** autenticacao de usuarios do sistema.

## Contextos delimitados (Bounded Contexts) mapeados

- Cliente
- Veiculo
- Servico
- Peca / Estoque
- Ordem de Servico (agregado central)

Relacao observada: o contexto de Ordem de Servico e Cliente do contexto de
Gestao de Pecas (padrao Cliente-Fornecedor). Se o controle de estoque tiver
particularidades, considerar uma Camada Anticorrupcao entre os dois.

## Fluxo principal (Criacao e acompanhamento da OS) - visao geral

Atendente registra cliente + veiculo -> cria a OS (status inicial) ->
Mecanico faz diagnostico -> adiciona servicos/pecas na OS -> gera orcamento
-> cliente aprova ou recusa -> se aprovado, reserva/baixa peca -> status
"aguardando execucao" -> mecanico inicia execucao -> se encontrar reparo
adicional, volta a etapa de adicionar servicos/pecas (gera novo orcamento
parcial) -> finaliza o servico -> cliente retira o veiculo -> OS concluida.

Pontos que ficaram em aberto no desenho do fluxo (revisar com o time antes de
travar a regra em codigo):
1. Reserva de peca acontece antes ou depois da aprovacao do orcamento pelo
   cliente? (Recomendacao: so reservar/baixar estoque apos aprovacao, para
   nao travar peca em orcamento que pode ser recusado.)
2. O que fazer quando o mecanico descobre que uma peca nao existe no
   catalogo — encerrar a OS, ou permitir cadastrar a peca e continuar?
3. Regra clara para diferenciar "recusa total do orcamento inicial" (fim de
   OS) de "recusa parcial de reparo adicional durante a execucao" (retorna
   para "em execucao").

## Linguagem Ubiqua (termos centrais)

- **Ordem de Servico (OS):** agregado central, com status proprio.
- **Orcamento:** gerado automaticamente a partir de servicos + pecas
  selecionados; precisa de aprovacao do cliente antes da execucao.
- **Diagnostico:** etapa realizada pelo mecanico apos o recebimento do
  veiculo, que determina quais servicos/pecas entram no orcamento.
- **Reserva de peca / Baixa de estoque:** dois momentos distintos do ciclo de
  vida do estoque (reservar != debitar definitivamente).
- **Reparo adicional:** necessidade identificada durante a execucao, que
  exige um novo ciclo de orcamento/aprovacao sem reiniciar a OS.

Consultar o board do Miro para o dicionario completo e para termos
ambiguos/sinonimos identificados por contexto (ex.: "peca" tem significado
diferente para o mecanico - componente fisico - e para o financeiro - item de
estoque com custo).
