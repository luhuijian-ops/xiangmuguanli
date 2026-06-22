package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.request.LoginRequest;
import com.xiangmuguanli.dto.request.OAuthLoginRequest;
import com.xiangmuguanli.dto.request.RefreshTokenRequest;
import com.xiangmuguanli.dto.request.RegisterRequest;
import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.dto.response.AuthResponse;
import com.xiangmuguanli.dto.response.UserResponse;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.service.AuthService;
import com.xiangmuguanli.service.DingTalkOAuthService;
import com.xiangmuguanli.service.WeChatOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final WeChatOAuthService weChatOAuthService;
    private final DingTalkOAuthService dingTalkOAuthService;

    public AuthController(AuthService authService,
                          WeChatOAuthService weChatOAuthService,
                          DingTalkOAuthService dingTalkOAuthService) {
        this.authService = authService;
        this.weChatOAuthService = weChatOAuthService;
        this.dingTalkOAuthService = dingTalkOAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String device = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.login(request, ip, device);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(UserResponse.fromEntity(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        authService.logout(authHeader, user);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/wechat/login")
    public ResponseEntity<ApiResponse<AuthResponse>> wechatLogin(
            @Valid @RequestBody OAuthLoginRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String device = httpRequest.getHeader("User-Agent");
        AuthResponse response = weChatOAuthService.handleCallback(request.getCode(), ip, device, request.getState());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/dingtalk/login")
    public ResponseEntity<ApiResponse<AuthResponse>> dingtalkLogin(
            @Valid @RequestBody OAuthLoginRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String device = httpRequest.getHeader("User-Agent");
        AuthResponse response = dingTalkOAuthService.handleCallback(request.getCode(), ip, device, request.getState());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
