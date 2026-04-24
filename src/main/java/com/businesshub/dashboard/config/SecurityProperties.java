package com.businesshub.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "businesshub.security")
public class SecurityProperties {

    private final User admin = new User("admin", "Admin@123", "ADMIN");
    private final User ops = new User("ops", "Ops@123", "OPS");

    public User getAdmin() {
        return admin;
    }

    public User getOps() {
        return ops;
    }

    public static class User {
        private String username;
        private String password;
        private String role;

        public User() {
        }

        public User(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
