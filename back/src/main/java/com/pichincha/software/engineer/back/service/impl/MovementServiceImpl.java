package com.pichincha.software.engineer.back.service.impl;

import com.pichincha.software.engineer.back.configuration.MovementProperties;
import com.pichincha.software.engineer.back.exception.ApplicationException;
import com.pichincha.software.engineer.back.model.Account;
import com.pichincha.software.engineer.back.model.Movement;
import com.pichincha.software.engineer.back.model.enums.MovementType;
import com.pichincha.software.engineer.back.repository.AccountRepository;
import com.pichincha.software.engineer.back.repository.MovementRepository;
import com.pichincha.software.engineer.back.service.MovementService;
import com.pichincha.software.engineer.back.service.dto.MovementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovementServiceImpl implements MovementService {

    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;
    private final MovementProperties movementProperties;

    @Override
    @Transactional
    public MovementDto create(MovementDto movementDto) {
        validateMovementPayload(movementDto);

        try {

            Account account = getAccountOrThrow(movementDto.getAccountId());

            BigDecimal adjustedValue = movementDto.getValue();
            if (MovementType.WITHDRAW.equals(movementDto.getType())) {
                adjustedValue = adjustedValue.negate();
            }

            BigDecimal newBalance = account.getCurrentBalance().add(adjustedValue);

            if (MovementType.WITHDRAW.equals(movementDto.getType()) && account.getCurrentBalance().signum() == 0) {
                throw new ApplicationException("No available balance", HttpStatus.BAD_REQUEST);
            }

            if (MovementType.WITHDRAW.equals(movementDto.getType())) {
                validateDailyWithdrawLimit(account.getId(), adjustedValue);
            }

            Movement movement = toEntity(movementDto, account, adjustedValue, newBalance);
            movement.setId(null);

            account.setCurrentBalance(newBalance);
            accountRepository.save(account);

            return toDto(movementRepository.saveAndFlush(movement));
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

    private void validateDailyWithdrawLimit(Long accountId, BigDecimal withdrawValue) {
        Timestamp date = new Timestamp(System.currentTimeMillis());
        BigDecimal dailyWithdraws = movementRepository.sumDailyWithdraws(accountId, date)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalWithdraws = dailyWithdraws.add(withdrawValue.abs());

        if (totalWithdraws.compareTo(movementProperties.getDailyWithdrawLimit()) > 0) {
            throw new ApplicationException(
                    "Daily withdraw limit exceeded. Limit: " + movementProperties.getDailyWithdrawLimit(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private Movement toEntity(MovementDto movementDto, Account account, BigDecimal adjustedValue, BigDecimal newBalance) {
        Movement movement = new Movement();
        movement.setDate(new Timestamp(System.currentTimeMillis()));
        movement.setType(movementDto.getType());
        movement.setBalance(newBalance);
        movement.setValue(adjustedValue);
        movement.setActive(true);
        movement.setAccount(account);
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

