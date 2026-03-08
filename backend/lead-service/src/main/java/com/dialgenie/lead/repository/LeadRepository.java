package com.dialgenie.lead.repository;

import com.dialgenie.shared.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, String> {
    
    List<Lead> findByCampaignIdAndStatus(String campaignId, String status);
    
    List<Lead> findByCampaignId(String campaignId);
    
    Optional<Lead> findByPhoneNumberAndOrganizationId(String phoneNumber, String organizationId);
    
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.campaignId = ?1 AND l.status = 'COMPLETED'")
    Long countCompletedLeads(String campaignId);
    
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.campaignId = ?1 AND l.status = 'FAILED'")
    Long countFailedLeads(String campaignId);
    
    List<Lead> findByOrganizationIdAndStatus(String organizationId, String status);
}
