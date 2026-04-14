package com.pichincha.software.engineer.back.repository;

import com.pichincha.software.engineer.back.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByIdAndActiveTrue(Long id);

    Optional<Account> findByNumberAndActiveTrue(String number);

    List<Account> findAllByActiveTrue();

    @Query(value = "SELECT MAX(CAST(number AS BIGINT)) FROM account", nativeQuery = true)
    Long findMaxNumber();
}
