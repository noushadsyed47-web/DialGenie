package com.dialgenie.lead.controller;

import com.dialgenie.lead.service.LeadService;
import com.dialgenie.shared.dto.ApiResponse;
import com.dialgenie.shared.dto.LeadDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {
    private static final Logger logger = LoggerFactory.getLogger(LeadController.class);

    @Autowired
    private LeadService leadService;

    @PostMapping
    public ResponseEntity<?> createLead(
            @RequestHeader("X-Organization-Id") String organizationId,
            @Valid @RequestBody LeadDTO leadDTO) {
        logger.info("Creating lead for organization: {}", organizationId);
        LeadDTO createdLead = leadService.createLead(organizationId, leadDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdLead, "Lead created successfully"));
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<?> getLeadById(@PathVariable String leadId) {
        logger.info("Fetching lead with id: {}", leadId);
        LeadDTO lead = leadService.getLeadById(leadId);
        return ResponseEntity.ok(ApiResponse.success(lead, "Lead retrieved successfully"));
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<?> getLeadsByCampaign(@PathVariable String campaignId) {
        logger.info("Fetching leads for campaign: {}", campaignId);
        List<LeadDTO> leads = leadService.getLeadsByCampaign(campaignId);
        return ResponseEntity.ok(ApiResponse.success(leads, "Leads retrieved successfully"));
    }

    @GetMapping("/campaign/{campaignId}/status/{status}")
    public ResponseEntity<?> getLeadsByStatus(
            @PathVariable String campaignId,
            @PathVariable String status) {
        logger.info("Fetching leads for campaign: {} with status: {}", campaignId, status);
        List<LeadDTO> leads = leadService.getLeadsByStatus(campaignId, status);
        return ResponseEntity.ok(ApiResponse.success(leads, "Leads retrieved successfully"));
    }

    @PutMapping("/{leadId}/status")
    public ResponseEntity<?> updateLeadStatus(
            @PathVariable String leadId,
            @RequestParam String status) {
        logger.info("Updating lead {} status to {}", leadId, status);
        LeadDTO updatedLead = leadService.updateLeadStatus(leadId, status);
        return ResponseEntity.ok(ApiResponse.success(updatedLead, "Lead status updated successfully"));
    }

    @PutMapping("/{leadId}/solution")
    public ResponseEntity<?> updateLeadSolution(
            @PathVariable String leadId,
            @RequestBody String solution) {
        logger.info("Updating lead {} solution", leadId);
        LeadDTO updatedLead = leadService.updateLeadSolution(leadId, solution);
        return ResponseEntity.ok(ApiResponse.success(updatedLead, "Lead solution updated successfully"));
    }

    @GetMapping("/campaign/{campaignId}/pending")
    public ResponseEntity<?> getPendingLeads(@PathVariable String campaignId) {
        logger.info("Fetching pending leads for campaign: {}", campaignId);
        List<LeadDTO> leads = leadService.getPendingLeads(campaignId);
        return ResponseEntity.ok(ApiResponse.success(leads, "Pending leads retrieved successfully"));
    }
}
