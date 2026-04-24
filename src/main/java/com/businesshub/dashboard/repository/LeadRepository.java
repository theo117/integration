package com.businesshub.dashboard.repository;

import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    long countByStatus(LeadStatus status);
    List<Lead> findTop8ByOrderByCreatedAtDesc();
}
