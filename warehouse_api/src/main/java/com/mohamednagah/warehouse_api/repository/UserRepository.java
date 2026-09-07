package com.mohamednagah.warehouse_api.repository;

import com.mohamednagah.warehouse_api.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    Optional<User> findById(Long id);

    User save(User user, String provider, String providerId);
}
