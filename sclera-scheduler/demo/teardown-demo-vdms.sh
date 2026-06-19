#!/usr/bin/env bash
#
# Demo teardown: remove the 3 demo VDMS (DELETE /api/vdms/{id}).
# Remove is a SOFT delete — the registry row stays with active=false and its
# per-VDMS Dapr jobs are torn down. Re-running seed-demo-vdms.sh reactivates them.
#
set -euo pipefail
BASE="${SCHEDULER_URL:-http://localhost:8098}"

for v in demo-nyc demo-london demo-mumbai; do
  printf 'DELETE %-12s -> ' "$v"
  curl -s -o /dev/null -w "%{http_code}\n" -X DELETE "$BASE/api/vdms/$v"
done
echo "Done (soft-removed; rows show as 'removed' on the dashboard)."
