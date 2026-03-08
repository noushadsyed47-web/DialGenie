package com.dialgenie.campaign.repository;

import com.dialgenie.shared.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, String> {
    
    List<Campaign> findByOrganizationId(String organizationId);
    
    List<Campaign> findByOrganizationIdAndStatus(String organizationId, String status);
    
    @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' ORDER BY c.createdAt DESC")
    List<Campaign> findActiveCampaigns();
}
