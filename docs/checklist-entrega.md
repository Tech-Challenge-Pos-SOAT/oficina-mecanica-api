# Checklist de entrega — Tech Challenge Fase 2

Conferencia contra o enunciado. Marcado como feito (`[x]`) apenas o que foi
verificado nos arquivos reais do repositorio nesta sessao (28/10/2026, branch
`feature/fase2-entrega`). Prazo no portal do aluno: **03/11/2026**.

## Codigo e testes

- [x] Codigo refatorado para a Fase 2 (listagem por status/idade, abertura de
  OS com servicos e pecas, Actuator, ShedLock, e-mail via Brevo).
- [x] Testes automatizados: `mvn test` — **495 testes, 0 falhas, 0 erros, 0
  skipped** (resultado real, rodado nesta sessao; Testcontainers via Docker).
- [x] As cinco APIs do dominio presentes: Clientes (`/api/customers`),
  Veiculos (`/api/vehicles`), Materiais (`/api/materials`), Servicos
  (`/api/services`), Ordem de Servico (`/api/service-orders` +
  `/public/service-orders`). Tambem existem `Auth` e `Employees`, adicionadas
  ao longo do projeto.

## Container e Kubernetes

- [x] `Dockerfile` e `docker-compose.yml` na raiz.
- [x] `/k8s` com `namespace.yaml`, `serviceaccount.yaml`, `configmap.yaml`,
  `secret.example.yaml`, `deployment.yaml` (sidecar Cloud SQL Auth Proxy),
  `service.yaml` (LoadBalancer), `hpa.yaml` (2 a 5 replicas, CPU 60%),
  `pdb.yaml`, `kustomization.yaml`.

## Infraestrutura

- [x] `/infra` com Terraform documentado (`infra/README.md` lista todos os
  recursos: GKE Autopilot, Cloud SQL, Artifact Registry, IAM, Workload
  Identity Federation) e secao de problemas conhecidos.

## Pipeline

- [x] CI (`.github/workflows/ci.yml`): check de branch, build, test (+
  JaCoCo), dependency-check (OWASP), trivy (imagem), sonar.
- [x] CD (etapa 8 do `.github/workflows/ci.yml`, so na branch
  `deploy-google-cloud`): build e push da imagem no Artifact Registry, deploy no
  cluster (Secret a partir de GitHub Secrets, apply dos manifestos, rollout
  status) — autenticado via Workload Identity Federation.

## Documentacao

- [x] `README.md`: descricao da solucao, objetivos da Fase 2, diagrama
  Mermaid (componentes + infraestrutura + fluxo CI/CD), execucao local,
  deploy em Kubernetes, provisionamento com Terraform, secao "Collection das
  APIs" (link absoluto para `docs/insomnia` na `main` + mencao ao Swagger),
  secao "Video demonstrativo" (placeholder `SEU_LINK_DO_VIDEO_AQUI`), secao
  "Resultado do teste de escalabilidade" com os numeros reais.
- [x] `docs/img/arquitetura.png` e `.svg` gerados via `mmdc`, referenciados
  no README.
- [x] `docs/entrega/Tech-Challenge-Fase2-Oficina-Mecanica.pdf` gerado por
  `scripts/gerar-pdf-entrega.sh` (capa, link do repositorio + compartilhamento
  com `soat-architecture`, arquitetura com paragrafo por recurso, link do
  video com placeholder).
- [x] `docs/roteiro-video.md`: roteiro com marcacao de tempo, comandos
  exatos, pre-requisitos e plano B, cobrindo deploy, CI/CD, APIs e
  escalabilidade — cabe em 15 minutos.
- [x] Collection de APIs em `docs/insomnia/`.

## Dependente de voce (nao verificavel em arquivo)

- [ ] Preencher nomes reais dos integrantes na capa do PDF (hoje com
  placeholders) e regenerar: `scripts/gerar-pdf-entrega.sh URL_DO_VIDEO`.
- [ ] Gravar o video (ate 15 minutos) seguindo `docs/roteiro-video.md`.
- [ ] Subir o video no YouTube ou Vimeo e colar o link no README (no lugar
  de `SEU_LINK_DO_VIDEO_AQUI`) e no PDF.
- [ ] Compartilhar o repositorio com o usuario **soat-architecture** no
  GitHub (Settings > Collaborators).
- [ ] Abrir o Pull Request desta branch (`feature/fase2-entrega`) para a
  `main`.
- [ ] Confirmar que o CI e o CD ficam **verdes** apos o merge na `main`.
- [ ] Rotacionar segredos que apareceram em prints: chave SMTP da Brevo
  (`BREVO_SMTP_PASSWORD`) e credenciais do Dokploy.
- [ ] Enviar no portal do aluno, ate **03/11/2026**: PDF com link do
  repositorio, desenho da arquitetura e link do video.
- [ ] Depois da avaliacao: `terraform destroy` em `infra/` para zerar o
  custo do ambiente no Google Cloud.
