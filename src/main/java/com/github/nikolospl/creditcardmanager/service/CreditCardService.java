package com.github.nikolospl.creditcardmanager.service;

import com.github.nikolospl.creditcardmanager.dto.CreateCardRequest;
import com.github.nikolospl.creditcardmanager.dto.CreditCardResponse;
import com.github.nikolospl.creditcardmanager.dto.PayRequest;
import com.github.nikolospl.creditcardmanager.exception.ResourceNotFoundException;
import com.github.nikolospl.creditcardmanager.exception.CardOperationException;
import com.github.nikolospl.creditcardmanager.model.CardStatus;
import com.github.nikolospl.creditcardmanager.model.CardTransaction;
import com.github.nikolospl.creditcardmanager.model.CreditCard;
import com.github.nikolospl.creditcardmanager.model.TransactionType;
import com.github.nikolospl.creditcardmanager.repository.CardTransactionRepository;
import com.github.nikolospl.creditcardmanager.repository.CreditCardRepository;
import com.github.nikolospl.creditcardmanager.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreditCardService {
    private static final String CARD_NOT_FOUND_MESSAGE = "Karta o podanym ID nie została znaleziona!";
    private final CreditCardRepository repository;
    private final CardTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public CreditCardService(CreditCardRepository repository,
                             CardTransactionRepository transactionRepository,
                             UserRepository userRepository){
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CreditCardResponse issueCard(CreateCardRequest request){
        CreditCard card = new CreditCard();
        card.setCustomerId(request.customerId());
        card.setCardLimit(request.initialLimit());
        card.setUsedFunds(BigDecimal.ZERO);
        card.setCardNumber(generateRandomCardNumber());
        card.setStatus(CardStatus.ACTIVE);

        CreditCard savedCard = repository.save(card);

        return new CreditCardResponse(
                savedCard.getId(),
                savedCard.getCardNumber(),
                savedCard.getCardLimit(),
                savedCard.getUsedFunds(),
                savedCard.getStatus().name()
        );
    }
    public CreditCardResponse getCardById(UUID id){
        return repository.findById(id)
            .map(card -> {
                assertCardAccess(card);
                return new CreditCardResponse(
                    card.getId(),
                    card.getCardNumber(),
                    card.getCardLimit(),
                    card.getUsedFunds(),
                    card.getStatus().name()
                );
            })
            .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE));
    }
    @Transactional
    public CreditCardResponse changeLimit(UUID id, PayRequest request){
        CreditCard card = repository.findByIdForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE));

        assertCardAccess(card);

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardOperationException("Nowy limit musi być większy od zera!");
        }

        if(card.getStatus() == CardStatus.BLOCKED){
            throw new CardOperationException("Nie można zmienić limitu zablokowanej karty!");
        }

        if(card.getCardLimit().subtract(card.getUsedFunds()).compareTo(request.amount()) < 0){
            throw new CardOperationException("Nowy limit jest niższy niż aktualnie używane środki!");
        }

        card.setCardLimit(request.amount());
        CreditCard updatedCard = repository.save(card);

        CardTransaction tx = new CardTransaction();
        tx.setCardId(card.getId());
        tx.setAmount(request.amount());
        tx.setType(TransactionType.LIMIT_CHANGE);
        tx.setTimestamp(LocalDateTime.now());
        transactionRepository.save(tx);

        return new CreditCardResponse(
                updatedCard.getId(),
                updatedCard.getCardNumber(),
                updatedCard.getCardLimit(),
                updatedCard.getUsedFunds(),
                updatedCard.getStatus().name()
        );


    }
    @Transactional
    public CreditCardResponse blockCard(UUID id){
        CreditCard card = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE));

        assertCardAccess(card);
        card.block();

        CreditCard updatedCard = repository.save(card);

        return new CreditCardResponse(
                updatedCard.getId(),
                updatedCard.getCardNumber(),
                updatedCard.getCardLimit(),
                updatedCard.getUsedFunds(),
                updatedCard.getStatus().name()
        );
    }
    @Transactional
    public CreditCardResponse unblockCard(UUID id){
        CreditCard card = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE));

        assertCardAccess(card);

        if(card.getStatus() == CardStatus.ACTIVE){
            throw new CardOperationException("Karta już jest aktywna!");
        }

        card.setStatus(CardStatus.ACTIVE);

        CreditCard updatedCard = repository.save(card);

        return new CreditCardResponse(
                updatedCard.getId(),
                updatedCard.getCardNumber(),
                updatedCard.getCardLimit(),
                updatedCard.getUsedFunds(),
                updatedCard.getStatus().name()
        );
    }

    @Transactional
    public CreditCardResponse processPayment(UUID id, PayRequest request){
        CreditCard card = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Karta o podanym ID nie istnieje!"));

        assertCardAccess(card);
        card.processPayment(request.amount());
        CreditCard updatedCard = repository.save(card);

        CardTransaction tx = new CardTransaction();
        tx.setCardId(card.getId());
        tx.setAmount(request.amount());
        tx.setType(TransactionType.PAYMENT);
        tx.setTimestamp(LocalDateTime.now());
        transactionRepository.save(tx);

        return new CreditCardResponse(
                updatedCard.getId(),
                updatedCard.getCardNumber(),
                updatedCard.getCardLimit(),
                updatedCard.getUsedFunds(),
                updatedCard.getStatus().name()
        );
    }

    @Transactional
    public CreditCardResponse repayDebt(UUID id, PayRequest request){
        CreditCard card = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Karta o podanym ID nie istnieje!"));

        assertCardAccess(card);
        card.repay(request.amount());
        CreditCard updatedCard = repository.save(card);

        CardTransaction tx = new CardTransaction();
        tx.setCardId(card.getId());
        tx.setAmount(request.amount());
        tx.setType(TransactionType.REPAYMENT);
        tx.setTimestamp(LocalDateTime.now());
        transactionRepository.save(tx);

        return new CreditCardResponse(
                updatedCard.getId(),
                updatedCard.getCardNumber(),
                updatedCard.getCardLimit(),
                updatedCard.getUsedFunds(),
                updatedCard.getStatus().name()
        );
    }

    private String generateRandomCardNumber(){
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int digit = random.nextInt(10);
            cardNumber.append(digit);
        }
        return cardNumber.toString();
    }

    private void assertCardAccess(CreditCard card) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Brak uwierzytelnionego użytkownika");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (isAdmin) {
            return;
        }

        String username = authentication.getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Użytkownik nie istnieje"));

        if (!user.getId().equals(card.getCustomerId())) {
            throw new AccessDeniedException("Brak dostępu do tej karty");
        }
    }
}
