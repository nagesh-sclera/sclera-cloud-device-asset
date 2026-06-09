package io.sclera.scheduler.service;

/** One active VDMS as returned by vdms-service GET /vdms/active. */
public record VdmsActiveDto(String vdmsId, String timezone) {}
