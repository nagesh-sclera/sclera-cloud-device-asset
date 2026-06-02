package io.sclera.scheduler.component;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Layer-3 component test. Proves the full fire -> trigger -> result loop and exactly-once
 * firing against a LIVE stack (Dapr Scheduler control plane + Redis + Postgres). Disabled
 * by default so the normal build stays hermetic; the steps below are the current manual
 * acceptance procedure (Task 15 of the implementation plan).
 *
 * Prerequisites: daprd >= 1.15 with the Scheduler control plane running (the rest of the
 * platform pins 1.12.0 — see dapr/APP_IDS.md). docker-compose.yml wires this for the
 * scheduler service only.
 *
 * Manual run:
 *   1) docker compose up -d postgres redis dapr-scheduler
 *   2) Start the app with its sidecar (compose: docker compose up -d sclera-scheduler
 *      sclera-scheduler-dapr), or locally:
 *        dapr run --app-id sclera-scheduler --app-port 8098 --dapr-http-port 3500 \
 *          --scheduler-host-address localhost:50006 \
 *          --resources-path sclera-cloud-device-asset/dapr/components -- ./mvnw spring-boot:run
 *
 * Verify (no automated infra needed):
 *   - Register a fast probe job through the sidecar Jobs API:
 *       curl -X POST localhost:3500/v1.0-alpha1/jobs/pingTest \
 *         -H 'Content-Type: application/json' \
 *         -d '{"schedule":"@every 5s","data":{"jobName":"pingTest"}}'
 *     (This is exactly what SchedulerClient.schedule() issues — confirm the body shape is
 *      accepted by your Dapr version; adjust SchedulerClient only if it differs.)
 *   - Watch the service log: POST /job/pingTest every ~5s; a scheduler.trigger is published.
 *   - GET localhost:8098/api/jobs -> pingTest shows recent FIRED runs (last fired updates).
 *   - Close the loop by publishing a result (simulating the owning service):
 *       curl -X POST localhost:3500/v1.0/publish/pubsub/scheduler.result \
 *         -H 'Content-Type: application/json' \
 *         -d '{"jobName":"pingTest","runId":"<runId-from-/api/jobs/pingTest/runs>",
 *              "status":"SUCCESS","durationMs":10,"error":null}'
 *   - GET localhost:8098/api/jobs/pingTest/runs -> that run flips to SUCCESS with duration.
 *   - Controls: POST /api/jobs/pingTest/pause stops the fires; /resume re-arms; /run fires
 *     immediately (manual run row).
 *   - Clean up: curl -X DELETE localhost:3500/v1.0-alpha1/jobs/pingTest
 *
 * Exactly-once check: start a SECOND scheduler replica (app-port 8099, SAME app-id) pointed
 * at the same Scheduler control plane. Confirm each tick produces exactly ONE job_run row,
 * not two — the control plane distributes each trigger to a single instance.
 *
 * DLQ check: force a permanent failure (publish a scheduler.result with status "BOGUS" or a
 * malformed runId) and confirm the message lands on scheduler.result.dlq rather than looping.
 */
@Disabled("Requires a live Dapr Scheduler control plane (daprd >= 1.15) + Redis + Postgres; run manually")
class SchedulerFlowComponentTest {

    @Test
    void triggerToResultLoop() {
        // Placeholder for an automated harness once a Dapr-Scheduler Testcontainer is wired.
        // The manual procedure in the class javadoc is the current acceptance gate.
    }
}
