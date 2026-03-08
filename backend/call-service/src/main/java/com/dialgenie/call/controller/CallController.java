package com.dialgenie.call.controller;

import com.dialgenie.call.service.CallLogService;
import com.dialgenie.shared.dto.ApiResponse;
import com.dialgenie.shared.dto.CallLogDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calls")
public class CallController {
    private static final Logger logger = LoggerFactory.getLogger(CallController.class);

    @Autowired
    private CallLogService callLogService;

    @PostMapping
    public ResponseEntity<?> initiateCall(
            @RequestHeader("X-Organization-Id") String organizationId,
            @RequestParam String leadId,
            @RequestParam String campaignId,
            @RequestParam String phoneNumber) {
        logger.info("Initiating call for lead: {}", leadId);
        CallLogDTO callLog = callLogService.createCallLog(leadId, campaignId, organizationId, phoneNumber);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(callLog, "Call initiated successfully"));
    }

    @GetMapping("/{callLogId}")
    public ResponseEntity<?> getCallLog(@PathVariable String callLogId) {
        logger.info("Fetching call log: {}", callLogId);
        CallLogDTO callLog = callLogService.getCallLogById(callLogId);
        return ResponseEntity.ok(ApiResponse.success(callLog, "Call log retrieved successfully"));
    }

    @GetMapping("/twilio/{twilioCallSid}")
    public ResponseEntity<?> getCallLogByTwilioSid(@PathVariable String twilioCallSid) {
        logger.info("Fetching call log for Twilio SID: {}", twilioCallSid);
        CallLogDTO callLog = callLogService.getCallLogByTwilioSid(twilioCallSid);
        return ResponseEntity.ok(ApiResponse.success(callLog, "Call log retrieved successfully"));
    }

    @GetMapping("/history/{leadId}")
    public ResponseEntity<?> getCallHistory(@PathVariable String leadId) {
        logger.info("Fetching call history for lead: {}", leadId);
        List<CallLogDTO> callHistory = callLogService.getCallHistoryForLead(leadId);
        return ResponseEntity.ok(ApiResponse.success(callHistory, "Call history retrieved successfully"));
    }

    @PutMapping("/{callLogId}/status")
    public ResponseEntity<?> updateCallStatus(
            @PathVariable String callLogId,
            @RequestParam String status) {
        logger.info("Updating call log {} status to {}", callLogId, status);
        CallLogDTO updatedCall = callLogService.updateCallStatus(callLogId, status);
        return ResponseEntity.ok(ApiResponse.success(updatedCall, "Call status updated successfully"));
    }

    @PutMapping("/{callLogId}/outcome")
    public ResponseEntity<?> updateCallOutcome(
            @PathVariable String callLogId,
            @RequestParam String outcome,
            @RequestParam(required = false) Float sentimentScore,
            @RequestParam(required = false) String sentimentLabel) {
        logger.info("Updating call log {} outcome to {}", callLogId, outcome);
        CallLogDTO updatedCall = callLogService.updateCallOutcome(callLogId, outcome, sentimentScore, sentimentLabel);
        return ResponseEntity.ok(ApiResponse.success(updatedCall, "Call outcome updated successfully"));
    }
}
