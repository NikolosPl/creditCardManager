package com.github.nikolospl.creditcardmanager.repository;

import com.github.nikolospl.creditcardmanager.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {
    List<CreditCard> findByCustomerId(UUID customerId);
    Optional<CreditCard> findByCardNumber(String cardNumber);
}
