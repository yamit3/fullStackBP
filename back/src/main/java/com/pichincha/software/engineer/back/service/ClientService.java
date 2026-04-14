package com.pichincha.software.engineer.back.service;

import com.pichincha.software.engineer.back.service.dto.ClientDto;
import com.pichincha.software.engineer.back.service.dto.report.AccountReportRequestDto;
import com.pichincha.software.engineer.back.service.dto.report.AccountReportResponseDto;

import java.util.List;

public interface ClientService {

    ClientDto create(ClientDto clientDto);

    ClientDto findById(String identification);

    List<ClientDto> findAll();

    ClientDto update(Long id, ClientDto clientDto);

    void delete(Long id);

    AccountReportResponseDto generateReport(AccountReportRequestDto request);
}
