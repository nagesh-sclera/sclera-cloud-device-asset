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

# Top-level templates: substitute {{servicekey}} (as servicekey) and {{port}}
sed -e "s/{{servicekey}}/$SERVICEKEY/g" -e "s/{{port}}/$PORT/g" \
    "$TEMPLATE_DIR/pom.xml.template"            > "$OUT/pom.xml"
sed -e "s/{{servicekey}}/$SERVICEKEY/g" -e "s/{{port}}/$PORT/g" \
    "$TEMPLATE_DIR/Dockerfile.template"         > "$OUT/Dockerfile"
sed -e "s/{{servicekey}}/$SERVICEKEY/g" \
    "$TEMPLATE_DIR/CLAUDE.md.template"          > "$OUT/CLAUDE.md"

# Java source templates: substitute {{servicekey}} as the PACKAGE (snake-cased servicekey)
sed -e "s/{{servicekey}}/$PKG/g" \
    "$TEMPLATE_DIR/src/main/java/io/sclera/{{servicekey}}/Application.java.template" \
    > "$OUT/src/main/java/io/sclera/$PKG/Application.java"
sed -e "s/{{servicekey}}/$PKG/g" \
    "$TEMPLATE_DIR/src/main/java/io/sclera/{{servicekey}}/defaults/Defaults.java.template" \
    > "$OUT/src/main/java/io/sclera/$PKG/defaults/Defaults.java"

# Resources: application.yml uses servicekey form (kebab-case in spring.application.name)
sed -e "s/{{servicekey}}/$SERVICEKEY/g" -e "s/{{port}}/$PORT/g" \
    "$TEMPLATE_DIR/src/main/resources/application.yml.template" \
    > "$OUT/src/main/resources/application.yml"
cp "$TEMPLATE_DIR/src/main/resources/topics.yaml.template" \
    "$OUT/src/main/resources/topics.yaml"

# Generate controllers from inventory.
# We write the Python generator to a temp file so that piped stdin from the parser
# and the generator script body don't conflict (can't use <<heredoc + pipe on same stdin).
_GEN_PY=$(mktemp /tmp/gen-controllers-XXXXXX.py)
trap 'rm -f "$_GEN_PY"' EXIT
cat > "$_GEN_PY" <<'PY'
import json, sys, pathlib

out_dir, pkg = sys.argv[1], sys.argv[2]
data = json.load(sys.stdin)
for cls in data:
    name = cls["class"].replace("Service", "")
    controller = f"{name}Controller"
    path = f"/{name.lower()}"
    body_lines = []
    for m in cls["methods"]:
        params = ", ".join(
            f'@org.springframework.web.bind.annotation.RequestParam {p["type"]} {p["name"]}'
            for p in m["params"]
        )
        ret = m["return"].strip()
        default = m["default"]
        # Void: no return statement, just a comment (avoids `return Defaults.X;` compile error).
        if ret == "void":
            body = "    // no-op\n"
        else:
            body = f'    return io.sclera.{pkg}.defaults.Defaults.{default};\n'
        body_lines.append(
            f'  @org.springframework.web.bind.annotation.GetMapping("/{m["name"]}")\n'
            f'  public {ret} {m["name"]}({params}) {{\n'
            f'{body}'
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

python3 "$TEMPLATE_DIR/parse-stub-inventory.py" \
    --inventory "$INVENTORY" --target "$TARGET" --emit json \
  | python3 "$_GEN_PY" "$OUT/src/main/java/io/sclera/$PKG/controller" "$PKG"

echo "scaffolded sclera-$SERVICEKEY → $OUT"
