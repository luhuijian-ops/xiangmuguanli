package com.xiangmuguanli.service;

import com.xiangmuguanli.config.OAuthConfig;
import com.xiangmuguanli.dto.response.AuthResponse;
import com.xiangmuguanli.dto.response.UserResponse;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.entity.UserBinding;
import com.xiangmuguanli.enums.OAuthPlatform;
import com.xiangmuguanli.enums.UserStatus;
import com.xiangmuguanli.repository.LoginLogRepository;
import com.xiangmuguanli.repository.UserBindingRepository;
import com.xiangmuguanli.repository.UserRepository;
import com.xiangmuguanli.security.JwtTokenProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xiangmuguanli.exception.BadRequestException;
import com.xiangmuguanli.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class DingTalkOAuthService {

    private static final Logger log = LoggerFactory.getLogger(DingTalkOAuthService.class);
    private static final long STATE_TTL_MS = 10 * 60 * 1000L; // 10 minutes
    private final ConcurrentMap<String, Long> stateStore = new ConcurrentHashMap<>();

    private final OAuthConfig oAuthConfig;
    private final UserRepository userRepository;
    private final UserBindingRepository userBindingRepository;
    private final LoginLogRepository loginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public DingTalkOAuthService(OAuthConfig oAuthConfig,
                                  UserRepository userRepository,
                                  UserBindingRepository userBindingRepository,
                                  LoginLogRepository loginLogRepository,
                                  PasswordEncoder passwordEncoder,
                                  JwtTokenProvider jwtTokenProvider) {
        this.oAuthConfig = oAuthConfig;
        this.userRepository = userRepository;
        this.userBindingRepository = userBindingRepository;
        this.loginLogRepository = loginLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String getAuthUrl(String state) {
        return getAuthUrl(state, null);
    }

    public String getAuthUrl(String state, String redirectUri) {
        OAuthConfig.DingTalkConfig dingtalk = oAuthConfig.getDingtalk();
        if (dingtalk.getAppId() == null || dingtalk.getAppId().isBlank()) {
            throw new BadRequestException("钉钉登录未配置 appId");
        }
        String configuredRedirectUri = dingtalk.getRedirectUri();
        String finalRedirectUri = redirectUri != null ? redirectUri : configuredRedirectUri;
        if (finalRedirectUri == null || finalRedirectUri.isEmpty()) {
            throw new IllegalStateException("DingTalk redirect URI is not configured");
        }
        if (configuredRedirectUri != null && !configuredRedirectUri.isEmpty() && !configuredRedirectUri.equals(finalRedirectUri)) {
            throw new IllegalArgumentException("Invalid DingTalk redirect URI");
        }
        stateStore.put(state, System.currentTimeMillis() + STATE_TTL_MS);
        return String.format(
            "https://login.dingtalk.com/oauth2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=openid&prompt=consent&state=%s",
            dingtalk.getAppId(),
            java.net.URLEncoder.encode(finalRedirectUri),
            state
        );
    }

    @Transactional
    public AuthResponse handleCallback(String code) {
        return handleCallback(code, null, null, null);
    }

    @Transactional
    public AuthResponse handleCallback(String code, String ip, String device) {
        return handleCallback(code, ip, device, null);
    }

    @Transactional
    public AuthResponse handleCallback(String code, String ip, String device, String state) {
        validateState(state);
        Map<String, String> userInfo = getDingTalkUserInfo(code);

        String openId = userInfo.get("openid");
        String nickname = userInfo.get("nickname");
        String avatar = userInfo.get("avatar");

        // Find or create user binding
        Optional<UserBinding> bindingOpt = userBindingRepository.findByPlatformAndOpenId(OAuthPlatform.DINGTALK, openId);

        User user;
        if (bindingOpt.isPresent()) {
            user = bindingOpt.get().getUser();
        } else {
            // Create new user
            user = new User();
            user.setUsername("dingtalk_" + UUID.randomUUID().toString().substring(0, 8));
            user.setName(nickname != null ? nickname : "DingTalk User");
            user.setAvatar(avatar);
            user.setStatus(UserStatus.ACTIVE);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user = userRepository.save(user);

            // Create binding
            UserBinding binding = new UserBinding();
            binding.setUser(user);
            binding.setPlatform(OAuthPlatform.DINGTALK);
            binding.setOpenId(openId);
            binding.setNickname(nickname);
            binding.setAvatar(avatar);
            userBindingRepository.save(binding);
        }

        // Generate JWT tokens
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER")
                .build();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Record login log
        if (ip != null || device != null) {
            com.xiangmuguanli.entity.LoginLog loginLog = new com.xiangmuguanli.entity.LoginLog();
            loginLog.setUser(user);
            loginLog.setIp(ip);
            loginLog.setDevice(device);
            loginLog.setSuccess(true);
            loginLog.setPlatform("dingtalk");
            loginLogRepository.save(loginLog);
        }

        return new AuthResponse(accessToken, refreshToken, UserResponse.fromEntity(user));
    }

    private void validateState(String state) {
        if (state == null || state.isEmpty()) {
            throw new UnauthorizedException("Missing OAuth state");
        }
        Long expiry = stateStore.remove(state);
        if (expiry == null || expiry < System.currentTimeMillis()) {
            throw new UnauthorizedException("Invalid or expired OAuth state");
        }
    }

    private Map<String, String> getDingTalkUserInfo(String code) {
        OAuthConfig.DingTalkConfig config = oAuthConfig.getDingtalk();
        if (config.getAppId() == null || config.getAppId().isBlank()
                || config.getAppSecret() == null || config.getAppSecret().isBlank()) {
            throw new BadRequestException("钉钉登录配置不完整，请检查 appId 和 appSecret");
        }
        RestTemplate restTemplate = new RestTemplate();

        // Step 1: Exchange code for user access token (DingTalk OAuth2.0)
        String tokenUrl = "https://api.dingtalk.com/v1.0/oauth2/userAccessToken";

        Map<String, String> tokenBody = new HashMap<>();
        tokenBody.put("clientId", config.getAppId());
        tokenBody.put("clientSecret", config.getAppSecret());
        tokenBody.put("code", code);
        tokenBody.put("grantType", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(tokenBody, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse;
        try {
            tokenResponse = restTemplate.postForObject(tokenUrl, request, Map.class);
        } catch (HttpClientErrorException e) {
            log.warn("DingTalk access token request failed: {}", e.getResponseBodyAsString());
            throw new UnauthorizedException("DingTalk authentication failed");
        }

        if (tokenResponse == null || tokenResponse.get("accessToken") == null) {
            String errMsg = tokenResponse != null && tokenResponse.get("message") != null
                ? tokenResponse.get("message").toString()
                : "Failed to get DingTalk access token";
            throw new UnauthorizedException(errMsg);
        }

        String accessToken = tokenResponse.get("accessToken").toString();

        // Step 2: Get user info with access token
        String userInfoUrl = "https://api.dingtalk.com/v1.0/contact/users/me";

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set("x-acs-dingtalk-access-token", accessToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse;
        try {
            userResponse = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, userRequest, Map.class
            );
        } catch (HttpClientErrorException e) {
            log.warn("DingTalk user info request failed: {}", e.getResponseBodyAsString());
            throw new UnauthorizedException("DingTalk authentication failed");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = userResponse.getBody();

        if (userInfo == null || userInfo.get("unionId") == null) {
            String errMsg = userInfo != null && userInfo.get("message") != null
                ? userInfo.get("message").toString()
                : "Failed to get DingTalk user info";
            throw new UnauthorizedException(errMsg);
        }

        Map<String, String> result = new HashMap<>();
        result.put("openid", userInfo.get("unionId").toString());
        result.put("nickname",
            userInfo.get("nick") != null ? userInfo.get("nick").toString() : "DingTalk User");
        result.put("avatar",
            userInfo.get("avatarUrl") != null ? userInfo.get("avatarUrl").toString() : "");
        return result;
    }
}
