#!/usr/bin/env bash
# Enhanced AB suite:
# - No early stop on 4xx/5xx (suite continúa siempre)
# - Registra métricas por escenario en CSV (summary.csv)
# - Exporta percentiles opcional (--percentiles) en TSV/CSV (-g/-e)
# - Muestra Non-2xx y Failed requests por escenario
# - (Opcional) Muestra desglose 2xx/3xx/4xx/5xx via "code sample" con -v 3 (CODESAMPLE=1)
#   * Para evitar archivos gigantes, el code sample usa N reducida (CODE_N, default 500)
#
# Requisitos: awk, sed, grep
#
# Variables de entorno configurables:
#   BASE_URL, OUT, ID, PERCENTILES=1, CODESAMPLE=1, CODE_N=500
#
# Uso típico:
#   chmod +x run-suite-plus.sh
#   ./run-suite-plus.sh
#
# Al finalizar:
#   - $OUT/summary.csv              (resumen por escenario)
#   - $OUT/raw/<escenario>.txt      (salida cruda de ab)
#   - $OUT/codes/<escenario>.codes  (solo si CODESAMPLE=1)
#   - $OUT/percentiles/*            (si PERCENTILES=1)
#
set -u  # no -e: no abortar por 4xx/5xx ni parseos sin match
BASE_URL=${BASE_URL:-"http://localhost:8080"}
OUT=${OUT:-"ab-results-$(date +%Y%m%d-%H%M%S)"}
ID=${ID:-"123e4567-e89b-12d3-a456-426614174000"}
PERCENTILES=${PERCENTILES:-0}
CODESAMPLE=${CODESAMPLE:-0}
CODE_N=${CODE_N:-500}

mkdir -p "$OUT/raw"
[ "$PERCENTILES" = "1" ] && mkdir -p "$OUT/percentiles"
[ "$CODESAMPLE" = "1" ] && mkdir -p "$OUT/codes"

SUMMARY="$OUT/summary.csv"
echo "scenario,concurrency,requests,failed_requests,non2xx,requests_per_sec,time_per_request_ms,p50_ms,p90_ms,p99_ms,transfer_kBps" > "$SUMMARY"

parse_and_append() {
  # $1 = scenario name, $2 = path to ab output file
  local name="$1"
  local file="$2"
  # Safe default values
  local conc=""
  local requests=""
  local failed="0"
  local non2xx="0"
  local rps=""
  local tpr=""
  local p50=""
  local p90=""
  local p99=""
  local kbps=""

  # Extract metrics
  conc=$(awk -F: '/Concurrency Level/{gsub(/ /,"",$2); print $2}' "$file" 2>/dev/null || echo "")
  requests=$(awk -F: '/Complete requests/{gsub(/ /,"",$2); print $2}' "$file" 2>/dev/null || echo "")
  failed=$(awk -F: '/Failed requests/{gsub(/ /,"",$2); print $2+0}' "$file" 2>/dev/null || echo "0")
  non2xx=$(awk -F: '/Non-2xx responses/{gsub(/ /,"",$2); print $2+0}' "$file" 2>/dev/null || echo "0")
  rps=$(awk -F: '/Requests per second/{match($0,/([0-9]+\.[0-9]+)/,m); if(m[1]!=""){print m[1]}}' "$file" 2>/dev/null || echo "")
  tpr=$(awk -F: '/Time per request:/{match($0,/([0-9]+\.[0-9]+)\s*\[ms\]\s*\(mean\)/,m); if(m[1]!=""){print m[1]} }' "$file" 2>/dev/null | head -n1)
  kbps=$(awk -F: '/Transfer rate/{match($0,/([0-9]+\.[0-9]+)\s*\[Kbytes\/sec\]/,m); if(m[1]!=""){print m[1]}}' "$file" 2>/dev/null || echo "")

  # Percentiles
  p50=$(awk '/ 50%/{print $2}' "$file" 2>/dev/null || echo "")
  p90=$(awk '/ 90%/{print $2}' "$file" 2>/dev/null || echo "")
  p99=$(awk '/ 99%/{print $2}' "$file" 2>/dev/null || echo "")

  echo "$name,$conc,$requests,$failed,$non2xx,$rps,$tpr,$p50,$p90,$p99,$kbps" >> "$SUMMARY"
}

