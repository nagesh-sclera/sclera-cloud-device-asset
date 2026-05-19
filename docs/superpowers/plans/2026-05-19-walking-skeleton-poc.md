# Walking-skeleton PoC Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Scaffold all 7 walking-skeleton microservices (Phase A) with full Dapr surface and hardcoded defaults, then migrate the 3 sclera-audit stub classes in `sclera-cloud-device-asset` to thin Dapr clients (Phase B Wave 1). PoC scope per `docs/superpowers/specs/2026-05-19-walking-skeleton-extraction-design.md`.

**Architecture:** Template-driven scaffolding (`tools/skeleton-template/` + `scaffold-skeleton.sh`) reads `migration-notes/stub-inventory.md` and emits per-AP-Cx skeleton modules. Each skeleton boots, registers Dapr endpoints, hosts pub/sub subscriptions, returns hardcoded null/default values matching today's stub behavior. Phase B replaces the in-process stub classes in `sclera-cloud-device-asset` with thin `<Stub>Client` classes wrapping `DaprClient.invokeMethod(...)`.

**Tech Stack:** Java 17, Spring Boot 2.6.5, Dapr 1.12 (sidecar pattern), Dapr Java SDK 1.12, Redis 7 (pubsub), Docker Compose, JUnit 5, MockMvc, WireMock, Maven.

**Prerequisites:** Phases 1.1–1.6 of the Dapr expansion spec (`2026-05-19-dapr-expansion-and-extraction-design.md`) should be complete or running in parallel. Specifically Phase 1.3 (Dapr Java SDK adoption) — clients in this plan import the SDK. If 1.3 is not yet done, complete the SDK setup as part of Task 17 (Dapr client base).

**Source-of-truth files referenced by this plan:**
- `sclera-cloud-device-asset/migration-notes/stub-inventory.md` (input to the scaffold script)
- `docs/superpowers/specs/2026-05-19-walking-skeleton-extraction-design.md` (the spec)

---

## File structure

### Created
```
tools/skeleton-template/
  pom.xml.template
  Dockerfile.template
  CLAUDE.md.template
  src/main/java/io/sclera/{{servicekey}}/Application.java.template
  src/main/java/io/sclera/{{servicekey}}/defaults/Defaults.java.template
  src/main/resources/application.yml.template
  src/main/resources/topics.yaml.template
  scaffold-skeleton.sh                          # bash entrypoint
  parse-stub-inventory.py                       # reads stub-inventory.md
  tests/test-scaffold.sh                        # asserts generator output

sclera-audit/                                   # PoC + Wave 1 target
  pom.xml
  Dockerfile
  CLAUDE.md
  src/main/java/io/sclera/audit/Application.java
  src/main/java/io/sclera/audit/controller/HistoryController.java
  src/main/java/io/sclera/audit/controller/ArchivedRecordController.java
  src/main/java/io/sclera/audit/controller/SyslogController.java
  src/main/java/io/sclera/audit/subscriber/DeviceAuditSubscriber.java
  src/main/java/io/sclera/audit/defaults/Defaults.java
  src/main/resources/application.yml
  src/main/resources/topics.yaml
  src/test/java/io/sclera/audit/SkeletonContractTest.java

sclera-identity/                                # same shape, parameters in Task 6
sclera-alerts/                                  # same shape, parameters in Task 7
sclera-inventory/                               # same shape, parameters in Task 8
sclera-workorders/                              # same shape, parameters in Task 9
sclera-inspection/                              # same shape, parameters in Task 10
sclera-integrations/                            # same shape, parameters in Task 11

sclera-cloud-device-asset/src/main/java/io/sclera/client/
  DaprClientConfig.java                         # @Bean DaprClient
  HistoryClient.java
  ArchivedRecordClient.java
  SyslogClient.java

dapr/APP_IDS.md                                 # app-id registry
```

### Modified
```
docker-compose.yml                              # 14 new entries (7 apps + 7 sidecars)
dapr/resiliency.yaml                            # add 7 new app-ids to scopes/targets
sclera-cloud-device-asset/pom.xml               # add dapr-sdk dependency if not already
```

### Deleted
```
sclera-cloud-device-asset/src/main/java/io/sclera/service/HistoryService.java
sclera-cloud-device-asset/src/main/java/io/sclera/service/ArchivedRecordService.java
sclera-cloud-device-asset/src/main/java/io/sclera/service/SyslogService.java
```

---

## Phase A — Skeleton scaffolding

### Task 1: Initial scaffold template directory

**Files:**
- Create: `tools/skeleton-template/pom.xml.template`
- Create: `tools/skeleton-template/Dockerfile.template`
- Create: `tools/skeleton-template/CLAUDE.md.template`
- Create: `tools/skeleton-template/src/main/java/io/sclera/{{servicekey}}/Application.java.template`
- Create: `tools/skeleton-template/src/main/java/io/sclera/{{servicekey}}/defaults/Defaults.java.template`
- Create: `tools/skeleton-template/src/main/resources/application.yml.template`
- Create: `tools/skeleton-template/src/main/resources/topics.yaml.template`

- [ ] **Step 1: Create `tools/skeleton-template/pom.xml.template`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.6.5</version>
    <relativePath/>
  </parent>
  <groupId>io.sclera</groupId>
  <artifactId>sclera-{{servicekey}}</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <properties>
    <java.version>17</java.version>
    <dapr.sdk.version>1.12.0</dapr.sdk.version>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
      <groupId>io.dapr</groupId>
      <artifactId>dapr-sdk</artifactId>
      <version>${dapr.sdk.version}</version>
    </dependency>
    <dependency>
      <groupId>io.dapr</groupId>
      <artifactId>dapr-sdk-springboot</artifactId>
      <version>${dapr.sdk.version}</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 2: Create `tools/skeleton-template/Dockerfile.template`**

```dockerfile
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/sclera-{{servicekey}}-0.0.1-SNAPSHOT.jar app.jar
EXPOSE {{port}}
ENV JAVA_OPTS="-Xmx128m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

- [ ] **Step 3: Create `tools/skeleton-template/src/main/java/io/sclera/{{servicekey}}/Application.java.template`**

```java
package io.sclera.{{servicekey}};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

- [ ] **Step 4: Create `tools/skeleton-template/src/main/java/io/sclera/{{servicekey}}/defaults/Defaults.java.template`**

```java
package io.sclera.{{servicekey}}.defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Defaults {
    private Defaults() {}

    public static final String NULL_STRING = null;
    public static final Integer ZERO = 0;
    public static final Long ZERO_LONG = 0L;
    public static final Boolean FALSE = Boolean.FALSE;

    public static <T> List<T> emptyList() { return List.of(); }
    public static <T> Set<T> emptySet() { return Set.of(); }
    public static <K, V> Map<K, V> emptyMap() { return Map.of(); }
}
```

- [ ] **Step 5: Create `tools/skeleton-template/src/main/resources/application.yml.template`**

