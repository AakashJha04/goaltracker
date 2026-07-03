package com.aakash.goalkeeper.auth;

import com.aakash.goalkeeper.auth.dto.AuthDtos.*;
import com.aakash.goalkeeper.common.ApiException;
import com.aakash.goalkeeper.security.JwtService;
import com.aakash.goalkeeper.user.Role;
import com.aakash.goalkeeper.user.User;
import com.aakash.goalkeeper.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final long refreshTtlSeconds;

    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users,
                       RefreshTokenRepository refreshTokens,
                       PasswordEncoder encoder,
                       JwtService jwt,
                       @Value("${app.jwt.refresh-token-ttl-seconds}") long refreshTtlSeconds) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.encoder = encoder;
        this.jwt = jwt;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    /** Carries the response plus the raw refresh token the controller puts in a cookie. */
    public record AuthResult(AuthResponse response, String refreshTokenRaw, long refreshTtlSeconds) {}

    @Transactional
    public AuthResult register(RegisterRequest req) {
        if (users.existsByEmailIgnoreCase(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        User u = new User();
        u.setEmail(req.email().toLowerCase());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setDisplayName(req.displayName().trim());
        u.setRole(Role.USER);
        // Phase 1: no SMTP wired, so accounts are usable immediately.
        // Phase 3 flips this to false + a verification email.
        u.setEmailVerified(true);
        users.save(u);
        return issueTokens(u);
    }

    @Transactional
    public AuthResult login(LoginRequest req) {
        User u = users.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return issueTokens(u);
    }

    @Transactional
    public AuthResult refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }
        RefreshToken stored = refreshTokens.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Session expired, please log in again"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            // Reuse of a revoked token is a theft signal → nuke the family.
            refreshTokens.revokeAllForUser(stored.getUserId());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Session expired, please log in again");
        }

        // Rotate: revoke the used token, issue a fresh pair.
        stored.setRevoked(true);
        refreshTokens.save(stored);

        User u = users.findById(stored.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account not found"));
        return issueTokens(u);
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        refreshTokens.findByTokenHash(sha256(rawToken)).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokens.save(t);
        });
    }

    // ----- helpers -----

    private AuthResult issueTokens(User u) {
        String accessToken = jwt.generateAccessToken(u);

        String raw = newRawToken();
        RefreshToken rt = new RefreshToken();
        rt.setUserId(u.getId());
        rt.setTokenHash(sha256(raw));
        rt.setExpiresAt(Instant.now().plusSeconds(refreshTtlSeconds));
        refreshTokens.save(rt);

        AuthResponse response = new AuthResponse(accessToken, jwt.getAccessTtlSeconds(), toDto(u));
        return new AuthResult(response, raw, refreshTtlSeconds);
    }

    public UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getDisplayName(), u.getRole().name(), u.isEmailVerified());
    }

    public User requireUser(UUID id) {
        return users.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    private String newRawToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
