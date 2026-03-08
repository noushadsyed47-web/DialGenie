package com.dialgenie.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "transcripts", indexes = {
        @Index(name = "idx_transcripts_call_log_id", columnList = "call_log_id"),
        @Index(name = "idx_transcripts_lead_id", columnList = "lead_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transcript {
    @Id
    private String id;

    @Column(nullable = false)
    private String callLogId;

    @Column(nullable = false)
    private String leadId;

    @Column(nullable = false)
    private String organizationId;

    @Column(columnDefinition = "TEXT")
    private String fullTranscript;

    @Column(columnDefinition = "TEXT")
    private String conversationSegments;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer aiResponseCount;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer userResponseCount;

    @Column(columnDefinition = "TEXT")
    private String objectionsRaised;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer objectionsHandled;

    private String intentDetected;

    @Column(columnDefinition = "TEXT")
    private String keyPoints;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
