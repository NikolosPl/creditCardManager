package com.github.nikolospl.creditcardmanager.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import java.math.BigDecimal;

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

}
