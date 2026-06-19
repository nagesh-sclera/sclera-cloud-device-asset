# Cursor (keyset) pagination for the Sclera UI asset-list calls

**Date:** 2026-06-15
**Status:** Approved (design) — pending spec review
**Scope:** `sclera-cloud-device-asset` backend + `sclera-ui` demo frontend

## Problem

The asset-list endpoints used by the Sclera UI demo paginate with offset (`pageno`/`pagesize`
→ `LIMIT/OFFSET`). Offset paging degrades as the dataset grows (the DB still scans and discards
`OFFSET` rows) and is unstable under concurrent inserts/deletes (rows skip or repeat across pages).
We want the industry-standard **keyset (seek) pagination** used by large-data products, exposed to
the demo UI as infinite scroll — without breaking the existing offset callers (the real frontend and
other services still hit these endpoints).

## Goals

- Add **keyset cursor pagination** to the 3 asset GET calls the demo uses:
  - `GET /docker/{dockername}/getsubsystemparentdevicesbypagination`
  - `GET /docker/{dockername}/getfilterdevice`
  - `POST /docker/{dockername}/searchsortfilterdevices`
- **Backward compatible:** no `cursor` param → existing offset behaviour, unchanged response.
- Cost is **O(pageSize)** regardless of depth for the keyset path.
- Stable under concurrent inserts/deletes (no skips/dupes) for the default ordering.
- sclera-ui asset list becomes **infinite scroll** ("Load more" button + auto-trigger on scroll).

## Non-goals

- Converting every paginated endpoint in the service. Only the 3 the demo calls.
- Removing offset pagination. It stays as the default/fallback.
- An accurate grand-total count from the paginated call (the existing dedicated count endpoints
  remain the source for the header badge).
- Numbered-page / jump-to-page navigation (incompatible with keyset; not needed by the demo).

## Cursor model

An **opaque, stateless cursor**: base64url of a compact JSON object holding the last returned row's
sort key plus a version tag.

```
cursor = base64url(JSON)   where JSON = { "v":1, "t": <effTimestamp:number>, "i": "<id:string>" }
```

- `t` — the effective ordering timestamp of the last row (see per-endpoint ordering below).
- `i` — the last row's `id`, the unique tiebreaker that makes the sort a total order.
- `v` — schema version, so the encoding can evolve.
- The client treats it as opaque and only echoes `nextCursor` back.
- **No server-side state.**

**Validation:** a malformed/undecodable cursor → HTTP 400 (`Invalid cursor`). Cursors are opaque but
validated; we do not silently restart from the beginning (that would mask client bugs).

For `searchsortfilterdevices` with a custom sort column, the cursor carries the sort-column value
instead of a timestamp: `{ "v":1, "s": <sortValue>, "i": "<id>", "d": "asc|desc" }`.

## Response envelope

A purpose-built DTO (not Spring `Page`/`Slice`, which are offset-oriented and would carry a
meaningless total):

```java
public final class CursorPage<T> {
    private List<T> content;
    private String nextCursor;   // null when hasMore == false
    private boolean hasMore;
    private int pageSize;
}
```

**hasMore detection:** the query fetches `pageSize + 1` rows. If `pageSize + 1` come back, `hasMore =
true`, the extra row is trimmed, and `nextCursor` is computed from the last *kept* row. Otherwise
`hasMore = false` and `nextCursor = null`.

## Backward-compatible endpoint changes

Each endpoint gains an optional `cursor` request param:

| `cursor` param | Behaviour | Response |
|---|---|---|
| absent | existing `pageno`/`pagesize` offset path, untouched | unchanged (`Page<T>` envelope as today) |
| present | keyset path | `CursorPage<T>` |

This keeps the real frontend and other consumers working unchanged; only the demo opts into the
cursor path.

### `getsubsystemparentdevicesbypagination`

Backed by the `Device.getSubsystemParentDevicesByPagination` `@NamedNativeQuery`, already ordered:

```
ORDER BY (CASE ?6 WHEN 3 THEN d.updated_timestamp ELSE d.created_timestamp END) DESC, d.id
```

Add a **keyset variant** named query that injects, before the `ORDER BY`:

```
AND ( (CASE WHEN ?6 = 3 THEN d.updated_timestamp ELSE d.created_timestamp END), d.id ) < ( :ct, :ci )
```

(Postgres row-value comparison.) `effTimestamp` for the cursor is the same CASE expression. `LIMIT`
becomes `:pageSize + 1`; no `OFFSET`.

### `getfilterdevice`