```yaml
server:
  port: {{port}}

spring:
  application:
    name: sclera-{{servicekey}}

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always

dapr:
  http-port: ${DAPR_HTTP_PORT:3500}
  grpc-port: ${DAPR_GRPC_PORT:50001}

logging:
  level:
    io.sclera.{{servicekey}}: INFO
    io.dapr: INFO
```

- [ ] **Step 6: Create `tools/skeleton-template/src/main/resources/topics.yaml.template`**

```yaml
# Pub/sub contract for sclera-{{servicekey}}
# Filled in per service by scaffold-skeleton.sh
publishes: []
subscribes: []
```

- [ ] **Step 7: Create `tools/skeleton-template/CLAUDE.md.template`**

```markdown
# CLAUDE.md — sclera-{{servicekey}}

## Project identity
Walking-skeleton microservice scaffolded by `tools/skeleton-template/scaffold-skeleton.sh`. Spring Boot 2.6.5, Java 17. Hosts the future real `sclera-{{servicekey}}` service's Dapr surface.

## Rules
- No database, no Flyway, no JPA. All endpoints return hardcoded defaults from `defaults/Defaults.java`.
- Endpoints generated from `sclera-cloud-device-asset/migration-notes/stub-inventory.md` — do not edit by hand. Re-run the scaffold script to regenerate.
- Pub/sub subscriptions are no-op handlers that log the event and return 200.

## See also
- Spec: `docs/superpowers/specs/2026-05-19-walking-skeleton-extraction-design.md`
- Plan: `docs/superpowers/plans/2026-05-19-walking-skeleton-poc.md`
```

- [ ] **Step 8: Commit**

```bash
git add tools/skeleton-template/
git commit -m "feat(scaffold): add skeleton-template templates for walking-skeleton modules"
```

---

### Task 2: Stub inventory parser

**Files:**
- Create: `tools/skeleton-template/parse-stub-inventory.py`
- Create: `tools/skeleton-template/tests/test-parse-inventory.sh`
- Test: `tools/skeleton-template/tests/fixtures/sample-inventory.md`

- [ ] **Step 1: Create the test fixture `tools/skeleton-template/tests/fixtures/sample-inventory.md`**

```markdown
## AP-C6 history/audit

### `io.sclera.service.HistoryService`
File: `src/main/java/io/sclera/service/HistoryService.java`
- `String getDeviceIdByHistoryId(String historyId)` — returns null
- `Integer getHistoryCount(String deviceId)` — returns 0
```

- [ ] **Step 2: Write the failing test `tools/skeleton-template/tests/test-parse-inventory.sh`**

```bash
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
```

Make it executable: `chmod +x tools/skeleton-template/tests/test-parse-inventory.sh`

- [ ] **Step 3: Run the test and verify it fails**

Run: `bash tools/skeleton-template/tests/test-parse-inventory.sh`
Expected: FAIL (parse-stub-inventory.py does not exist yet)

- [ ] **Step 4: Implement `tools/skeleton-template/parse-stub-inventory.py`**

```python
#!/usr/bin/env python3
"""Read migration-notes/stub-inventory.md, emit structured stub class data for a given target."""
import argparse, json, re, sys
from pathlib import Path

DEFAULT_MAP = {
    "null":                "NULL_STRING",
    "0":                   "ZERO",
    "Boolean.FALSE":       "FALSE",
    "false":               "FALSE",
    "empty list":          "emptyList()",
    "empty set":           "emptySet()",
    "empty map":           "emptyMap()",
}

CLASS_HEADER = re.compile(r"^### `(?P<fqn>[\w\.]+)`\s*$")
METHOD_LINE  = re.compile(
    r"^- `(?P<return>[\w<>,\s\?]+?)\s+(?P<name>\w+)\((?P<params>[^)]*)\)`\s*[—-]\s*(?P<default>.+)$"
)
SECTION      = re.compile(r"^## (?P<target>[\w\-]+)\b")

def parse(inventory_path, target):
    in_target = False
    out = []
    current = None
    with open(inventory_path, encoding="utf-8") as fh:
        for line in fh:
            line = line.rstrip()
            m = SECTION.match(line)
            if m:
                in_target = m.group("target").startswith(target)
                continue
            if not in_target:
                continue
            m = CLASS_HEADER.match(line)
            if m:
                if current:
                    out.append(current)
                fqn = m.group("fqn")
                current = {"fqn": fqn, "class": fqn.rsplit(".", 1)[1], "methods": []}
                continue
            m = METHOD_LINE.match(line)
            if m and current is not None:
                params = []
                if m.group("params").strip():
                    for raw in m.group("params").split(","):
                        ptype, pname = raw.strip().rsplit(" ", 1)
                        params.append({"type": ptype.strip(), "name": pname.strip()})
                default_phrase = m.group("default").strip().lower()
                default = next(
                    (v for k, v in DEFAULT_MAP.items() if k in default_phrase),
                    "NULL_STRING"
                )
                current["methods"].append({
                    "name":   m.group("name"),
                    "return": m.group("return").strip(),
                    "params": params,
                    "default": default,
                })
        if current:
            out.append(current)
    return out

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--inventory", required=True)
    ap.add_argument("--target",    required=True, help="AP-C2 / CP-2 / AP-C6 etc")
    ap.add_argument("--emit",      choices=["json"], default="json")
    args = ap.parse_args()
    data = parse(args.inventory, args.target)
    print(json.dumps(data, separators=(",", ":")))

if __name__ == "__main__":
    main()
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `bash tools/skeleton-template/tests/test-parse-inventory.sh`
Expected: `OK`

- [ ] **Step 6: Commit**

```bash
git add tools/skeleton-template/parse-stub-inventory.py tools/skeleton-template/tests/
git commit -m "feat(scaffold): parser for stub-inventory.md emits per-target JSON"
```

---

### Task 3: Scaffold script — generates a complete skeleton module

**Files:**
- Create: `tools/skeleton-template/scaffold-skeleton.sh`
- Create: `tools/skeleton-template/tests/test-scaffold.sh`
- Create: `tools/skeleton-template/tests/fixtures/expected-audit/` (a known-good output for the test target)

- [ ] **Step 1: Write the failing test `tools/skeleton-template/tests/test-scaffold.sh`**

```bash
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
```

Make executable: `chmod +x tools/skeleton-template/tests/test-scaffold.sh`

- [ ] **Step 2: Run the test, verify it fails**

Run: `bash tools/skeleton-template/tests/test-scaffold.sh`
Expected: FAIL (scaffold-skeleton.sh does not exist)

- [ ] **Step 3: Implement `tools/skeleton-template/scaffold-skeleton.sh`**

```bash
#!/usr/bin/env bash
# Scaffold a walking-skeleton microservice module.
set -euo pipefail

