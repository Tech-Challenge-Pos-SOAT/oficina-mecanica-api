# Roteiro do video (ate 15 minutos)

O enunciado exige um video (YouTube ou Vimeo, ate 15 minutos) mostrando: o
deploy da aplicacao, a execucao do CI/CD, o consumo das APIs e a
escalabilidade automatica. O roteiro abaixo cobre exatamente esses quatro
blocos, com tempo e comandos de cada cena.

## Pre-requisitos antes de gravar

- Abas abertas: console do Google Cloud em Kubernetes Engine > Workloads,
  Cloud SQL, Artifact Registry e Monitoring > Escalonamento (HPA); repositorio
  no GitHub (Actions); Swagger da aplicacao; Insomnia com a colecao
  importada; `docs/arquitetura.md` com o diagrama.
- Dois terminais: um autenticado no cluster (`gcloud container clusters
  get-credentials oficina-cluster --region us-central1`), outro livre para o
  `k6`.
- Pegue o IP do LoadBalancer antes de gravar e anote: `kubectl -n oficina get
  svc oficina-api` (coluna `EXTERNAL-IP`). Exporte: `export
  BASE_URL=http://IP_EXTERNO`.
- **O HPA precisa comecar em 2 replicas.** Se um teste de carga rodou antes,
  aguarde cerca de 5 minutos (janela de estabilizacao de scale-down) e
  confirme com `kubectl -n oficina get hpa` antes de iniciar a gravacao.
- Nao mostre a tela de Secrets/Variables do GitHub nem variaveis de ambiente
  com senha, token ou chave.

## Plano B (se o CD falhar ao vivo)

Se o workflow "CD (GKE)" falhar durante a gravacao, nao tente depurar ao
vivo: comente que isso acontece e explique a causa mais comum por nome (ex.:
`ImagePullBackOff` por falta de `roles/artifactregistry.reader`, documentado
em `docs/deploy-gcp.md`), mostre o deploy manual como alternativa
(`./scripts/deploy-gcp-manual.sh`) e siga para a proxima cena com os pods ja
rodando de uma execucao anterior. O objetivo e provar que o pipeline existe e
funciona normalmente, nao arriscar o tempo do video corrigindo um incidente.

## Sequencia

### 00:00 - 00:30 - Abertura

Apresente o projeto: API da oficina mecanica, Clean Architecture, Fase 2 do
Tech Challenge, e o que o video vai mostrar (deploy, CI/CD, APIs,
escalabilidade).

### (a) 00:30 - 04:00 - Deploy da aplicacao

Mostre o diagrama de `docs/arquitetura.md`, depois os recursos no console:

1. Console > Cloud SQL: instancia `oficina-db` (PostgreSQL 16) rodando.
2. Console > Artifact Registry > repositorio `oficina`: imagem publicada.
3. Console > Kubernetes Engine > Workloads: Deployment `oficina-api` com
   pods healthy.
4. Terminal:
   ```bash
   kubectl -n oficina get pods,svc
   ```
   Destaque o `EXTERNAL-IP` do Service e os pods `2/2 Running` (API +
   Cloud SQL Auth Proxy no mesmo Pod).

Fale: tudo isso nasceu do `terraform apply` em `infra/` — cluster, banco,
registry e IAM como codigo, sem clique manual no console.

### (b) 04:00 - 07:30 - Execucao do CI/CD

1. Mostre um Pull Request mesclado na `main` e o **CI** rodando (build,
   testes, dependency-check, trivy, sonar) — aponte os estagios na tela.
2. Atualize a branch `deploy-google-cloud` para disparar o deploy
   (`git push origin SUA-BRANCH:deploy-google-cloud`); o CD roda por push nessa
   branch, sem depender da `main` e sem esperar o CI completo, de proposito,
   pra nao gastar credito do GCP em todo merge.
3. Workflow **CD (GKE)**: build da imagem, push no Artifact Registry e
   aplicacao dos manifestos.
4. Terminal, acompanhando o rollout:
   ```bash
   kubectl -n oficina rollout status deployment/oficina-api
   ```

Fale: autenticacao do pipeline no Google Cloud via Workload Identity
Federation, sem chave JSON armazenada.

### (c) 07:30 - 11:30 - Consumo das APIs (Insomnia)

Siga o fluxo real da OS:

1. **Login**: `POST /auth/login` com `carlos.souza@oficina.com` / `senha123`
   — copie o token.
2. **Abertura de OS com servicos e pecas**: `POST /api/service-orders` com
   `services` e `materials` no corpo — mostre o `id` retornado na resposta.
3. **Listagem ordenada**: `GET /api/service-orders` — mostre que a ordem
   segue status e idade e que OS finalizadas/entregues nao aparecem.
4. **Consulta publica de status** (sem token): `GET
   /public/service-orders?document=...`.
5. **Aprovacao de orcamento**: rota de aprovacao da OS criada.
6. **Atualizacao de status**: `PATCH` do status da OS e, na caixa de e-mail
   (ou no log da aplicacao), mostre a notificacao enviada via Brevo.

### (d) 11:30 - 14:30 - Escalabilidade automatica

1. Confirme `kubectl -n oficina get hpa` mostrando 2 replicas antes de
   iniciar.
2. Em um terminal, deixe rodando:
   ```bash
   kubectl -n oficina get hpa -w
   ```
3. No outro terminal, dispare a carga:
   ```bash
   BASE_URL=http://IP_EXTERNO k6 run -e PEAK_VUS=60 -e P95_MS=3000 load-test/hpa-load.js
   ```
4. Enquanto a carga sobe, alterne para o console > Monitoring >
   Escalonamento (ou a aba do HPA em Workloads) mostrando o grafico de CPU e
   replicas subindo de 2 para ate 5.

Fale os numeros reais validados: com 150 VUs as replicas foram de 2 para 5
em cerca de 1 minuto, CPU chegou a 269% do alvo de 60%, 0% de requisicoes
com falha.

### 14:30 - 15:00 - Encerramento

Resuma o que foi mostrado (deploy via Terraform, CI/CD sem chave JSON,
APIs da OS ponta a ponta, HPA escalando sob carga) e encerre.

## Dicas de gravacao

Corte esperas longas (criacao de cluster, rollout, carga do k6 subindo) com
edicao, mas mantenha os comandos e resultados visiveis antes do corte. Grave
em 1080p. Fale o motivo de cada decisao, nao so o que esta na tela.
