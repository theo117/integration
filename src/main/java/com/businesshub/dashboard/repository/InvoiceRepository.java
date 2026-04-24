package com.businesshub.dashboard.repository;

import com.businesshub.dashboard.domain.Invoice;
import com.businesshub.dashboard.domain.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    long countByStatus(InvoiceStatus status);
    List<Invoice> findTop8ByOrderByCreatedAtDesc();
    List<Invoice> findByStatusNotAndDueDateBefore(InvoiceStatus status, LocalDate dueDate);
}
