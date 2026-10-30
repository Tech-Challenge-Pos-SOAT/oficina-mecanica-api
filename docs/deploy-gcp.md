# Deploy no Google Cloud (GKE Autopilot + Cloud SQL)

Este guia leva do zero ao ambiente final do Tech Challenge Fase 2. O desenho
completo esta em `docs/arquitetura.md`. Tudo que cria recurso na nuvem esta no
Terraform (`infra/`), os manifestos estao em `k8s/` e o pipeline de entrega
em `.github/workflows/ci.yml` (CI e CD no mesmo workflow).

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

CI e CD ficam em um unico workflow, `.github/workflows/ci.yml`. A etapa 8
("Deploy no GKE") roda so em push na branch `deploy-google-cloud`, depois de
build, testes, Dependency-Check e Trivy: builda a imagem, publica no Artifact
Registry e aplica os manifestos. Nas outras branches e em PR ela fica
"skipped", o que evita gastar credito do GCP a cada merge comum. Como o gatilho
e o proprio push, o deploy nao depende da `main` nem de merge. O Sonar (runner
self-hosted) roda em paralelo ao deploy e nao o bloqueia. Para subir qualquer branch de
trabalho, sem passar pela `main`:

```bash
git push origin SUA-BRANCH:deploy-google-cloud
# se a deploy-google-cloud ja existir e estiver atras, use --force-with-lease
```

Acompanhe e pegue o IP:

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

### ImagePullBackOff nos pods da API

**Causa**: os nos do GKE Autopilot usam a service account padrao do Compute
Engine, que por padrao nao tem permissao de leitura no Artifact Registry.

**Diagnostico**:

```bash
kubectl -n oficina get pods
kubectl -n oficina describe pod POD_COM_ERRO
# procure no final: "Failed to pull image" / "denied: Permission denied"
```

**Correcao via Terraform** (ambientes novos ou recriados): o recurso
`google_artifact_registry_repository_iam_member.nodes_pull_images` em
`infra/iam.tf` concede `roles/artifactregistry.reader` a essa service account.
Basta `terraform apply`.

**Correcao manual** (ambiente ja criado, sem reaplicar Terraform):

```bash
PROJECT_NUMBER=$(gcloud projects describe SEU_PROJECT_ID --format='value(projectNumber)')
gcloud artifacts repositories add-iam-policy-binding oficina \
  --location=us-central1 \
  --member="serviceAccount:${PROJECT_NUMBER}-compute@developer.gserviceaccount.com" \
  --role=roles/artifactregistry.reader
kubectl -n oficina rollout restart deployment oficina-api
```

### Instalar o k6 sem sudo no Cloud Shell

O Cloud Shell nao permite `apt install` sem privilegios. Baixe o binario para
`~/bin` (ja esta no `PATH` do Cloud Shell):

```bash
mkdir -p ~/bin
curl -L https://github.com/grafana/k6/releases/latest/download/k6-v0.54.0-linux-amd64.tar.gz \
  | tar -xz --strip-components=1 -C ~/bin k6-v0.54.0-linux-amd64/k6
k6 version
```

### Rodar o teste de carga com parametros ajustaveis

`load-test/hpa-load.js` aceita `PEAK_VUS` (pico de usuarios virtuais) e
`P95_MS` (limite de latencia do p(95) usado no threshold do k6):

```bash
# padrao: 150 VUs, p(95) < 1500ms
BASE_URL=http://IP_EXTERNO k6 run load-test/hpa-load.js

# para o video, carga menor e threshold mais tolerante ao hash de senha do login
BASE_URL=http://IP_EXTERNO k6 run -e PEAK_VUS=60 -e P95_MS=3000 load-test/hpa-load.js
```

## Teste de fumaca ponta a ponta (e e-mails)

`scripts/smoke-test.sh` percorre o fluxo completo da OS contra um ambiente no ar
e falha se qualquer passo devolver codigo ou status inesperado: health, login,
401 sem token, abertura da OS com servico e peca, diagnostico, orcamento,
consulta publica, listagem ordenada, aprovacao, finalizacao, entrega e saida da
listagem. Ele aponta o e-mail do cliente do seed (Maria, 529.982.247-25) para o
e-mail informado, entao os avisos de status chegam nessa caixa:

```bash
scripts/smoke-test.sh http://IP_EXTERNO kawanmelofernandes@gmail.com
PAUSE=1 scripts/smoke-test.sh http://IP_EXTERNO seu@email.com   # pausa a cada passo (video)
REJECT=1 scripts/smoke-test.sh http://IP_EXTERNO seu@email.com  # inclui a recusa do orcamento
```

Os e-mails saem do remetente `MAIL_FROM` (precisa estar validado no Brevo).
Se nao chegarem, olhe a caixa de spam e os logs:
`kubectl -n oficina logs -l app=oficina-api -c api --tail=100 | grep -i mail`.
