package io.sclera.scheduler.domain;

/** Spring Data projection: instance count per (jobName, state). */
public interface JobInstanceStateCount {
    String getJobName();
    JobInstanceState getState();
    long getCnt();
}
