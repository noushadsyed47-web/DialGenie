package com.dialgenie.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDTO {
    private String id;

    @NotBlank(message = "Campaign name is required")
    private String name;

    private String description;
    private String status;
    private String campaignType;
    private String startDate;
    private String endDate;
    private Integer totalLeads;
    private Integer processedLeads;
    private Integer successfulCalls;
    private Integer failedCalls;
    private String greetingMessage;
    private String closingMessage;
    private String voiceConfig;
}
