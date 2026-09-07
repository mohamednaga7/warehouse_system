package com.mohamednagah.warehouse_api.repository;

import com.mohamednagah.warehouse_api.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    void revoke(String token);

    void revokeAllForUser(Long userId);
}
