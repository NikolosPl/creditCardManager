package com.github.nikolospl.creditcardmanager.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCardResponse(UUID id, String cardNumber, BigDecimal cardLimit, BigDecimal usedFunds, String status) {
}