SERVICEKEY=""
PORT=""
TARGET=""
INVENTORY=""
OUT=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --servicekey) SERVICEKEY="$2"; shift 2;;
    --port)       PORT="$2";       shift 2;;
    --target)     TARGET="$2";     shift 2;;
    --inventory)  INVENTORY="$2";  shift 2;;
    --out)        OUT="$2";        shift 2;;
    *) echo "unknown flag: $1" >&2; exit 2;;
  esac
done

for v in SERVICEKEY PORT TARGET INVENTORY OUT; do
  [ -n "${!v}" ] || { echo "missing --${v,,}" >&2; exit 2; }
done

TEMPLATE_DIR="$(cd "$(dirname "$0")" && pwd)"
PKG="${SERVICEKEY//-/_}"

mkdir -p "$OUT/src/main/java/io/sclera/$PKG/controller"
mkdir -p "$OUT/src/main/java/io/sclera/$PKG/subscriber"
mkdir -p "$OUT/src/main/java/io/sclera/$PKG/defaults"
mkdir -p "$OUT/src/main/resources"
mkdir -p "$OUT/src/test/java/io/sclera/$PKG"

subst() {
  sed -e "s/{{servicekey}}/$SERVICEKEY/g" \
      -e "s/{{port}}/$PORT/g" \
      -e "s|{{servicekey}}|$PKG|g" \
      "$1"
}

# Substitute templates that use {{servicekey}} as PACKAGE need pkg substitution; we use two-pass:
sed -e "s/{{servicekey}}/$SERVICEKEY/g" -e "s/{{port}}/$PORT/g" \
    "$TEMPLATE_DIR/pom.xml.template"            > "$OUT/pom.xml"
sed -e "s/{{servicekey}}/$SERVICEKEY/g" -e "s/{{port}}/$PORT/g" \
    "$TEMPLATE_DIR/Dockerfile.template"         > "$OUT/Dockerfile"
sed -e "s/{{servicekey}}/$SERVICEKEY/g" \
    "$TEMPLATE_DIR/CLAUDE.md.template"          > "$OUT/CLAUDE.md"
sed -e "s/{{servicekey}}/$PKG/g" \
    "$TEMPLATE_DIR/src/main/java/io/sclera/{{servicekey}}/Application.java.template" \
    > "$OUT/src/main/java/io/sclera/$PKG/Application.java"
sed -e "s/{{servicekey}}/$PKG/g" \
    "$TEMPLATE_DIR/src/main/java/io/sclera/{{servicekey}}/defaults/Defaults.java.template" \
    > "$OUT/src/main/java/io/sclera/$PKG/defaults/Defaults.java"
sed -e "s/{{servicekey}}/$SERVICEKEY/g" -e "s/{{port}}/$PORT/g" \
    "$TEMPLATE_DIR/src/main/resources/application.yml.template" \
    > "$OUT/src/main/resources/application.yml"
cp "$TEMPLATE_DIR/src/main/resources/topics.yaml.template" \
    "$OUT/src/main/resources/topics.yaml"

# Generate controllers from inventory
python3 "$TEMPLATE_DIR/parse-stub-inventory.py" \
    --inventory "$INVENTORY" --target "$TARGET" --emit json |
python3 - "$OUT/src/main/java/io/sclera/$PKG/controller" "$PKG" <<'PY'
import json, sys, os, pathlib

out_dir, pkg = sys.argv[1], sys.argv[2]
data = json.load(sys.stdin)
for cls in data:
    name = cls["class"].replace("Service", "")
    controller = f"{name}Controller"
    path = f"/{name.lower()}"
    body_lines = []
    for m in cls["methods"]:
        params = ", ".join(f'@org.springframework.web.bind.annotation.RequestParam {p["type"]} {p["name"]}' for p in m["params"])
        body_lines.append(
            f'  @org.springframework.web.bind.annotation.GetMapping("/{m["name"]}")\n'
            f'  public {m["return"]} {m["name"]}({params}) {{\n'
            f'    return io.sclera.{pkg}.defaults.Defaults.{m["default"] if not m["default"].endswith("()") else m["default"]};\n'
            f'  }}\n'
        )
    src = (
        f'package io.sclera.{pkg}.controller;\n'
        f'\n'
        f'import org.springframework.web.bind.annotation.RestController;\n'
        f'import org.springframework.web.bind.annotation.RequestMapping;\n'
        f'\n'
        f'@RestController\n'
        f'@RequestMapping("{path}")\n'
        f'public class {controller} {{\n'
        + "\n".join(body_lines) +
        f'}}\n'
    )
    pathlib.Path(out_dir, f"{controller}.java").write_text(src, encoding="utf-8")
PY

echo "scaffolded sclera-$SERVICEKEY → $OUT"
```

- [ ] **Step 4: Make scaffold script executable and run the test**

```bash
chmod +x tools/skeleton-template/scaffold-skeleton.sh
bash tools/skeleton-template/tests/test-scaffold.sh
```

Expected: `OK`

- [ ] **Step 5: Commit**

```bash
git add tools/skeleton-template/scaffold-skeleton.sh tools/skeleton-template/tests/
git commit -m "feat(scaffold): scaffold-skeleton.sh generates skeleton modules from inventory"
```

---

### Task 4: Scaffold `sclera-audit`

**Files:**
- Create: `sclera-audit/` (entire module via scaffold script)
- Manual edit: `sclera-audit/src/main/java/io/sclera/audit/subscriber/DeviceAuditSubscriber.java`
- Test: `sclera-audit/src/test/java/io/sclera/audit/SkeletonContractTest.java`

- [ ] **Step 1: Run the scaffold script**

```bash
cd "C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice123/sclera-cloud-device-asset"
bash tools/skeleton-template/scaffold-skeleton.sh \
  --servicekey audit \
  --port 8090 \
  --target AP-C6 \
  --inventory sclera-cloud-device-asset/migration-notes/stub-inventory.md \
  --out sclera-audit
```

Expected: `scaffolded sclera-audit → sclera-audit`

- [ ] **Step 2: Verify generated structure**

```bash
ls sclera-audit/src/main/java/io/sclera/audit/controller/
```

Expected: `HistoryController.java`, `ArchivedRecordController.java`, `SyslogController.java`

- [ ] **Step 3: Add the device-audit subscriber (no-op)**

Create `sclera-audit/src/main/java/io/sclera/audit/subscriber/DeviceAuditSubscriber.java`:

```java
package io.sclera.audit.subscriber;

