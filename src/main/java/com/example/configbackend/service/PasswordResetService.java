package com.example.configbackend.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.configbackend.model.PasswordReset;
import com.example.configbackend.model.User;
import com.example.configbackend.repository.PasswordResetRepository;
import com.example.configbackend.repository.UserRepository;

@Service
public class PasswordResetService {

    private final PasswordResetRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(PasswordResetRepository tokenRepository,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Étape 1 - Générer et enregistrer un token
    public String createPasswordResetToken(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return "Aucun utilisateur trouvé avec cet email.";
        }

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(30);

        PasswordReset resetToken = new PasswordReset(token, user, now, expiresAt);
        tokenRepository.save(resetToken);

        // Simuler envoi email (à remplacer plus tard par vrai service)
        System.out.println("Lien de réinitialisation du mot de passe : http://localhost:8080/reset-password?token=" + token);

        return "Un lien de réinitialisation a été envoyé.";
    }

    // Étape 2 - Réinitialiser le mot de passe
    public String resetPassword(String token, String newPassword) {
        Optional<PasswordReset> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            return "Token invalide.";
        }

        PasswordReset resetToken = optionalToken.get();

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "Le token a expiré.";
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Optionnel : supprimer le token une fois utilisé
        tokenRepository.delete(resetToken);

        return "Mot de passe réinitialisé avec succès.";
    }
}
