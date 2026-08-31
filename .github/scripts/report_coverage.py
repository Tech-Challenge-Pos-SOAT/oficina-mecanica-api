#!/usr/bin/env python3
"""
Le o relatorio XML do JaCoCo (target/site/jacoco/jacoco.xml, gerado pelo
"mvn test") e imprime um resumo em Markdown no stdout - pensado para ser
redirecionado para o $GITHUB_STEP_SUMMARY do job "3. Test", pra cobertura
ficar visivel direto na aba Summary do run, sem precisar baixar o artifact
"jacoco-report" e abrir o HTML localmente.

Uso:
  python3 report_coverage.py --jacoco-xml target/site/jacoco/jacoco.xml \
      --min-coverage 80 >> "$GITHUB_STEP_SUMMARY"

Nunca falha o processo (sys.exit sempre 0) - e so um relatorio informativo;
quem decide se o build falha por cobertura baixa e um jacoco:check (se/quando
configurado), nao este script.
"""
import argparse
import sys
import xml.etree.ElementTree as ET

COUNTER_LABELS = {
    "INSTRUCTION": "Instrucoes",
    "LINE": "Linhas",
    "BRANCH": "Branches",
}


def parse_counters(xml_path):
    tree = ET.parse(xml_path)
    root = tree.getroot()
    counters = {}
    # Os <counter> filhos diretos de <report> sao o total agregado do
    # projeto inteiro - os <counter> dentro de <package>/<class> sao
    # parciais (por pacote/classe), nao usamos esses aqui.
    for counter in root.findall("counter"):
        ctype = counter.get("type")
        missed = int(counter.get("missed"))
        covered = int(counter.get("covered"))
        counters[ctype] = (missed, covered)
    return counters


def pct(missed, covered):
    total = missed + covered
    if total == 0:
        return 100.0
    return round(100 * covered / total, 1)


def build_summary(counters, min_coverage):
    line_missed, line_covered = counters["LINE"]
    line_pct = pct(line_missed, line_covered)
    ok = line_pct >= min_coverage
    icon = "✅" if ok else "⚠️"
    status = "OK" if ok else "ABAIXO DO MINIMO"

    lines = [
        f"## {icon} Cobertura de testes (JaCoCo): {line_pct}% de linhas ({status})",
        "",
        f"Minimo exigido pelo edital: {min_coverage}%.",
        "",
        "| Metrica | Cobertos | Total | % |",
        "|---|---|---|---|",
    ]
    for ctype in ("INSTRUCTION", "LINE", "BRANCH"):
        if ctype not in counters:
            continue
        missed, covered = counters[ctype]
        total = missed + covered
        lines.append(f"| {COUNTER_LABELS[ctype]} | {covered} | {total} | {pct(missed, covered)}% |")
    lines.append("")
    lines.append(
        "Relatorio completo (por pacote/classe) no artifact `jacoco-report` "
        "desta run, ou local em `target/site/jacoco/index.html` apos `mvn test`."
    )
    lines.append("")
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--jacoco-xml", required=True)
    parser.add_argument("--min-coverage", type=float, default=80.0)
    args = parser.parse_args()

    try:
        counters = parse_counters(args.jacoco_xml)
        print(build_summary(counters, args.min_coverage))
    except (OSError, ET.ParseError, KeyError) as e:
        # Nao falha o job por causa disso - so avisa que o resumo nao pode
        # ser gerado desta vez (testes falharam antes de gerar o XML, etc.).
        print(
            "## ⚠️ Cobertura de testes (JaCoCo)\n\n"
            f"Nao foi possivel ler o relatorio XML neste run ({e}).\n"
        )
        print(f"Aviso: falha ao ler {args.jacoco_xml}: {e}", file=sys.stderr)

    sys.exit(0)


if __name__ == "__main__":
    main()
