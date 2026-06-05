#!/usr/bin/env python3
"""Generate a Postman v2.1 collection from the service's live OpenAPI doc.

Reads an OpenAPI 3.x JSON spec and emits a Postman collection:
  - folders grouped by first path segment (the spec has no tags)
  - one request per path+method with path vars, query params and a JSON body example
  - collection-level Bearer {{token}} auth; {{baseUrl}} variable
"""
import json
import sys

SPEC_PATH = sys.argv[1]
OUT_PATH = sys.argv[2]
BASE_URL = sys.argv[3]

spec = json.load(open(SPEC_PATH, encoding="utf-8"))
schemas = spec.get("components", {}).get("schemas", {})


def resolve_ref(ref):
    # "#/components/schemas/Foo" -> schema dict
    name = ref.split("/")[-1]
    return schemas.get(name, {})


def example_for(schema, depth=0, seen=None):
    """Build a minimal example value from a JSON schema (refs resolved, depth-capped)."""
    if seen is None:
        seen = set()
    if not isinstance(schema, dict) or depth > 5:
        return None
    if "$ref" in schema:
        ref = schema["$ref"]
        if ref in seen:
            return {}
        seen = seen | {ref}
        return example_for(resolve_ref(ref), depth + 1, seen)
    if "example" in schema:
        return schema["example"]
    # composed schemas
    for key in ("allOf", "anyOf", "oneOf"):
        if key in schema and schema[key]:
            return example_for(schema[key][0], depth + 1, seen)
    if "enum" in schema and schema["enum"]:
        return schema["enum"][0]
    t = schema.get("type")
    if isinstance(t, list):
        t = next((x for x in t if x != "null"), t[0]) if t else None
    if t == "object" or "properties" in schema:
        out = {}
        for pname, pschema in (schema.get("properties") or {}).items():
            out[pname] = example_for(pschema, depth + 1, seen)
        return out
    if t == "array":
        item = example_for(schema.get("items", {}), depth + 1, seen)
        return [item] if item is not None else []
    if t == "integer" or t == "number":
        return 0
    if t == "boolean":
        return False
    if t == "string":
        fmt = schema.get("format")
        if fmt == "date-time":
            return "1970-01-01T00:00:00Z"
        if fmt == "date":
            return "1970-01-01"
        if fmt == "binary":
            return ""
        return ""
    return None


def build_request(path, method, op):
    params = op.get("parameters", []) or []
    path_params = [p for p in params if p.get("in") == "path"]
    query_params = [p for p in params if p.get("in") == "query"]

    # Postman path: split, convert {var} -> :var
    raw_segments = [s for s in path.split("/") if s != ""]
    pm_path = [(":" + s[1:-1]) if (s.startswith("{") and s.endswith("}")) else s
               for s in raw_segments]

    url = {
        "raw": "{{baseUrl}}" + path,
        "host": ["{{baseUrl}}"],
        "path": pm_path,
    }
    if query_params:
        url["query"] = [{
            "key": p["name"],
            "value": "",
            "description": (("(required) " if p.get("required") else "") + (p.get("description") or "")).strip(),
            "disabled": not p.get("required", False),
        } for p in query_params]
        qs = "&".join(p["name"] + "=" for p in query_params)
        url["raw"] = "{{baseUrl}}" + path + "?" + qs
    if path_params:
        url["variable"] = [{
            "key": p["name"],
            "value": "",
            "description": (p.get("description") or ""),
        } for p in path_params]

    request = {"method": method.upper(), "header": [], "url": url}

    desc = op.get("summary") or op.get("description")

    body = op.get("requestBody", {})
    content = body.get("content", {}) if isinstance(body, dict) else {}
    if "application/json" in content:
        ex = example_for(content["application/json"].get("schema", {}))
        request["header"].append({"key": "Content-Type", "value": "application/json"})
        request["body"] = {
            "mode": "raw",
            "raw": json.dumps(ex if ex is not None else {}, indent=2),
            "options": {"raw": {"language": "json"}},
        }
    elif "multipart/form-data" in content:
        sch = content["multipart/form-data"].get("schema", {})
        if "$ref" in sch:
            sch = resolve_ref(sch["$ref"])
        props = (sch.get("properties") or {})
        formdata = []
        for fname, fsch in props.items():
            is_file = isinstance(fsch, dict) and fsch.get("format") == "binary"
            formdata.append({"key": fname, "type": "file" if is_file else "text",
                             "value": "" if not is_file else None, "src": "" if is_file else None})
            formdata[-1] = {k: v for k, v in formdata[-1].items() if v is not None}
        request["body"] = {"mode": "formdata", "formdata": formdata}

    # Prefer the descriptive operationId as the request name; keep method+path in the description.
    op_id = op.get("operationId")
    name = op_id if op_id else (method.upper() + " " + path)
    method_path = method.upper() + " " + path
    request["description"] = (desc + "\n\n" + method_path).strip() if desc else method_path
    return {"name": name, "request": request, "response": []}


METHODS = ("get", "post", "put", "patch", "delete", "head", "options")
folders = {}
for path in sorted(spec.get("paths", {})):
    ops = spec["paths"][path]
    for method in METHODS:
        if method not in ops:
            continue
        op = ops[method]
        # Group by controller tag (springdoc auto-tags by @RestController); fall back to first path segment.
        tags = op.get("tags") or []
        if tags:
            folder = tags[0]
        else:
            folder = next((s for s in path.split("/") if s and not s.startswith("{")), "root")
        folders.setdefault(folder, []).append(build_request(path, method, op))

# Sort requests within each folder by name for stable, navigable output.
for name in folders:
    folders[name].sort(key=lambda r: r["name"])
items = [{"name": name, "item": folders[name]} for name in sorted(folders)]

collection = {
    "info": {
        "name": spec.get("info", {}).get("title", "API") +
                " v" + str(spec.get("info", {}).get("version", "")),
        "description": "Auto-generated from the live OpenAPI doc (/v3/api-docs). "
                       "Base URL targets the API gateway (/asset route, StripPrefix=1). "
                       "Set {{token}} to a JWT; all requests inherit collection-level Bearer auth.",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "auth": {"type": "bearer", "bearer": [{"key": "token", "value": "{{token}}", "type": "string"}]},
    "variable": [
        {"key": "baseUrl", "value": BASE_URL, "type": "string"},
        {"key": "token", "value": "", "type": "string"},
    ],
    "item": items,
}

with open(OUT_PATH, "w", encoding="utf-8") as f:
    json.dump(collection, f, indent=2)

total = sum(len(v) for v in folders.values())
print(f"folders={len(items)} requests={total}")
print("folder breakdown:")
for name in sorted(folders):
    print(f"  {name}: {len(folders[name])}")
