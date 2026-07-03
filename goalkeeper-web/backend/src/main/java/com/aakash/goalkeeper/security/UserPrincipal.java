package com.aakash.goalkeeper.security;

import java.util.UUID;

/** Lightweight principal carried in the SecurityContext (no DB hit per request). */
public record UserPrincipal(UUID id, String email, String role) {
}
