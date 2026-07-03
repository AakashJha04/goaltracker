package com.aakash.goalkeeper;

import com.aakash.goalkeeper.auth.dto.AuthDtos.AuthResponse;
import com.aakash.goalkeeper.auth.dto.AuthDtos.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Runs against the same Postgres the app uses in dev (docker compose up -d), matching
 * this project's run commands. Every test uses a freshly registered user so runs don't
 * collide with existing data.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate rest;

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    /** Registers a fresh user and returns their access token. */
    protected String registerAndGetToken() {
        RegisterRequest req = new RegisterRequest(
                "test-" + UUID.randomUUID() + "@example.com", "password123", "Test User");
        AuthResponse res = rest.postForEntity(baseUrl() + "/auth/register", req, AuthResponse.class).getBody();
        return res.accessToken();
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    protected <T> ResponseEntity<T> get(String path, String token, Class<T> type) {
        return rest.exchange(baseUrl() + path, HttpMethod.GET, new HttpEntity<>(authHeaders(token)), type);
    }

    protected <T> ResponseEntity<T> post(String path, String token, Object body, Class<T> type) {
        return rest.exchange(baseUrl() + path, HttpMethod.POST, new HttpEntity<>(body, authHeaders(token)), type);
    }

    protected <T> ResponseEntity<T> put(String path, String token, Object body, Class<T> type) {
        return rest.exchange(baseUrl() + path, HttpMethod.PUT, new HttpEntity<>(body, authHeaders(token)), type);
    }

    protected <T> ResponseEntity<T> patch(String path, String token, Object body, Class<T> type) {
        return rest.exchange(baseUrl() + path, HttpMethod.PATCH, new HttpEntity<>(body, authHeaders(token)), type);
    }

    protected <T> ResponseEntity<T> delete(String path, String token, Class<T> type) {
        return rest.exchange(baseUrl() + path, HttpMethod.DELETE, new HttpEntity<>(authHeaders(token)), type);
    }
}
