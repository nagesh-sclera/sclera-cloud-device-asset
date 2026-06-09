package io.sclera.scheduler.domain;

import java.io.Serializable;
import java.util.Objects;

/** Composite primary key for JobInstanceEntity: (jobName, vdmsId). */
public class JobInstanceId implements Serializable {

    private String jobName;
    private String vdmsId;

    public JobInstanceId() {}

    public JobInstanceId(String jobName, String vdmsId) {
        this.jobName = jobName;
        this.vdmsId = vdmsId;
    }

    public String getJobName() { return jobName; }
    public String getVdmsId() { return vdmsId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobInstanceId that)) return false;
        return Objects.equals(jobName, that.jobName) && Objects.equals(vdmsId, that.vdmsId);
    }

    @Override
    public int hashCode() { return Objects.hash(jobName, vdmsId); }
}
