package com.pichincha.software.engineer.back.controller;

import com.pichincha.software.engineer.back.configuration.GlobalExceptionConfiguration;
import com.pichincha.software.engineer.back.exception.ApplicationException;
import com.pichincha.software.engineer.back.model.enums.Gender;
import com.pichincha.software.engineer.back.service.ClientService;
import com.pichincha.software.engineer.back.service.dto.ClientDto;
import com.pichincha.software.engineer.back.service.dto.report.AccountReportRequestDto;
import com.pichincha.software.engineer.back.service.dto.report.AccountReportResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ClientController(clientService))
                .setControllerAdvice(new GlobalExceptionConfiguration())
                .build();
    }

    @Test
    void createShouldReturnCreated() throws Exception {
        ClientDto response = validClientDto();
        response.setId(1L);

        given(clientService.create(any(ClientDto.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClientRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void createShouldReturnBadRequestWhenNameInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidClientRequestJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createReportShouldReturnCreatedWithAccounts() throws Exception {
        AccountReportResponseDto response = AccountReportResponseDto.builder()
                .accounts(List.of(
                        AccountReportResponseDto.Account.builder()
                                .number("ACC-001")
                                .balance(new BigDecimal("120.00"))
                                .deposits(new BigDecimal("30.00"))
                                .withdraws(new BigDecimal("10.00"))
                                .build()))
                .build();

        given(clientService.generateReport(any(AccountReportRequestDto.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/clientes/reporte")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validReportRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accounts[0].number").value("ACC-001"));
    }

    @Test
    void createReportShouldReturnBadRequestWhenIdentificationInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/reporte")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidReportRequestJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void findByIdShouldReturnNotFoundWhenServiceFails() throws Exception {
        given(clientService.findById("1234567894"))
                .willThrow(new ApplicationException("Client not found", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/clientes/1234567894"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void findAllShouldReturnOk() throws Exception {
        ClientDto response = validClientDto();
        response.setId(1L);

        given(clientService.findAll()).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void updateShouldReturnOk() throws Exception {
        ClientDto response = validClientDto();
        response.setId(1L);

        given(clientService.update(eq(1L), any(ClientDto.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClientRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        willDoNothing().given(clientService).delete(1L);

        mockMvc.perform(delete("/api/v1/clientes/1"))
                .andExpect(status().isNoContent());
    }

    private ClientDto validClientDto() {
        return ClientDto.builder()
                .name("Juan Perez")
                .gender(Gender.MALE)
                .age(30)
                .identification("1234567894")
                .address("Main St")
                .phone("0987654321")
                .password("secret")
                .active(true)
                .build();
    }

    private String validClientRequestJson() {
        return """
                {
                  "name": "Juan Perez",
                  "gender": "MALE",
                  "age": 30,
                  "identification": "1234567894",
                  "address": "Main St",
                  "phone": "0987654321",
                  "password": "secret",
                  "active": true
                }
                """;
    }

    private String invalidClientRequestJson() {
        return """
                {
                  "name": "",
                  "gender": "MALE",
                  "age": 30,
                  "identification": "1234567894",
                  "address": "Main St",
                  "phone": "0987654321",
                  "password": "secret",
                  "active": true
                }
                """;
    }

    private String validReportRequestJson() {
        return """
                {
                  "identification": "1234567894",
                  "startDate": 1744588800000,
                  "endDate": 1744675199000,
                  "pdf": false
                }
                """;
    }

    private String invalidReportRequestJson() {
        return """
                {
                  "identification": "123",
                  "startDate": 1744588800000,
                  "endDate": 1744675199000,
                  "pdf": false
                }
                """;
    }
}
