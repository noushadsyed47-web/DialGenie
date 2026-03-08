package com.dialgenie.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDTO {
    private String id;
    private String campaignId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Email(message = "Email should be valid")
    private String email;

    private String concern;
    private String solution;
    private Integer leadScore;
    private String status;
    private Boolean dndFlag;
    private String lastCalledAt;
    private Integer totalAttempts;
}
