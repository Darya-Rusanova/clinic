package com.example.clinic.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private Long id;
    private String clientName;
    private String clientPhone;
    private String doctorName;
    private String serviceName;
    private Double price;
    private Integer duration;
    private LocalDateTime dateTime;
    private String status;
    private LocalDate createdAt;
}