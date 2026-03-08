package com.dialgenie.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "VARCHAR(50) DEFAULT 'STARTER'")
    private String subscriptionTier;

    @Column(columnDefinition = "INTEGER DEFAULT 10000")
    private Integer apiCallsLimit;

    @Column(columnDefinition = "INTEGER DEFAULT 5")
    private Integer concurrentCallsLimit;

    @Column(columnDefinition = "INTEGER DEFAULT 10")
    private Integer storageLimitGb;

    @Column(columnDefinition = "BOOLEAN DEFAULT 1")
    private Boolean isActive = true;

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
