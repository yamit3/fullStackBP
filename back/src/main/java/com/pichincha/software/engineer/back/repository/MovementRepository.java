package com.pichincha.software.engineer.back.repository;

import com.pichincha.software.engineer.back.model.Movement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface MovementRepository extends JpaRepository<Movement, Long> {

    Optional<Movement> findByIdAndActiveTrue(Long id);

    List<Movement> findAllByActiveTrue();

    List<Movement> findByAccount_IdInAndDateBetweenAndActiveTrue(List<Long> accountIds, Timestamp startDate, Timestamp endDate);
}
