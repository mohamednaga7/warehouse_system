package com.mohamednagah.warehouse_api.repository;

import com.mohamednagah.warehouse_api.jooq.tables.records.ProvidersRecord;
import com.mohamednagah.warehouse_api.jooq.tables.records.UsersRecord;
import com.mohamednagah.warehouse_api.model.User;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.mohamednagah.warehouse_api.jooq.Tables.PROVIDERS;
import static com.mohamednagah.warehouse_api.jooq.Tables.USERS;
import static com.mohamednagah.warehouse_api.jooq.Tables.USER_PROVIDER;

@Repository
@Primary
@RequiredArgsConstructor
public class JooqUserRepository implements UserRepository {

    private final DSLContext context;

    @Override
    public Optional<User> findByProviderAndProviderId(String provider, String userProviderId) {
        return context.select(USERS)
                .from(PROVIDERS)
                .join(USER_PROVIDER).on(PROVIDERS.ID.eq(USER_PROVIDER.PROVIDER_ID))
                .join(USERS).on(USERS.ID.eq(USER_PROVIDER.USER_ID))
                .where(USER_PROVIDER.USER_PROVIDER_ID.eq(userProviderId))
                .and(PROVIDERS.NAME.eq(provider))
                .fetchOptionalInto(User.class);
    }

    @Override
    public Optional<User> findById(Long id) {
        return context.selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .fetchOptional()
                .map(JooqUserRepository::toUser);
    }

    @Override
    public User save(User user, String provider, String userProviderId) {
        return user.id() == null ? insert(user, provider, userProviderId) : update(user);
    }

    private User insert(User user, String provider, String userProviderId) {
        AtomicReference<User> stored = new AtomicReference<>();

        context.transaction(txn -> {
            DSLContext ctx = txn.dsl();

            // check if provider exists and if not create one
            Optional<ProvidersRecord> optionalProvider = ctx.selectFrom(PROVIDERS)
                    .where(PROVIDERS.NAME.eq(provider))
                    .fetchOptional();

            Integer providerId = null;

            if (optionalProvider.isPresent()) {
                providerId = optionalProvider.get().getId();
            } else {
                providerId = ctx.insertInto(PROVIDERS)
                        .set(PROVIDERS.NAME, provider)
                        .returning(PROVIDERS.ID)
                        .fetchOptional()
                        .orElseThrow(() -> new RuntimeException("Failed to create provider"))
                        .getId();
            }

            Optional<User> foundUser = ctx.selectFrom(USERS)
                    .where(USERS.EMAIL.eq(user.email()))
                    .fetchOptional().map(JooqUserRepository::toUser);

            User inserted = foundUser.orElseGet(() -> ctx.insertInto(USERS)
                    .set(USERS.EMAIL, user.email())
                    .set(USERS.NAME, user.name())
                    .set(USERS.CREATED_AT, user.createdAt() != null ? user.createdAt() : Instant.now())
                    .returning()
                    .fetchOptional()
                    .map(JooqUserRepository::toUser)
                    .orElseThrow(() -> new RuntimeException("Failed to create user")));

            ctx.insertInto(USER_PROVIDER)
                    .set(USER_PROVIDER.USER_ID, inserted.id())
                    .set(USER_PROVIDER.PROVIDER_ID, providerId)
                    .set(USER_PROVIDER.USER_PROVIDER_ID, userProviderId)
                    .execute();

            stored.set(inserted);
        });

        return stored.get();
    }

    private User update(User user) {
        UsersRecord updated = context.update(USERS)
                .set(USERS.EMAIL, user.email())
                .set(USERS.NAME, user.name())
                .where(USERS.ID.eq(user.id()))
                .returning()
                .fetchOptional()
                .orElseThrow(() -> new IllegalArgumentException("No user with id " + user.id()));
        return toUser(updated);
    }

    private static User toUser(UsersRecord row) {
        return new User(
                row.getId(),
                row.getEmail(),
                row.getName(),
                row.getCreatedAt());
    }
}
