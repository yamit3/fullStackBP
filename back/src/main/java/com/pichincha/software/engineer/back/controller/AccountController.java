package com.pichincha.software.engineer.back.controller;

import com.pichincha.software.engineer.back.service.AccountService;
import com.pichincha.software.engineer.back.service.dto.AccountDto;
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
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto create(@Valid @RequestBody AccountDto accountDto) {
        return accountService.create(accountDto);
    }

    @GetMapping("/{number}")
    public AccountDto findById(@PathVariable String number) {
        return accountService.findById(number);
    }

    @GetMapping
    public List<AccountDto> findAll() {
        return accountService.findAll();
    }

    @PutMapping("/{id}")
    public AccountDto update(@PathVariable Long id, @Valid @RequestBody AccountDto accountDto) {
        return accountService.update(id, accountDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        accountService.delete(id);
    }
}
