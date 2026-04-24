package com.businesshub.dashboard.web.request;

import com.businesshub.dashboard.domain.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateInvoiceStatusRequest {

    @NotNull
    private InvoiceStatus status;

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }
}
