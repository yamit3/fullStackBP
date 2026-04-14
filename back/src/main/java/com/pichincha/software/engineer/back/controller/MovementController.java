package com.pichincha.software.engineer.back.controller;

import com.pichincha.software.engineer.back.service.MovementService;
import com.pichincha.software.engineer.back.service.dto.MovementDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movimientos")
@RequiredArgsConstructor
public class MovementController {

    private final MovementService movementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovementDto create(@Valid @RequestBody MovementDto movementDto) {
        return movementService.create(movementDto);
    }

    @GetMapping("/{id}")
    public MovementDto findById(@PathVariable Long id) {
        return movementService.findById(id);
    }

    @GetMapping
    public List<MovementDto> findAll() {
        return movementService.findAll();
    }

    @PutMapping("/{id}")
    public MovementDto update(@PathVariable Long id, @Valid @RequestBody MovementDto movementDto) {
        return movementService.update(id, movementDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        movementService.delete(id);
    }
}