import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeviceAuditSubscriber {
    private static final Logger log = LoggerFactory.getLogger(DeviceAuditSubscriber.class);

    @Topic(name = "device.audit-recorded", pubsubName = "pubsub")
    @PostMapping("/internal/device-audit")
    public ResponseEntity<Void> onDeviceAudit(@RequestBody CloudEvent<Map<String, Object>> evt) {
        log.info("[skeleton] received {}: id={}", evt.getType(), evt.getId());
        return ResponseEntity.ok().build();
    }
}
```

- [ ] **Step 4: Write the SkeletonContractTest**

Create `sclera-audit/src/test/java/io/sclera/audit/SkeletonContractTest.java`:

```java
package io.sclera.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SkeletonContractTest {

    @Autowired MockMvc mvc;

    @Test
    void history_getDeviceIdByHistoryId_returnsEmptyBody() throws Exception {
        mvc.perform(get("/history/getDeviceIdByHistoryId").param("historyId", "h1"))
           .andExpect(status().isOk())
           .andExpect(content().string(""));   // null Java string → empty HTTP body
    }
}
```

- [ ] **Step 5: Build and run the test**

```bash
cd sclera-audit
./mvnw -B test
```

Expected: BUILD SUCCESS, 1 test passes.

If you do not yet have `mvnw` wrappers, copy them from `sclera-cloud-device-asset/mvnw*` and `.mvn/` into `sclera-audit/`.

- [ ] **Step 6: Update topics.yaml**

Edit `sclera-audit/src/main/resources/topics.yaml`:

```yaml
publishes: []
subscribes:
  - topic: device.audit-recorded
    pubsub: pubsub
    handler: /internal/device-audit
```

- [ ] **Step 7: Commit**

```bash
git add sclera-audit/
git commit -m "feat(skeleton): scaffold sclera-audit (AP-C6) with 3 controllers + audit subscriber"
```

---

### Task 5: Scaffold `sclera-identity`

**Files:**
- Create: `sclera-identity/` (entire module)
- Test: `sclera-identity/src/test/java/io/sclera/identity/SkeletonContractTest.java`

- [ ] **Step 1: Run scaffold script**

```bash
bash tools/skeleton-template/scaffold-skeleton.sh \
  --servicekey identity \
  --port 8091 \
  --target CP-2 \
  --inventory sclera-cloud-device-asset/migration-notes/stub-inventory.md \
  --out sclera-identity
```

- [ ] **Step 2: Verify generated controllers**

```bash
ls sclera-identity/src/main/java/io/sclera/identity/controller/
```

Expected: at minimum `UserController.java`, `CustomerOrganisationController.java`, `VendorAdminController.java`, `PhonebookController.java`.

- [ ] **Step 3: Write a smoke SkeletonContractTest**

Create `sclera-identity/src/test/java/io/sclera/identity/SkeletonContractTest.java`:

```java
package io.sclera.identity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SkeletonContractTest {
    @Autowired MockMvc mvc;

    @Test
    void contextLoads_andOneEndpointResponds() throws Exception {
        // Smoke: ANY generated GET endpoint returns 200 (exact path depends on inventory).
        mvc.perform(get("/user/getUserById").param("userId", "u1"))
           .andExpect(status().isOk());
    }
}
```

If `/user/getUserById` does not exist in the inventory, swap to an endpoint that does. The point is one GET round-trip succeeds.

- [ ] **Step 4: Build and verify**

```bash
cd sclera-identity
./mvnw -B test
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add sclera-identity/
git commit -m "feat(skeleton): scaffold sclera-identity (CP-2)"
```

---

### Task 6: Scaffold `sclera-alerts`

**Files:**
- Create: `sclera-alerts/`
- Test: `sclera-alerts/src/test/java/io/sclera/alerts/SkeletonContractTest.java`

- [ ] **Step 1: Run scaffold script**

```bash
bash tools/skeleton-template/scaffold-skeleton.sh \
  --servicekey alerts \
  --port 8092 \
  --target AP-C5 \
  --inventory sclera-cloud-device-asset/migration-notes/stub-inventory.md \
  --out sclera-alerts
```

- [ ] **Step 2: Write smoke SkeletonContractTest**

Create `sclera-alerts/src/test/java/io/sclera/alerts/SkeletonContractTest.java`:

```java
package io.sclera.alerts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SkeletonContractTest {
    @Test
    void contextLoads() {}
}
```

- [ ] **Step 3: Build and verify**

```bash
cd sclera-alerts
./mvnw -B test
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add sclera-alerts/
git commit -m "feat(skeleton): scaffold sclera-alerts (AP-C5)"
```

---

### Task 7: Scaffold `sclera-inventory`

**Files:**
- Create: `sclera-inventory/`
- Test: `sclera-inventory/src/test/java/io/sclera/inventory/SkeletonContractTest.java`

- [ ] **Step 1: Run scaffold script**

```bash
bash tools/skeleton-template/scaffold-skeleton.sh \
  --servicekey inventory \
  --port 8093 \
  --target AP-C8 \
  --inventory sclera-cloud-device-asset/migration-notes/stub-inventory.md \
  --out sclera-inventory
```

- [ ] **Step 2: Write context-loads test**

Create `sclera-inventory/src/test/java/io/sclera/inventory/SkeletonContractTest.java`:

```java
package io.sclera.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SkeletonContractTest {
    @Test
    void contextLoads() {}
}
```

- [ ] **Step 3: Build and verify**

```bash
cd sclera-inventory
./mvnw -B test
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add sclera-inventory/
git commit -m "feat(skeleton): scaffold sclera-inventory (AP-C8)"
```

---

### Task 8: Scaffold `sclera-workorders`

**Files:**
- Create: `sclera-workorders/`
- Test: `sclera-workorders/src/test/java/io/sclera/workorders/SkeletonContractTest.java`

- [ ] **Step 1: Run scaffold script**

```bash
bash tools/skeleton-template/scaffold-skeleton.sh \
  --servicekey workorders \
  --port 8094 \
  --target AP-C3 \
  --inventory sclera-cloud-device-asset/migration-notes/stub-inventory.md \
  --out sclera-workorders
```

- [ ] **Step 2: Write context-loads test**

Create `sclera-workorders/src/test/java/io/sclera/workorders/SkeletonContractTest.java`:

```java
package io.sclera.workorders;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SkeletonContractTest {
    @Test
    void contextLoads() {}
}
```

- [ ] **Step 3: Build and verify**

```bash
cd sclera-workorders
./mvnw -B test
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add sclera-workorders/
git commit -m "feat(skeleton): scaffold sclera-workorders (AP-C3)"
```

---

### Task 9: Scaffold `sclera-inspection`

**Files:**
- Create: `sclera-inspection/`
- Test: `sclera-inspection/src/test/java/io/sclera/inspection/SkeletonContractTest.java`

- [ ] **Step 1: Run scaffold script**

```bash
bash tools/skeleton-template/scaffold-skeleton.sh \
  --servicekey inspection \
  --port 8095 \
  --target AP-C4 \
  --inventory sclera-cloud-device-asset/migration-notes/stub-inventory.md \
  --out sclera-inspection
