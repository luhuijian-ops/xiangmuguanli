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

import com.xiangmuguanli.exception.UnauthorizedException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class WeChatOAuthService {

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
        String finalRedirectUri = redirectUri != null ? redirectUri : wechat.getRedirectUri();
        if (finalRedirectUri == null || finalRedirectUri.isEmpty()) {
            throw new IllegalStateException("WeChat redirect URI is not configured");
        }
        return String.format(
            "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
            wechat.getAppId(),
            java.net.URLEncoder.encode(finalRedirectUri),
            state
        );
    }

    @Transactional
    public AuthResponse handleCallback(String code) {
        return handleCallback(code, null, null);
    }

    @Transactional
    public AuthResponse handleCallback(String code, String ip, String device) {
        Map<String, String> userInfo = getWeChatUserInfo(code);

        String openId = userInfo.get("openid");
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

    private Map<String, String> getWeChatUserInfo(String code) {
        OAuthConfig.WeChatConfig config = oAuthConfig.getWechat();
        RestTemplate restTemplate = new RestTemplate();

        // Step 1: Exchange code for access_token and openid
        String accessTokenUrl = String.format(
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
            config.getAppId(), config.getAppSecret(), code
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = restTemplate.getForObject(accessTokenUrl, Map.class);

        if (tokenResponse == null || tokenResponse.get("openid") == null) {
            String errMsg = tokenResponse != null && tokenResponse.get("errmsg") != null
                ? tokenResponse.get("errmsg").toString()
                : "Failed to get WeChat access token";
            throw new UnauthorizedException(errMsg);
        }

        String accessToken = tokenResponse.get("access_token").toString();
        String openId = tokenResponse.get("openid").toString();

        // Step 2: Get user info with access_token and openid
        String userInfoUrl = String.format(
            "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
            accessToken, openId
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> userInfoResponse = restTemplate.getForObject(userInfoUrl, Map.class);

        if (userInfoResponse == null || userInfoResponse.get("openid") == null) {
            String errMsg = userInfoResponse != null && userInfoResponse.get("errmsg") != null
                ? userInfoResponse.get("errmsg").toString()
                : "Failed to get WeChat user info";
            throw new UnauthorizedException(errMsg);
        }

        Map<String, String> result = new HashMap<>();
        result.put("openid", openId);
        result.put("nickname",
            userInfoResponse.get("nickname") != null ? userInfoResponse.get("nickname").toString() : "WeChat User");
        result.put("headimgurl",
            userInfoResponse.get("headimgurl") != null ? userInfoResponse.get("headimgurl").toString() : "");
        return result;
    }
}
