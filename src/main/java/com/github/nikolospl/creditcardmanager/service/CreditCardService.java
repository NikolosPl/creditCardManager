package com.github.nikolospl.creditcardmanager.service;

import com.github.nikolospl.creditcardmanager.dto.CreateCardRequest;
import com.github.nikolospl.creditcardmanager.dto.CreditCardResponse;
import com.github.nikolospl.creditcardmanager.dto.PayRequest;
import com.github.nikolospl.creditcardmanager.exception.CardNotFoundException;
import com.github.nikolospl.creditcardmanager.exception.CardOperationException;
import com.github.nikolospl.creditcardmanager.model.CardStatus;
import com.github.nikolospl.creditcardmanager.model.CardTransaction;
import com.github.nikolospl.creditcardmanager.model.CreditCard;
import com.github.nikolospl.creditcardmanager.model.TransactionType;
import com.github.nikolospl.creditcardmanager.repository.CardTransactionRepository;
import com.github.nikolospl.creditcardmanager.repository.CreditCardRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreditCardService {
    private final CreditCardRepository repository;
    private final CardTransactionRepository transactionRepository;

    public CreditCardService(CreditCardRepository repository, CardTransactionRepository transactionRepository){
        this.repository = repository;
        this.transactionRepository = transactionRepository;
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
                .map(card -> new CreditCardResponse(
                        card.getId(),
                        card.getCardNumber(),
                        card.getCardLimit(),
                        card.getUsedFunds(),
                        card.getStatus().name()
                ))
                .orElseThrow(() -> new CardNotFoundException("Karta o podanym ID nie została znaleziona!"));
    }
    @Transactional
    public CreditCardResponse blockCard(UUID id){
        CreditCard card = repository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Karta o podanym ID nie została znaleziona!"));

        if(card.getStatus() == CardStatus.BLOCKED){
            throw new CardOperationException("Karta jest już zablokowana!");
        }

        card.setStatus(CardStatus.BLOCKED);

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
                .orElseThrow(() -> new CardNotFoundException("Karta o podanym ID nie została znaleziona!"));

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
        CreditCard card = repository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Karta o podanym ID nie istnieje!"));

        if(card.getStatus() == CardStatus.BLOCKED){
            throw new CardOperationException("Nie można dokonać płatności zablokowaną kartą!");
        }

        BigDecimal availableFunds = card.getCardLimit().subtract(card.getUsedFunds());
        if(request.amount().compareTo(availableFunds) > 0){
            throw new CardOperationException("Brak wystarczających środków na karcie!");
        }

        card.setUsedFunds(card.getUsedFunds().add(request.amount()));
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
        CreditCard card = repository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Karta o podanym Id nie istnieje!"));

        if(card.getStatus() == CardStatus.BLOCKED){
            throw new CardOperationException("Nie można spłacić zablokowanej karty!");
        }

        if(request.amount().compareTo(card.getUsedFunds()) > 0){
            throw new CardOperationException("Nie możesz spłacić więcej, niż wynosi twój aktualny dług!");
        }

        card.setUsedFunds(card.getUsedFunds().subtract(request.amount()));
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
            int digit = (int) (Math.random() * 10);
            cardNumber.append(digit);
        }
        return cardNumber.toString();
    }
}
