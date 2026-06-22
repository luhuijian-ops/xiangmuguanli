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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class WeChatOAuthService {

    private static final Logger log = LoggerFactory.getLogger(WeChatOAuthService.class);
    private static final long STATE_TTL_MS = 10 * 60 * 1000L; // 10 minutes
    private final ConcurrentMap<String, Long> stateStore = new ConcurrentHashMap<>();

    private final OAuthConfig oAuthConfig;
    private final UserRepository userRepository;
    private final UserBindingRepository userBindingRepository;
    private final LoginLogRepository loginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public WeChatOAuthService(OAuthConfig oAuthConfig,
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
        OAuthConfig.WeChatConfig wechat = oAuthConfig.getWechat();
        if (wechat.getAppId() == null || wechat.getAppId().isBlank()) {
            throw new BadRequestException("微信登录未配置 appId");
        }
        String configuredRedirectUri = wechat.getRedirectUri();
        String finalRedirectUri = redirectUri != null ? redirectUri : configuredRedirectUri;
        if (finalRedirectUri == null || finalRedirectUri.isEmpty()) {
            throw new IllegalStateException("WeChat redirect URI is not configured");
        }
        if (configuredRedirectUri != null && !configuredRedirectUri.isEmpty() && !configuredRedirectUri.equals(finalRedirectUri)) {
            throw new IllegalArgumentException("Invalid WeChat redirect URI");
        }
        stateStore.put(state, System.currentTimeMillis() + STATE_TTL_MS);
        return String.format(
            "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
            wechat.getAppId(),
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
        Map<String, String> userInfo = getWeChatUserInfo(code);

        String openId = userInfo.get("openid");
        String unionId = userInfo.get("unionid");
        String nickname = userInfo.get("nickname");
        String avatar = userInfo.get("headimgurl");

        // Find or create user binding
        Optional<UserBinding> bindingOpt = userBindingRepository.findByPlatformAndOpenId(OAuthPlatform.WECHAT, openId);

        User user;
        if (bindingOpt.isPresent()) {
            user = bindingOpt.get().getUser();
        } else {
            // Create new user
            user = new User();
            user.setUsername("wechat_" + UUID.randomUUID().toString().substring(0, 8));
            user.setName(nickname != null ? nickname : "WeChat User");
            user.setAvatar(avatar);
            user.setStatus(UserStatus.ACTIVE);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password for OAuth users
            user = userRepository.save(user);

            // Create binding
            UserBinding binding = new UserBinding();
            binding.setUser(user);
            binding.setPlatform(OAuthPlatform.WECHAT);
            binding.setOpenId(openId);
            binding.setUnionId(unionId);
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
            loginLog.setPlatform("wechat");
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

    private Map<String, String> getWeChatUserInfo(String code) {
        OAuthConfig.WeChatConfig config = oAuthConfig.getWechat();
        if (config.getAppId() == null || config.getAppId().isBlank()
                || config.getAppSecret() == null || config.getAppSecret().isBlank()) {
            throw new BadRequestException("微信登录配置不完整，请检查 appId 和 appSecret");
        }
        RestTemplate restTemplate = new RestTemplate();

        // Step 1: Exchange code for access_token and openid
        String accessTokenUrl = String.format(
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
            config.getAppId(), config.getAppSecret(), code
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse;
        try {
            tokenResponse = restTemplate.getForObject(accessTokenUrl, Map.class);
        } catch (HttpClientErrorException e) {
            log.warn("WeChat access token request failed: {}", e.getResponseBodyAsString());
            throw new UnauthorizedException("WeChat authentication failed");
        }

        if (tokenResponse == null || tokenResponse.get("openid") == null || tokenResponse.get("access_token") == null) {
            String errMsg = tokenResponse != null && tokenResponse.get("errmsg") != null
                ? tokenResponse.get("errmsg").toString()
                : "Failed to get WeChat access token";
            throw new UnauthorizedException(errMsg);
        }

        String accessToken = tokenResponse.get("access_token").toString();
        String openId = tokenResponse.get("openid").toString();
        String unionId = tokenResponse.get("unionid") != null ? tokenResponse.get("unionid").toString() : null;

        // Step 2: Get user info with access_token and openid
        String userInfoUrl = String.format(
            "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
            accessToken, openId
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> userInfoResponse;
        try {
            userInfoResponse = restTemplate.getForObject(userInfoUrl, Map.class);
        } catch (HttpClientErrorException e) {
            log.warn("WeChat user info request failed: {}", e.getResponseBodyAsString());
            throw new UnauthorizedException("WeChat authentication failed");
        }

        if (userInfoResponse == null || userInfoResponse.get("openid") == null) {
            String errMsg = userInfoResponse != null && userInfoResponse.get("errmsg") != null
                ? userInfoResponse.get("errmsg").toString()
                : "Failed to get WeChat user info";
            throw new UnauthorizedException(errMsg);
        }

        if (unionId == null && userInfoResponse.get("unionid") != null) {
            unionId = userInfoResponse.get("unionid").toString();
        }

        Map<String, String> result = new HashMap<>();
        result.put("openid", openId);
        result.put("unionid", unionId);
        result.put("nickname",
            userInfoResponse.get("nickname") != null ? userInfoResponse.get("nickname").toString() : "WeChat User");
        result.put("headimgurl",
            userInfoResponse.get("headimgurl") != null ? userInfoResponse.get("headimgurl").toString() : "");
        return result;
    }
}
