package io.sclera.workorder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for Dapr sidecar communication.
 * Properties live under {@code dapr.*} in application.yml.
 *
 * Defaults match dapr-spring-ak conventions; ports differ from the reference
 * project (3501 for maximo, 3502 for vdms) to allow both sidecars to run on
 * the same host during local development.
 */
@ConfigurationProperties(prefix = "dapr")
public class DaprProperties {

    /** HTTP port of the local Dapr sidecar (set automatically by `dapr run`). */
    private int httpPort = 3501;

    /** Dapr app-id of vdms-service (used for service invocation). */
    private String vdmsAppId = "vdms-service";

    /** Dapr app-id of sclera-cloud-device-asset (used for ticket-sync invocation). */
    private String deviceAssetAppId = "sclera-cloud-device-asset";

    /** Dapr app-id of sclera-scheduler (used to create one-time jobs). */
    private String schedulerAppId = "sclera-scheduler";

    /** Dapr pub/sub component name (must match metadata.name in pubsub.yaml). */
    private String pubsubName = "pubsub";

    /** Dapr pub/sub topic name for audit events published by sclera-cloud-workorder. */
    private String topicName = "user-action-log-events";

    public String baseUrl() {
        return "http://localhost:" + httpPort + "/v1.0";
    }

    public int getHttpPort() { return httpPort; }
    public void setHttpPort(int httpPort) { this.httpPort = httpPort; }

    public String getVdmsAppId() { return vdmsAppId; }
    public void setVdmsAppId(String vdmsAppId) { this.vdmsAppId = vdmsAppId; }

    public String getDeviceAssetAppId() { return deviceAssetAppId; }
    public void setDeviceAssetAppId(String deviceAssetAppId) { this.deviceAssetAppId = deviceAssetAppId; }

    public String getSchedulerAppId() { return schedulerAppId; }
    public void setSchedulerAppId(String schedulerAppId) { this.schedulerAppId = schedulerAppId; }

    public String getPubsubName() { return pubsubName; }
    public void setPubsubName(String pubsubName) { this.pubsubName = pubsubName; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
}
