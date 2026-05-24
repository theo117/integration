package com.businesshub.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "businesshub.integration")
public class IntegrationProperties {

    private String webhookApiKey = "local-webhook-key";

    public String getWebhookApiKey() {
        return webhookApiKey;
    }

    public void setWebhookApiKey(String webhookApiKey) {
        this.webhookApiKey = webhookApiKey;
    }
}
