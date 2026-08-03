#!/usr/bin/env python3
"""
Converte o relatorio JSON do OWASP Dependency-Check para o formato generico
de importacao de issues externas do SonarQube (sonar.externalIssuesReportPaths).

Por que assim e nao um plugin do SonarQube: o plugin da comunidade
(dependency-check-sonar-plugin) esta sem release desde ago/2024 e tem bugs
conhecidos em versoes recentes do SonarQube - preferimos o mecanismo nativo
e sempre atualizado do proprio SonarQube em vez de depender de um plugin
sem manutencao.

Doc do formato: https://docs.sonarsource.com/sonarqube-server/latest/analyzing-source-code/importing-external-issues/generic-issue-import-format/

Uso: python3 dependency_check_to_sonar.py <entrada.json> <saida.json>
"""
import json
import sys

SEVERITY_MAP = {
    "CRITICAL": "BLOCKER",
    "HIGH": "HIGH",
    "MEDIUM": "MEDIUM",
    "LOW": "LOW",
}


def convert(input_path, output_path):
    with open(input_path, "r", encoding="utf-8") as f:
        report = json.load(f)

    rules = {}
    issues = []

    for dep in report.get("dependencies", []) or []:
        file_name = dep.get("fileName") or dep.get("filePath") or "dependencia-desconhecida"
        for vuln in dep.get("vulnerabilities", []) or []:
            cve = vuln.get("name", "SEM-ID")
            severity_raw = (vuln.get("severity") or "MEDIUM").upper()
            severity = SEVERITY_MAP.get(severity_raw, "MEDIUM")
            rule_id = f"dependency-check:{cve}"

            if rule_id not in rules:
                desc = (vuln.get("description") or "").strip()
                if not desc:
                    desc = f"Vulnerabilidade {cve} detectada pelo OWASP Dependency-Check."
                rules[rule_id] = {
                    "id": rule_id,
                    "name": f"{cve} (OWASP Dependency-Check)",
                    "description": desc[:1000],
                    "engineId": "dependency-check",
                    "cleanCodeAttribute": "TRUSTWORTHY",
                    "impacts": [
                        {"softwareQuality": "SECURITY", "severity": severity}
                    ],
                }

            issues.append({
                "ruleId": rule_id,
                "effortMinutes": 30,
                "primaryLocation": {
                    "message": f"{cve}: dependencia vulneravel '{file_name}'",
                    "filePath": "pom.xml",
                },
            })

    output = {"rules": list(rules.values()), "issues": issues}

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2, ensure_ascii=False)

    print(f"Convertido: {len(rules)} regra(s) unica(s), {len(issues)} issue(s) -> {output_path}")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Uso: python3 dependency_check_to_sonar.py <entrada.json> <saida.json>")
        sys.exit(1)
    convert(sys.argv[1], sys.argv[2])
