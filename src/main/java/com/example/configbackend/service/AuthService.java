package com.example.configbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.configbackend.config.JWTUtils;
import com.example.configbackend.dto.LoginRequest;
import com.example.configbackend.dto.LoginResponse;
import com.example.configbackend.dto.RegisterRequest;
import com.example.configbackend.model.User;
import com.example.configbackend.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private RefreshTokenService refreshTokenService; // à ajouter

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail().toLowerCase()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();

        if (request.getName() != null && !request.getName().isBlank()) {
            String[] parts = request.getName().trim().split(" ", 2);
            user.setNom(parts[0]);
            user.setPrenom(parts.length > 1 ? parts[1] : "");
        } else {
            user.setNom("");
            user.setPrenom("");
        }

        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAge(request.getAge());
        user.setRole(request.getRole() == null ? "USER" : request.getRole().toUpperCase());
        user.setActif(false);  // inactif jusqu'à la vérification

        User savedUser = userRepository.save(user);

        String verificationToken = verificationService.createVerificationToken(savedUser);
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

        return "Utilisateur enregistré avec succès, veuillez vérifier votre email";
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Identifiants invalides");
        }

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.isActif()) {
            throw new RuntimeException("Compte non vérifié");
        }

        String role = user.getRole().toLowerCase();

        // Génération du JWT (authToken)
        String token = jwtUtils.generateToken(user.getEmail(), role);

        // Génération du refresh token
        String refreshTokenStr = refreshTokenService.createRefreshToken(user).getToken();

        // Créer le cookie authToken
        ResponseCookie authCookie = ResponseCookie.from("authToken", token)
                .httpOnly(true)
                .secure(false) // mettre true en prod avec HTTPS
                .path("/")
                .maxAge(jwtUtils.getJwtExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        // Créer le cookie refreshToken
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshTokenStr)
                .httpOnly(true)
                .secure(false) // mettre true en prod avec HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 jours
                .sameSite("Strict")
                .build();

        // Ajouter les cookies à la réponse
        response.addHeader("Set-Cookie", authCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return new LoginResponse("Connexion réussie !", token);
    }

    public String verifyUser(String token) {
        String result = verificationService.confirmToken(token);
        if (result.contains("succès")) {
            return result;
        } else {
            throw new RuntimeException(result);
        }
    }

    public void logout(HttpServletResponse response) {
        // Supprimer le cookie authToken
        ResponseCookie authCookie = ResponseCookie.from("authToken", "")
                .httpOnly(true)
                .secure(false) // true en prod
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        // Supprimer le cookie refreshToken
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // true en prod
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", authCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }
}
