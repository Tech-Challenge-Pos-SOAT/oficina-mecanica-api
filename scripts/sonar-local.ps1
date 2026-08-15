<#
.SYNOPSIS
  Sobe o SonarQube local (Docker Compose, perfil "sonar"), faz o bootstrap
  (troca de senha do admin + geracao de token) na primeira vez, e dispara a
  analise do projeto contra essa instancia local.

.DESCRIPTION
  Equivalente PowerShell nativo de scripts/sonar-local.sh, para quem usa
  Windows sem WSL/Git Bash (Docker Desktop ou Podman Desktop).
  Documentacao completa: docs/sonarqube-local.md

.PARAMETER Command
  up | analyze | down | reset | all (padrao: all = up + analyze)

.EXAMPLE
  ./scripts/sonar-local.ps1 up
  ./scripts/sonar-local.ps1 analyze
  ./scripts/sonar-local.ps1
#>

param(
    [Parameter(Position = 0)]
    [ValidateSet("up", "analyze", "down", "reset", "all")]
    [string]$Command = "all"
)

$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot

$SonarUrl = "http://localhost:9000"
$SonarDefaultUser = "admin"
$SonarDefaultPass = "admin"
$SonarNewPass = if ($env:SONAR_LOCAL_ADMIN_PASSWORD) { $env:SONAR_LOCAL_ADMIN_PASSWORD } else { "oficina-mecanica-local" }
$TokenDir = ".sonar"
$TokenFile = Join-Path $TokenDir "local-token"
$TokenName = "local-$($env:COMPUTERNAME -replace '[^a-zA-Z0-9]', '-')"

function Log($msg)     { Write-Host "==> $msg" -ForegroundColor Cyan }
function LogOk($msg)   { Write-Host "OK   $msg" -ForegroundColor Green }
function LogWarn($msg) { Write-Host "...  $msg" -ForegroundColor Yellow }
function LogErr($msg)  { Write-Host "ERRO $msg" -ForegroundColor Red }

# ---------------------------------------------------------------------------
# Detecta o comando de compose disponivel (Docker Desktop ou Podman Desktop -
# ambos comuns em maquinas Windows do time).
# ---------------------------------------------------------------------------
function Get-ComposeCommand {
    if (Get-Command docker -ErrorAction SilentlyContinue) {
        try {
            docker compose version | Out-Null
            return @("docker", "compose")
        } catch { }
    }
    if (Get-Command podman -ErrorAction SilentlyContinue) {
        try {
            podman compose version | Out-Null
            return @("podman", "compose")
        } catch { }
    }
    if (Get-Command docker-compose -ErrorAction SilentlyContinue) {
        return @("docker-compose")
    }
    if (Get-Command podman-compose -ErrorAction SilentlyContinue) {
        return @("podman-compose")
    }
    LogErr "nenhum comando de compose encontrado (docker compose / podman compose / docker-compose / podman-compose)"
    LogErr "instale o Docker Desktop ou o Podman Desktop e tente de novo."
    exit 1
}

function Invoke-Compose {
    param([string[]]$Args)
    $exe = $script:ComposeCmd[0]
    $baseArgs = $script:ComposeCmd[1..($script:ComposeCmd.Length - 1)]
    & $exe @baseArgs @Args
}

$script:ComposeCmd = Get-ComposeCommand
Log "Runtime de container detectado: $($script:ComposeCmd -join ' ')"

function Wait-ForSonar {
    Log "Aguardando o SonarQube ficar UP em $SonarUrl (pode levar 1-2 min no primeiro start)..."
    for ($i = 0; $i -lt 60; $i++) {
        try {
            $resp = Invoke-RestMethod -Uri "$SonarUrl/api/system/status" -TimeoutSec 5
            if ($resp.status -eq "UP") {
                LogOk "SonarQube UP"
                return
            }
        } catch { }
        Start-Sleep -Seconds 5
    }
    LogErr "SonarQube nao ficou UP a tempo."
    LogErr "Rode '$($script:ComposeCmd -join ' ') --profile sonar logs sonarqube' para investigar."
    LogErr "Causa comum: pouca memoria alocada ao Docker/Podman - ver docs/sonarqube-local.md."
    exit 1
}

