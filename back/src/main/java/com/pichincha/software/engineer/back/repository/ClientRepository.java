package com.pichincha.software.engineer.back.repository;

import com.pichincha.software.engineer.back.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
