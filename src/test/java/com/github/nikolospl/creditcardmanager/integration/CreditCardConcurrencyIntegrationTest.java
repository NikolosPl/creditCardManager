package com.github.nikolospl.creditcardmanager.integration;

import com.github.nikolospl.creditcardmanager.dto.PayRequest;
import com.github.nikolospl.creditcardmanager.model.CardStatus;
import com.github.nikolospl.creditcardmanager.model.CreditCard;
import com.github.nikolospl.creditcardmanager.model.Role;
import com.github.nikolospl.creditcardmanager.model.User;
import com.github.nikolospl.creditcardmanager.repository.CardTransactionRepository;
import com.github.nikolospl.creditcardmanager.repository.CreditCardRepository;
import com.github.nikolospl.creditcardmanager.repository.UserRepository;
import com.github.nikolospl.creditcardmanager.service.CreditCardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class CreditCardConcurrencyIntegrationTest {

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
        registry.add("app.security.jwt.secret", () -> "test-jwt-secret-at-least-32-characters");
    }

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private CardTransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User owner;
    private CreditCard card;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        creditCardRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User();
        owner.setUsername("concurrent_user");
        owner.setPassword(passwordEncoder.encode("password"));
        owner.setRole(Role.USER);
        owner = userRepository.save(owner);

        card = new CreditCard();
        card.setCardNumber("1234567812345678");
        card.setCustomerId(owner.getId());
        card.setCardLimit(new BigDecimal("500.00"));
        card.setUsedFunds(BigDecimal.ZERO);
        card.setStatus(CardStatus.ACTIVE);
        card = creditCardRepository.save(card);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void concurrentPaymentsShouldNotExceedCardLimit() throws Exception {
        int threadCount = 12;
        BigDecimal paymentAmount = new BigDecimal("100.00");

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successfulPayments = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executorService.submit(() -> {
                readyLatch.countDown();
                startLatch.await();

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                owner.getUsername(),
                                "N/A",
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))));

                try {
                    creditCardService.processPayment(card.getId(), new PayRequest(paymentAmount));
                    successfulPayments.incrementAndGet();
                } catch (RuntimeException ignored) {
                    // rejected payments are expected when available limit is exhausted
                } finally {
                    SecurityContextHolder.clearContext();
                }
                return null;
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }
        executorService.shutdown();

        CreditCard updatedCard = creditCardRepository.findById(card.getId()).orElseThrow();

        assertEquals(new BigDecimal("500.00"), updatedCard.getUsedFunds());
        assertEquals(5, successfulPayments.get());
    }
}
