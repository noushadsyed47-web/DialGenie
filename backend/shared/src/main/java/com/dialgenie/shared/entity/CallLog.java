package com.dialgenie.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_logs", indexes = {
        @Index(name = "idx_call_logs_lead_id", columnList = "lead_id"),
        @Index(name = "idx_call_logs_campaign_id", columnList = "campaign_id"),
        @Index(name = "idx_call_logs_status", columnList = "call_status"),
        @Index(name = "idx_call_logs_twilio_call_sid", columnList = "twilio_call_sid"),
        @Index(name = "idx_call_logs_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallLog {
    @Id
    private String id;

    @Column(nullable = false)
    private String leadId;

    @Column(nullable = false)
    private String campaignId;

    @Column(nullable = false)
    private String organizationId;

    @Column(unique = true)
    private String twilioCallSid;

    @Column(nullable = false)
    private String phoneNumber;

    private String callStatus;
    private String outcome;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer callDurationSeconds;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String recordingUrl;
    private String transcriptId;

    private Float sentimentScore;
    private String sentimentLabel;
    private Float aiConfidenceScore;
    private String failureReason;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer retryCount;

    private LocalDateTime nextRetryAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
