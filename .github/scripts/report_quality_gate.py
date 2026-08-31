#!/usr/bin/env python3
"""
Consulta o status da Quality Gate do SonarQube (via API) e imprime um resumo
em Markdown no stdout - pensado para ser redirecionado para o
$GITHUB_STEP_SUMMARY do job, para ficar visivel direto na aba "Summary" da
execucao do Actions, sem precisar abrir o dashboard do SonarQube.

Por que isso existe: a partir de agora o step de scan (sonar-maven-plugin)
roda SEM "sonar.qualitygate.wait=true" - ou seja, o job do GitHub Actions
nao falha mais so porque a Quality Gate falhou (decisao do time: nao
bloquear o pipeline por causa de code smells/issues menores, so registrar).
Sem esse script, uma Quality Gate "Failed" ficaria escondida - so apareceria
para quem entrasse manualmente no SonarQube. Este script existe para que o
resultado real (passou/falhou, e por qual condicao) continue visivel em todo
push/PR, mesmo o job nao falhando mais.

Uso:
  python3 report_quality_gate.py \
    --host "$SONAR_HOST_URL" \
    --token "$SONAR_TOKEN" \
    --project-key oficina-mecanica-api \
    >> "$GITHUB_STEP_SUMMARY"

Nunca falha o processo (sys.exit sempre 0) - isso e so um relatorio
informativo, nao deve derrubar o job por causa de timeout/erro de rede na
propria consulta.
"""
import argparse
import json
import sys
import urllib.error
import urllib.request
from base64 import b64encode


def normalize_host(host):
    """Remove espacos/quebras de linha acidentais (ex: secret do GitHub
    salvo com um '\n' no final) e garante que a URL tenha esquema
    (http/https), senao o link vira relativo no markdown do Actions e o
    GitHub tenta resolver contra a propria pagina do run (gerando 404)."""
    host = host.strip().rstrip("/")
    if not host.startswith(("http://", "https://")):
        host = f"http://{host}"
    return host


def fetch_quality_gate_status(host, token, project_key):
    url = f"{host}/api/qualitygates/project_status?projectKey={project_key}"
    auth = b64encode(f"{token}:".encode("utf-8")).decode("ascii")
    req = urllib.request.Request(url, headers={"Authorization": f"Basic {auth}"})
    with urllib.request.urlopen(req, timeout=20) as resp:
        return json.load(resp)


def build_summary(data, host, project_key):
    status = data["projectStatus"]["status"]  # OK | ERROR | WARN | NONE
    conditions = data["projectStatus"].get("conditions", [])

    icon = "✅" if status == "OK" else "⚠️"
    lines = [f"## {icon} SonarQube Quality Gate: {status}", ""]
    lines.append(
        "_Este job nao bloqueia mais o pipeline por causa da Quality Gate "
        "(decisao do time) - o status abaixo e so informativo._"
    )
    lines.append("")

    failed = [c for c in conditions if c.get("status") == "ERROR"]
    if failed:
        lines.append("| Condicao | Valor atual | Limite (New Code) | Status |")
        lines.append("|---|---|---|---|")
        for c in conditions:
            metric = c.get("metricKey", "?")
            actual = c.get("actualValue", "?")
            threshold = c.get("errorThreshold", "-")
            cond_status = "❌ Failed" if c.get("status") == "ERROR" else "✅ OK"
            lines.append(f"| {metric} | {actual} | {threshold} | {cond_status} |")
    else:
        lines.append("Todas as condicoes da Quality Gate passaram.")

    lines.append("")
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--host", required=True)
    parser.add_argument("--token", required=True)
    parser.add_argument("--project-key", required=True)
    args = parser.parse_args()

    host = normalize_host(args.host)

    try:
        data = fetch_quality_gate_status(host, args.token, args.project_key)
        print(build_summary(data, host, args.project_key))
    except (urllib.error.URLError, KeyError, json.JSONDecodeError, TimeoutError) as e:
        # Nao falha o job por causa disso - so avisa que o resumo nao pode
        # ser gerado desta vez (rede fora, instancia reiniciando, etc.).
        print(
            "## ⚠️ SonarQube Quality Gate\n\n"
            f"Nao foi possivel consultar o status via API neste run ({e}).\n"
        )
        print(f"Aviso: falha ao consultar Quality Gate API: {e}", file=sys.stderr)

    sys.exit(0)


if __name__ == "__main__":
    main()
