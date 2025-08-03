package com.example.configbackend.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.configbackend.config.JWTUtils;
import com.example.configbackend.model.RefreshToken;
import com.example.configbackend.model.User;
import com.example.configbackend.repository.RefreshTokenRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        logger.debug("Recherche du refreshToken: token={}", token);
        Optional<RefreshToken> rt = refreshTokenRepository.findByToken(token);
        logger.debug("RefreshToken trouvé: {}", rt.isPresent());
        return rt;
    }

    public boolean isRefreshTokenExpired(RefreshToken token) {
        boolean expired = token.getExpiryDate().isBefore(Instant.now());
        logger.debug("Vérification expiration refreshToken: token={}, expiryDate={}, now={}, expired={}",
            token.getToken(), token.getExpiryDate(), Instant.now(), expired);
        return expired;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        logger.debug("Création d'un refreshToken pour userId={}", user.getId());
        deleteByUserId(user.getId());

        String token = generateUniqueToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        logger.debug("RefreshToken créé: token={}, expiryDate={}", saved.getToken(), saved.getExpiryDate());

        return saved;
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        logger.debug("Suppression des refreshTokens pour userId={}", userId);
        refreshTokenRepository.deleteByUserId(userId);
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = UUID.randomUUID().toString();
            logger.debug("Génération token UUID={}", token);
        } while (refreshTokenRepository.findByToken(token).isPresent());
        return token;
    }

public Map<String, String> generateNewAccessTokenFromRefreshToken(HttpServletRequest request, JWTUtils jwtUtils) {
    // Récupérer le refreshToken dans les cookies de manière fonctionnelle
    Optional<String> optionalToken = Optional.empty();

    if (request.getCookies() != null) {
        optionalToken = Arrays.stream(request.getCookies())
                              .filter(cookie -> "refreshToken".equals(cookie.getName()))
                              .map(Cookie::getValue)
                              .findFirst();
    }

    // Si absent, exception
    final String refreshTokenValue = optionalToken.orElseThrow(() -> {
        // Log si besoin
        System.out.println("Refresh token manquant dans les cookies.");
        return new RuntimeException("Refresh token manquant dans les cookies.");
    });

    // Recherche du RefreshToken en base
    RefreshToken refreshToken = findByToken(refreshTokenValue)
        .orElseThrow(() -> {
            System.out.println("Refresh token invalide: " + refreshTokenValue);
            return new RuntimeException("Refresh token invalide.");
        });

    // Vérifier expiration
    if (isRefreshTokenExpired(refreshToken)) {
        System.out.println("Refresh token expiré: " + refreshTokenValue);
        throw new RuntimeException("Refresh token expiré.");
    }

    User user = refreshToken.getUser();
    String newAccessToken = jwtUtils.generateToken(user.getEmail(), user.getRole());

    return Map.of(
        "accessToken", newAccessToken,
        "message", "Nouveau access token généré avec succès"
    );
}

}
