# Resumo do edital - Tech Challenge Fase 1 (POSTECH/FIAP)

Vale 90% da nota da fase. Fonte: "15SOAT - Fase 1 - Tech Challenge.pdf".

## Desafio

Oficina mecanica de medio porte, hoje com atendimento/diagnostico/execucao
desorganizados (anotacoes manuais, planilhas). Problemas: erros de priorizacao,
falhas de controle de pecas, dificuldade de acompanhar status, perda de
historico de clientes/veiculos, ineficiencia no fluxo de orcamento/autorizacao.

## Proposta

Desenvolver o MVP do back-end do sistema, com foco em gestao de ordens de
servico, clientes e pecas, aplicando DDD e boas praticas de Qualidade de
Software e Seguranca.

## Funcionalidades obrigatorias

**Criacao da OS:** identificacao do cliente por CPF/CNPJ; cadastro de veiculo
(placa, marca, modelo, ano); inclusao de servicos solicitados; inclusao de
pecas/insumos; orcamento gerado automaticamente; envio do orcamento ao
cliente para aprovacao.

**Acompanhamento da OS:** status (Recebida, Em diagnostico, Aguardando
aprovacao, Em execucao, Finalizada, Entregue); transicao automatica de status
conforme acoes no sistema; consulta do cliente via API.

**Gestao administrativa:** CRUD de clientes, veiculos, servicos, pecas/insumos
(com controle de estoque); listagem/detalhamento de OS; monitoramento do
tempo medio de execucao.

**Seguranca e qualidade:** autenticacao JWT nas APIs administrativas; validacao
de dados sensiveis (CPF/CNPJ, placa); testes unitarios e de integracao para os
principais fluxos.

## Requisitos tecnicos

- Back-end monolitico (arquitetura em camadas e aceitavel, ja que e um MVP).
- Banco de dados livre, mas a escolha precisa ser justificada.
- APIs RESTful documentadas via Swagger (ou similar).
- Dockerfile + docker-compose.yml.
- Cobertura minima de 80% de testes automatizados nos dominios criticos.
- README.md explicando como rodar localmente.
- Repositorio privado no GitHub, com acesso liberado para o usuario `soat-architecture`.

## Entregaveis da Fase 1

1. Video de ate 15 minutos demonstrando todos os pontos.
2. Documentacao DDD (Miro ou equivalente): Event Storming completo dos fluxos
   de (a) criacao/acompanhamento da OS e (b) gestao de pecas/insumos; diagramas
   da disciplina de DDD; Linguagem Ubiqua aplicada.
3. Codigo-fonte no repositorio privado: APIs, Dockerfile/docker-compose,
   README.md completo.
4. Relatorio de analise de vulnerabilidades (incluindo o scan realizado no
   codigo).
5. Documento de entrega em PDF: nome do grupo, participantes e usernames no
   Discord, link da documentacao, link do repositorio, relatorio de
   vulnerabilidades.
