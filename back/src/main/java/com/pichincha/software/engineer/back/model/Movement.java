package com.pichincha.software.engineer.back.model;

import com.pichincha.software.engineer.back.model.enums.MovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "movement",
    indexes = @Index(name = "idx_movement_account_id", columnList = "account_id")
)
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movement_seq_gen")
    @SequenceGenerator(name = "movement_seq_gen", sequenceName = "movement_seq", allocationSize = 1)
    Long id;

    @NotNull(message = "Date must not be null")
    @Column(nullable = false)
    Timestamp date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MovementType type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    BigDecimal value;

    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal balance;

    @Column(nullable = false)
    Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    Account account;
}
