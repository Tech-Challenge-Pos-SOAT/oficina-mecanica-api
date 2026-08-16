# SonarQube local para o time (self-service)

Cada integrante do time pode subir o **proprio** SonarQube Community Edition
na sua maquina, ver o relatorio de qualidade e re-analisar quando quiser -
sem depender da maquina que roda o SonarQube usado pelo GitHub Actions.

Isso e **separado e independente** do job "6. SonarQube" do
`.github/workflows/ci.yml`: o CI continua apontando exclusivamente para a
maquina configurada nos secrets `SONAR_HOST_URL`/`SONAR_TOKEN` (ver
README.md, secao "Analise de qualidade de codigo (SonarQube)"). O SonarQube
"local" descrito aqui e so para inspecao/analise individual, no seu proprio
notebook.

## Pre-requisitos

- Docker Compose v2 (`docker compose`) ou Podman com suporte a compose
  (`podman compose` ou `podman-compose`). Docker Desktop, OrbStack e Podman
  Desktop ja trazem isso.
- **Memoria**: o SonarQube (Elasticsearch embutido) precisa de pelo menos
  ~2GB livres so para ele, alem do que o Postgres/app do projeto ja usam.
  Recomendado alocar **4-6GB** de RAM ao Docker Desktop/Podman Machine
  (Settings/Preferences > Resources > Memory). Em maquinas com pouca RAM
  disponivel, feche o `db`/`app` do projeto (`docker compose stop db app`)
  antes de subir o Sonar, se precisar liberar memoria.
- `curl` (ja vem em Mac/Linux; no Windows 10/11 tambem, via `curl.exe`).
- `mvn` (Maven) configurado, igual ao resto do projeto.

## Uso rapido

**macOS / Linux (ou Windows com WSL/Git Bash):**

```bash
./scripts/sonar-local.sh          # sobe o Sonar, faz bootstrap e ja roda a 1a analise
./scripts/sonar-local.sh analyze  # so re-roda a analise depois (quando quiser "ativar de novo")
./scripts/sonar-local.sh down     # para os containers, mantendo os dados
./scripts/sonar-local.sh reset    # apaga tudo e comeca do zero
```

**Windows (PowerShell nativo, sem WSL):**

```powershell
.\scripts\sonar-local.ps1
.\scripts\sonar-local.ps1 analyze
.\scripts\sonar-local.ps1 down
.\scripts\sonar-local.ps1 reset
```

Depois do primeiro `up`, acesse **http://localhost:9000** no navegador:
login `admin`, senha `oficina-mecanica-local` (o script troca a senha padrao
automaticamente no primeiro start; para usar outra senha, defina a variavel
de ambiente `SONAR_LOCAL_ADMIN_PASSWORD` antes de rodar `up`).

## O que os scripts fazem (por que sao "dinamicos")

1. **Detectam o runtime disponivel** na maquina (`docker compose`,
   `podman compose`, `docker-compose` ou `podman-compose`) - o time usa
   Windows/Linux/macOS com Docker Desktop, OrbStack e Podman misturados, e o
   script nao assume um especifico.
2. **Sobem so o perfil `sonar`** do `docker-compose.yml`
   (`sonarqube-db` + `sonarqube`), via Compose **profiles**
   (`profiles: ["sonar"]`). Isso significa que `docker compose up` "normal"
   (sem `--profile sonar`) continua subindo so `db` + `app`, exatamente como
   antes - o Sonar e 100% opt-in, nao afeta quem nao quer usar.
3. **Bootstrap automatico na primeira vez**: troca a senha padrao do admin
   (`admin`/`admin`) via API do SonarQube e gera um token de analise
   (`/api/user_tokens/generate`), salvo em `.sonar/local-token` (arquivo
   pessoal, **ja esta no `.gitignore`** - nunca vai pro Git).
4. **Analise via o mesmo goal Maven do CI**
   (`org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar`),
   so que apontando para `http://localhost:9000` com o token local -
   qualquer um pode rodar `analyze` quantas vezes quiser ("ativar para
   analisar de novo"), sem precisar abrir PR nem depender do CI.
5. **Projeto separado do CI**: a analise local usa o projectKey
   `oficina-mecanica-api-local`, diferente dos projectKeys usados pelo CI
   (`oficina-mecanica-api` / `oficina-mecanica-api-pr-<numero>`) - assim a
   sua analise local nunca sobrescreve/mistura com o dashboard usado pelo
   time no CI.

## Notas por sistema operacional / runtime

| SO | Runtime | Observacao |
|---|---|---|
| macOS | Docker Desktop | Funciona direto. Ajuste memoria em Settings > Resources. |
| macOS | OrbStack | Funciona direto, `docker compose` ja nativo. |
| macOS | Podman Desktop | Use `podman machine set --memory 6144` (MB) se precisar aumentar a memoria da VM antes do `up`. |
| Linux | Docker Desktop/Engine | Roda direto no kernel do host (sem VM) - se o Elasticsearch reclamar de `vm.max_map_count`, o script ja desabilita esse bootstrap check via `SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true` no `docker-compose.yml`, entao normalmente nao precisa mexer em `sysctl`. |
| Linux | Podman (rootless) | Se os volumes derem erro de permissao (SELinux), rode com `podman compose --profile sonar up -d` (Podman geralmente resolve os labels automaticamente); em distros com SELinux enforcing mais estrito, pode ser necessario ajustar o contexto dos volumes nomeados - normalmente nao e preciso, ja que os volumes aqui sao gerenciados pelo proprio Compose (nao bind mounts). |
| Windows | Docker Desktop (WSL2) | Use `scripts/sonar-local.sh` de dentro do WSL, ou `scripts/sonar-local.ps1` no PowerShell - qualquer um funciona, pois o Docker Desktop expoe o daemon para os dois. |
| Windows | Podman Desktop | Use `scripts/sonar-local.ps1`. Se `podman compose` nao estiver disponivel, instale `podman-compose` (`pip install podman-compose`) ou atualize o Podman Desktop para uma versao que ja inclui `podman compose`. |

## Problemas comuns

- **"SonarQube nao ficou UP a tempo"**: normalmente e memoria insuficiente.
  Veja os logs com `docker compose --profile sonar logs sonarqube` (ou
  `podman compose ...`) e procure por erros de Elasticsearch/OOM. Aumente a
  memoria alocada ao Docker/Podman (ver tabela acima) e rode `up` de novo.
- **Porta 9000 ja em uso**: outro processo/instancia de Sonar ja esta
  rodando nessa porta. Pare o outro processo ou ajuste a porta no
  `docker-compose.yml` (ex.: `"9001:9000"`) e acesse por essa porta nova.
- **Perdi/corrompi o token local**: rode `./scripts/sonar-local.sh reset`
  (ou `.ps1 reset`) - apaga os dados do Sonar local e o token, e comeca do
  zero no proximo `up`.
- **Quero resetar so o token, sem apagar as analises**: apague manualmente
  `.sonar/local-token` e rode `up` de novo (ele gera um token novo sem
  mexer nos dados existentes do SonarQube).
