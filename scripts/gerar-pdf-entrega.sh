#!/usr/bin/env bash
# Gera docs/entrega/Tech-Challenge-Fase2-Oficina-Mecanica.pdf a partir de
# docs/entrega/template.html, usando Chrome headless (sem dependencia nova).
#
# Uso: scripts/gerar-pdf-entrega.sh https://youtu.be/SEU_VIDEO
# Sem argumento, mantem o placeholder VIDEO_URL_PLACEHOLDER no PDF.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TEMPLATE="$ROOT_DIR/docs/entrega/template.html"
OUT_HTML="$ROOT_DIR/docs/entrega/.render.html"
OUT_PDF="$ROOT_DIR/docs/entrega/Tech-Challenge-Fase2-Oficina-Mecanica.pdf"
VIDEO_URL="${1:-VIDEO_URL_PLACEHOLDER}"

CHROME_BIN="${CHROME_BIN:-}"
if [ -z "$CHROME_BIN" ]; then
  for candidate in \
    "$HOME/.cache/puppeteer/chrome"/*/chrome-*/Google\ Chrome\ for\ Testing.app/Contents/MacOS/Google\ Chrome\ for\ Testing \
    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
    "$(command -v google-chrome 2>/dev/null || true)" \
    "$(command -v chromium 2>/dev/null || true)"; do
    if [ -n "$candidate" ] && [ -x "$candidate" ]; then
      CHROME_BIN="$candidate"
      break
    fi
  done
fi

if [ -z "$CHROME_BIN" ]; then
  echo "Chrome nao encontrado. Instale com: npx puppeteer browsers install chrome" >&2
  exit 1
fi

sed \
  -e "s#ARQUITETURA_IMG_SRC#$ROOT_DIR/docs/img/arquitetura.png#" \
  -e "s#VIDEO_URL_PLACEHOLDER#${VIDEO_URL}#" \
  "$TEMPLATE" > "$OUT_HTML"

"$CHROME_BIN" --headless --disable-gpu --no-sandbox \
  --print-to-pdf="$OUT_PDF" --no-pdf-header-footer \
  "file://$OUT_HTML"

rm -f "$OUT_HTML"
echo "PDF gerado em $OUT_PDF"
