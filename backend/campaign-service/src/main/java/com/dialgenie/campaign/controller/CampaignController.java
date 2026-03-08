package com.dialgenie.campaign.controller;

import com.dialgenie.campaign.service.CampaignService;
import com.dialgenie.shared.dto.ApiResponse;
import com.dialgenie.shared.dto.CampaignDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campaigns")
public class CampaignController {
    private static final Logger logger = LoggerFactory.getLogger(CampaignController.class);

    @Autowired
    private CampaignService campaignService;

    @PostMapping
    public ResponseEntity<?> createCampaign(
            @RequestHeader("X-Organization-Id") String organizationId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CampaignDTO campaignDTO) {
        logger.info("Creating campaign for organization: {}", organizationId);
        CampaignDTO createdCampaign = campaignService.createCampaign(organizationId, userId, campaignDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdCampaign, "Campaign created successfully"));
    }

    @GetMapping("/{campaignId}")
    public ResponseEntity<?> getCampaignById(@PathVariable String campaignId) {
        logger.info("Fetching campaign with id: {}", campaignId);
        CampaignDTO campaign = campaignService.getCampaignById(campaignId);
        return ResponseEntity.ok(ApiResponse.success(campaign, "Campaign retrieved successfully"));
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<?> getCampaignsByOrganization(@PathVariable String organizationId) {
        logger.info("Fetching campaigns for organization: {}", organizationId);
        List<CampaignDTO> campaigns = campaignService.getCampaignsByOrganization(organizationId);
        return ResponseEntity.ok(ApiResponse.success(campaigns, "Campaigns retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<?> getActiveCampaigns() {
        logger.info("Fetching all active campaigns");
        List<CampaignDTO> campaigns = campaignService.getActiveCampaigns();
        return ResponseEntity.ok(ApiResponse.success(campaigns, "Active campaigns retrieved successfully"));
    }

    @PutMapping("/{campaignId}/status")
    public ResponseEntity<?> updateCampaignStatus(
            @PathVariable String campaignId,
            @RequestParam String status) {
        logger.info("Updating campaign {} status to {}", campaignId, status);
        CampaignDTO updatedCampaign = campaignService.updateCampaignStatus(campaignId, status);
        return ResponseEntity.ok(ApiResponse.success(updatedCampaign, "Campaign status updated successfully"));
    }
}
