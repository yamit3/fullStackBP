package com.pichincha.software.engineer.back.service.impl;

import com.pichincha.software.engineer.back.exception.ApplicationException;
import com.pichincha.software.engineer.back.model.Account;
import com.pichincha.software.engineer.back.model.Client;
import com.pichincha.software.engineer.back.model.Movement;
import com.pichincha.software.engineer.back.model.enums.MovementType;
import com.pichincha.software.engineer.back.repository.ClientRepository;
import com.pichincha.software.engineer.back.repository.MovementRepository;
import com.pichincha.software.engineer.back.service.ClientService;
import com.pichincha.software.engineer.back.service.PdfReportService;
import com.pichincha.software.engineer.back.service.dto.ClientDto;
import com.pichincha.software.engineer.back.service.dto.report.AccountReportRequestDto;
import com.pichincha.software.engineer.back.service.dto.report.AccountReportResponseDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ClientServiceImpl implements ClientService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    ClientRepository clientRepository;
    MovementRepository movementRepository;
    PdfReportService pdfReportService;

    @Override
    @Transactional
    public ClientDto create(ClientDto clientDto) {
        try {
            Client client = toEntity(clientDto);
            client.setId(null);
            return toDto(clientRepository.saveAndFlush(client));
        } catch (DataIntegrityViolationException ex) {
            throw new ApplicationException("Client data violates constraints", HttpStatus.CONFLICT);
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while creating client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ClientDto findById(String identification) {
        try {
            return toDto(getClientByIdentificationOrThrow(identification));
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while searching client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<ClientDto> findAll() {
        try {
            return clientRepository.findAllByActiveTrue()
                    .stream()
                    .map(this::toDto)
                    .toList();
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while listing clients", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ClientDto update(Long id, ClientDto clientDto) {
        try {
            Client existingClient = getClientOrThrow(id);
            mergeClient(existingClient, clientDto);
            return toDto(clientRepository.save(existingClient));
        } catch (DataIntegrityViolationException ex) {
            throw new ApplicationException("Client data violates constraints", HttpStatus.CONFLICT);
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while updating client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            Client existingClient = getClientOrThrow(id);
            existingClient.setActive(false);
            clientRepository.save(existingClient);
        }  catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while deleting client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional( readOnly = true )
    public AccountReportResponseDto generateReport(AccountReportRequestDto request) {

        if (request.getEndDate() < request.getStartDate()) {
            throw new ApplicationException("Invalid date range", HttpStatus.BAD_REQUEST);
        }

        Client client = getClientByIdentificationOrThrow(request.getIdentification());
        List<Account> accounts = client.getAccounts();
        Map<Long, AccountReportResponseDto.Account> reportAccounts = new HashMap<>();

        accounts.forEach(account -> reportAccounts.put(
                account.getId(),
                AccountReportResponseDto.Account.builder()
                        .number(account.getNumber())
                        .balance(account.getCurrentBalance())
                        .deposits(BigDecimal.ZERO)
                        .withdraws(BigDecimal.ZERO)
                        .build()
        ));

        List<Movement> movements = movementRepository.findByAccountIdInAndDateBetweenAndActiveTrue(
                accounts.stream().map(Account::getId).toList(),
                toStartOfDay(request.getStartDate()),
                toEndOfDay(request.getEndDate())
        );

        movements.forEach(movement -> {
            AccountReportResponseDto.Account account = reportAccounts.get(movement.getAccount().getId());
            if(movement.getType() == MovementType.DEPOSIT){
                account.setDeposits( account.getDeposits().add( movement.getValue().abs()));
            }
            else {
                account.setWithdraws( account.getWithdraws().add( movement.getValue().abs()));
            }
        });


        List<AccountReportResponseDto.Account> reportList = new ArrayList<>(reportAccounts.values());

        if (request.isPdf()) {
            String base64Pdf = pdfReportService.generateAccountReportPdf(reportList);
            return AccountReportResponseDto.builder()
                    .pdf(base64Pdf)
                    .build();
        } else {
            return AccountReportResponseDto.builder()
                    .accounts(reportList)
                    .build();
        }
    }

    private Timestamp toStartOfDay(Long epochMillis) {
        LocalDateTime start = Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay();
        return Timestamp.valueOf(start);
    }

    private Timestamp toEndOfDay(Long epochMillis) {
        LocalDateTime end = Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atTime(23, 59, 59, 999_000_000);
        return Timestamp.valueOf(end);
    }

    private Client getClientOrThrow(Long id) {
        if (id == null) {
            throw new ApplicationException("Client id is required", HttpStatus.BAD_REQUEST);
        }

        return clientRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApplicationException("Client not found with id: " + id, HttpStatus.NOT_FOUND));
    }

    private Client getClientByIdentificationOrThrow(String identification) {
        if (identification == null || identification.isBlank()) {
            throw new ApplicationException("Client identification is required", HttpStatus.BAD_REQUEST);
        }

        return clientRepository.findByIdentificationAndActiveTrue(identification)
                .orElseThrow(() -> new ApplicationException("Client not found with identification: " + identification, HttpStatus.NOT_FOUND));
    }

    private void mergeClient(Client client, ClientDto clientDto) {
        client.setGender(clientDto.getGender());
        client.setName(clientDto.getName());
        client.setAge(clientDto.getAge());
        client.setPhone(clientDto.getPhone());
        client.setIdentification(clientDto.getIdentification());
        client.setPassword(hashPasswordWithRandomSalt(clientDto.getPassword()));
        client.setAddress(clientDto.getAddress());
        client.setActive(clientDto.getActive());
    }

    private Client toEntity(ClientDto clientDto) {
        Client client = new Client();
        client.setName(clientDto.getName());
        client.setGender(clientDto.getGender());
        client.setAge(clientDto.getAge());
        client.setIdentification(clientDto.getIdentification());
        client.setAddress(clientDto.getAddress());
        client.setPhone(clientDto.getPhone());
        client.setPassword(hashPasswordWithRandomSalt(clientDto.getPassword()));
        client.setActive(clientDto.getActive());
        return client;
    }

    private String hashPasswordWithRandomSalt(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new ApplicationException("Password is required", HttpStatus.BAD_REQUEST);
        }

        String salt = String.format("%03d", SECURE_RANDOM.nextInt(1000));
        String hash = sha256Hex(rawPassword + ":" + salt);
        return salt + ":" + hash;
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte hashByte : hashBytes) {
                hex.append(String.format("%02x", hashByte));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new ApplicationException("Unable to hash password", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ClientDto toDto(Client client) {
        return ClientDto.builder()
                .id(client.getId())
                .name(client.getName())
                .gender(client.getGender())
                .age(client.getAge())
                .identification(client.getIdentification())
                .address(client.getAddress())
                .phone(client.getPhone())
                .active(client.getActive())
                .build();
    }
}
