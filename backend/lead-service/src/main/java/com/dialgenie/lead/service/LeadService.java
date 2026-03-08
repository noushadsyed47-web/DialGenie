package com.dialgenie.lead.service;

import com.dialgenie.lead.repository.LeadRepository;
import com.dialgenie.shared.dto.LeadDTO;
import com.dialgenie.shared.entity.Lead;
import com.dialgenie.shared.exception.DuplicateResourceException;
import com.dialgenie.shared.exception.InvalidInputException;
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
public class LeadService {
    private static final Logger logger = LoggerFactory.getLogger(LeadService.class);

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public LeadDTO createLead(String organizationId, LeadDTO leadDTO) {
        logger.info("Creating lead for campaign: {}", leadDTO.getCampaignId());

        // Validate phone number format
        if (!isValidPhoneNumber(leadDTO.getPhoneNumber())) {
            throw new InvalidInputException("Invalid phone number format");
        }

        // Check for duplicates
        leadRepository.findByPhoneNumberAndOrganizationId(leadDTO.getPhoneNumber(), organizationId)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Lead with phone number already exists: " + leadDTO.getPhoneNumber());
                });

        Lead lead = Lead.builder()
                .id(UUID.randomUUID().toString())
                .campaignId(leadDTO.getCampaignId())
                .organizationId(organizationId)
                .name(leadDTO.getName())
                .phoneNumber(leadDTO.getPhoneNumber())
                .email(leadDTO.getEmail())
                .concern(leadDTO.getConcern())
                .status("PENDING")
                .dndFlag(false)
                .totalAttempts(0)
                .build();

        Lead savedLead = leadRepository.save(lead);
        logger.info("Lead created with id: {}", savedLead.getId());

        // Publish event to Kafka for call scheduling
        publishLeadCreatedEvent(savedLead);

        return mapToDTO(savedLead);
    }

    @Transactional
    public List<LeadDTO> uploadLeads(String organizationId, String campaignId, List<LeadDTO> leads) {
        logger.info("Uploading {} leads for campaign: {}", leads.size(), campaignId);

        List<LeadDTO> createdLeads = leads.stream()
                .map(lead -> {
                    lead.setCampaignId(campaignId);
                    return createLead(organizationId, lead);
                })
                .collect(Collectors.toList());

        logger.info("Successfully uploaded {} leads", createdLeads.size());
        return createdLeads;
    }

    public LeadDTO getLeadById(String leadId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));
        return mapToDTO(lead);
    }

    public List<LeadDTO> getLeadsByCampaign(String campaignId) {
        List<Lead> leads = leadRepository.findByCampaignId(campaignId);
        return leads.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<LeadDTO> getLeadsByStatus(String campaignId, String status) {
        List<Lead> leads = leadRepository.findByCampaignIdAndStatus(campaignId, status);
        return leads.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public LeadDTO updateLeadStatus(String leadId, String status) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));

        lead.setStatus(status);
        lead.setUpdatedAt(LocalDateTime.now());

        if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
            lead.setLastCalledAt(LocalDateTime.now());
        }

        Lead updatedLead = leadRepository.save(lead);
        logger.info("Lead {} status updated to {}", leadId, status);

        return mapToDTO(updatedLead);
    }

    @Transactional
    public LeadDTO updateLeadSolution(String leadId, String solution) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));

        lead.setSolution(solution);
        lead.setUpdatedAt(LocalDateTime.now());

        Lead updatedLead = leadRepository.save(lead);
        logger.info("Lead {} solution updated", leadId);

        return mapToDTO(updatedLead);
    }

    @Transactional
    public void incrementAttemptCount(String leadId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));

        lead.setTotalAttempts(lead.getTotalAttempts() + 1);
        leadRepository.save(lead);
    }

    public List<LeadDTO> getPendingLeads(String campaignId) {
        return getLeadsByStatus(campaignId, "PENDING");
    }

    private void publishLeadCreatedEvent(Lead lead) {
        try {
            String event = String.format("LEAD_CREATED:%s:%s", lead.getId(), lead.getCampaignId());
            kafkaTemplate.send("lead-events", lead.getId(), event);
            logger.debug("Published lead created event for lead: {}", lead.getId());
        } catch (Exception e) {
            logger.error("Failed to publish lead created event: {}", e.getMessage());
        }
    }

    private LeadDTO mapToDTO(Lead lead) {
        return LeadDTO.builder()
                .id(lead.getId())
                .campaignId(lead.getCampaignId())
                .name(lead.getName())
                .phoneNumber(lead.getPhoneNumber())
                .email(lead.getEmail())
                .concern(lead.getConcern())
                .solution(lead.getSolution())
                .leadScore(lead.getLeadScore())
                .status(lead.getStatus())
                .dndFlag(lead.getDndFlag())
                .totalAttempts(lead.getTotalAttempts())
                .build();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^[+]?[0-9]{10,15}$");
    }
}
