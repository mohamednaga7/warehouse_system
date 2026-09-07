package com.mohamednagah.warehouse_api.model;

import java.time.Instant;

public record User(Long id, String email, String name, Instant createdAt) {
}