```

- [ ] **Step 2: Write context-loads test**

Create `sclera-inspection/src/test/java/io/sclera/inspection/SkeletonContractTest.java`:

```java
package io.sclera.inspection;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SkeletonContractTest {
    @Test
    void contextLoads() {}
}
```

- [ ] **Step 3: Build and verify**

```bash
cd sclera-inspection
./mvnw -B test
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add sclera-inspection/
git commit -m "feat(skeleton): scaffold sclera-inspection (AP-C4)"
```

---

### Task 10: Scaffold `sclera-integrations`

**Files:**
- Create: `sclera-integrations/`
- Test: `sclera-integrations/src/test/java/io/sclera/integrations/SkeletonContractTest.java`

- [ ] **Step 1: Run scaffold script**

```bash
bash tools/skeleton-template/scaffold-skeleton.sh \
  --servicekey integrations \
  --port 8096 \
  --target AP-C2 \
  --inventory sclera-cloud-device-asset/migration-notes/stub-inventory.md \
  --out sclera-integrations
```

- [ ] **Step 2: Verify 16 controllers generated**

```bash
ls sclera-integrations/src/main/java/io/sclera/integrations/controller/ | wc -l
```

Expected: `16`

- [ ] **Step 3: Write context-loads test**

Create `sclera-integrations/src/test/java/io/sclera/integrations/SkeletonContractTest.java`:

```java
package io.sclera.integrations;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SkeletonContractTest {
    @Test
    void contextLoads() {}
}
```

- [ ] **Step 4: Build and verify**

```bash
cd sclera-integrations
./mvnw -B test
```

Expected: BUILD SUCCESS. (This is the largest skeleton — 16 controllers.)

- [ ] **Step 5: Commit**

```bash
git add sclera-integrations/
git commit -m "feat(skeleton): scaffold sclera-integrations (AP-C2, 16 controllers)"
```

---

### Task 11: Compose entries for all 7 skeletons + sidecars

**Files:**
- Modify: `docker-compose.yml` (append 14 services after the existing `vdms-dapr` entry, before the `ui` block)

- [ ] **Step 1: Append the seven skeleton + sidecar blocks**

Open `docker-compose.yml` and add, after the existing `vdms-dapr` block (before `api-gateway`):

```yaml
  sclera-audit:
    build: ./sclera-audit
    container_name: sclera-audit
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DAPR_HTTP_PORT: "3500"
      DAPR_GRPC_PORT: "50001"
    ports: ["8090:8090"]
    networks: [sclera-net]
    restart: on-failure

  sclera-audit-dapr:
    image: daprio/daprd:1.12.0
    command: ["./daprd",
      "--app-id", "sclera-audit", "--app-port", "8090",
      "--dapr-http-port", "3500", "--dapr-grpc-port", "50001",
      "--placement-host-address", "",
      "--components-path", "/dapr/components/local",
      "--log-level", "info"]
    volumes: ["./sclera-cloud-device-asset/dapr:/dapr"]
    network_mode: "service:sclera-audit"
    restart: on-failure
    depends_on: [sclera-audit, redis]

  sclera-identity:
    build: ./sclera-identity
    container_name: sclera-identity
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DAPR_HTTP_PORT: "3500"
      DAPR_GRPC_PORT: "50001"
    ports: ["8091:8091"]
    networks: [sclera-net]
    restart: on-failure

  sclera-identity-dapr:
    image: daprio/daprd:1.12.0
    command: ["./daprd",
      "--app-id", "sclera-identity", "--app-port", "8091",
      "--dapr-http-port", "3500", "--dapr-grpc-port", "50001",
      "--placement-host-address", "",
      "--components-path", "/dapr/components/local",
      "--log-level", "info"]
    volumes: ["./sclera-cloud-device-asset/dapr:/dapr"]
    network_mode: "service:sclera-identity"
    restart: on-failure
    depends_on: [sclera-identity, redis]

  sclera-alerts:
    build: ./sclera-alerts
    container_name: sclera-alerts
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DAPR_HTTP_PORT: "3500"
      DAPR_GRPC_PORT: "50001"
    ports: ["8092:8092"]
    networks: [sclera-net]
    restart: on-failure

  sclera-alerts-dapr:
    image: daprio/daprd:1.12.0
    command: ["./daprd",
      "--app-id", "sclera-alerts", "--app-port", "8092",
      "--dapr-http-port", "3500", "--dapr-grpc-port", "50001",
      "--placement-host-address", "",
      "--components-path", "/dapr/components/local",
      "--log-level", "info"]
    volumes: ["./sclera-cloud-device-asset/dapr:/dapr"]
    network_mode: "service:sclera-alerts"
    restart: on-failure
    depends_on: [sclera-alerts, redis]

  sclera-inventory:
    build: ./sclera-inventory
    container_name: sclera-inventory
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DAPR_HTTP_PORT: "3500"
      DAPR_GRPC_PORT: "50001"
    ports: ["8093:8093"]
    networks: [sclera-net]
    restart: on-failure

  sclera-inventory-dapr:
    image: daprio/daprd:1.12.0
    command: ["./daprd",
      "--app-id", "sclera-inventory", "--app-port", "8093",
      "--dapr-http-port", "3500", "--dapr-grpc-port", "50001",
      "--placement-host-address", "",
      "--components-path", "/dapr/components/local",
      "--log-level", "info"]
    volumes: ["./sclera-cloud-device-asset/dapr:/dapr"]
    network_mode: "service:sclera-inventory"
    restart: on-failure
    depends_on: [sclera-inventory, redis]

  sclera-workorders:
    build: ./sclera-workorders
    container_name: sclera-workorders
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DAPR_HTTP_PORT: "3500"
      DAPR_GRPC_PORT: "50001"
    ports: ["8094:8094"]
    networks: [sclera-net]
    restart: on-failure

  sclera-workorders-dapr:
    image: daprio/daprd:1.12.0
    command: ["./daprd",
      "--app-id", "sclera-workorders", "--app-port", "8094",
      "--dapr-http-port", "3500", "--dapr-grpc-port", "50001",
      "--placement-host-address", "",
      "--components-path", "/dapr/components/local",
      "--log-level", "info"]
    volumes: ["./sclera-cloud-device-asset/dapr:/dapr"]
    network_mode: "service:sclera-workorders"
    restart: on-failure
    depends_on: [sclera-workorders, redis]

  sclera-inspection:
    build: ./sclera-inspection
    container_name: sclera-inspection
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DAPR_HTTP_PORT: "3500"
      DAPR_GRPC_PORT: "50001"
    ports: ["8095:8095"]
    networks: [sclera-net]
    restart: on-failure

  sclera-inspection-dapr:
    image: daprio/daprd:1.12.0
    command: ["./daprd",
      "--app-id", "sclera-inspection", "--app-port", "8095",
      "--dapr-http-port", "3500", "--dapr-grpc-port", "50001",
      "--placement-host-address", "",
      "--components-path", "/dapr/components/local",
      "--log-level", "info"]
    volumes: ["./sclera-cloud-device-asset/dapr:/dapr"]
    network_mode: "service:sclera-inspection"
    restart: on-failure
    depends_on: [sclera-inspection, redis]

  sclera-integrations:
    build: ./sclera-integrations
    container_name: sclera-integrations
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DAPR_HTTP_PORT: "3500"
      DAPR_GRPC_PORT: "50001"
    ports: ["8096:8096"]
    networks: [sclera-net]
    restart: on-failure

  sclera-integrations-dapr:
    image: daprio/daprd:1.12.0
    command: ["./daprd",
      "--app-id", "sclera-integrations", "--app-port", "8096",
      "--dapr-http-port", "3500", "--dapr-grpc-port", "50001",
      "--placement-host-address", "",
      "--components-path", "/dapr/components/local",
      "--log-level", "info"]
    volumes: ["./sclera-cloud-device-asset/dapr:/dapr"]
    network_mode: "service:sclera-integrations"
    restart: on-failure
    depends_on: [sclera-integrations, redis]
