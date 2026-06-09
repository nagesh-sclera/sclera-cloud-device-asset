package io.sclera.scheduler.client;

/**
 * A job registration request. schedule is a cron expression (e.g. "0 0 0 * * *") or "@every 90s".
 * timezone is an IANA zone applied to cron schedules (null = UTC; ignored for "@every").
 */
public record JobSchedule(String name, String schedule, String timezone) {
    public JobSchedule(String name, String schedule) {
        this(name, schedule, null);
    }
}
