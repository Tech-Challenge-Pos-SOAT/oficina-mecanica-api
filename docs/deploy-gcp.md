# Deploy no Google Cloud (GKE Autopilot + Cloud SQL)

Este guia leva do zero ao ambiente final do Tech Challenge Fase 2. O desenho
completo esta em `docs/arquitetura.md`. Tudo que cria recurso na nuvem esta no
Terraform (`infra/`), os manifestos estao em `k8s/` e o pipeline de entrega
em `.github/workflows/cd.yml`.

## 1. Conta e projeto

Crie a conta gratuita do Google Cloud (o credito inicial cobre este projeto com
folga), crie um projeto novo e anote o `PROJECT_ID`. Em Billing, crie um alerta
de orcamento (por exemplo US$ 20 com avisos em 50%, 90% e 100%) para nao ter
surpresa. Instale e autentique o gcloud e o Terraform na sua maquina:

```bash
gcloud auth login
gcloud auth application-default login
```

## 2. Estado remoto e infraestrutura

O `bootstrap.sh` habilita as APIs minimas e cria o bucket que guarda o estado do
Terraform. Em seguida o Terraform cria Artifact Registry, GKE Autopilot, Cloud
SQL, service accounts e a Workload Identity Federation do GitHub.

```bash
cd infra
./bootstrap.sh SEU_PROJECT_ID
cp terraform.tfvars.example terraform.tfvars      # edite project_id e github_repository
export TF_VAR_db_password='uma-senha-forte-nova'
terraform init -backend-config="bucket=SEU_PROJECT_ID-tfstate"
terraform apply
terraform output
```

O cluster Autopilot e o Cloud SQL levam de 10 a 15 minutos para ficar prontos.

## 3. Variaveis e segredos do GitHub

Em Settings, Secrets and variables, Actions, cadastre como **Variables** os
valores que saem do `terraform output`: `GCP_PROJECT_ID`, `GCP_REGION`,
`GKE_CLUSTER`, `CLOUDSQL_CONNECTION_NAME` (output `instance_connection_name`),
`GCP_WORKLOAD_IDENTITY_PROVIDER` (output `wif_provider`), `GCP_SERVICE_ACCOUNT`
(output `deployer_service_account`) e `MAIL_FROM`. Cadastre como **Secrets**:
`DB_PASSWORD` (a mesma do Terraform), `JWT_SECRET` (um valor longo e aleatorio,
gere com `openssl rand -base64 48`), `BREVO_SMTP_USERNAME` e
`BREVO_SMTP_PASSWORD`. Use credenciais novas: as do ambiente de testes antigo
nao devem ser reaproveitadas.

## 4. Primeiro deploy

Um push na `main` roda o CI e, se passar, o workflow "CD (GKE)" builda a imagem,
publica no Artifact Registry e aplica os manifestos. Tambem da para disparar
manualmente em Actions, "CD (GKE)", Run workflow. Acompanhe e pegue o IP:

```bash
gcloud container clusters get-credentials oficina-cluster --region us-central1
kubectl -n oficina get pods,hpa,svc
curl http://IP_EXTERNO/actuator/health
```

O Swagger fica em `http://IP_EXTERNO/swagger-ui.html`.

## Validacao manual pelo Cloud Shell (antes do pipeline)

Para validar a infraestrutura sem depender do GitHub, abra o Cloud Shell no
console (icone de terminal no topo), clone o repositorio, rode o Terraform e use
o script de deploy manual, que builda a imagem, publica no Artifact Registry,
cria o Secret e aplica os manifestos, mostrando o IP no final:

```bash
export DB_PASSWORD='a-mesma-senha-do-terraform'
export JWT_SECRET="$(openssl rand -base64 48)"
./scripts/deploy-gcp-manual.sh
```

## 5. Demonstracao do HPA

Com o k6 instalado, rode o teste de carga e observe as replicas subirem em
outro terminal (detalhes em `docs/roteiro-video.md`):

```bash
BASE_URL=http://IP_EXTERNO k6 run load-test/hpa-load.js
kubectl -n oficina get hpa -w
```

## 6. Encerramento

Terminada a avaliacao, `terraform destroy` em `infra/` remove cluster, banco e
registry, e o custo volta a zero.

## Problemas comuns

Se o pod reiniciar logo na primeira subida, quase sempre e o Flyway tentando
conectar antes do proxy do Cloud SQL ficar pronto: o Kubernetes reinicia e na
segunda tentativa funciona. Se aparecer `permission denied` no proxy, confira o
annotation `iam.gke.io/gcp-service-account` na ServiceAccount e se o
`terraform apply` terminou sem erro no recurso `api_workload_identity`. Se o
HPA mostrar `<unknown>` na CPU, confira se o Deployment tem `resources.requests.cpu`
(tem) e aguarde cerca de um minuto pelo metrics-server.
