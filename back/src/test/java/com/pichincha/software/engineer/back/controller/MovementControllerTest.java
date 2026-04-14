package com.pichincha.software.engineer.back.controller;

import com.pichincha.software.engineer.back.configuration.GlobalExceptionConfiguration;
import com.pichincha.software.engineer.back.exception.ApplicationException;
import com.pichincha.software.engineer.back.model.enums.MovementType;
import com.pichincha.software.engineer.back.service.MovementService;
import com.pichincha.software.engineer.back.service.dto.MovementDto;
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
class MovementControllerTest {

    @Mock
    private MovementService movementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MovementController(movementService))
                .setControllerAdvice(new GlobalExceptionConfiguration())
                .build();
    }

    @Test
    void createShouldReturnCreated() throws Exception {
        MovementDto response = validMovementDto();
        response.setId(1L);

        given(movementService.create(any(MovementDto.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovementRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void createShouldReturnBadRequestWhenValueInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidMovementRequestJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void findByIdShouldReturnOk() throws Exception {
        MovementDto response = validMovementDto();
        response.setId(1L);

        given(movementService.findById(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/movimientos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void findByIdShouldReturnNotFoundWhenServiceFails() throws Exception {
        given(movementService.findById(99L))
                .willThrow(new ApplicationException("Movement not found", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/movimientos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Movement not found"));
    }

    @Test
    void findAllShouldReturnOk() throws Exception {
        MovementDto response = validMovementDto();
        response.setId(1L);

        given(movementService.findAll()).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void updateShouldReturnOk() throws Exception {
        MovementDto response = validMovementDto();
        response.setId(1L);

        given(movementService.update(eq(1L), any(MovementDto.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/movimientos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovementRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        willDoNothing().given(movementService).delete(1L);

        mockMvc.perform(delete("/api/v1/movimientos/1"))
                .andExpect(status().isNoContent());
    }

    private MovementDto validMovementDto() {
        return MovementDto.builder()
                .date(1744588800000L)
                .type(MovementType.DEPOSIT)
                .value(new BigDecimal("15.00"))
                .balance(new BigDecimal("115.00"))
                .accountId(1L)
                .build();
    }

    private String validMovementRequestJson() {
        return """
                {
                  "date": 1744588800000,
                  "type": "DEPOSIT",
                  "value": 15.00,
                  "balance": 115.00,
                  "accountId": 1
                }
                """;
    }

    private String invalidMovementRequestJson() {
        return """
                {
                  "date": 1744588800000,
                  "type": "DEPOSIT",
                  "value": 0,
                  "balance": 115.00,
                  "accountId": 1
                }
                """;
    }
}
