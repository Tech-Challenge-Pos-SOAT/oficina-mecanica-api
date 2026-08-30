# Coleção do Insomnia — fluxo da Ordem de Serviço

Importe `oficina-mecanica-api.insomnia.json` em **Insomnia > Import > From File**.
A coleção tem 24 requisições em 5 pastas, na ordem de execução.

## Ambientes

| Ambiente | baseUrl | Quando usar |
|---|---|---|
| Base Environment | `http://localhost:8080` | `docker compose up --build` (padrão) |
| Local (mvn spring-boot:run) | `http://localhost:8084` | rodando fora do Docker, na porta do `application.yml` |

Variáveis que você provavelmente vai ajustar: `token`, `orderId`, `serviceId`, `materialId`.

## Subindo a API

```bash
docker compose up --build          # sobe Postgres + API em http://localhost:8080
# ou
mvn spring-boot:run                # sobe em http://localhost:8084
```

O Flyway já cria o schema e semeia dados: 5 clientes (V2), 7 materiais (V3_1 e V4) e
1 funcionário (V6 — `carlos.souza@oficina.com` / `senha123`).

## Roteiro

1. **1. Autenticação → POST /auth/login**. Copie o `token` da resposta para a variável de
   ambiente `token`. Todas as requisições de `/api/*` usam Bearer com essa variável.
2. **2. Pré-requisitos**. O cliente já vem do seed (`529.982.247-25`, Maria Oliveira — confirme
   o `id` com o `GET /api/customers/document`). Crie o **veículo** com a placa `XYZ-9876` e um
   **serviço** de catálogo. Material não precisa criar: use `materialId=1` (Óleo 5W30, estoque 40).
3. **3. Fluxo da OS**. Abra a OS, guarde o `id` da resposta em `orderId` e siga a ordem das
   requisições até a entrega.
4. **4. Acompanhamento do cliente**. Rodam **sem** `Authorization` — é a consulta do cliente
   via API que o edital pede. A aprovação é o que dispara a baixa de estoque.
5. **5. Cenários de erro**. Cada uma prova uma regra; rode depois de entender o caminho feliz.

## O que esperar em cada passo

| Requisição | HTTP | Status da OS depois | Efeito colateral |
|---|---|---|---|
| POST /api/service-orders | 201 | `RECEIVED` | histórico ganha a 1ª entrada, `price` nulo |
| POST /{id}/diagnosis | 200 | `IN_DIAGNOSIS` | — |
| POST /{id}/services | 200 | `IN_DIAGNOSIS` | `price` = preço do serviço |
| POST /{id}/materials | 200 | `IN_DIAGNOSIS` | `price` soma preço × quantidade |
| POST /{id}/budget | 200 | `AWAITING_APPROVAL` | ordem congelada para novos itens |
| GET /public/service-orders?document= | 200 | — | lista sem token, com status e valor |
| POST /{id}/approval (approved=true) | 200 | `IN_EXECUTION` | **estoque debitado** + `material_transaction` OUT |
| POST /{id}/completion | 200 | `FINISHED` | observação vai para o histórico |
| POST /{id}/delivery | 200 | `DELIVERED` | histórico fecha com 6 entradas |

Conferindo a baixa: `GET /api/materials/1` (estoque menor) e
`GET /api/materials/1/transactions` (uma transação `OUT` ligada à OS).

## Reparo adicional

Com a OS em `IN_EXECUTION`, chame de novo `POST /{id}/materials` e depois
`POST /{id}/budget`: a ordem volta para `AWAITING_APPROVAL` com o orçamento atualizado.
Na nova aprovação **apenas o item novo** sai do estoque — os anteriores estão marcados
como `stock_debited`.

## Códigos de erro do fluxo

| Código | HTTP | Quando |
|---|---|---|
| `INVALID_STATUS_TRANSITION` | 409 | transição que a máquina de estados não permite |
| `SERVICE_ORDER_NOT_OPEN_FOR_ITEMS` | 409 | item incluído com a ordem aguardando aprovação, finalizada ou entregue |
| `SERVICE_ORDER_ACCESS_DENIED` | 403 | CPF/CNPJ informado não é o dono da ordem |
| `VEHICLE_NOT_OWNED_BY_CUSTOMER` | 409 | placa pertence a outro cliente |
| `BUSINESS_RULE_VIOLATION` | 400 | estoque insuficiente na aprovação, orçamento sem itens, material/serviço inativo |
| `SERVICE_ORDER_NOT_FOUND` | 404 | id inexistente |
