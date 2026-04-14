package com.pichincha.software.engineer.back.service.impl;

import com.pichincha.software.engineer.back.exception.ApplicationException;
import com.pichincha.software.engineer.back.model.Account;
import com.pichincha.software.engineer.back.model.Movement;
import com.pichincha.software.engineer.back.repository.AccountRepository;
import com.pichincha.software.engineer.back.repository.MovementRepository;
import com.pichincha.software.engineer.back.service.MovementService;
import com.pichincha.software.engineer.back.service.dto.MovementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovementServiceImpl implements MovementService {

    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public MovementDto create(MovementDto movementDto) {
        validateMovementPayload(movementDto);

        try {
            Movement movement = toEntity(movementDto);
            movement.setId(null);
            return toDto(movementRepository.save(movement));
        } catch (DataIntegrityViolationException ex) {
            throw new ApplicationException("Movement data violates constraints", HttpStatus.CONFLICT);
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while creating movement", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public MovementDto findById(Long id) {
        try {
            return toDto(getMovementOrThrow(id));
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while searching movement", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<MovementDto> findAll() {
        try {
            return movementRepository.findAllByActiveTrue()
                    .stream()
                    .map(this::toDto)
                    .toList();
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while listing movements", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public MovementDto update(Long id, MovementDto movementDto) {
        validateMovementPayload(movementDto);

        try {
            Movement existingMovement = getMovementOrThrow(id);
            mergeMovement(existingMovement, movementDto);
            return toDto(movementRepository.save(existingMovement));
        } catch (DataIntegrityViolationException ex) {
            throw new ApplicationException("Movement data violates constraints", HttpStatus.CONFLICT);
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while updating movement", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            Movement existingMovement = getMovementOrThrow(id);
            existingMovement.setActive(false);
            movementRepository.save(existingMovement);
        } catch (DataIntegrityViolationException ex) {
            throw new ApplicationException("Movement cannot be deleted due to related data", HttpStatus.CONFLICT);
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while deleting movement", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateMovementPayload(MovementDto movementDto) {
        if (movementDto == null) {
            throw new ApplicationException("Movement payload is required", HttpStatus.BAD_REQUEST);
        }
        if (movementDto.getDate() == null) {
            throw new ApplicationException("Movement date is required", HttpStatus.BAD_REQUEST);
        }
        if (movementDto.getType() == null) {
            throw new ApplicationException("Movement type is required", HttpStatus.BAD_REQUEST);
        }
        if (movementDto.getValue() == null) {
            throw new ApplicationException("Movement value is required", HttpStatus.BAD_REQUEST);
        }
        if (movementDto.getBalance() == null) {
            throw new ApplicationException("Movement balance is required", HttpStatus.BAD_REQUEST);
        }
        if (movementDto.getAccountId() == null) {
            throw new ApplicationException("Account id is required", HttpStatus.BAD_REQUEST);
        }
    }

    private Movement getMovementOrThrow(Long id) {
        if (id == null) {
            throw new ApplicationException("Movement id is required", HttpStatus.BAD_REQUEST);
        }

        return movementRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApplicationException("Movement not found with id: " + id, HttpStatus.NOT_FOUND));
    }

    private Account getAccountOrThrow(Long accountId) {
        return accountRepository.findByIdAndActiveTrue(accountId)
                .orElseThrow(() -> new ApplicationException("Account not found with id: " + accountId, HttpStatus.NOT_FOUND));
    }

    private Movement toEntity(MovementDto movementDto) {
        Movement movement = new Movement();
        movement.setDate(new Timestamp(movementDto.getDate()));
        movement.setType(movementDto.getType());
        movement.setBalance(movementDto.getBalance());
        movement.setValue(movementDto.getValue());
        movement.setActive(true);
        movement.setAccount(getAccountOrThrow(movementDto.getAccountId()));
        return movement;
    }

    private void mergeMovement(Movement movement, MovementDto movementDto) {
        movement.setDate(new Timestamp(movementDto.getDate()));
        movement.setBalance(movementDto.getBalance());
        movement.setType(movementDto.getType());
        movement.setValue(movementDto.getValue());
        movement.setAccount(getAccountOrThrow(movementDto.getAccountId()));
    }

    private MovementDto toDto(Movement movement) {
        return MovementDto.builder()
                .id(movement.getId())
                .date(movement.getDate() != null ? movement.getDate().getTime() : null)
                .type(movement.getType())
                .value(movement.getValue())
                .balance(movement.getBalance())
                .accountId(movement.getAccount() != null ? movement.getAccount().getId() : null)
                .build();
    }
}