function New-SonarToken {
    New-Item -ItemType Directory -Force -Path $TokenDir | Out-Null

    if ((Test-Path $TokenFile) -and (Get-Item $TokenFile).Length -gt 0) {
        LogOk "Token local ja existe em $TokenFile (bootstrap pulado)"
        return
    }

    Log "Primeiro uso: trocando a senha padrao do admin e gerando um token de analise..."

    $defaultCreds = [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${SonarDefaultUser}:${SonarDefaultPass}"))
    try {
        Invoke-RestMethod -Uri "$SonarUrl/api/users/change_password" -Method Post `
            -Headers @{ Authorization = "Basic $defaultCreds" } `
            -Body @{ login = $SonarDefaultUser; previousPassword = $SonarDefaultPass; password = $SonarNewPass } | Out-Null
        LogOk "Senha do admin trocada"
    } catch {
        LogWarn "Nao troquei a senha agora (provavelmente ja foi trocada em um start anterior). Seguindo com a senha configurada."
    }

    $newCreds = [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${SonarDefaultUser}:${SonarNewPass}"))
    try {
        $tokenResp = Invoke-RestMethod -Uri "$SonarUrl/api/user_tokens/generate" -Method Post `
            -Headers @{ Authorization = "Basic $newCreds" } `
            -Body @{ name = $TokenName }
    } catch {
        LogErr "Nao consegui gerar o token automaticamente: $_"
        LogErr "Gere manualmente em $SonarUrl > My Account > Security e salve o valor em $TokenFile"
        exit 1
    }

    if (-not $tokenResp.token) {
        LogErr "Resposta sem token: $($tokenResp | ConvertTo-Json -Compress)"
        exit 1
    }

    Set-Content -Path $TokenFile -Value $tokenResp.token -NoNewline
    LogOk "Token gerado e salvo em $TokenFile (arquivo pessoal, ja esta no .gitignore)"
}

function Invoke-Up {
    Log "Subindo SonarQube local (--profile sonar up -d)..."
    Invoke-Compose @("--profile", "sonar", "up", "-d", "sonarqube-db", "sonarqube")
    Wait-ForSonar
    New-SonarToken
    Write-Host ""
    LogOk "Pronto. Acesse $SonarUrl"
    LogOk "Login: admin / senha: '$SonarNewPass' (ou a que voce configurou em SONAR_LOCAL_ADMIN_PASSWORD)"
}

function Invoke-Analyze {
    if (-not (Test-Path $TokenFile) -or (Get-Item $TokenFile).Length -eq 0) {
        LogErr "Token nao encontrado em $TokenFile. Rode '.\scripts\sonar-local.ps1 up' primeiro."
        exit 1
    }
    $token = Get-Content -Path $TokenFile -Raw
    Log "Rodando a analise (mvn clean verify + sonar-maven-plugin) contra $SonarUrl..."
    mvn -B clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar `
        "-Dsonar.host.url=$SonarUrl" `
        "-Dsonar.token=$token" `
        "-Dsonar.projectKey=oficina-mecanica-api-local"
    LogOk "Analise concluida. Relatorio: $SonarUrl/dashboard?id=oficina-mecanica-api-local"
}

function Invoke-Down {
    Log "Parando os containers do Sonar local (dados preservados nos volumes)..."
    Invoke-Compose @("--profile", "sonar", "stop", "sonarqube", "sonarqube-db")
    LogOk "Parado. '.\scripts\sonar-local.ps1 up' religa depois sem precisar bootstrap de novo."
}

function Invoke-Reset {
    LogWarn "Isso apaga TODOS os dados/analises do Sonar local (volumes + token salvo)."
    $confirm = Read-Host "Confirma? (digite 'sim' para continuar)"
    if ($confirm -ne "sim") {
        Log "Cancelado."
        return
    }
    Invoke-Compose @("--profile", "sonar", "down", "-v")
    Remove-Item -Path $TokenFile -ErrorAction SilentlyContinue
    LogOk "Resetado. Rode '.\scripts\sonar-local.ps1 up' para comecar do zero."
}

switch ($Command) {
    "up"      { Invoke-Up }
    "analyze" { Invoke-Analyze }
    "down"    { Invoke-Down }
    "reset"   { Invoke-Reset }
    "all"     { Invoke-Up; Invoke-Analyze }
}
