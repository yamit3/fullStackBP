package com.pichincha.software.engineer.back.model;

import com.pichincha.software.engineer.back.model.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "account",
    indexes = @Index(name = "idx_account_client_id", columnList = "client_id")
)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq_gen")
    @SequenceGenerator(name = "account_seq_gen", sequenceName = "account_seq", allocationSize = 1)
    Long id;

    @Column(nullable = false, unique = true)
    String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AccountType type;

    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal initialBalance;

    @Column(nullable = false)
    Boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    Client client;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    List<Movement> movements = new ArrayList<>();
}