```

- [ ] **Step 2: Validate compose syntax**

```bash
docker compose config > /dev/null
```

Expected: exit 0, no errors.

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml
git commit -m "feat(compose): add 7 skeleton services and Dapr sidecars"
```

---

### Task 12: APP_IDS registry

**Files:**
- Create: `dapr/APP_IDS.md` (if it does not yet exist from the Dapr foundation phase; otherwise modify)

- [ ] **Step 1: Create or extend `dapr/APP_IDS.md`**

```markdown
# Dapr app-id registry

| App-id | Service | Port | Subscribes | Publishes | Owner |
|---|---|---|---|---|---|
| `sclera-api-gateway` | sclera-api-gateway | 8080 | — | — | platform |
| `sclera-cloud-device-asset` | sclera-cloud-device-asset | 8085 | — | device.audit-recorded, vdms.* | platform |
| `vdms-service` | sclera-vdms-service | 8089 | device.audit, vdms.* | — | platform |
| `sclera-audit` | sclera-audit (skeleton) | 8090 | device.audit-recorded | — | platform |
| `sclera-identity` | sclera-identity (skeleton) | 8091 | — | identity.org-renamed, identity.user-deactivated | platform |
| `sclera-alerts` | sclera-alerts (skeleton) | 8092 | device.alert-condition-fired (future) | alerts.notification-dispatched (future) | platform |
| `sclera-inventory` | sclera-inventory (skeleton) | 8093 | — | — | platform |
| `sclera-workorders` | sclera-workorders (skeleton) | 8094 | — | — | platform |
| `sclera-inspection` | sclera-inspection (skeleton) | 8095 | — | — | platform |
| `sclera-integrations` | sclera-integrations (skeleton) | 8096 | — | — | platform |

## Port range
- 8080–8089: existing services
- 8090–8099: skeleton services (this plan)
- 8100+: reserved for future split (e.g. AP-C2a/b/c)

## Convention
App-id = kebab-case prefixed `sclera-`. Matches `spring.application.name`.
Topics = `<domain>.<event-past-tense>`.
```

- [ ] **Step 2: Commit**

```bash
git add dapr/APP_IDS.md
git commit -m "docs(dapr): app-id registry covering existing services + 7 skeletons"
```

---

### Task 13: Smoke-test all 7 skeletons via Compose

**Files:**
- No file changes — verification only.

- [ ] **Step 1: Bring the system up**

```bash
docker compose up -d --build redis sclera-audit sclera-audit-dapr sclera-identity sclera-identity-dapr sclera-alerts sclera-alerts-dapr sclera-inventory sclera-inventory-dapr sclera-workorders sclera-workorders-dapr sclera-inspection sclera-inspection-dapr sclera-integrations sclera-integrations-dapr
```

Expected: all 14 containers reach `Up` state within ~60s.

- [ ] **Step 2: Check health endpoint per skeleton**

```bash
for port in 8090 8091 8092 8093 8094 8095 8096; do
  echo -n "port $port: "
  curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:$port/actuator/health"
done
```

Expected: seven `200` lines.

- [ ] **Step 3: Invoke each skeleton via its Dapr sidecar**

For each port, verify the sidecar accepts and routes an invoke:

```bash
docker exec sclera-audit curl -s -o /dev/null -w "audit invoke: %{http_code}\n" \
  "http://localhost:3500/v1.0/invoke/sclera-audit/method/history/getDeviceIdByHistoryId?historyId=test"
```

Expected: `200`.

Repeat for each of the seven skeletons with one of its known endpoint paths.

- [ ] **Step 4: Verify sclera-audit's subscription is registered**

```bash
docker exec sclera-audit curl -s "http://localhost:3500/v1.0/metadata" | grep -o 'device.audit-recorded'
```

Expected: prints `device.audit-recorded` once.

- [ ] **Step 5: Tear down**

```bash
docker compose down
```

- [ ] **Step 6: Commit Phase A milestone marker**

```bash
git commit --allow-empty -m "milestone: Phase A skeleton scaffolding complete — 7 services up via compose"
```

---

## Phase B — Wave 1: sclera-audit client migration

### Task 14: Dapr client foundation in sclera-cloud-device-asset

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/client/DaprClientConfig.java`
- Modify: `sclera-cloud-device-asset/pom.xml` (add dapr-sdk if not present)
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/client/DaprClientConfigTest.java`

- [ ] **Step 1: Ensure Dapr SDK dependency is in pom.xml**

In `sclera-cloud-device-asset/pom.xml`, add inside `<dependencies>`:

```xml
<dependency>
  <groupId>io.dapr</groupId>
  <artifactId>dapr-sdk</artifactId>
  <version>1.12.0</version>
</dependency>
<dependency>
  <groupId>io.dapr</groupId>
  <artifactId>dapr-sdk-springboot</artifactId>
  <version>1.12.0</version>
</dependency>
```

If they already exist, skip. Verify with `grep dapr-sdk sclera-cloud-device-asset/pom.xml`.

- [ ] **Step 2: Write the failing test**

Create `sclera-cloud-device-asset/src/test/java/io/sclera/client/DaprClientConfigTest.java`:

```java
package io.sclera.client;

import io.dapr.client.DaprClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DaprClientConfigTest {
    @Autowired DaprClient client;

    @Test
    void daprClientBeanIsAvailable() {
        assertThat(client).isNotNull();
    }
}
```

- [ ] **Step 3: Run the test and verify it fails**

```bash
cd sclera-cloud-device-asset
./mvnw -B -Dtest=DaprClientConfigTest test
```

Expected: FAIL with `NoSuchBeanDefinitionException` for `DaprClient`.

- [ ] **Step 4: Add the DaprClient bean**

Create `sclera-cloud-device-asset/src/main/java/io/sclera/client/DaprClientConfig.java`:

```java
package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaprClientConfig {
    @Bean(destroyMethod = "close")
    public DaprClient daprClient() {
        return new DaprClientBuilder().build();
    }
}
```

- [ ] **Step 5: Run test and verify it passes**

