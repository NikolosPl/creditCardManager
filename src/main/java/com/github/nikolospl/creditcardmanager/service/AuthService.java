package com.github.nikolospl.creditcardmanager.service;

import com.github.nikolospl.creditcardmanager.config.JwtTokenProvider;
import com.github.nikolospl.creditcardmanager.dto.LoginRequest;
import com.github.nikolospl.creditcardmanager.dto.LoginResponse;
import com.github.nikolospl.creditcardmanager.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Nieprawidłowy login lub hasło"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Nieprawidłowy login lub hasło");
        }

        var userDetails = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        String token = jwtTokenProvider.generateToken(userDetails);
        return new LoginResponse(token);
    }
}
