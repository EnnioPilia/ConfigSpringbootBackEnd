package com.example.configbackend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.configbackend.model.User;
import com.example.configbackend.repository.UserRepository;
import com.example.configbackend.repository.RefreshTokenRepository;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
                this.refreshTokenRepository = refreshTokenRepository;

    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail().toLowerCase()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }
        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActif(false);
        user.setRole(user.getRole() == null ? "USER" : user.getRole().toUpperCase());
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setActif(true);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec email : " + email));

        if (!user.isActif()) {
            throw new UsernameNotFoundException("Utilisateur non actif");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }

@Transactional
public boolean deleteUserById(UUID id) {
    // Supprimer les refresh tokens liés à l'utilisateur
    refreshTokenRepository.deleteByUser_Id(id);
    
    if (userRepository.existsById(id)) {
        userRepository.deleteById(id);
        return true;
    }
    return false;
}


    public User save(User user) {
        return userRepository.save(user);
    }

    public User registerAdmin(User user) {
        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ADMIN");
        user.setActif(true);
        return userRepository.save(user);
    }
}
