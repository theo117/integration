package com.businesshub.dashboard.repository;

import com.businesshub.dashboard.domain.IntegrationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntegrationEventRepository extends JpaRepository<IntegrationEvent, Long> {
    List<IntegrationEvent> findTop25ByOrderByCreatedAtDesc();
}
