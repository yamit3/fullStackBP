package com.pichincha.software.engineer.back.repository;

import com.pichincha.software.engineer.back.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByIdAndActiveTrue(Long id);

    List<Account> findAllByActiveTrue();
}
