package com.dialgenie.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallLogDTO {
    private String id;
    private String leadId;
    private String campaignId;
    private String twilioCallSid;
    private String phoneNumber;
    private String callStatus;
    private String outcome;
    private Integer callDurationSeconds;
    private String startTime;
    private String endTime;
    private String recordingUrl;
    private String transcriptId;
    private Float sentimentScore;
    private String sentimentLabel;
    private Float aiConfidenceScore;
    private String failureReason;
    private Integer retryCount;
}
