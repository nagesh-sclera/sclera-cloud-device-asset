CREATE TABLE job (
    name           TEXT PRIMARY KEY,
    schedule       TEXT NOT NULL,
    owner          TEXT NOT NULL,
    trigger_topic  TEXT NOT NULL DEFAULT 'scheduler.trigger',
    state          TEXT NOT NULL DEFAULT 'ENABLED',
    last_run_id    UUID,
    next_fire_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE job_run (
    run_id       UUID PRIMARY KEY,
    job_name     TEXT NOT NULL REFERENCES job(name),
    status       TEXT NOT NULL,
    manual       BOOLEAN NOT NULL DEFAULT FALSE,
    fired_at     TIMESTAMPTZ NOT NULL,
    finished_at  TIMESTAMPTZ,
    duration_ms  BIGINT,
    error        TEXT
);

CREATE INDEX idx_job_run_job_fired ON job_run (job_name, fired_at DESC);
CREATE INDEX idx_job_run_status ON job_run (status);
