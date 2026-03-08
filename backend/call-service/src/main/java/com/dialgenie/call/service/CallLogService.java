package com.dialgenie.call.service;

import com.dialgenie.call.repository.CallLogRepository;
import com.dialgenie.shared.dto.CallLogDTO;
import com.dialgenie.shared.entity.CallLog;
import com.dialgenie.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
public class CallLogService {
    private static final Logger logger = LoggerFactory.getLogger(CallLogService.class);

    @Autowired
    private CallLogRepository callLogRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public CallLogDTO createCallLog(String leadId, String campaignId, String organizationId, String phoneNumber) {
        logger.info("Creating call log for lead: {}, campaign: {}", leadId, campaignId);

        CallLog callLog = CallLog.builder()
                .id(UUID.randomUUID().toString())
                .leadId(leadId)
                .campaignId(campaignId)
                .organizationId(organizationId)
                .phoneNumber(phoneNumber)
                .callStatus("INITIATED")
                .callDurationSeconds(0)
                .retryCount(0)
                .startTime(LocalDateTime.now())
                .build();

        CallLog savedCallLog = callLogRepository.save(callLog);
        logger.info("Call log created with id: {}", savedCallLog.getId());

        publishCallInitiatedEvent(savedCallLog);

        return mapToDTO(savedCallLog);
    }

    @Transactional
    public CallLogDTO updateCallStatus(String callLogId, String status) {
        CallLog callLog = callLogRepository.findById(callLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Call log not found with id: " + callLogId));

        callLog.setCallStatus(status);
        callLog.setUpdatedAt(LocalDateTime.now());

        if ("IN_PROGRESS".equals(status)) {
            callLog.setStartTime(LocalDateTime.now());
        } else if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
            callLog.setEndTime(LocalDateTime.now());
            if (callLog.getStartTime() != null) {
                long durationSeconds = java.time.temporal.ChronoUnit.SECONDS.between(callLog.getStartTime(), callLog.getEndTime());
                callLog.setCallDurationSeconds((int) durationSeconds);
            }
        }

        CallLog updatedCallLog = callLogRepository.save(callLog);
        publishCallStatusChangedEvent(updatedCallLog);

        return mapToDTO(updatedCallLog);
    }

    @Transactional
    public CallLogDTO updateTwilioCallSid(String callLogId, String twilioCallSid) {
        CallLog callLog = callLogRepository.findById(callLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Call log not found with id: " + callLogId));

        callLog.setTwilioCallSid(twilioCallSid);
        callLog.setUpdatedAt(LocalDateTime.now());

        CallLog updatedCallLog = callLogRepository.save(callLog);
        
        // Store mapping in Redis for quick lookup
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set("twilio:call:" + twilioCallSid, callLogId);
            } catch (Exception e) {
                logger.warn("Failed to cache Twilio call mapping: {}", e.getMessage());
            }
        }

        return mapToDTO(updatedCallLog);
    }

    @Transactional
    public CallLogDTO updateCallOutcome(String callLogId, String outcome, Float sentimentScore, String sentimentLabel) {
        CallLog callLog = callLogRepository.findById(callLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Call log not found with id: " + callLogId));

        callLog.setOutcome(outcome);
        callLog.setSentimentScore(sentimentScore);
        callLog.setSentimentLabel(sentimentLabel);
        callLog.setUpdatedAt(LocalDateTime.now());

        CallLog updatedCallLog = callLogRepository.save(callLog);
        publishCallCompletedEvent(updatedCallLog);

        return mapToDTO(updatedCallLog);
    }

    @Transactional
    public CallLogDTO scheduleRetry(String callLogId, int delayMinutes) {
        CallLog callLog = callLogRepository.findById(callLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Call log not found with id: " + callLogId));

        callLog.setRetryCount(callLog.getRetryCount() + 1);
        callLog.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
        callLog.setUpdatedAt(LocalDateTime.now());

        CallLog updatedCallLog = callLogRepository.save(callLog);
        logger.info("Scheduled retry for call {} at {}", callLogId, callLog.getNextRetryAt());

        return mapToDTO(updatedCallLog);
    }

    public CallLogDTO getCallLogById(String callLogId) {
        CallLog callLog = callLogRepository.findById(callLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Call log not found with id: " + callLogId));
        return mapToDTO(callLog);
    }

    public CallLogDTO getCallLogByTwilioSid(String twilioCallSid) {
        CallLog callLog = callLogRepository.findByTwilioCallSid(twilioCallSid)
                .orElseThrow(() -> new ResourceNotFoundException("Call log not found for Twilio SID: " + twilioCallSid));
        return mapToDTO(callLog);
    }

    public List<CallLogDTO> getCallHistoryForLead(String leadId) {
        List<CallLog> callLogs = callLogRepository.findByLeadIdOrderByCreatedAtDesc(leadId);
        return callLogs.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CallLogDTO> getCallsReadyForRetry() {
        List<CallLog> callLogs = callLogRepository.findCallsReadyForRetry(LocalDateTime.now());
        return callLogs.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private void publishCallInitiatedEvent(CallLog callLog) {
        try {
            String event = String.format("CALL_INITIATED:%s:%s", callLog.getId(), callLog.getLeadId());
            kafkaTemplate.send("call-events", callLog.getId(), event);
            logger.debug("Published call initiated event for call: {}", callLog.getId());
        } catch (Exception e) {
            logger.error("Failed to publish call initiated event: {}", e.getMessage());
        }
    }

    private void publishCallStatusChangedEvent(CallLog callLog) {
        try {
            String event = String.format("CALL_STATUS_CHANGED:%s:%s", callLog.getId(), callLog.getCallStatus());
            kafkaTemplate.send("call-events", callLog.getId(), event);
        } catch (Exception e) {
            logger.error("Failed to publish call status changed event: {}", e.getMessage());
        }
    }

    private void publishCallCompletedEvent(CallLog callLog) {
        try {
            String event = String.format("CALL_COMPLETED:%s:%s", callLog.getId(), callLog.getOutcome());
            kafkaTemplate.send("call-events", callLog.getId(), event);
            logger.debug("Published call completed event for call: {}", callLog.getId());
        } catch (Exception e) {
            logger.error("Failed to publish call completed event: {}", e.getMessage());
        }
    }

    private CallLogDTO mapToDTO(CallLog callLog) {
        return CallLogDTO.builder()
                .id(callLog.getId())
                .leadId(callLog.getLeadId())
                .campaignId(callLog.getCampaignId())
                .twilioCallSid(callLog.getTwilioCallSid())
                .phoneNumber(callLog.getPhoneNumber())
                .callStatus(callLog.getCallStatus())
                .outcome(callLog.getOutcome())
                .callDurationSeconds(callLog.getCallDurationSeconds())
                .recordingUrl(callLog.getRecordingUrl())
                .sentimentScore(callLog.getSentimentScore())
                .sentimentLabel(callLog.getSentimentLabel())
                .aiConfidenceScore(callLog.getAiConfidenceScore())
                .failureReason(callLog.getFailureReason())
                .retryCount(callLog.getRetryCount())
                .build();
    }
}