Backed by the `Device.getfilterdevices` `@NamedNativeQuery`, which currently has **no `ORDER BY`** —
its offset paging is therefore non-deterministic (a latent bug). The keyset variant imposes a
deterministic order and predicate:

```
ORDER BY d.created_timestamp DESC, d.id
... AND (d.created_timestamp, d.id) < (:ct, :ci)
LIMIT :pageSize + 1
```

This fixes the non-determinism as a side effect of the work.

### `searchsortfilterdevices` (the constrained one)

Dynamic SQL built in `DeviceSearchService` with arbitrary sort columns; its **fuzzy / custom-field
path sorts in Java memory** (`FuzzySearch`, `Comparator`). DB-level keyset is impossible once sorting
happens after the query. Therefore:

- **Standard column sort** (sort resolved in SQL): real keyset on `(sortCol, id)` with a
  direction-aware comparison — `>` for ascending, `<` for descending — and `id` as the tiebreaker.
  Cursor carries `{ s: <sortValue>, i: <id>, d: <dir> }`.
- **Fuzzy / custom-field / any in-memory-sorted path:** keeps **offset** paging (documented in code
  and in the API). Keyset cannot be applied there; attempting it would be incorrect.

The endpoint inspects the resolved sort strategy and chooses keyset vs offset accordingly. When it
falls back to offset, it still returns `CursorPage<T>` for a uniform client contract, encoding the
next offset inside the opaque cursor (`{ "v":1, "o": <nextOffset> }`) so the UI code path is
identical. `hasMore` is derived from the `pageSize + 1` probe in both modes.

## sclera-ui changes

### `services/api.js`

The 3 calls accept an opaque `cursor` and, in cursor mode, return the raw `CursorPage` shape
(`{ content, nextCursor, hasMore }`) rather than unwrapping to a bare array. Add a small helper so
callers get `{ rows, nextCursor, hasMore }` uniformly.

### `pages/AssetPage.jsx`

- Replace the `page` number + Prev/Next with **accumulated `rows` + `cursor` + `hasMore` state**.
- Append the next page's `content` to `rows` on demand.
- **"Load more" button** at the list foot, **also auto-triggered** by an `IntersectionObserver`
  sentinel when scrolled into view (both, per design confirmation).
- Reset `rows`/`cursor`/`hasMore` whenever filter, search text, advanced criteria, or network change.
- Keep the existing count endpoints for the header total badge.

## Edge cases

- **Unique total order:** `id` tiebreaker guarantees determinism even when timestamps/sort values tie.
- **Concurrent inserts:** with `created_timestamp DESC, id` ordering, new rows appear at the head and
  do not affect an in-progress cursor — no skips/dupes.
- **Deleted boundary row:** keyset uses strict inequality (`<`/`>`), not equality, so deleting the
  exact cursor row does not break the next fetch.
- **Empty result / end of data:** `hasMore = false`, `nextCursor = null`; UI hides "Load more".
- **Invalid/tampered cursor:** HTTP 400.
- **Filter change mid-scroll:** UI resets state; stale responses for a superseded query are ignored
  via an "alive"/request-id guard (matching the existing `AssetPage` pattern).

## Testing

Backend:
- `CursorCodecTest` — encode/decode round-trip, version handling, malformed → error.
- Repository/service tests for each endpoint: first page returns `pageSize` rows + `nextCursor`;
  following the cursor returns the next disjoint slice; last page sets `hasMore = false`; an
  inserted head row does not duplicate already-seen rows.
- `searchsortfilterdevices`: column-sort path uses keyset; fuzzy path falls back to offset and still
  returns a valid `CursorPage`.
- Backward-compat: no `cursor` param returns the unchanged offset response.

Frontend:
- Manual: load demo, scroll/Load-more appends disjoint rows, filter change resets, end hides the
  control. (No automated FE test harness in the demo UI.)

## Phasing

1. `CursorPage<T>` + `CursorCodec` + keyset on the two named-query endpoints (covers the demo's
   default list and search).
2. `searchsortfilterdevices` keyset (column-sort path; offset fallback for fuzzy/custom).
3. sclera-ui infinite scroll wiring.

## Risks / constraints

- This is a "verbatim-extracted seed microservice" (CLAUDE.md: do not modernize without a plan). The
  additive, opt-in `cursor` param keeps the change non-breaking and confined to the demo's call paths,
  which satisfies that constraint.
- The named native queries are duplicated (offset variant + keyset variant) rather than parameterised,
  because `@NamedNativeQuery` parameter lists are fixed. Accepted for clarity over cleverness.
