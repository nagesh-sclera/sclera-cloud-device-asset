#!/usr/bin/env bash
#
# Demo seed: register 3 VDMS in different timezones against the running scheduler,
# via the new VDMS management API (POST /api/vdms). Because jobs.yaml's
# `vdmsSystemHealth` is scope: PER_VDMS, each VDMS immediately gets its own
# scheduled instance ("vdmsSystemHealth::<id>") firing in its own CRON_TZ.
#
# Run from git-bash / the Bash tool (NOT PowerShell — PS 5.1 mangles JSON quotes
# for native curl). Override the target with SCHEDULER_URL if needed.
#
#   ./seed-demo-vdms.sh
#
set -euo pipefail
BASE="${SCHEDULER_URL:-http://localhost:8098}"

add() {  # $1 = vdmsId, $2 = IANA timezone
  printf 'POST %-12s %-18s -> ' "$1" "$2"
  curl -s -o /dev/null -w "%{http_code}\n" -X POST "$BASE/api/vdms" \
    -H 'Content-Type: application/json' \
    -d "{\"vdmsId\":\"$1\",\"timezone\":\"$2\"}"
}

echo "Seeding demo VDMS at $BASE ..."
add demo-nyc    America/New_York
add demo-london Europe/London
add demo-mumbai Asia/Kolkata

echo
echo "Registry now (GET /api/vdms):"
curl -s "$BASE/api/vdms" | sed 's/},{/}\n{/g'
echo
echo "Open the dashboard:  $BASE/scheduler.html  → expand 'vdmsSystemHealth' to see one"
echo "per-VDMS instance per demo site, each scheduled in its own timezone."
