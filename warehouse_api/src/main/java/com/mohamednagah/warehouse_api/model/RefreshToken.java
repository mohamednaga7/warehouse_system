package com.mohamednagah.warehouse_api.model;

import java.time.Instant;

public record RefreshToken(Long id, String token, Long userId, Instant expiresAt, boolean revoked, Instant createdAt) {
}
