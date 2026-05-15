# Known breakages

Behaviors that compile but are intentionally non-functional until stubs become real remote calls in Phase 2. Also security and version-skew items inherited verbatim from the source repo.

## Inherited security CVEs (no action in Phase 1)
- `org.json:json:20090211` — known CVE-2022-45688 / CVE-2023-5072.
- `com.alibaba:fastjson:1.1.24` — multiple high-severity CVEs including RCE.

## Version skew (verify in compile pass)
- `spring-boot-starter-oauth2-resource-server:3.0.6` on Spring Boot 2.6.5 / Spring Security 5.7.3 — inherited verbatim from source. If `mvn compile` fails, this is the first place to look.

## Missing validation starter (verify in compile pass)
- `spring-boot-starter-validation` not declared. If any extracted controller/DTO uses `@Valid` / `javax.validation`, add it during compile-pass loop.

## Test-context JWT decoder
- `application-test.yml` sets `issuer-uri: http://localhost/test-issuer` which is unreachable. The `@SpringBootTest` context-load smoke (Task 18) will fail unless a `TestSecurityConfig` stubs out `JwtDecoder`. Address during Task 18.