```bash
./mvnw -B -Dtest=DaprClientConfigTest test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add sclera-cloud-device-asset/pom.xml sclera-cloud-device-asset/src/main/java/io/sclera/client/ sclera-cloud-device-asset/src/test/java/io/sclera/client/
git commit -m "feat(client): add DaprClient bean for Dapr SDK invocation"
```

---

### Task 15: Migrate `HistoryService` → `HistoryClient`

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/client/HistoryClient.java`
- Test:   `sclera-cloud-device-asset/src/test/java/io/sclera/client/HistoryClientTest.java`
- Delete: `sclera-cloud-device-asset/src/main/java/io/sclera/service/HistoryService.java`
- Modify: every call site that used `@Autowired HistoryService` (use grep to find them; expect 3-10 sites)

- [ ] **Step 1: Snapshot the original signatures**

Open `sclera-cloud-device-asset/src/main/java/io/sclera/service/HistoryService.java` and copy the method signatures to a scratchpad. Confirm each returns null / 0 / empty per the original stub. These are the contract `HistoryClient` must preserve.

- [ ] **Step 2: Write failing client tests**

Create `sclera-cloud-device-asset/src/test/java/io/sclera/client/HistoryClientTest.java`:

```java
package io.sclera.client;

import io.dapr.client.DaprClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HistoryClientTest {

    @Test
    void getDeviceIdByHistoryId_returnsNullWhenSkeletonReturnsNull() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-audit"), eq("history/getDeviceIdByHistoryId"), any(), any(), eq(String.class)))
            .thenReturn(Mono.justOrEmpty(null));

        HistoryClient client = new HistoryClient(dapr);

        assertThat(client.getDeviceIdByHistoryId("h1")).isNull();
    }

    @Test
    void getDeviceIdByHistoryId_returnsNullOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-audit"), eq("history/getDeviceIdByHistoryId"), any(), any(), eq(String.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        HistoryClient client = new HistoryClient(dapr);

        // Documented behavior: stub returned null; client returns null on failure too.
        assertThat(client.getDeviceIdByHistoryId("h1")).isNull();
    }
}
```

- [ ] **Step 3: Run the test and verify it fails**

```bash
cd sclera-cloud-device-asset
./mvnw -B -Dtest=HistoryClientTest test
```

Expected: FAIL — `HistoryClient` does not exist.

- [ ] **Step 4: Implement `HistoryClient`**

Create `sclera-cloud-device-asset/src/main/java/io/sclera/client/HistoryClient.java`:

```java
package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.dapr.client.domain.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HistoryClient {
    private static final Logger log = LoggerFactory.getLogger(HistoryClient.class);
    private static final String APP_ID = "sclera-audit";

    private final DaprClient dapr;

    public HistoryClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    public String getDeviceIdByHistoryId(String historyId) {
        try {
            return dapr.invokeMethod(
                APP_ID,
                "history/getDeviceIdByHistoryId",
                Map.of("historyId", historyId),
                HttpExtension.GET,
                String.class
            ).block();
        } catch (Exception e) {
            log.warn("HistoryClient.getDeviceIdByHistoryId failed; returning null", e);
            return null;
        }
    }

    // Add a method here for each public method previously on HistoryService.
    // Mirror the original return type and default-on-failure value.
}
```

For each remaining `HistoryService` method, add a matching wrapper method following the same shape: log + try/catch + return-the-stub-default on failure. Keep signatures byte-for-byte identical to the stub.

- [ ] **Step 5: Run client tests, verify pass**

```bash
./mvnw -B -Dtest=HistoryClientTest test
```

Expected: PASS.

- [ ] **Step 6: Replace stub call sites**

```bash
grep -rn "HistoryService" sclera-cloud-device-asset/src/main/java/
```

For each match:
- Replace `import io.sclera.service.HistoryService;` → `import io.sclera.client.HistoryClient;`
- Replace `@Autowired HistoryService historyService;` → `@Autowired HistoryClient historyClient;`
- Replace `historyService.` → `historyClient.`

- [ ] **Step 7: Run full module test suite to confirm no regressions**

```bash
./mvnw -B test
```

Expected: all existing tests pass (or fail in the same way they did before this PR — record any pre-existing failures in the commit message).

- [ ] **Step 8: Delete the stub**

```bash
rm sclera-cloud-device-asset/src/main/java/io/sclera/service/HistoryService.java
```

Run compile again to confirm nothing broken:

```bash
./mvnw -B compile
```

Expected: BUILD SUCCESS.

- [ ] **Step 9: Cross-service integration test (manual smoke)**

Bring up the audit skeleton + cloud-device-asset:

```bash
cd ..
docker compose up -d --build redis sclera-audit sclera-audit-dapr app app-dapr
sleep 20

curl -s "http://localhost:8085/<path-that-calls-HistoryClient>" | head
```

Replace `<path>` with any controller route in `cloud-device-asset` that exercises `HistoryClient`. The response should match what the stub used to produce.

```bash
docker compose down
```

- [ ] **Step 10: Commit**

```bash
git add -u sclera-cloud-device-asset/
git add sclera-cloud-device-asset/src/main/java/io/sclera/client/HistoryClient.java sclera-cloud-device-asset/src/test/java/io/sclera/client/HistoryClientTest.java
git commit -m "migrate(stub→client): HistoryService via Dapr to sclera-audit"
```

---

### Task 16: Migrate `ArchivedRecordService` → `ArchivedRecordClient`

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/client/ArchivedRecordClient.java`
- Test:   `sclera-cloud-device-asset/src/test/java/io/sclera/client/ArchivedRecordClientTest.java`
- Delete: `sclera-cloud-device-asset/src/main/java/io/sclera/service/ArchivedRecordService.java`
- Modify: every call site that used `@Autowired ArchivedRecordService`

- [ ] **Step 1: Snapshot original signatures**

Open `sclera-cloud-device-asset/src/main/java/io/sclera/service/ArchivedRecordService.java`; list every public method and its documented default.

- [ ] **Step 2: Write failing client test**

Create `sclera-cloud-device-asset/src/test/java/io/sclera/client/ArchivedRecordClientTest.java`:

```java
package io.sclera.client;

import io.dapr.client.DaprClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArchivedRecordClientTest {

    @Test
    void firstMethod_returnsDocumentedDefault() {
        DaprClient dapr = mock(DaprClient.class);
        // Adjust eq("...") to match the actual first method name from the stub.
        when(dapr.invokeMethod(eq("sclera-audit"), any(), any(), any(), eq(java.util.List.class)))
            .thenReturn(Mono.just(java.util.List.of()));

        ArchivedRecordClient client = new ArchivedRecordClient(dapr);
        assertThat(client.findAll()).isEmpty();
    }
}
```

Adjust the method name and return type to match the actual first method on `ArchivedRecordService`.

- [ ] **Step 3: Run, verify fail**

```bash
cd sclera-cloud-device-asset
./mvnw -B -Dtest=ArchivedRecordClientTest test
```

Expected: FAIL.

- [ ] **Step 4: Implement `ArchivedRecordClient`**

