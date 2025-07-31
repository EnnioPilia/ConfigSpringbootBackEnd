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
    private final EmailService emailService;

    public PasswordResetService(PasswordResetRepository tokenRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // Étape 1 - Générer et enregistrer un token, puis envoyer mail
    public String createPasswordResetToken(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email est requis");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email.toLowerCase());
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Aucun utilisateur trouvé avec cet email.");
        }

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(30);

        PasswordReset resetToken = new PasswordReset(token, user, now, expiresAt);
        tokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);

        return "Un lien de réinitialisation a été envoyé à votre adresse email.";
    }

    // Étape 2 - Réinitialiser le mot de passe
    public String resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Token et nouveau mot de passe sont requis");
        }

        Optional<PasswordReset> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            throw new IllegalArgumentException("Token invalide.");
        }

        PasswordReset resetToken = optionalToken.get();

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Le token a expiré.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);

        return "Mot de passe réinitialisé avec succès.";
    }
}
