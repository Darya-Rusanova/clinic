package com.example.clinic.repository;

import com.example.clinic.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client,Integer> {
    Optional<Client> findByUser_Email(String email);
}
