package com.example.clinic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
public class Client {
    @Id
    @Column(name = "user_id")
    private Integer userId;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "birth_date")
    private LocalDate birthDate;
}
