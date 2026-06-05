# Postman collection — sclera-cloud-device-asset

`sclera-cloud-device-asset.postman_collection.json` is a Postman v2.1 collection auto-generated
from the service's live OpenAPI doc (`/v3/api-docs`). It covers all 200 endpoints (201 requests),
grouped into folders by controller (springdoc tag).

## Import & configure

1. In Postman: **Import** → select the `.json` file.
2. Open the collection's **Variables** tab and set:
   - `baseUrl` — defaults to `http://localhost:8080/asset` (the **API gateway**, `asset-route`,
     which strips the `/asset` prefix and forwards to the service on `:8085`).
     To hit the service directly, set it to `http://localhost:8085`.
   - `token` — paste a JWT. Every request inherits collection-level **Bearer `{{token}}`** auth
     (the service is fronted by `JwtRequestFilter`). `/healthz` and a few endpoints are public.
3. Path params appear as Postman path variables (`:username`, `:vdmsid`, …) on each request's
   **Params** tab. Query params are pre-listed (required ones enabled, optional ones disabled).
4. Request bodies are pre-filled with a JSON skeleton derived from the OpenAPI schema — replace the
   placeholder values.

## Regenerating

When the API changes, regenerate from the running service (no manual editing):

```bash
# from the monorepo root (sclera-cloud-device-asset/)
curl -s http://localhost:8085/v3/api-docs -o tools/apidocs.json
python tools/gen_postman.py tools/apidocs.json \
  sclera-cloud-device-asset/postman/sclera-cloud-device-asset.postman_collection.json \
  http://localhost:8080/asset
rm tools/apidocs.json
```

> Note: the OpenAPI doc currently declares no per-endpoint security scheme (per-endpoint Swagger
> annotations were deferred), so auth is wired at the collection level rather than per request.
