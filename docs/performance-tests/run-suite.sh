#!/usr/bin/env bash
set -euo pipefail
BASE_URL=${BASE_URL:-"http://localhost:8080"}
OUT=${OUT:-"ab-results-$(date +%Y%m%d-%H%M%S)"}
mkdir -p "$OUT/raw"

run() {
  local name="$1"; shift
  echo "==> $name"
  ab "$@" | tee "$OUT/raw/${name}.txt"
}

# Warm-up
run "00_warmup_health"  -k -n 500  -c 20  "$BASE_URL/public/health"
run "01_warmup_list"    -k -n 1000 -c 50  "$BASE_URL/api/items"

# Lecturas
run "10_read_items"     -k -n 10000 -c 100 "$BASE_URL/api/items"
run "11_read_count"     -k -n 8000  -c 100 "$BASE_URL/api/items/count"
run "12_read_search_nm" -k -n 8000  -c 100 "$BASE_URL/api/items/search?name=laptop"
run "13_read_search_rt" -k -n 8000  -c 100 "$BASE_URL/api/items/search/rating?minRating=4"
run "14_read_search_pr" -k -n 8000  -c 100 "$BASE_URL/api/items/search/price?minPrice=500&maxPrice=2000"

# Escrituras (ajustá ID a uno real si lo tenés; este es de ejemplo)
ID=${ID:-"123e4567-e89b-12d3-a456-426614174000"}
run "20_write_create"   -k -n 3000 -c 50  -p "$OUT/payloads/create.json" -T "application/json" "$BASE_URL/api/items"
run "21_write_update"   -k -n 2000 -c 50  -p "$OUT/payloads/update.json" -T "application/json" "$BASE_URL/api/items/$ID"
run "22_write_patch"    -k -n 2000 -c 50  -p "$OUT/payloads/patch.json"  -T "application/json" "$BASE_URL/api/items/$ID"
run "23_write_addspecs" -k -n 2000 -c 50  -p "$OUT/payloads/add-specs.json" -T "application/json" "$BASE_URL/api/items/$ID/specifications"
run "24_read_batch"     -k -n 6000 -c 80   -p "$OUT/payloads/batch.json" -T "application/json" "$BASE_URL/api/items/batch"
run "25_write_delete"   -k -n 1000 -c 25   "$BASE_URL/api/items/$ID"

echo "DONE -> $OUT"
