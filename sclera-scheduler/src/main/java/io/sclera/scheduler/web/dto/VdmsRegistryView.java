package io.sclera.scheduler.web.dto;

public record VdmsRegistryView(String vdmsId, String timezone, boolean active, long jobCount) {}
