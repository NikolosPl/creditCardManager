package com.github.nikolospl.creditcardmanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCardRequest(@NotNull UUID customerId, String cardNumber, @Positive(message = "Początkowy limit musi być większy od zera!") BigDecimal initialLimit) { }
