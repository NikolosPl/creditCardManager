package com.github.nikolospl.creditcardmanager.service;

import com.github.nikolospl.creditcardmanager.dto.PayRequest;
import com.github.nikolospl.creditcardmanager.exception.CardOperationException;
import com.github.nikolospl.creditcardmanager.exception.ResourceNotFoundException;
import com.github.nikolospl.creditcardmanager.model.CardStatus;
import com.github.nikolospl.creditcardmanager.model.CreditCard;
import com.github.nikolospl.creditcardmanager.model.Role;
import com.github.nikolospl.creditcardmanager.model.User;
import com.github.nikolospl.creditcardmanager.repository.CardTransactionRepository;
import com.github.nikolospl.creditcardmanager.repository.CreditCardRepository;
import com.github.nikolospl.creditcardmanager.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditCardServiceTest {

    @Mock
    private CreditCardRepository cardRepository;

    @Mock
    private CardTransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreditCardService creditCardService;

    private UUID cardId;
    private CreditCard card;

    @BeforeEach
    void setUp() {
        cardId = UUID.randomUUID();

        card = new CreditCard();
        card.setId(cardId);
        card.setCustomerId(UUID.randomUUID());
        card.setStatus(CardStatus.ACTIVE);
        card.setCardLimit(new BigDecimal("500.00"));
        card.setUsedFunds(new BigDecimal("100.00"));

        User owner = new User();
        owner.setId(card.getCustomerId());
        owner.setUsername("owner");
        owner.setRole(Role.USER);

        lenient().when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "owner",
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void processPaymentShouldThrowWhenAmountIsZeroOrNegative() {
        when(cardRepository.findByIdForUpdate(cardId)).thenReturn(Optional.of(card));

        assertThrows(CardOperationException.class,
                () -> creditCardService.processPayment(cardId, new PayRequest(BigDecimal.ZERO)));
        assertThrows(CardOperationException.class,
                () -> creditCardService.processPayment(cardId, new PayRequest(new BigDecimal("-1.00"))));

        verify(cardRepository, never()).save(any(CreditCard.class));
    }

    @Test
    void processPaymentShouldThrowWhenAmountExceedsAvailableFunds() {
        when(cardRepository.findByIdForUpdate(cardId)).thenReturn(Optional.of(card));

        assertThrows(CardOperationException.class,
                () -> creditCardService.processPayment(cardId, new PayRequest(new BigDecimal("401.00"))));

        verify(cardRepository, never()).save(any(CreditCard.class));
    }

    @Test
    void processPaymentShouldThrowWhenCardDoesNotExist() {
        when(cardRepository.findByIdForUpdate(cardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> creditCardService.processPayment(cardId, new PayRequest(new BigDecimal("50.00"))));
    }
}
