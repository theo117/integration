package com.businesshub.dashboard;

import com.businesshub.dashboard.config.EmailAutomationProperties;
import com.businesshub.dashboard.config.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableConfigurationProperties({EmailAutomationProperties.class, SecurityProperties.class})
public class IntegrationDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationDashboardApplication.class, args);
    }
}
