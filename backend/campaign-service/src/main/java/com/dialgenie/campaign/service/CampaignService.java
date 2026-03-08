package com.dialgenie.campaign.service;

import com.dialgenie.campaign.repository.CampaignRepository;
import com.dialgenie.shared.dto.CampaignDTO;
import com.dialgenie.shared.entity.Campaign;
import com.dialgenie.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CampaignService {
    private static final Logger logger = LoggerFactory.getLogger(CampaignService.class);

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public CampaignDTO createCampaign(String organizationId, String createdBy, CampaignDTO campaignDTO) {
        logger.info("Creating campaign: {} for organization: {}", campaignDTO.getName(), organizationId);

        Campaign campaign = Campaign.builder()
                .id(UUID.randomUUID().toString())
                .organizationId(organizationId)
                .createdBy(createdBy)
                .name(campaignDTO.getName())
                .description(campaignDTO.getDescription())
                .status("DRAFT")
                .campaignType(campaignDTO.getCampaignType() != null ? campaignDTO.getCampaignType() : "OUTBOUND")
                .greetingMessage(campaignDTO.getGreetingMessage())
                .closingMessage(campaignDTO.getClosingMessage())
                .voiceConfig(campaignDTO.getVoiceConfig())
                .aiModelVersion("v1.0")
                .totalLeads(0)
                .processedLeads(0)
                .successfulCalls(0)
                .failedCalls(0)
                .build();

        Campaign savedCampaign = campaignRepository.save(campaign);
        logger.info("Campaign created with id: {}", savedCampaign.getId());

        return mapToDTO(savedCampaign);
    }

    public CampaignDTO getCampaignById(String campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
        return mapToDTO(campaign);
    }

    public List<CampaignDTO> getCampaignsByOrganization(String organizationId) {
        List<Campaign> campaigns = campaignRepository.findByOrganizationId(organizationId);
        return campaigns.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CampaignDTO> getActiveCampaigns() {
        List<Campaign> campaigns = campaignRepository.findActiveCampaigns();
        return campaigns.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public CampaignDTO updateCampaignStatus(String campaignId, String status) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        String previousStatus = campaign.getStatus();
        campaign.setStatus(status);
        campaign.setUpdatedAt(LocalDateTime.now());

        if ("ACTIVE".equals(status)) {
            campaign.setStartDate(LocalDateTime.now());
            publishCampaignStartedEvent(campaign);
            logger.info("Campaign {} activated", campaignId);
        } else if ("PAUSED".equals(status) || "COMPLETED".equals(status)) {
            campaign.setEndDate(LocalDateTime.now());
            publishCampaignStoppedEvent(campaign);
            logger.info("Campaign {} stopped with status {}", campaignId, status);
        }

        Campaign updatedCampaign = campaignRepository.save(campaign);
        return mapToDTO(updatedCampaign);
    }

    @Transactional
    public CampaignDTO updateCampaignMetrics(String campaignId, Integer successfulCalls, Integer failedCalls) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        campaign.setProcessedLeads((campaign.getProcessedLeads() != null ? campaign.getProcessedLeads() : 0) + 1);
        campaign.setSuccessfulCalls((campaign.getSuccessfulCalls() != null ? campaign.getSuccessfulCalls() : 0) + (successfulCalls != null ? successfulCalls : 0));
        campaign.setFailedCalls((campaign.getFailedCalls() != null ? campaign.getFailedCalls() : 0) + (failedCalls != null ? failedCalls : 0));
        campaign.setUpdatedAt(LocalDateTime.now());

        Campaign updatedCampaign = campaignRepository.save(campaign);
        logger.debug("Campaign {} metrics updated", campaignId);

        return mapToDTO(updatedCampaign);
    }

    private void publishCampaignStartedEvent(Campaign campaign) {
        try {
            String event = String.format("CAMPAIGN_STARTED:%s", campaign.getId());
            kafkaTemplate.send("campaign-events", campaign.getId(), event);
            logger.debug("Published campaign started event for campaign: {}", campaign.getId());
        } catch (Exception e) {
            logger.error("Failed to publish campaign started event: {}", e.getMessage());
        }
    }

    private void publishCampaignStoppedEvent(Campaign campaign) {
        try {
            String event = String.format("CAMPAIGN_STOPPED:%s", campaign.getId());
            kafkaTemplate.send("campaign-events", campaign.getId(), event);
            logger.debug("Published campaign stopped event for campaign: {}", campaign.getId());
        } catch (Exception e) {
            logger.error("Failed to publish campaign stopped event: {}", e.getMessage());
        }
    }

    private CampaignDTO mapToDTO(Campaign campaign) {
        return CampaignDTO.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .status(campaign.getStatus())
                .campaignType(campaign.getCampaignType())
                .totalLeads(campaign.getTotalLeads())
                .processedLeads(campaign.getProcessedLeads())
                .successfulCalls(campaign.getSuccessfulCalls())
                .failedCalls(campaign.getFailedCalls())
                .greetingMessage(campaign.getGreetingMessage())
                .closingMessage(campaign.getClosingMessage())
                .voiceConfig(campaign.getVoiceConfig())
                .build();
    }
}
