package com.dialgenie.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads", indexes = {
        @Index(name = "idx_leads_campaign_id", columnList = "campaign_id"),
        @Index(name = "idx_leads_phone_number", columnList = "phone_number"),
        @Index(name = "idx_leads_status", columnList = "status"),
        @Index(name = "idx_leads_organization_id", columnList = "organization_id"),
        @Index(name = "idx_leads_email", columnList = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {
    @Id
    private String id;

    @Column(nullable = false)
    private String campaignId;

    @Column(nullable = false)
    private String organizationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    private String email;
    private String concern;
    private String solution;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer leadScore;

    @Column(columnDefinition = "VARCHAR(50) DEFAULT 'PENDING'")
    private String status;

    @Column(columnDefinition = "BOOLEAN DEFAULT 0")
    private Boolean dndFlag;

    private LocalDateTime lastCalledAt;
    private LocalDateTime nextCallScheduledAt;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer totalAttempts;

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
