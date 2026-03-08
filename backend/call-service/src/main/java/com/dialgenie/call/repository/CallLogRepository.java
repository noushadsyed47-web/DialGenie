package com.dialgenie.call.repository;

import com.dialgenie.shared.entity.CallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CallLogRepository extends JpaRepository<CallLog, String> {
    
    List<CallLog> findByLeadIdOrderByCreatedAtDesc(String leadId);
    
    Optional<CallLog> findByTwilioCallSid(String twilioCallSid);
    
    List<CallLog> findByCampaignIdAndCallStatusOrderByCreatedAtDesc(String campaignId, String callStatus);
    
    @Query("SELECT c FROM CallLog c WHERE c.campaignId = ?1 AND c.callStatus = 'IN_PROGRESS'")
    List<CallLog> findOngoingCalls(String campaignId);
    
    @Query("SELECT COUNT(c) FROM CallLog c WHERE c.campaignId = ?1 AND c.outcome = 'INTERESTED'")
    Long countInterestedLeads(String campaignId);
    
    @Query("SELECT c FROM CallLog c WHERE c.nextRetryAt IS NOT NULL AND c.nextRetryAt <= ?1 AND c.retryCount < 3")
    List<CallLog> findCallsReadyForRetry(LocalDateTime now);
}
