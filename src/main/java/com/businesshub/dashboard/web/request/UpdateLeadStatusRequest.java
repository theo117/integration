package com.businesshub.dashboard.web.request;

import com.businesshub.dashboard.domain.LeadStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateLeadStatusRequest {

    @NotNull
    private LeadStatus status;

    public LeadStatus getStatus() {
        return status;
    }

    public void setStatus(LeadStatus status) {
        this.status = status;
    }
}
