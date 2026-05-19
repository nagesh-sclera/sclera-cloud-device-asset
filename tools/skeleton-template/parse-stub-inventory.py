#!/usr/bin/env python3
"""Read migration-notes/stub-inventory.md, emit structured stub class data for a given target."""
import argparse, json, re

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
