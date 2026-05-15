# Stubbed services

Cross-microservice dependencies replaced with stubs in `io.sclera.stubs/` during the Phase 2 compile loop. Each stub mirrors original signatures, returns safe defaults, and emits one WARN log per invocation.

| Stub class | Target microservice | Methods mirrored | Notes |
|---|---|---|---|