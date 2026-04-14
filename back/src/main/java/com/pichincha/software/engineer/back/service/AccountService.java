package com.pichincha.software.engineer.back.service;

import com.pichincha.software.engineer.back.service.dto.AccountDto;

import java.util.List;

public interface AccountService {

    AccountDto create(AccountDto accountDto);

    AccountDto findById(String number);

    List<AccountDto> findAll();

    AccountDto update(Long id, AccountDto accountDto);

    void delete(Long id);
}
