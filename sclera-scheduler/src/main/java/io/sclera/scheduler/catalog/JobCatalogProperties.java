package io.sclera.scheduler.catalog;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "scheduler.catalog")
public record JobCatalogProperties(List<Entry> jobs) {

    public record Entry(String name, String schedule, String owner, String triggerTopic) {
        public Entry {
            if (triggerTopic == null) triggerTopic = "scheduler.trigger";
        }
    }
}
