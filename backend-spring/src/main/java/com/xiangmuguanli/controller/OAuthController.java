package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.dto.response.AuthResponse;
import com.xiangmuguanli.service.DingTalkOAuthService;
import com.xiangmuguanli.service.WeChatOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/oauth")
public class OAuthController {

    private final WeChatOAuthService weChatOAuthService;
    private final DingTalkOAuthService dingTalkOAuthService;

    public OAuthController(WeChatOAuthService weChatOAuthService,
                           DingTalkOAuthService dingTalkOAuthService) {
        this.weChatOAuthService = weChatOAuthService;
        this.dingTalkOAuthService = dingTalkOAuthService;
    }

    @GetMapping("/wechat/url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getWeChatAuthUrl(
            @RequestParam(required = false) String redirectUri) {
        String state = UUID.randomUUID().toString();
        String url = weChatOAuthService.getAuthUrl(state, redirectUri);
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url, "state", state)));
    }

    @GetMapping("/wechat/callback")
    public ResponseEntity<ApiResponse<AuthResponse>> weChatCallback(
            @RequestParam String code,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String device = httpRequest.getHeader("User-Agent");
        AuthResponse response = weChatOAuthService.handleCallback(code, ip, device);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/dingtalk/url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getDingTalkAuthUrl(
            @RequestParam(required = false) String redirectUri) {
        String state = UUID.randomUUID().toString();
        String url = dingTalkOAuthService.getAuthUrl(state, redirectUri);
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url, "state", state)));
    }

    @GetMapping("/dingtalk/callback")
    public ResponseEntity<ApiResponse<AuthResponse>> dingTalkCallback(
            @RequestParam String code,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String device = httpRequest.getHeader("User-Agent");
        AuthResponse response = dingTalkOAuthService.handleCallback(code, ip, device);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
