package com.pichincha.software.engineer.back.service;

import com.pichincha.software.engineer.back.service.dto.MovementDto;

import java.util.List;

public interface MovementService {

    MovementDto create(MovementDto movementDto);

    MovementDto findById(Long id);

    List<MovementDto> findAll();

    MovementDto update(Long id, MovementDto movementDto);

    void delete(Long id);


}
