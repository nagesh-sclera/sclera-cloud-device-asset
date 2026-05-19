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

CTRL="$OUT/sclera-audit-test/src/main/java/io/sclera/audit_test/controller/HistoryController.java"

grep -q "sclera-audit-test"       "$OUT/sclera-audit-test/pom.xml"
grep -q "18090"                   "$OUT/sclera-audit-test/Dockerfile"
grep -q "getDeviceIdByHistoryId"  "$CTRL"
grep -q "NULL_STRING"             "$CTRL"

# Improvement 2: imports instead of fully-qualified annotations
grep -q "import org.springframework.web.bind.annotation.GetMapping;"    "$CTRL"
grep -q "import org.springframework.web.bind.annotation.RequestMapping;" "$CTRL"
grep -q "import org.springframework.web.bind.annotation.RequestParam;"   "$CTRL"
grep -q "import org.springframework.web.bind.annotation.RestController;" "$CTRL"
grep -q "import io.sclera.audit_test.defaults.Defaults;"                 "$CTRL"
grep -q "import java.util.List;"  "$CTRL"
grep -q "import java.util.Set;"   "$CTRL"
grep -q "import java.util.Map;"   "$CTRL"

# Must NOT contain fully-qualified annotation forms
if grep -q "@org.springframework.web.bind.annotation.GetMapping" "$CTRL"; then
  echo "FAIL: controller still uses fully-qualified @GetMapping" >&2; exit 1
fi
if grep -q "@org.springframework.web.bind.annotation.RequestParam" "$CTRL"; then
  echo "FAIL: controller still uses fully-qualified @RequestParam" >&2; exit 1
fi

# Improvement 2: Defaults import, short return reference
grep -q "return Defaults\."       "$CTRL"
if grep -q "return io.sclera.audit_test.defaults.Defaults\." "$CTRL"; then
  echo "FAIL: controller still uses fully-qualified Defaults reference" >&2; exit 1
fi

# Improvement 1: non-allowlisted param type (HistoryDTO) must be substituted with String
# addHistory(HistoryDTO historyDTO) -> should become @RequestParam String historyDTO
grep -q "@RequestParam String historyDTO" "$CTRL"

# Improvement 1: parameterized return type with non-allowlisted inner type
# getAuditLogs returns List<UserActionLogDTO> -> should become List<String>
grep -q "public List<String> getAuditLogs" "$CTRL"

# Improvement 1: unknown bare return type (JSONObject) -> should become String
grep -q "public String getSyslogConfig" "$CTRL"

echo "OK"
