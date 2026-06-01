package com.github.nikolospl.creditcardmanager.repository;

import com.github.nikolospl.creditcardmanager.model.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, UUID> {
    List<CardTransaction> findByCardIdOrderByTimestampDesc(UUID cardId);
}
