package com.pichincha.software.engineer.back.service;

import com.pichincha.software.engineer.back.service.dto.ClientDto;

import java.util.List;

public interface ClientService {

    ClientDto create(ClientDto clientDto);

    ClientDto findById(Long id);

    List<ClientDto> findAll();

    ClientDto update(Long id, ClientDto clientDto);

    void delete(Long id);
}
