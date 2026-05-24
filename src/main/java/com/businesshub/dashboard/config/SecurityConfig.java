package com.businesshub.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/samples/**", "/login", "/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/leads/webhook").permitAll()
                        .requestMatchers("/api/users/account/password").hasAnyRole("ADMIN", "OPS")
                        .requestMatchers("/integrations", "/api/integrations/**").hasAnyRole("ADMIN", "OPS")
                        .requestMatchers("/reports", "/api/reports", "/admin/**", "/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/account/**", "/api/account/**").hasAnyRole("ADMIN", "OPS")
                        .requestMatchers("/dashboard", "/", "/api/dashboard", "/api/leads/**", "/api/invoices/**")
                        .hasAnyRole("ADMIN", "OPS")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/leads/webhook")
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
