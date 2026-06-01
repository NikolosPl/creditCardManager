package com.github.nikolospl.creditcardmanager.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PayRequest(@Positive(message = "Kwota operacji musi być dodatnia!") BigDecimal amount) {
}
