package com.xiangmuguanli.service;

import com.xiangmuguanli.dto.request.LoginRequest;
import com.xiangmuguanli.dto.request.RegisterRequest;
import com.xiangmuguanli.dto.response.AuthResponse;
import com.xiangmuguanli.dto.response.UserResponse;
import com.xiangmuguanli.entity.Activity;
import com.xiangmuguanli.entity.LoginLog;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.enums.AlertType;
import com.xiangmuguanli.enums.Severity;
import com.xiangmuguanli.enums.UserStatus;
import com.xiangmuguanli.exception.BadRequestException;
import com.xiangmuguanli.exception.UnauthorizedException;
import com.xiangmuguanli.entity.TokenBlacklist;
import com.xiangmuguanli.repository.ActivityRepository;
import com.xiangmuguanli.repository.LoginLogRepository;
import com.xiangmuguanli.repository.TokenBlacklistRepository;
import com.xiangmuguanli.repository.UserRepository;
import com.xiangmuguanli.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final ActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AlertService alertService;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       LoginLogRepository loginLogRepository,
                       ActivityRepository activityRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AlertService alertService,
                       TokenBlacklistRepository tokenBlacklistRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
        this.activityRepository = activityRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.alertService = alertService;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ip, String device) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            // Log successful login
            LoginLog loginLog = new LoginLog();
            loginLog.setUser(user);
            loginLog.setIp(ip);
            loginLog.setDevice(device);
            loginLog.setSuccess(true);
            loginLogRepository.save(loginLog);

            // Log audit activity
            Activity activity = new Activity();
            activity.setUser(user);
            activity.setAction("LOGIN_SUCCESS");
            activity.setEntityType("User");
            activity.setEntityId(user.getId());
            activity.setMetadata("{\"ip\":\"" + ip + "\"}");
            activityRepository.save(activity);

            return new AuthResponse(accessToken, refreshToken, UserResponse.fromEntity(user));

        } catch (Exception e) {
            // Log failed login
            LoginLog loginLog = new LoginLog();
            loginLog.setUser(user);
            loginLog.setIp(ip);
            loginLog.setDevice(device);
            loginLog.setSuccess(false);
            loginLogRepository.save(loginLog);

            // Log audit activity for failed login
            Activity activity = new Activity();
            activity.setUser(user);
            activity.setAction("LOGIN_FAILURE");
            activity.setEntityType("User");
            activity.setEntityId(user.getId());
            activity.setMetadata("{\"ip\":\"" + ip + "\"}");
            activityRepository.save(activity);

            // Check for suspicious activity (5 failures in 15 minutes)
            checkLoginFailureAlert(user);

            throw new UnauthorizedException("Invalid username or password");
        }
    }

    private void checkLoginFailureAlert(User user) {
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        long failureCount = loginLogRepository.countRecentFailedLogins(user.getId(), fifteenMinutesAgo);

        if (failureCount >= 5) {
            alertService.createAlert(
                    AlertType.LOGIN_FAILURE,
                    Severity.CRITICAL,
                    user,
                    user.getId(),
                    "User",
                    "5 failed login attempts in 15 minutes"
            );
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);
        user.setAdmin(false);

        user = userRepository.save(user);

        // Log audit activity
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setAction("USER_REGISTER");
        activity.setEntityType("User");
        activity.setEntityId(user.getId());
        activity.setMetadata("{\"username\":\"" + user.getUsername() + "\"}");
        activityRepository.save(activity);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return new AuthResponse(accessToken, refreshToken, UserResponse.fromEntity(user));
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String token = refreshToken.substring(7);
        String username = jwtTokenProvider.extractUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER")
                .build();

        if (!jwtTokenProvider.isTokenValid(token, userDetails)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return new AuthResponse(newAccessToken, newRefreshToken, UserResponse.fromEntity(user));
    }

    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    @Transactional
    public void logout(String token, User user) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid token");
        }

        String jwt = token.substring(7);

        // If already blacklisted, skip
        if (tokenBlacklistRepository.existsByToken(jwt)) {
            return;
        }

        // Get token expiration
        Date expireDate = jwtTokenProvider.extractExpiration(jwt);
        LocalDateTime expireTime = Instant.ofEpochMilli(expireDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // Add token to blacklist
        TokenBlacklist blacklist = new TokenBlacklist();
        blacklist.setUser(user);
        blacklist.setToken(jwt);
        blacklist.setExpireTime(expireTime);
        tokenBlacklistRepository.save(blacklist);

        // Log audit activity
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setAction("LOGOUT");
        activity.setEntityType("User");
        activity.setEntityId(user.getId());
        activityRepository.save(activity);
    }
}
