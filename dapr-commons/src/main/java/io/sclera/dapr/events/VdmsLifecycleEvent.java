package io.sclera.dapr.events;

/**
 * Published by the VDMS-owning activation flow when a VDMS becomes active or is
 * deactivated. The scheduler subscribes to register/tear down that VDMS's per-VDMS jobs.
 * status is "ACTIVATED" or "DEACTIVATED". timezone is an IANA zone (may be null → UTC).
 */
public record VdmsLifecycleEvent(String vdmsId, String timezone, String status) {}
