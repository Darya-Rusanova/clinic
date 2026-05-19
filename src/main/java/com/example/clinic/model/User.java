package com.example.clinic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean gender;

    public String getFirstName(){
        return name.split(" ")[1];
    }
    public String getLastName(){
        return name.split(" ")[0];
    }
    public String getPatronymic(){
        return name.split(" ").length == 3 ? name.split(" ")[2] : "";
    }
}
