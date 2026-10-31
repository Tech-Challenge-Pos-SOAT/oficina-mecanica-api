# Manifestos Kubernetes (GKE)

Aplicados com `kubectl apply -k k8s/` (ou pelo pipeline). Ordem de leitura:
`namespace`, `serviceaccount` (Workload Identity), `configmap`, Secret criado a
parte (`secret.example.yaml`), `deployment` (API + Cloud SQL Auth Proxy),
`service` (LoadBalancer com IP publico), `hpa` e `pdb`.

Antes do primeiro deploy manual, troque `__PROJECT_ID__` (serviceaccount.yaml),
`__INSTANCE_CONNECTION_NAME__` (configmap.yaml) e a imagem em `kustomization.yaml`.
O passo a passo completo esta em `docs/deploy-gcp.md`.
