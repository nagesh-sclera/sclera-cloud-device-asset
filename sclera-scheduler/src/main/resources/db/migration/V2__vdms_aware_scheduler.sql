-- job becomes catalog/template: add scope (GLOBAL | PER_VDMS)
ALTER TABLE job ADD COLUMN scope TEXT NOT NULL DEFAULT 'GLOBAL';

-- run history attributable per VDMS (null for GLOBAL jobs)
ALTER TABLE job_run ADD COLUMN vdms_id TEXT;

-- scheduler's own copy of the active-VDMS list (no cross-schema reads)
CREATE TABLE vdms_registry (
    vdms_id     TEXT PRIMARY KEY,
    timezone    TEXT,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- the per-VDMS runtime unit (pause/resume/snooze/disable/one-shot act on this)
CREATE TABLE job_instance (
    job_name      TEXT NOT NULL REFERENCES job(name),
    vdms_id       TEXT NOT NULL REFERENCES vdms_registry(vdms_id),
    state         TEXT NOT NULL DEFAULT 'ENABLED',
    snooze_until  TIMESTAMPTZ,
    dapr_job_name TEXT NOT NULL,
    next_fire_at  TIMESTAMPTZ,
    last_run_id   UUID,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (job_name, vdms_id)
);

CREATE INDEX idx_job_instance_snooze ON job_instance (state, snooze_until);
