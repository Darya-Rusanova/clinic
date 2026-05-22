package com.example.clinic.repository;

import com.example.clinic.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<ServiceCategory, Integer> {
}
