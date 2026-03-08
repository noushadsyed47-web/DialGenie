package com.dialgenie.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns", indexes = {
        @Index(name = "idx_campaigns_organization_id", columnList = "organization_id"),
        @Index(name = "idx_campaigns_status", columnList = "status"),
        @Index(name = "idx_campaigns_created_by", columnList = "created_by")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {
    @Id
    private String id;

    @Column(nullable = false)
    private String organizationId;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(columnDefinition = "VARCHAR(50) DEFAULT 'DRAFT'")
    private String status;

    @Column(columnDefinition = "VARCHAR(50) DEFAULT 'OUTBOUND'")
    private String campaignType;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer totalLeads;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer processedLeads;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer successfulCalls;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer failedCalls;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer avgCallDurationSeconds;

    @Column(columnDefinition = "VARCHAR(50) DEFAULT 'v1.0'")
    private String aiModelVersion;

    @Column(columnDefinition = "TEXT")
    private String voiceConfig;

    private String greetingMessage;
    private String closingMessage;

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
