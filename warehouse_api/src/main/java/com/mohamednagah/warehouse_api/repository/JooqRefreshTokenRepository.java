package com.mohamednagah.warehouse_api.repository;

import com.mohamednagah.warehouse_api.jooq.tables.records.RefreshTokensRecord;
import com.mohamednagah.warehouse_api.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import static com.mohamednagah.warehouse_api.jooq.Tables.REFRESH_TOKENS;

/**
 * Placeholder store, backed by memory instead of a real database.
 * Replace with a jOOQ-backed implementation once the schema exists.
 */
@Repository
@RequiredArgsConstructor
@Primary
public class JooqRefreshTokenRepository implements RefreshTokenRepository {

    private final AtomicLong idSequence = new AtomicLong(1);
    private final DSLContext context;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshToken toStore = refreshToken.id() != null
                ? refreshToken
                : new RefreshToken(idSequence.getAndIncrement(), refreshToken.token(), refreshToken.userId(),
                        refreshToken.expiresAt(), refreshToken.revoked(), refreshToken.createdAt());

        return context.insertInto(REFRESH_TOKENS)
                .set(REFRESH_TOKENS.TOKEN, toStore.token())
                .set(REFRESH_TOKENS.USER_ID, toStore.userId())
                .set(REFRESH_TOKENS.EXPIRES_AT, toStore.expiresAt())
                .set(REFRESH_TOKENS.REVOKED, toStore.revoked())
                .set(REFRESH_TOKENS.CREATED_AT, toStore.createdAt())
                .returning()
                .fetchOptional().map(this::toRefreshToken).orElseThrow(() -> new RuntimeException("Failed to save refresh token"));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return context.selectFrom(REFRESH_TOKENS)
                .where(REFRESH_TOKENS.TOKEN.eq(token))
                .fetchOptional()
                .map(this::toRefreshToken);
    }

    @Override
    public void revoke(String token) {
        context.update(REFRESH_TOKENS)
                .set(REFRESH_TOKENS.REVOKED, true)
                .where(REFRESH_TOKENS.TOKEN.eq(token))
                .execute();
    }

    @Override
    public void revokeAllForUser(Long userId) {
        context.update(REFRESH_TOKENS)
                .set(REFRESH_TOKENS.REVOKED, true)
                .where(REFRESH_TOKENS.USER_ID.eq(userId))
                .execute();
    }

    private RefreshToken toRefreshToken(RefreshTokensRecord record) {
        return new RefreshToken(
                record.get(REFRESH_TOKENS.ID),
                record.get(REFRESH_TOKENS.TOKEN),
                record.get(REFRESH_TOKENS.USER_ID),
                record.get(REFRESH_TOKENS.EXPIRES_AT),
                record.get(REFRESH_TOKENS.REVOKED),
                record.get(REFRESH_TOKENS.CREATED_AT)
        );
    }
}
