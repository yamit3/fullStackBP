package com.pichincha.software.engineer.back.repository;

import com.pichincha.software.engineer.back.model.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface MovementRepository extends JpaRepository<Movement, Long> {

    Optional<Movement> findByIdAndActiveTrue(Long id);

    List<Movement> findAllByActiveTrue();

    List<Movement> findByAccountIdInAndDateBetweenAndActiveTrue(List<Long> accountIds, Timestamp startDate, Timestamp endDate);

    @Query( "SELECT SUM(ABS(m.value)) FROM Movement m WHERE m.account.id = :accountId AND m.type = 'WITHDRAW' AND m.active = true AND CAST(m.date AS DATE) = CAST(:date AS DATE)")
    Optional<BigDecimal> sumDailyWithdraws(@Param("accountId") Long accountId, @Param("date") Timestamp date);
}
