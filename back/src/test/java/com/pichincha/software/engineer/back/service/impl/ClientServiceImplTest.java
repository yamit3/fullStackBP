package com.pichincha.software.engineer.back.service.impl;

import com.pichincha.software.engineer.back.exception.ApplicationException;
import com.pichincha.software.engineer.back.model.Account;
import com.pichincha.software.engineer.back.model.Client;
import com.pichincha.software.engineer.back.model.Movement;
import com.pichincha.software.engineer.back.model.enums.Gender;
import com.pichincha.software.engineer.back.model.enums.MovementType;
import com.pichincha.software.engineer.back.repository.ClientRepository;
import com.pichincha.software.engineer.back.repository.MovementRepository;
import com.pichincha.software.engineer.back.service.PdfReportService;
import com.pichincha.software.engineer.back.service.dto.ClientDto;
import com.pichincha.software.engineer.back.service.dto.report.AccountReportRequestDto;
import com.pichincha.software.engineer.back.service.dto.report.AccountReportResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private PdfReportService pdfReportService;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    void createShouldHashPasswordAndSaveClient() {
        ClientDto dto = ClientDto.builder()
                .name("John Doe")
                .gender(Gender.MALE)
                .age(30)
                .identification("1234567894")
                .address("Main")
                .phone("0999999999")
                .password("plainSecret")
                .active(true)
                .build();

        when(clientRepository.saveAndFlush(any(Client.class))).thenAnswer(invocation -> {
            Client c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        ClientDto result = clientService.create(dto);

        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).saveAndFlush(captor.capture());
        String storedPassword = captor.getValue().getPassword();
        assertNotNull(storedPassword);
        assertTrue(storedPassword.matches("\\d{3}:[a-f0-9]{64}"));
        assertFalse(storedPassword.contains("plainSecret"));
    }

    @Test
    void createShouldThrowBadRequestWhenPasswordIsMissing() {
        ClientDto dto = ClientDto.builder()
                .name("John Doe")
                .gender(Gender.MALE)
                .age(30)
                .identification("1234567894")
                .address("Main")
                .phone("0999999999")
                .password(" ")
                .active(true)
                .build();

        ApplicationException ex = assertThrows(ApplicationException.class, () -> clientService.create(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Password is required", ex.getMessage());
    }

    @Test
    void generateReportShouldThrowBadRequestWhenDateRangeIsInvalid() {
        AccountReportRequestDto request = AccountReportRequestDto.builder()
                .identification("1234567894")
                .startDate(2000L)
                .endDate(1000L)
                .pdf(false)
                .build();

        ApplicationException ex = assertThrows(ApplicationException.class, () -> clientService.generateReport(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Invalid date range", ex.getMessage());
    }

    @Test
    void generateReportShouldAggregateDepositsAndWithdrawsWhenPdfFalse() {
        Account account = new Account();
        account.setId(10L);
        account.setNumber("100010");
        account.setCurrentBalance(new BigDecimal("500.00"));

        Client client = new Client();
        client.setId(1L);
        client.setIdentification("1234567894");
        client.setAccounts(List.of(account));

        Movement deposit = new Movement();
        deposit.setAccount(account);
        deposit.setType(MovementType.DEPOSIT);
        deposit.setValue(new BigDecimal("150.00"));
        deposit.setDate(new Timestamp(System.currentTimeMillis()));

        Movement withdraw = new Movement();
        withdraw.setAccount(account);
        withdraw.setType(MovementType.WITHDRAW);
        withdraw.setValue(new BigDecimal("-40.00"));
        withdraw.setDate(new Timestamp(System.currentTimeMillis()));

        when(clientRepository.findByIdentificationAndActiveTrue("1234567894")).thenReturn(Optional.of(client));
        when(movementRepository.findByAccountIdInAndDateBetweenAndActiveTrue(anyList(), any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(List.of(deposit, withdraw));

        AccountReportRequestDto request = AccountReportRequestDto.builder()
                .identification("1234567894")
                .startDate(1744588800000L)
                .endDate(1744675199000L)
                .pdf(false)
                .build();

        AccountReportResponseDto result = clientService.generateReport(request);

        assertNotNull(result.getAccounts());
        assertEquals(1, result.getAccounts().size());
        assertEquals(new BigDecimal("150.00"), result.getAccounts().get(0).getDeposits());
        assertEquals(new BigDecimal("40.00"), result.getAccounts().get(0).getWithdraws());
        assertNull(result.getPdf());
    }

    @Test
    void generateReportShouldReturnBase64PdfWhenPdfFlagIsTrue() {
        Account account = new Account();
        account.setId(10L);
        account.setNumber("100010");
        account.setCurrentBalance(new BigDecimal("500.00"));

        Client client = new Client();
        client.setId(1L);
        client.setIdentification("1234567894");
        client.setAccounts(List.of(account));

        when(clientRepository.findByIdentificationAndActiveTrue("1234567894")).thenReturn(Optional.of(client));
        when(movementRepository.findByAccountIdInAndDateBetweenAndActiveTrue(anyList(), any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(List.of());
        when(pdfReportService.generateAccountReportPdf(anyList())).thenReturn("base64-pdf-content");

        AccountReportRequestDto request = AccountReportRequestDto.builder()
                .identification("1234567894")
                .startDate(1744588800000L)
                .endDate(1744675199000L)
                .pdf(true)
                .build();

        AccountReportResponseDto result = clientService.generateReport(request);

        assertEquals("base64-pdf-content", result.getPdf());
        assertNull(result.getAccounts());
    }

    @Test
    void findByIdShouldThrowBadRequestWhenIdentificationIsBlank() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> clientService.findById("  "));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Client identification is required", ex.getMessage());
    }
}

