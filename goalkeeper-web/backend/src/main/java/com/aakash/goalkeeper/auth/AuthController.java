package com.aakash.goalkeeper.auth;

import com.aakash.goalkeeper.auth.dto.AuthDtos.*;
import com.aakash.goalkeeper.security.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;

    @Value("${app.cookie.refresh-name}")
    private String cookieName;
    @Value("${app.cookie.secure}")
    private boolean cookieSecure;
    @Value("${app.cookie.same-site}")
    private String sameSite;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req,
                                                 HttpServletResponse res) {
        return respond(auth.register(req), res, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                              HttpServletResponse res) {
        return respond(auth.login(req), res, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "${app.cookie.refresh-name}", required = false) String refreshToken,
            HttpServletResponse res) {
        return respond(auth.refresh(refreshToken), res, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "${app.cookie.refresh-name}", required = false) String refreshToken,
            HttpServletResponse res) {
        auth.logout(refreshToken);
        clearCookie(res);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal UserPrincipal principal) {
        return auth.toDto(auth.requireUser(principal.id()));
    }

    // ----- cookie helpers -----

    private ResponseEntity<AuthResponse> respond(AuthService.AuthResult result,
                                                 HttpServletResponse res, HttpStatus status) {
        setCookie(res, result.refreshTokenRaw(), result.refreshTtlSeconds());
        return ResponseEntity.status(status).body(result.response());
    }

    private void setCookie(HttpServletResponse res, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(sameSite)
                .path("/auth")
                .maxAge(maxAgeSeconds)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearCookie(HttpServletResponse res) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(sameSite)
                .path("/auth")
                .maxAge(0)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
