package com.example.clinic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
public class Doctor {
    @Id
    @Column(name = "user_id")
    private Integer userId;
    private String bio;
    @Column(name = "experience_years")
    private Integer experienceYears;
    @Column(name = "image_path")
    private String imagePath;
    @Column(name="license_path")
    private String licensePath;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "doctorservice",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    List<Service> services = new ArrayList<>();

    public List<String> getCategories() {
        if (services == null || services.isEmpty()) {
            return List.of();
        }
        return services.stream()
                .filter(s -> s.getCategory() != null)
                .map(s -> s.getCategory().getName())
                .distinct()
                .toList();
    }
    public Integer getMinPrice() {
        if (services == null || services.isEmpty()) {
            return 0;
        }
        services.sort((s1, s2) -> s1.getPrice() - s2.getPrice());
        return services.get(0).getPrice();
    }
}
