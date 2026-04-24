package com.businesshub.dashboard.web;

import com.businesshub.dashboard.domain.InvoiceStatus;
import com.businesshub.dashboard.domain.LeadSource;
import com.businesshub.dashboard.domain.LeadStatus;
import com.businesshub.dashboard.domain.UserRole;
import com.businesshub.dashboard.service.AppUserService;
import com.businesshub.dashboard.service.DashboardService;
import com.businesshub.dashboard.service.ReportingService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    private final AppUserService appUserService;
    private final DashboardService dashboardService;
    private final ReportingService reportingService;

    public DashboardController(AppUserService appUserService,
                               DashboardService dashboardService,
                               ReportingService reportingService) {
        this.appUserService = appUserService;
        this.dashboardService = dashboardService;
        this.reportingService = reportingService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("dashboard", dashboardService.getDashboardView());
        model.addAttribute("leadStatuses", LeadStatus.values());
        model.addAttribute("leadSources", LeadSource.values());
        model.addAttribute("invoiceStatuses", InvoiceStatus.values());
        addShellContext(model, authentication);
        return "dashboard";
    }

    @GetMapping("/reports")
    public String reports(Model model, Authentication authentication) {
        model.addAttribute("reporting", reportingService.getReportingView());
        addShellContext(model, authentication);
        return "reports";
    }

    @GetMapping("/admin/users")
    public String users(Model model, Authentication authentication) {
        var users = appUserService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("userRoles", UserRole.values());
        model.addAttribute("activeUsers", appUserService.getActiveUserCount());
        model.addAttribute("adminUsers", users.stream().filter(user -> user.getRole() == UserRole.ADMIN).count());
        model.addAttribute("opsUsers", users.stream().filter(user -> user.getRole() == UserRole.OPS).count());
        addShellContext(model, authentication);
        return "users";
    }

    @GetMapping("/account/security")
    public String accountSecurity(Model model, Authentication authentication) {
        addShellContext(model, authentication);
        return "account-security";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        model.addAttribute("hasError", error != null);
        model.addAttribute("loggedOut", logout != null);
        return "login";
    }

    private void addShellContext(Model model, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUsername", authentication != null ? authentication.getName() : "");
    }
}