Create `sclera-cloud-device-asset/src/main/java/io/sclera/client/ArchivedRecordClient.java`. Use `HistoryClient` (Task 15) as the template. APP_ID = `"sclera-audit"`. One wrapper method per stub method; log + try/catch + return the stub's default on failure.

- [ ] **Step 5: Run client tests, verify pass**

```bash
./mvnw -B -Dtest=ArchivedRecordClientTest test
```

Expected: PASS.

- [ ] **Step 6: Replace call sites and delete stub**

```bash
grep -rn "ArchivedRecordService" sclera-cloud-device-asset/src/main/java/
# Replace imports, field types, field names, method calls.
rm sclera-cloud-device-asset/src/main/java/io/sclera/service/ArchivedRecordService.java
./mvnw -B compile
```

Expected: BUILD SUCCESS.

- [ ] **Step 7: Run full suite**

```bash
./mvnw -B test
```

Expected: all tests pass (or fail identically to pre-migration).

- [ ] **Step 8: Commit**

```bash
git add -u sclera-cloud-device-asset/
git add sclera-cloud-device-asset/src/main/java/io/sclera/client/ArchivedRecordClient.java sclera-cloud-device-asset/src/test/java/io/sclera/client/ArchivedRecordClientTest.java
git commit -m "migrate(stub→client): ArchivedRecordService via Dapr to sclera-audit"
```

---

### Task 17: Migrate `SyslogService` → `SyslogClient`

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/client/SyslogClient.java`
- Test:   `sclera-cloud-device-asset/src/test/java/io/sclera/client/SyslogClientTest.java`
- Delete: `sclera-cloud-device-asset/src/main/java/io/sclera/service/SyslogService.java`
- Modify: every call site that used `@Autowired SyslogService`

- [ ] **Step 1: Snapshot original signatures**

Open the stub; list method signatures + documented defaults.

- [ ] **Step 2: Write failing client test**

Create `sclera-cloud-device-asset/src/test/java/io/sclera/client/SyslogClientTest.java`:

```java
package io.sclera.client;

import io.dapr.client.DaprClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SyslogClientTest {

    @Test
    void firstMethod_returnsDocumentedDefault() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-audit"), any(), any(), any(), eq(String.class)))
            .thenReturn(Mono.justOrEmpty(null));

        SyslogClient client = new SyslogClient(dapr);
        // Adjust method name to match actual first method on SyslogService.
        assertThat(client.someMethod("arg")).isNull();
    }
}
```

- [ ] **Step 3: Run, verify fail**

```bash
./mvnw -B -Dtest=SyslogClientTest test
```

Expected: FAIL.

- [ ] **Step 4: Implement `SyslogClient`**

Create `sclera-cloud-device-asset/src/main/java/io/sclera/client/SyslogClient.java` following the `HistoryClient` template. APP_ID = `"sclera-audit"`.

- [ ] **Step 5: Run client tests, verify pass**

```bash
./mvnw -B -Dtest=SyslogClientTest test
```

Expected: PASS.

- [ ] **Step 6: Replace call sites and delete stub**

```bash
grep -rn "SyslogService" sclera-cloud-device-asset/src/main/java/
# Replace imports, fields, calls.
rm sclera-cloud-device-asset/src/main/java/io/sclera/service/SyslogService.java
./mvnw -B compile
```

Expected: BUILD SUCCESS.

- [ ] **Step 7: Run full suite**

```bash
./mvnw -B test
```

Expected: all tests pass.

- [ ] **Step 8: Commit**

```bash
git add -u sclera-cloud-device-asset/
git add sclera-cloud-device-asset/src/main/java/io/sclera/client/SyslogClient.java sclera-cloud-device-asset/src/test/java/io/sclera/client/SyslogClientTest.java
git commit -m "migrate(stub→client): SyslogService via Dapr to sclera-audit"
```

---

### Task 18: End-to-end PoC verification

**Files:**
- No new code — exercises everything built so far.

- [ ] **Step 1: Bring up the audit slice**

```bash
docker compose up -d --build redis sclera-audit sclera-audit-dapr app app-dapr
```

Wait for both `sclera-audit` and `app` containers to be healthy (~60s).

- [ ] **Step 2: Verify trace propagation**

Pick a controller endpoint in `sclera-cloud-device-asset` that exercises any of the three migrated clients. Hit it:

```bash
curl -s -H "traceparent: 00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01" \
  "http://localhost:8085/<path-that-exercises-HistoryClient>" > /tmp/resp.json
```

Verify the response is unchanged from pre-migration behavior (null / empty / etc.).

If OpenTelemetry collector + Jaeger are running (Phase 1.4 of the Dapr spec), open Jaeger UI and confirm a span tree exists: `sclera-cloud-device-asset → sclera-audit`. If OTel is not yet wired, skip this and note "trace verification deferred to Phase 1.4 completion" in the PR description.

- [ ] **Step 3: Verify graceful degradation (chaos)**

```bash
docker pause sclera-audit
# Within 30s, hit the same endpoint:
curl -s "http://localhost:8085/<path-that-exercises-HistoryClient>"
```

Expected: response still returns documented default (null / empty list, etc.) — NOT a 5xx. The client's try/catch swallows the Dapr exception.

```bash
docker unpause sclera-audit
```

- [ ] **Step 4: Verify subscription delivery**

Publish a CloudEvent for `device.audit-recorded` from `app` and confirm `sclera-audit` logs receipt:

```bash
docker exec sclera-cloud-device-asset curl -s -X POST -H "Content-Type: application/json" \
  -d '{"id":"audit-1","type":"test","data":{"action":"test"}}' \
  "http://localhost:3500/v1.0/publish/pubsub/device.audit-recorded"

docker logs sclera-audit --tail 5
```

Expected: a log line like `[skeleton] received test: id=audit-1`.

- [ ] **Step 5: Tear down**

```bash
docker compose down
```

- [ ] **Step 6: Commit PoC milestone marker**

```bash
git commit --allow-empty -m "milestone: Phase B Wave 1 complete — sclera-audit PoC verified"
```

---

## After this plan

The remaining waves (sclera-identity, sclera-alerts, sclera-inventory, sclera-workorders, sclera-inspection, sclera-integrations × 16 stub classes) follow the same Task 15 pattern. Suggested follow-up plans:

- `2026-MM-DD-walking-skeleton-wave-2-identity.md` — 4 PRs
- `2026-MM-DD-walking-skeleton-wave-3-alerts.md` — 5 PRs
- `2026-MM-DD-walking-skeleton-wave-4-inventory.md` — 1 PR
- `2026-MM-DD-walking-skeleton-wave-5-workorders.md` — 4 PRs
- `2026-MM-DD-walking-skeleton-wave-6-inspection.md` — 6 PRs
- `2026-MM-DD-walking-skeleton-wave-7-integrations.md` — 16 PRs

Each follows the Task 15 / Task 16 / Task 17 template above with parameter substitution.
