package com.github.nikolospl.creditcardmanager.config;

import com.github.nikolospl.creditcardmanager.model.Role;
import com.github.nikolospl.creditcardmanager.model.User;
import com.github.nikolospl.creditcardmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DefaultUsersInitializer {
    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.user.username:nikodem}") String userUsername,
            @Value("${app.security.user.password:haslo123}") String userPassword,
            @Value("${app.security.admin.username:admin_marysia}") String adminUsername,
            @Value("${app.security.admin.password:secure456}") String adminPassword) {
        return args -> {
            createIfMissing(userRepository, passwordEncoder, userUsername, userPassword, Role.USER);
            createIfMissing(userRepository, passwordEncoder, adminUsername, adminPassword, Role.ADMIN);
        };
    }

    private void createIfMissing(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String rawPassword,
            Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
    }
}
