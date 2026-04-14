package com.pichincha.software.engineer.back.controller;

import com.pichincha.software.engineer.back.configuration.GlobalExceptionConfiguration;
import com.pichincha.software.engineer.back.exception.ApplicationException;
import com.pichincha.software.engineer.back.model.enums.AccountType;
import com.pichincha.software.engineer.back.service.AccountService;
import com.pichincha.software.engineer.back.service.dto.AccountDto;
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
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController(accountService))
                .setControllerAdvice(new GlobalExceptionConfiguration())
                .build();
    }

    @Test
    void createShouldReturnCreated() throws Exception {
        AccountDto response = validAccountDto();
        response.setId(1L);
        response.setNumber("ACC-001");

        given(accountService.create(any(AccountDto.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.number").value("ACC-001"));
    }

    @Test
    void createShouldReturnBadRequestWhenInitialBalanceInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCreateRequestJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void findByIdShouldReturnOk() throws Exception {
        AccountDto response = validAccountDto();
        response.setId(1L);
        response.setNumber("ACC-001");

        given(accountService.findById("ACC-001")).willReturn(response);

        mockMvc.perform(get("/api/v1/cuentas/ACC-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("ACC-001"));
    }

    @Test
    void findByIdShouldReturnNotFoundWhenServiceFails() throws Exception {
        given(accountService.findById("ACC-404"))
                .willThrow(new ApplicationException("Account not found", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/cuentas/ACC-404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void findAllShouldReturnOk() throws Exception {
        AccountDto first = validAccountDto();
        first.setId(1L);
        first.setNumber("ACC-001");

        AccountDto second = validAccountDto();
        second.setId(2L);
        second.setNumber("ACC-002");

        given(accountService.findAll()).willReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/cuentas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value("ACC-001"))
                .andExpect(jsonPath("$[1].number").value("ACC-002"));
    }

    @Test
    void updateShouldReturnOk() throws Exception {
        AccountDto response = validAccountDto();
        response.setId(1L);
        response.setNumber("ACC-001");

        given(accountService.update(eq(1L), any(AccountDto.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/cuentas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        willDoNothing().given(accountService).delete(1L);

        mockMvc.perform(delete("/api/v1/cuentas/1"))
                .andExpect(status().isNoContent());
    }

    private AccountDto validAccountDto() {
        return AccountDto.builder()
                .type(AccountType.CHECKING)
                .initialBalance(new BigDecimal("100.00"))
                .currentBalance(new BigDecimal("100.00"))
                .active(true)
                .clientId(1L)
                .build();
    }

    private String validCreateRequestJson() {
        return """
                {
                  "type": "CHECKING",
                  "initialBalance": 100.00,
                  "currentBalance": 100.00,
                  "active": true,
                  "clientId": 1
                }
                """;
    }

    private String invalidCreateRequestJson() {
        return """
                {
                  "type": "CHECKING",
                  "initialBalance": 0,
                  "currentBalance": 100.00,
                  "active": true,
                  "clientId": 1
                }
                """;
    }
}
