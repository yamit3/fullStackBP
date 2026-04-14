package com.pichincha.software.engineer.back.repository;

import com.pichincha.software.engineer.back.model.Movement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementRepository extends JpaRepository<Movement, Long> {
}
