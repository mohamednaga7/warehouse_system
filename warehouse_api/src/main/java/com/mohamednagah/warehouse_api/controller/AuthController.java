package com.mohamednagah.warehouse_api.controller;

import com.mohamednagah.warehouse_api.model.RefreshToken;
import com.mohamednagah.warehouse_api.model.User;
import com.mohamednagah.warehouse_api.repository.RefreshTokenRepository;
import com.mohamednagah.warehouse_api.repository.UserRepository;
import com.mohamednagah.warehouse_api.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final long refreshTokenExpirationDays;

    public AuthController(RefreshTokenRepository refreshTokenRepository,
                           UserRepository userRepository,
                           JwtService jwtService,
                           @Value("${jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record LogoutRequest(String refreshToken) {
    }

    public record TokenPairResponse(String accessToken, String refreshToken) {
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestHeader String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String refreshToken = authorizationHeader.replace("Bearer ", "");
        RefreshToken existing = refreshTokenRepository.findByToken(refreshToken)
                .filter(token -> !token.revoked())
                .filter(token -> token.expiresAt().isAfter(Instant.now()))
                .orElse(null);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(existing.userId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        refreshTokenRepository.revoke(existing.token());

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshTokenValue = jwtService.generateRefreshTokenValue();
        refreshTokenRepository.save(new RefreshToken(null, newRefreshTokenValue, user.id(),
                Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS), false, Instant.now()));

        return ResponseEntity.ok(new TokenPairResponse(newAccessToken, newRefreshTokenValue));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        refreshTokenRepository.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
