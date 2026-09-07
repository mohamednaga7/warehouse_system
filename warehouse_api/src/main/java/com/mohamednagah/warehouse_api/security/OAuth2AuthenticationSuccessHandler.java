package com.mohamednagah.warehouse_api.security;

import com.mohamednagah.warehouse_api.model.RefreshToken;
import com.mohamednagah.warehouse_api.model.User;
import com.mohamednagah.warehouse_api.repository.RefreshTokenRepository;
import com.mohamednagah.warehouse_api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final String frontendUrl;
    private final long refreshTokenExpirationDays;

    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository,
                                               RefreshTokenRepository refreshTokenRepository,
                                               JwtService jwtService,
                                               @Value("${app.frontend-url}") String frontendUrl,
                                               @Value("${jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String providerId = resolveProviderId(provider, oAuth2User);
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, providerId);

        User user = userOptional.orElseGet(() -> new User(null, email, name, Instant.now()));

        if (userOptional.isEmpty()) {
            user = userRepository.save(user, provider, providerId);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshTokenValue();
        refreshTokenRepository.save(new RefreshToken(null, refreshTokenValue, user.id(),
                Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS), false, Instant.now()));

        String fragment = "access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refresh_token=" + URLEncoder.encode(refreshTokenValue, StandardCharsets.UTF_8);
        response.sendRedirect(frontendUrl + "/#" + fragment);
    }

    private String resolveProviderId(String provider, OAuth2User oAuth2User) {
        if ("github".equals(provider)) {
            Object id = oAuth2User.getAttribute("id");
            return String.valueOf(id);
        }
        return oAuth2User.getName();
    }
}
