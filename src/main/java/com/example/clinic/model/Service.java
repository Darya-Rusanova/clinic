package com.example.clinic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="services")
@Getter
@Setter
@NoArgsConstructor
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    private Integer duration;
    private Integer price;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ServiceCategory category;
    @Column(name = "image_path")
    private String path;
    @ManyToMany(mappedBy = "services", fetch = FetchType.LAZY)
    List<Doctor> doctors = new ArrayList<>();
}
