package com.github.nikolospl.creditcardmanager.model;


import com.github.nikolospl.creditcardmanager.exception.CardOperationException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "credit_cards")
@Getter @Setter
public class    CreditCard {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String cardNumber;

    private UUID customerId;
    private BigDecimal cardLimit;

    private BigDecimal usedFunds;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    public void activate() {
        if (status == CardStatus.CANCELLED) {
            throw new CardOperationException("Nie można aktywować anulowanej karty!");
        }
        status = CardStatus.ACTIVE;
    }

    public void block() {
        if (status == CardStatus.CANCELLED) {
            throw new CardOperationException("Nie można zablokować anulowanej karty!");
        }
        if (status == CardStatus.BLOCKED) {
            throw new CardOperationException("Karta jest już zablokowana!");
        }
        status = CardStatus.BLOCKED;
    }

    public void processPayment(BigDecimal amount) {
        validatePositiveAmount(amount);
        if (status != CardStatus.ACTIVE) {
            throw new CardOperationException("Nie można dokonać płatności kartą nieaktywną!");
        }

        BigDecimal availableFunds = cardLimit.subtract(usedFunds);
        if (amount.compareTo(availableFunds) > 0) {
            throw new CardOperationException("Brak wystarczających środków na karcie!");
        }

        usedFunds = usedFunds.add(amount);
    }

    public void repay(BigDecimal amount) {
        validatePositiveAmount(amount);
        if (status != CardStatus.ACTIVE) {
            throw new CardOperationException("Nie można spłacić karty nieaktywnej!");
        }

        if (amount.compareTo(usedFunds) > 0) {
            throw new CardOperationException("Nie możesz spłacić więcej, niż wynosi twój aktualny dług!");
        }

        usedFunds = usedFunds.subtract(amount);
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardOperationException("Kwota transakcji musi być większa od zera!");
        }
    }

}
