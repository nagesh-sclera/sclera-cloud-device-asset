package io.sclera.scheduler.web.dto;

public record JobInstanceView(
    String jobName,
    String vdmsId,
    String state,
    String snoozeUntil,
    String nextFireAt) {}
