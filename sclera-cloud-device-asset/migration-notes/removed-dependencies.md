# Removed Maven dependencies and system JARs

Maven coordinates and system JARs intentionally excluded from the new repo's pom.xml during Phase 1 extraction. Source: spec §5.4 + plan Task 2.

| Coordinate | Reason | Source line |
|---|---|---|

## Pending compile-pass additions to record:
- `org.json:json:20090211` — known CVE-2022-45688 / CVE-2023-5072 (inherited verbatim; flag for Phase 2).
- `com.alibaba:fastjson:1.1.24` — multiple high-severity CVEs including RCE (inherited verbatim; flag for Phase 2).