code_sample_breakdown() {
  # $1 = scenario name, $2 = ab URL/args (string), $3 = base options, $4 = payload file (optional), $5 = content-type (optional)
  local name="$1"; shift
  local endpoint="$1"; shift
  local base_opts="$1"; shift
  local payload="${1:-}"; shift || true
  local ctype="${1:-}" || true

  # Build command
  local codes_file="$OUT/codes/${name}.codes"
  local args=()
  # reduce requests to CODE_N, keep same concurrency from base_opts if present (-c N)
  # We'll parse -c from base_opts; if not found, default 10
  local conc=$(echo "$base_opts" | awk '{for(i=1;i<=NF;i++){if($i=="-c"){print $(i+1); exit}}}')
  if [ -z "$conc" ]; then conc=10; fi

  args+=(-n "$CODE_N" -c "$conc" -v 3)
  # if keep-alive present in base_opts
  if echo "$base_opts" | grep -q -- " -k "; then args+=(-k); fi
  # payload
  if [ -n "$payload" ] && [ -f "$payload" ]; then
    args+=(-p "$payload")
    if [ -n "$ctype" ]; then
      args+=(-T "$ctype")
    fi
  fi

  # Run code-sample and capture verbose output (headers + status lines)
  ab "${args[@]}" "$endpoint" > "$codes_file" 2>&1 || true

  # Count status classes
  local c2xx=$(grep -Eo 'HTTP/[0-9.]+\s+2[0-9]{2}' "$codes_file" | wc -l | tr -d ' ')
  local c3xx=$(grep -Eo 'HTTP/[0-9.]+\s+3[0-9]{2}' "$codes_file" | wc -l | tr -d ' ')
  local c4xx=$(grep -Eo 'HTTP/[0-9.]+\s+4[0-9]{2}' "$codes_file" | wc -l | tr -d ' ')
  local c5xx=$(grep -Eo 'HTTP/[0-9.]+\s+5[0-9]{2}' "$codes_file" | wc -l | tr -d ' ')

  echo "  ↳ CodeSample ($CODE_N reqs) => 2xx=$c2xx, 3xx=$c3xx, 4xx=$c4xx, 5xx=$c5xx"
}

run() {
  # name + full ab args including URL
  local name="$1"; shift
  local out="$OUT/raw/${name}.txt"
  echo "==> $name"
  # Run AB (no exit on errors), capture to file
  ab "$@" > "$out" 2>&1 || true

  # Parse and append to summary
  parse_and_append "$name" "$out"

  # Optional: export percentiles data
  if [ "$PERCENTILES" = "1" ]; then
    # (Opcional) El usuario puede re-ejecutar endpoints con -g/-e para gráficos.
    :
  fi
}

# ------------------ Escenarios ------------------

# Warm-up
run "00_warmup_health"  -k -n 500  -c 20  "$BASE_URL/public/health"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "00_warmup_health" "$BASE_URL/public/health" " -k -c 20 "

run "01_warmup_list"    -k -n 1000 -c 50  "$BASE_URL/api/items"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "01_warmup_list" "$BASE_URL/api/items" " -k -c 50 "

# Lecturas
run "10_read_items"     -k -n 10000 -c 100 "$BASE_URL/api/items"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "10_read_items" "$BASE_URL/api/items" " -k -c 100 "

run "11_read_count"     -k -n 8000  -c 100 "$BASE_URL/api/items/count"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "11_read_count" "$BASE_URL/api/items/count" " -k -c 100 "

run "12_read_search_nm" -k -n 8000  -c 100 "$BASE_URL/api/items/search?name=laptop"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "12_read_search_nm" "$BASE_URL/api/items/search?name=laptop" " -k -c 100 "

run "13_read_search_rt" -k -n 8000  -c 100 "$BASE_URL/api/items/search/rating?minRating=4"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "13_read_search_rt" "$BASE_URL/api/items/search/rating?minRating=4" " -k -c 100 "

run "14_read_search_pr" -k -n 8000  -c 100 "$BASE_URL/api/items/search/price?minPrice=500&maxPrice=2000"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "14_read_search_pr" "$BASE_URL/api/items/search/price?minPrice=500&maxPrice=2000" " -k -c 100 "

# Escrituras
run "20_write_create"   -k -n 3000 -c 50  -p "$OUT/payloads/create.json" -T "application/json" "$BASE_URL/api/items"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "20_write_create" "$BASE_URL/api/items" " -k -c 50 " "$OUT/payloads/create.json" "application/json"

run "21_write_update"   -k -n 2000 -c 50  -p "$OUT/payloads/update.json" -T "application/json" "$BASE_URL/api/items/$ID"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "21_write_update" "$BASE_URL/api/items/$ID" " -k -c 50 " "$OUT/payloads/update.json" "application/json"

run "22_write_patch"    -k -n 2000 -c 50  -p "$OUT/payloads/patch.json"  -T "application/json" "$BASE_URL/api/items/$ID"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "22_write_patch" "$BASE_URL/api/items/$ID" " -k -c 50 " "$OUT/payloads/patch.json" "application/json"

run "23_write_addspecs" -k -n 2000 -c 50  -p "$OUT/payloads/add-specs.json" -T "application/json" "$BASE_URL/api/items/$ID/specifications"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "23_write_addspecs" "$BASE_URL/api/items/$ID/specifications" " -k -c 50 " "$OUT/payloads/add-specs.json" "application/json"

run "24_read_batch"     -k -n 6000 -c 80   -p "$OUT/payloads/batch.json" -T "application/json" "$BASE_URL/api/items/batch"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "24_read_batch" "$BASE_URL/api/items/batch" " -k -c 80 " "$OUT/payloads/batch.json" "application/json"

run "25_write_delete"   -k -n 1000 -c 25   "$BASE_URL/api/items/$ID"
[ "$CODESAMPLE" = "1" ] && code_sample_breakdown "25_write_delete" "$BASE_URL/api/items/$ID" " -k -c 25 "

echo "------------------------------------------------------------"
echo "DONE -> $OUT"
echo "Resumen: $SUMMARY"
[ "$CODESAMPLE" = "1" ] && echo "Desglose por códigos en: $OUT/codes/*.codes"
