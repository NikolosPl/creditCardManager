package com.github.nikolospl.creditcardmanager.repository;

import com.github.nikolospl.creditcardmanager.model.CreditCard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {
    List<CreditCard> findByCustomerId(UUID customerId);
    Optional<CreditCard> findByCardNumber(String cardNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CreditCard c WHERE c.id = :id")
    Optional<CreditCard> findByIdForUpdate(@Param("id") UUID id);
}
