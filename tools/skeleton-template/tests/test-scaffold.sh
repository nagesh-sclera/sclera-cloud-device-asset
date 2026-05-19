#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

OUT=$(mktemp -d)
trap "rm -rf $OUT" EXIT

bash scaffold-skeleton.sh \
  --servicekey audit-test \
  --port 18090 \
  --target AP-C6 \
  --inventory tests/fixtures/sample-inventory.md \
  --out "$OUT/sclera-audit-test"

test -f "$OUT/sclera-audit-test/pom.xml"
test -f "$OUT/sclera-audit-test/Dockerfile"
test -f "$OUT/sclera-audit-test/src/main/java/io/sclera/audit_test/Application.java"
test -f "$OUT/sclera-audit-test/src/main/java/io/sclera/audit_test/defaults/Defaults.java"
test -f "$OUT/sclera-audit-test/src/main/java/io/sclera/audit_test/controller/HistoryController.java"
test -f "$OUT/sclera-audit-test/src/main/resources/application.yml"

grep -q "sclera-audit-test"     "$OUT/sclera-audit-test/pom.xml"
grep -q "18090"                 "$OUT/sclera-audit-test/Dockerfile"
grep -q "getDeviceIdByHistoryId" "$OUT/sclera-audit-test/src/main/java/io/sclera/audit_test/controller/HistoryController.java"
grep -q "NULL_STRING"           "$OUT/sclera-audit-test/src/main/java/io/sclera/audit_test/controller/HistoryController.java"

echo "OK"
