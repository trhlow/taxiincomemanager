package com.taxiincome.schedule.dto;

/**
 * Result of idempotent schedule upsert: {@code created} is true only when a new row was inserted
 * in this request (HTTP 201); false when the row already existed or a concurrent insert won (HTTP 200).
 */
public record ScheduleUpsertResult(boolean created, ScheduleResponse schedule) {
}
