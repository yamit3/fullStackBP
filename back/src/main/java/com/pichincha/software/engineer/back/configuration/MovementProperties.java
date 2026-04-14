package com.pichincha.software.engineer.back.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "movement")
@Getter
@Setter
public class MovementProperties {
    private BigDecimal dailyWithdrawLimit;
}

