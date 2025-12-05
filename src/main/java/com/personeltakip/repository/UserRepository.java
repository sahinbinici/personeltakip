package com.personeltakip.repository;

import com.personeltakip.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Additional method to find user by tckiml
    Optional<User> findByTckiml(Long tckiml);
}