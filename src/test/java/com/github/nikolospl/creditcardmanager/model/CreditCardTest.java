package com.github.nikolospl.creditcardmanager.model;

import com.github.nikolospl.creditcardmanager.exception.CardOperationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreditCardTest {

    @Test
    void activateShouldThrowWhenCardIsCancelled() {
        CreditCard card = new CreditCard();
        card.setStatus(CardStatus.CANCELLED);

        assertThrows(CardOperationException.class, card::activate);
    }

    @Test
    void processPaymentShouldIncreaseUsedFundsWhenAmountIsValid() {
        CreditCard card = new CreditCard();
        card.setStatus(CardStatus.ACTIVE);
        card.setCardLimit(new BigDecimal("1000.00"));
        card.setUsedFunds(new BigDecimal("100.00"));

        card.processPayment(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("300.00"), card.getUsedFunds());
    }
}
