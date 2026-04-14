package com.pichincha.software.engineer.back.repository;

import com.pichincha.software.engineer.back.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByIdAndActiveTrue(Long id);

    Optional<Client> findByIdentificationAndActiveTrue(String identification);

    List<Client> findAllByActiveTrue();
}
