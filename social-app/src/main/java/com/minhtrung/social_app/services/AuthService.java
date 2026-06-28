package com.minhtrung.social_app.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.minhtrung.social_app.dtos.ClientInfo;
import com.minhtrung.social_app.dtos.SignInRequest;
import com.minhtrung.social_app.dtos.SignUpRequest;
import com.minhtrung.social_app.dtos.VerifyRequest;
import com.minhtrung.social_app.enums.AuthErrorCode;
import com.minhtrung.social_app.exceptions.AuthenticationException;
import com.minhtrung.social_app.models.PendingUser;
import com.minhtrung.social_app.models.User;
import com.minhtrung.social_app.models.UserSession;
import com.minhtrung.social_app.repositories.UserRepository;
import com.minhtrung.social_app.repositories.UserSessionRepository;
import com.minhtrung.social_app.repositories.PendingUserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {
    private final EmailService emailService;
    private final JwtService jwtService;    
    private final UserRepository userRepository;
    private final PendingUserRepository pendingUserRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret.access}")
    private String accessSecret;

    @Value("${jwt.secret.refresh}")
    private String refreshSecret;

    AuthService(EmailService emailService, JwtService jwtService, UserRepository userRepository, PendingUserRepository pendingUserRepository, UserSessionRepository userSessionRepository, PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.pendingUserRepository = pendingUserRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void sendVerificationCode(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            log.warn("This email {} has been signed up", signUpRequest.getEmail());
            throw new RuntimeException("Email already exists");
        }

        pendingUserRepository.deleteByEmail(signUpRequest.getEmail());

        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);

        PendingUser pendingUser = new PendingUser();
        pendingUser.setEmail(signUpRequest.getEmail());
        pendingUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        pendingUser.setCode(code);
        pendingUser.setFirstName(signUpRequest.getFirstName());
        pendingUser.setLastName(signUpRequest.getLastName());
        pendingUser.setGender(signUpRequest.getGender());
        pendingUser.setBirthDate(signUpRequest.getBirthDate());

        pendingUserRepository.save(pendingUser);
        log.debug("Create a pending user for email {} and save in DB: {}", signUpRequest.getEmail(), pendingUser);

        emailService.sendVerificationEmail(signUpRequest.getEmail(), code);
    }

    @Transactional
    public void register(VerifyRequest verifyRequest) {
        PendingUser pendingUser = pendingUserRepository.findByEmail(verifyRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Not Found"));
        
        Duration validDuration = Duration.between(pendingUser.getCreatedDate(), LocalDateTime.now());

        if (pendingUser.getCode() == verifyRequest.getCode() && validDuration.toMinutes() <= 5) {
            User newUser = new User();
            newUser.setEmail(pendingUser.getEmail());
            newUser.setPassword(pendingUser.getPassword());
            newUser.setFirstName(pendingUser.getFirstName());
            newUser.setLastName(pendingUser.getLastName());
            newUser.setGender(pendingUser.getGender());
            newUser.setBirthDate(pendingUser.getBirthDate());
            newUser.setProfilePicUrl("");

            userRepository.save(newUser);
            pendingUserRepository.deleteByEmail(verifyRequest.getEmail());
        } else if (pendingUser.getCode() != verifyRequest.getCode()) {
            throw new AuthenticationException(AuthErrorCode.INVALID_CODE, "Code not match");
        } else if (validDuration.toMinutes() > 5) {
            pendingUserRepository.deleteByEmail(verifyRequest.getEmail());
            throw new AuthenticationException(AuthErrorCode.EXPIRED_CODE, "Code is already expired");
        }
    }

    public String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    // public void generateUserInfo(UUID sessionId, UUID userId, ClientInfo clientInfo) {
    //     UserInfo userInfo = new UserInfo();

    //     userInfo.setUserId(userId);
    //     userInfo.setSessionId(sessionId);
    //     userInfo.setDeviceType(clientInfo.getBrowser());
    //     userInfo.setDeviceOS(clientInfo.getDeviceOS());
    //     userInfo.setBrowser(clientInfo.getBrowser());
    //     userInfo.setIpAdd(clientInfo.getIpAddr());
    //     userInfo.setCountry(clientInfo.getCountry());
    //     userInfo.setCity(clientInfo.getCity());
    // }

    // public void updateUserInfo(UUID sessionId, UUID userId, ClientInfo clientInfo) {
    //     UserInfo userInfo = userInfoRepository.findBySessionId(sessionId);

    //     userInfo.setUserId(userId);
    //     userInfo.setDeviceType(clientInfo.getBrowser());
    //     userInfo.setDeviceOS(clientInfo.getDeviceOS());
    //     userInfo.setBrowser(clientInfo.getBrowser());
    //     userInfo.setIpAdd(clientInfo.getIpAddr());
    //     userInfo.setCountry(clientInfo.getCountry());
    //     userInfo.setCity(clientInfo.getCity());
    // }
    
    public Map<String, String> generateToken(UUID userId, UUID deviceId, ClientInfo clientInfo) {
        UserSession session = new UserSession();
        UUID sessionId = UUID.randomUUID();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setDeviceId(deviceId);
        session.setDeviceType(clientInfo.getBrowser());
        session.setDeviceOS(clientInfo.getDeviceOS());
        session.setBrowser(clientInfo.getBrowser());
        session.setIpAdd(clientInfo.getIpAddr());
        session.setCountry(clientInfo.getCountry());
        session.setCity(clientInfo.getCity());

        String accessToken = jwtService.generateAccessToken(userId, sessionId, deviceId);
        String refreshToken = jwtService.generateRefreshToken(userId, sessionId, deviceId);

        session.setToken(hash(refreshToken));
        userSessionRepository.save(session);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }
    
    public Map<String, String> updateToken(UUID userId, UUID deviceId, UserSession session, ClientInfo clientInfo) {
        String accessToken = jwtService.generateAccessToken(userId, session.getSessionId(), deviceId);
        String refreshToken = jwtService.generateRefreshToken(userId, session.getSessionId(), deviceId);

        session.setToken(hash(refreshToken));
        session.setDeviceType(clientInfo.getBrowser());
        session.setDeviceOS(clientInfo.getDeviceOS());
        session.setBrowser(clientInfo.getBrowser());
        session.setIpAdd(clientInfo.getIpAddr());
        session.setCountry(clientInfo.getCountry());
        session.setCity(clientInfo.getCity());
        userSessionRepository.save(session);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }
    
    public Map<String, String> signIn(SignInRequest signInRequest, UUID deviceId, ClientInfo clientInfo) {
        User user = userRepository.findByEmail(signInRequest.getEmail());

        if (user.getUserId() == null) {
            throw new AuthenticationException(AuthErrorCode.USER_NOT_FOUND,
                    "User doesn't exists");
        }

        try {
            if (passwordEncoder.matches(signInRequest.getPassword(), user.getPassword())) {
                List<UserSession> sessionList = userSessionRepository.findByUserId(user.getUserId());

                if (sessionList.isEmpty()) {
                    System.out.println("First Login");
                    return generateToken(user.getUserId(), deviceId, clientInfo);
                }

                UserSession session = sessionList.stream()
                        .filter(s -> s.getDeviceId().equals(deviceId))
                        .findFirst()
                        .orElse(null);

                if (session == null || deviceId.equals("")) {
                    System.out.println("Untrusted device");
                    emailService.sendUntrustedDeviceWarning(user.getEmail());
                    return generateToken(user.getUserId(), deviceId, clientInfo);
                } else {
                    System.out.println("Trusted device");
                    return generateToken(user.getUserId(), deviceId, clientInfo);
                }
            } else {
                throw new AuthenticationException(AuthErrorCode.USER_CREDENTIALS_INVALID, "Wrong credentials");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    } 
    
    public Map<String, String> autoSignIn(String token, UUID deviceId, ClientInfo clientInfo) {
        if (jwtService.isTokenValid(token, refreshSecret)) {
            Map<String, Object> payload = jwtService.extractAll(token, refreshSecret);

            UUID userId = UUID.fromString((String) payload.get("sub"));
            UUID sessionId = UUID.fromString((String) payload.get("sessionId"));
            UUID deviceIdFromToken = UUID.fromString((String) payload.get("deviceId"));

            UserSession session = userSessionRepository
                    .findById(sessionId)
                    .orElseThrow(() -> new AuthenticationException(AuthErrorCode.SESSION_NOT_FOUND,
                            "User's session not found"));

            if (!session.getUserId().equals(userId)) {
                throw new AuthenticationException(AuthErrorCode.USER_MISMATCH, "User mismatch");
            }

            if (!session.getDeviceId().equals(deviceIdFromToken)) {
                throw new AuthenticationException(AuthErrorCode.DEVICE_MISMATCH, "Device mismatch");
            }

            if (!deviceId.equals(deviceIdFromToken)) {
                throw new AuthenticationException(AuthErrorCode.DEVICE_COOKIE_MISMATCH, "DeviceID(Cookie) mismatch");
            }

            if (session.isRevoked()) {
                throw new AuthenticationException(AuthErrorCode.SESSION_REVOKED, "Session revoked");
            }

            if (!hash(token).equals(session.getToken())) {
                session.setRevoked(true);
                userSessionRepository.save(session);
                throw new AuthenticationException(AuthErrorCode.SESSION_COMPROMISED, "Session compromised");
            }

            System.out.println("Redirect to home page");
            return updateToken(userId, deviceIdFromToken, session, clientInfo);
        }
        
        System.out.println("Redirect to login page");
        throw new AuthenticationException(AuthErrorCode.SESSION_MISMATCH, "User's session not match");
    }
    
    public void signOut(String token) {
        if (jwtService.isTokenValid(token, refreshSecret)) {
            Map<String, Object> payload = jwtService.extractAll(token, refreshSecret);

            UUID sessionId = UUID.fromString((String) payload.get("sessionId"));

            UserSession session = userSessionRepository
                    .findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));
            
            session.setRevoked(true);
            userSessionRepository.save(session);
        } else {
            return;
        }
    }
}
