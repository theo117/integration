package com.businesshub.dashboard.web.api;

import com.businesshub.dashboard.service.AppUserService;
import com.businesshub.dashboard.web.request.ChangePasswordRequest;
import com.businesshub.dashboard.web.request.CreateUserRequest;
import com.businesshub.dashboard.web.request.ResetUserPasswordRequest;
import com.businesshub.dashboard.web.request.UpdateUserStatusRequest;
import com.businesshub.dashboard.web.response.ApiResponseMapper;
import com.businesshub.dashboard.web.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserManagementApiController {

    private final AppUserService appUserService;
    private final ApiResponseMapper apiResponseMapper;

    public UserManagementApiController(AppUserService appUserService, ApiResponseMapper apiResponseMapper) {
        this.appUserService = appUserService;
        this.apiResponseMapper = apiResponseMapper;
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return appUserService.getAllUsers().stream()
                .map(apiResponseMapper::toUserResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return apiResponseMapper.toUserResponse(appUserService.createUser(request));
    }

    @PatchMapping("/{userId}/status")
    public UserResponse updateUserStatus(@PathVariable Long userId,
                                         @Valid @RequestBody UpdateUserStatusRequest request,
                                         Principal principal) {
        return apiResponseMapper.toUserResponse(
                appUserService.updateUserStatus(userId, request.isActive(), principal.getName())
        );
    }

    @PatchMapping("/{userId}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable Long userId,
                              @Valid @RequestBody ResetUserPasswordRequest request) {
        appUserService.resetPassword(userId, request.getNewPassword());
    }

    @PostMapping("/account/password")
    public Map<String, String> changeOwnPassword(@Valid @RequestBody ChangePasswordRequest request,
                                                 Principal principal) {
        appUserService.changeOwnPassword(principal.getName(), request.getCurrentPassword(), request.getNewPassword());
        return Map.of("message", "Password updated successfully");
    }
}
