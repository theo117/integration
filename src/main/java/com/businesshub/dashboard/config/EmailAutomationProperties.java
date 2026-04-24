package com.businesshub.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "businesshub.email")
public class EmailAutomationProperties {

    private boolean enabled;
    private String from = "noreply@businesshub.local";
    private String opsInbox = "ops@businesshub.local";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getOpsInbox() {
        return opsInbox;
    }

    public void setOpsInbox(String opsInbox) {
        this.opsInbox = opsInbox;
    }
}
