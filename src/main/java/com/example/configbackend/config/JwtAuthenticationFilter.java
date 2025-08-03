package com.example.configbackend.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.configbackend.model.RefreshToken;
import com.example.configbackend.model.User;
import com.example.configbackend.service.RefreshTokenService;
import com.example.configbackend.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // Ne pas filtrer les endpoints d'authentification
        return path.startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authToken = null;
        String refreshToken = null;

        // Chercher dans les cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("authToken".equals(cookie.getName())) {
                    authToken = cookie.getValue();
                } else if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        System.out.println("[JwtAuthFilter] authToken from cookie: " + authToken);
        System.out.println("[JwtAuthFilter] refreshToken from cookie: " + refreshToken);

        // Si refreshToken pas trouvé dans cookies, chercher dans le header
        if (refreshToken == null) {
            refreshToken = request.getHeader("refreshToken");
            System.out.println("[JwtAuthFilter] refreshToken from header: " + refreshToken);
        }

        boolean isAuthenticated = false;

        boolean validAuthToken = authToken != null && jwtUtils.validateToken(authToken);
        System.out.println("[JwtAuthFilter] is authToken valid? " + validAuthToken);

        if (validAuthToken) {
            String username = jwtUtils.getUsernameFromToken(authToken);
            System.out.println("[JwtAuthFilter] authToken valid for user: " + username);
            authenticateUser(username, request);
            isAuthenticated = true;
        } else if (refreshToken != null) {
            // Si authToken absent ou invalide, tenter refresh via refreshToken
            RefreshToken rt = refreshTokenService.findByToken(refreshToken).orElse(null);
            System.out.println("[JwtAuthFilter] RefreshToken found in DB? " + (rt != null));
            if (rt != null) {
                boolean isExpired = refreshTokenService.isRefreshTokenExpired(rt);
                System.out.println("[JwtAuthFilter] is RefreshToken expired? " + isExpired);
                if (!isExpired) {
                    User user = rt.getUser();
                    // Générer un nouveau JWT
                    String newToken = jwtUtils.generateToken(user.getEmail(), user.getRole().toLowerCase());
                    System.out.println("[JwtAuthFilter] Generated new authToken: " + newToken);

                    // Ajouter le nouveau cookie authToken dans la réponse
                    ResponseCookie newAuthCookie = ResponseCookie.from("authToken", newToken)
                            .httpOnly(true)
                            .secure(false) // Mettre à true en prod (HTTPS)
                            .path("/")
                            .sameSite("Strict")
                            .maxAge(jwtUtils.getJwtExpirationMs() / 1000)
                            .build();
                    response.setHeader("Set-Cookie", newAuthCookie.toString());
                    System.out.println("[JwtAuthFilter] Set new authToken cookie in response");

                    authenticateUser(user.getEmail(), request);
                    isAuthenticated = true;
                }
            }
        }

        if (!isAuthenticated) {
            System.out.println("[JwtAuthFilter] Authentication failed, clearing context");
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String username, HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("[JwtAuthFilter] User authenticated: " + username);
        }
    }
}
