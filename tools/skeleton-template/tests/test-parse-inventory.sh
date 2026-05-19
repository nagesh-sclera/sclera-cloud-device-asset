#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

ACTUAL=$(python3 parse-stub-inventory.py \
  --inventory tests/fixtures/sample-inventory.md \
  --target AP-C6 \
  --emit json)

EXPECTED='[{"fqn":"io.sclera.service.HistoryService","class":"HistoryService","methods":[{"name":"getDeviceIdByHistoryId","return":"String","params":[{"type":"String","name":"historyId"}],"default":"NULL_STRING"},{"name":"getHistoryCount","return":"Integer","params":[{"type":"String","name":"deviceId"}],"default":"ZERO"}]}]'

if [ "$ACTUAL" != "$EXPECTED" ]; then
  echo "MISMATCH"
  echo "expected: $EXPECTED"
  echo "actual:   $ACTUAL"
  exit 1
fi
echo "OK"
