package io.sclera.scheduler.client;

// A job registration request. schedule is a cron ("0 0 */3 * * *") or "@every 90s".
public record JobSchedule(String name, String schedule) {}
