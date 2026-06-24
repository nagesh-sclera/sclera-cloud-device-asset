package io.sclera.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@EnableScheduling
@Service
@Slf4j
public class Scheduler {

    @Autowired
    private SchedulerService schedulerService;

    @Scheduled(fixedRate = 86400000)//to run every 24 hours From time When Server Started
    public void scheduleTaskWithFixedRate() {
        schedulerService.deleteQrCodeFromAwsAndDb();
    }


    @Scheduled(fixedRate = 86400000)//to run every 24 hours From time When Server Started
    public void scheduleTaskForExportDownload() {
        schedulerService.deleteExportFileFromAws();
    }

    @Scheduled(fixedRate = 1800000)
    public void scheduleTaskForOfflineVdmsAlerts() {
        schedulerService.scheduleTaskForOfflineVdmsAlerts();
    }

}
