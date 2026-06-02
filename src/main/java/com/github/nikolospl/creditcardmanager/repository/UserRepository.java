package com.github.nikolospl.creditcardmanager.repository;

import com.github.nikolospl.creditcardmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findUserById(UUID id);
    List<User> findUserByUsername(String lastName);
}
