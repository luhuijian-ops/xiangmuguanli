package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.dto.response.AuthResponse;
import com.xiangmuguanli.exception.BadRequestException;
import com.xiangmuguanli.service.DingTalkOAuthService;
import com.xiangmuguanli.service.SystemConfigService;
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
    private final SystemConfigService systemConfigService;

    public OAuthController(WeChatOAuthService weChatOAuthService,
                           DingTalkOAuthService dingTalkOAuthService,
                           SystemConfigService systemConfigService) {
        this.weChatOAuthService = weChatOAuthService;
        this.dingTalkOAuthService = dingTalkOAuthService;
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("/wechat/url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getWeChatAuthUrl(
            @RequestParam(required = false) String redirectUri) {
        String state = "wechat_" + UUID.randomUUID();
        String url = weChatOAuthService.getAuthUrl(state, redirectUri);
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url, "state", state)));
    }

    @GetMapping("/wechat/callback")
    public ResponseEntity<ApiResponse<AuthResponse>> weChatCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String device = httpRequest.getHeader("User-Agent");
        AuthResponse response = weChatOAuthService.handleCallback(code, ip, device, state);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/dingtalk/url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getDingTalkAuthUrl(
            @RequestParam(required = false) String redirectUri) {
        // 开关关闭时拒绝发起钉钉授权，避免前端被绕过直接调用
        if (!systemConfigService.isDingTalkLoginEnabled()) {
            throw new BadRequestException("钉钉登录已关闭");
        }
        String state = "dingtalk_" + UUID.randomUUID();
        String url = dingTalkOAuthService.getAuthUrl(state, redirectUri);
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url, "state", state)));
    }

    @GetMapping("/dingtalk/callback")
    public ResponseEntity<ApiResponse<AuthResponse>> dingTalkCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest httpRequest) {
        // 开关关闭时拒绝处理钉钉回调，防止已发出的授权链接在关闭后仍可登录
        if (!systemConfigService.isDingTalkLoginEnabled()) {
            throw new BadRequestException("钉钉登录已关闭");
        }
        String ip = httpRequest.getRemoteAddr();
        String device = httpRequest.getHeader("User-Agent");
        AuthResponse response = dingTalkOAuthService.handleCallback(code, ip, device, state);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
