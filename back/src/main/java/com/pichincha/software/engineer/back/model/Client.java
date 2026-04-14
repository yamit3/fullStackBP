package com.pichincha.software.engineer.back.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "client")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("CLI")
public class Client extends Person {

    @Column(nullable = false)
    String password;

    @Column(nullable = false)
    Boolean active;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    List<Account> accounts = new ArrayList<>();
}
