package com.github.nikolospl.creditcardmanager.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthLoginIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.flyway.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRESQL::getUsername);
        registry.add("spring.flyway.password", POSTGRESQL::getPassword);

        registry.add("app.security.user.username", () -> "login_user");
        registry.add("app.security.user.password", () -> "login_password");
        registry.add("app.security.admin.username", () -> "login_admin");
        registry.add("app.security.admin.password", () -> "login_admin_password");
        registry.add("app.security.jwt.secret", () -> "test-jwt-secret-at-least-32-characters");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginShouldReturnJwtTokenForValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"login_user\",\"password\":\"login_password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())));
    }

    @Test
    void loginShouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"login_user\",\"password\":\"wrong_password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void issuedTokenShouldAuthorizeAccessToProtectedEndpoint() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"login_user\",\"password\":\"login_password\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractToken(body);

        mockMvc.perform(get("/api/v1/cards/{id}", UUID.randomUUID())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private String extractToken(String jsonBody) {
        int markerIndex = jsonBody.indexOf("\"token\":\"");
        if (markerIndex < 0) {
            return "";
        }
        int start = markerIndex + 9;
        int end = jsonBody.indexOf('"', start);
        if (end < 0) {
            return "";
        }
        return jsonBody.substring(start, end);
    }
}
